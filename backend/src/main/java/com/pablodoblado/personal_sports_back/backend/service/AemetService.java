package com.pablodoblado.personal_sports_back.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablodoblado.personal_sports_back.backend.dto.AemetApi.AemetInitialResponseDTO;
import com.pablodoblado.personal_sports_back.backend.dto.AemetApi.AemetObservationsDTO;
import com.pablodoblado.personal_sports_back.backend.dto.AemetApi.AemetValuesDTO;
import com.pablodoblado.personal_sports_back.backend.entity.TrainingActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AemetService {

    private final Logger log = LoggerFactory.getLogger(AemetService.class);
    private final WebClient webClientAemet;
    private final ObjectMapper objectMapper;
    private final ApiRateLimiterService apiRateLimiter;

    @Value("${identificador.estacion.Alajar}")
    private String identificadorEstacionAlajar;

    public AemetService(@Qualifier("webClientAemet") WebClient webClient, ObjectMapper objectMapper, ApiRateLimiterService apiRateLimiter) {
        this.webClientAemet = webClient;
        this.objectMapper = objectMapper;
        this.apiRateLimiter = apiRateLimiter;
    }

    public Mono<TrainingActivity> getValoresClimatologicosRangoFechas(TrainingActivity trainingActivity) {
        return apiRateLimiter.checkAemetRateLimit()
                .then(Mono.defer(() -> {
                    Instant now = Instant.now();
                    Instant twelveHoursAgo = now.minus(12, ChronoUnit.HOURS);
                    Instant startTime = trainingActivity.getFechaComienzo().toInstant(ZoneOffset.UTC);

                    if (startTime.isAfter(twelveHoursAgo)) {
                        return getRecentObservations(trainingActivity, startTime);
                    } else {
                        return getHistoricalValues(trainingActivity);
                    }
                }))
                .doOnSuccess(ta -> log.info("Successfully fetched weather data for activity {}", ta.getId()))
                .onErrorResume(e -> {
                    log.error("Failed to get weather data for activity {}: {}. Returning activity without weather info.", trainingActivity.getId(), e.getMessage());
                    return Mono.just(trainingActivity);
                });
    }

    private Mono<TrainingActivity> getRecentObservations(TrainingActivity trainingActivity, Instant startTime) {
        log.info("Activity {} is recent. Calling Aemet observations endpoint.", trainingActivity.getId());
        String uri = "/observacion/convencional/datos/estacion/" + identificadorEstacionAlajar;

        return webClientAemet.get().uri(uri)
                .header("cache-control", "no-cache")
                .retrieve()
                .bodyToMono(AemetInitialResponseDTO.class)
                .flatMap(initialResponse -> webClientAemet.get().uri(initialResponse.getDatos())
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<AemetObservationsDTO>>() {}))
                .map(valuesMeteo -> {
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
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            throw new RuntimeException("AEMET API call failed after multiple retries.", retrySignal.failure());
                        }));
    }

    private Mono<TrainingActivity> getHistoricalValues(TrainingActivity trainingActivity) {
        log.info("Activity {} is historical. Calling Aemet values endpoint.", trainingActivity.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'UTC'");
        String fechaIniStr = trainingActivity.getFechaComienzo().format(formatter);
        String fechaFinStr = trainingActivity.getFechaComienzo().plusDays(1).format(formatter);
        String uri = String.format("/valores/climatologicos/diarios/datos/fechaini/%s/fechafin/%s/estacion/%s",
                fechaIniStr, fechaFinStr, identificadorEstacionAlajar);

        return webClientAemet.get().uri(uri)
                .header("cache-control", "no-cache")
                .retrieve()
                .bodyToMono(AemetInitialResponseDTO.class)
                .flatMap(initialResponse -> {
                    if (initialResponse == null || initialResponse.getDatos() == null) {
                        log.warn("No data URL received from Aemet initial response for activity {}", trainingActivity.getId());
                        return Mono.just(trainingActivity);
                    }
                    return webClientAemet.get().uri(initialResponse.getDatos())
                            .retrieve()
                            .bodyToMono(String.class)
                            .flatMap(datosMeteoString -> Mono.fromCallable(() -> {
                                List<AemetValuesDTO> datosMeteoList = objectMapper.readValue(datosMeteoString, new TypeReference<List<AemetValuesDTO>>() {});
                                if (datosMeteoList != null && !datosMeteoList.isEmpty()) {
                                    AemetValuesDTO datosMeteo = datosMeteoList.get(0);
                                    trainingActivity.setHumedad(Optional.ofNullable(datosMeteo.getHrmedia()).map(s -> Double.parseDouble(s.replace(',', '.'))).orElse(null));
                                    trainingActivity.setTemperatura(Optional.ofNullable(datosMeteo.getTmed()).map(s -> Double.parseDouble(s.replace(',', '.'))).orElse(null));
                                    trainingActivity.setViento(Optional.ofNullable(datosMeteo.getVelmedia()).map(s -> Double.parseDouble(s.replace(',', '.'))).orElse(null));
                                    trainingActivity.setLluvia(Optional.ofNullable(datosMeteo.getPrec()).map(s -> Double.parseDouble(s.replace(',', '.'))).map(prec -> prec != 0.0).orElse(false));
                                }
                                return trainingActivity;
                            }).subscribeOn(Schedulers.boundedElastic()));
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            throw new RuntimeException("AEMET API call failed after multiple retries.", retrySignal.failure());
                        }));
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
