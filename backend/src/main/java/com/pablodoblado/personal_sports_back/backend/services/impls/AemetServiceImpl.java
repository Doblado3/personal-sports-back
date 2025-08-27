package com.pablodoblado.personal_sports_back.backend.services.impls;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.models.AemetInitialResponseDTO;
import com.pablodoblado.personal_sports_back.backend.models.AemetObservationsDTO;
import com.pablodoblado.personal_sports_back.backend.models.AemetValuesDTO;
import com.pablodoblado.personal_sports_back.backend.services.AemetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class AemetServiceImpl implements AemetService {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    private final ObjectMapper objectMapper;
    @Qualifier("aemetRestTemplate")
    private final RestTemplate restTemplate;
    private final ApiRateLimiterServiceImpl apiRateLimiter;

    @Value("${identificador.estacion.Alajar}")
    private String identificadorEstacionAlajar;

    @Async("asyncExecutor")
    @Override
    public CompletableFuture<TrainingActivity> getValoresClimatologicosRangoFechas(TrainingActivity trainingActivity) {
        try {
            apiRateLimiter.checkAemetRateLimit();

            Instant now = Instant.now();
            Instant twelveHoursAgo = now.minus(12, ChronoUnit.HOURS);
            Instant startTime = trainingActivity.getFechaComienzo().toInstant(ZoneOffset.UTC);

            TrainingActivity result;
            if (startTime.isAfter(twelveHoursAgo)) {
                result = getRecentObservations(trainingActivity, startTime);
            } else {
                result = getHistoricalValues(trainingActivity);
            }
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Failed to get weather data for activity {}: {}. Returning activity without weather info", trainingActivity.getId(), e.getMessage());
            return CompletableFuture.completedFuture(trainingActivity);
        }
    }

    private TrainingActivity getRecentObservations(TrainingActivity trainingActivity, Instant startTime) {
        log.info("Activity {} is recent. Calling Aemet observations endpoint.", trainingActivity.getId());
        String uri = "/observacion/convencional/datos/estacion/" + identificadorEstacionAlajar;

        AemetInitialResponseDTO initialResponse = executeWithRetry(() -> restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(createHeaders()), AemetInitialResponseDTO.class).getBody());

        if (initialResponse == null || initialResponse.getDatos() == null) {
            log.warn("No data URL received from Aemet initial response for recent activity {}", trainingActivity.getId());
            return trainingActivity;
        }

        List<AemetObservationsDTO> valuesMeteo = executeWithRetry(() -> restTemplate.exchange(initialResponse.getDatos(), HttpMethod.GET, new HttpEntity<>(createHeaders()),
                new ParameterizedTypeReference<List<AemetObservationsDTO>>() { // NOSONAR
                }).getBody());

        if (valuesMeteo != null && !valuesMeteo.isEmpty()) {
            AemetObservationsDTO closestObservation = findClosestObservation(valuesMeteo, startTime);
            if (closestObservation != null) {
                trainingActivity.setTemperatura(closestObservation.getTa());
                trainingActivity.setViento(closestObservation.getVv());
                trainingActivity.setHumedad(closestObservation.getHr());
                trainingActivity.setLluvia(closestObservation.getPrec() != 0.0);
            }
        }
        return trainingActivity;
    }

    private TrainingActivity getHistoricalValues(TrainingActivity trainingActivity) {
        log.info("Activity {} is historical. Calling Aemet values endpoint.", trainingActivity.getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'UTC'");
        String fechaIniStr = trainingActivity.getFechaComienzo().format(formatter);
        String fechaFinStr = trainingActivity.getFechaComienzo().plusDays(1).format(formatter);
        String uri = String.format("/valores/climatologicos/diarios/datos/fechaini/%s/fechafin/%s/estacion/%s",
                fechaIniStr, fechaFinStr, identificadorEstacionAlajar);

        AemetInitialResponseDTO initialResponse = executeWithRetry(() -> restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(createHeaders()),
                AemetInitialResponseDTO.class).getBody());

        if (initialResponse == null || initialResponse.getDatos() == null) {
            log.warn("No data URL received from Aemet initial response for activity {}", trainingActivity.getId());
            return trainingActivity;
        }

        String datosMeteoString = executeWithRetry(() -> restTemplate.exchange(initialResponse.getDatos(), HttpMethod.GET, new HttpEntity<>(createHeaders()), String.class).getBody());

        try {
            List<AemetValuesDTO> datosMeteoList = objectMapper.readValue(datosMeteoString, new TypeReference<List<AemetValuesDTO>>() {});
            if (datosMeteoList != null && !datosMeteoList.isEmpty()) {
                AemetValuesDTO datosMeteo = datosMeteoList.get(0);
                trainingActivity.setHumedad(Optional.ofNullable(datosMeteo.getHrmedia()).map(s -> Double.parseDouble(s.replace(',', '.'))).orElse(null));
                trainingActivity.setTemperatura(Optional.ofNullable(datosMeteo.getTmed()).map(s -> Double.parseDouble(s.replace(',', '.'))).orElse(null));
                trainingActivity.setViento(Optional.ofNullable(datosMeteo.getVelmedia()).map(s -> Double.parseDouble(s.replace(',', '.'))).orElse(null));
                trainingActivity.setLluvia(Optional.ofNullable(datosMeteo.getPrec()).map(s -> Double.parseDouble(s.replace(',', '.'))).map(prec -> prec != 0.0).orElse(false));
            }
        } catch (Exception e) {
            log.error("Error parsing historical weather data for activity {}", trainingActivity.getId(), e);
        }

        return trainingActivity;
    }

    private <T> T executeWithRetry(Supplier<T> supplier) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return supplier.get();
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    log.warn("AEMET API returned 404 Not Found. No data available.");
                    return null;
                }

                log.warn("AEMET API call failed with status {} (attempt {}/{}), retrying in {} ms", e.getStatusCode(), i + 1, MAX_RETRIES, RETRY_DELAY_MS);
                if (i == MAX_RETRIES - 1) {
                    throw new RuntimeException("AEMET API call failed after multiple retries", e);
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
        throw new RuntimeException("AEMET API call failed after multiple retries");
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("cache-control", "no-cache");
        return headers;
    }

    private AemetObservationsDTO findClosestObservation(List<AemetObservationsDTO> observations, Instant targetTime) {
        DateTimeFormatter aemetDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

        return observations.stream()
                .filter(o -> Objects.nonNull(o.getFint()))
                .min((o1, o2) -> {
                    try {
                        Instant time1 = OffsetDateTime.parse(o1.getFint(), aemetDateTimeFormatter).toInstant();
                        Instant time2 = OffsetDateTime.parse(o2.getFint(), aemetDateTimeFormatter).toInstant();
                        long diff1 = Math.abs(ChronoUnit.SECONDS.between(time1, targetTime));
                        long diff2 = Math.abs(ChronoUnit.SECONDS.between(time2, targetTime));
                        return Long.compare(diff1, diff2);
                    } catch (Exception e) {
                        log.warn("Could not parse date from AEMET observation: {}", e.getMessage());
                        return 0;
                    }
                })
                .orElse(null);
    }
}
