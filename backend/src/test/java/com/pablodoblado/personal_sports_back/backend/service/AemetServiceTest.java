package com.pablodoblado.personal_sports_back.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.models.AemetInitialResponseDTO;
import com.pablodoblado.personal_sports_back.backend.models.AemetObservationsDTO;
import com.pablodoblado.personal_sports_back.backend.models.AemetValuesDTO;
import com.pablodoblado.personal_sports_back.backend.services.impls.AemetServiceImpl;
import com.pablodoblado.personal_sports_back.backend.services.impls.ApiRateLimiterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AemetServiceTest {

    @Mock
    @Qualifier("aemetRestTemplate")
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApiRateLimiterServiceImpl apiRateLimiter;

    @InjectMocks
    private AemetServiceImpl aemetService;

    private TrainingActivity trainingActivityHistorical;
    private TrainingActivity trainingActivityRecent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aemetService, "identificadorEstacionAlajar", "C449I");

        trainingActivityHistorical = new TrainingActivity();
        trainingActivityHistorical.setId(1L);
        trainingActivityHistorical.setFechaComienzo(LocalDateTime.now().minusDays(2));

        trainingActivityRecent = new TrainingActivity();
        trainingActivityRecent.setId(2L);
        trainingActivityRecent.setFechaComienzo(LocalDateTime.now().minusHours(1));
    }

    @Test
    void shouldGetHistoricalWeatherValues() throws Exception {
        AemetInitialResponseDTO initialResponse = new AemetInitialResponseDTO();
        initialResponse.setDatos("http://test.com/data");
        AemetValuesDTO valuesDTO = new AemetValuesDTO();
        valuesDTO.setTmed("15,5");
        valuesDTO.setVelmedia("10,0");
        valuesDTO.setHrmedia("80,0");
        valuesDTO.setPrec("0,0");

        doNothing().when(apiRateLimiter).checkAemetRateLimit();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(AemetInitialResponseDTO.class)))
                .thenReturn(ResponseEntity.ok(initialResponse));
        when(restTemplate.exchange(eq("http://test.com/data"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("[]"));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(Collections.singletonList(valuesDTO));

        CompletableFuture<TrainingActivity> resultFuture = aemetService.getValoresClimatologicosRangoFechas(trainingActivityHistorical);
        TrainingActivity result = resultFuture.join();

        assertThat(result).isNotNull();
        assertThat(result.getTemperatura()).isEqualTo(15.5);
        assertThat(result.getViento()).isEqualTo(10.0);
        assertThat(result.getHumedad()).isEqualTo(80.0);
        assertThat(result.getLluvia()).isFalse();
    }

    @Test
    void shouldGetRecentWeatherObservations() {
        AemetInitialResponseDTO initialResponse = new AemetInitialResponseDTO();
        initialResponse.setDatos("http://test.com/data");
        AemetObservationsDTO observationDTO = new AemetObservationsDTO();
        observationDTO.setTa(25.0);
        observationDTO.setVv(5.0);
        observationDTO.setHr(70.0);
        observationDTO.setPrec(1.0);
        observationDTO.setFint(trainingActivityRecent.getFechaComienzo().atOffset(ZoneOffset.UTC).toString());

        doNothing().when(apiRateLimiter).checkAemetRateLimit();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(AemetInitialResponseDTO.class)))
                .thenReturn(ResponseEntity.ok(initialResponse));
        when(restTemplate.exchange(eq("http://test.com/data"), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of(observationDTO)));

        CompletableFuture<TrainingActivity> resultFuture = aemetService.getValoresClimatologicosRangoFechas(trainingActivityRecent);
        TrainingActivity result = resultFuture.join();

        assertThat(result).isNotNull();
        assertThat(result.getTemperatura()).isEqualTo(25.0);
        assertThat(result.getViento()).isEqualTo(5.0);
        assertThat(result.getHumedad()).isEqualTo(70.0);
        assertThat(result.getLluvia()).isTrue();
    }

    @Test
    void shouldHandle404NotFoundGracefully() {
        doNothing().when(apiRateLimiter).checkAemetRateLimit();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(AemetInitialResponseDTO.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        CompletableFuture<TrainingActivity> resultFuture = aemetService.getValoresClimatologicosRangoFechas(trainingActivityRecent);
        TrainingActivity result = resultFuture.join();

        assertThat(result).isNotNull();
        assertThat(result.getTemperatura()).isNull();
        assertThat(result.getViento()).isNull();
        assertThat(result.getHumedad()).isNull();
        assertThat(result.getLluvia()).isNull();

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(AemetInitialResponseDTO.class));
    }

    @Test
    void shouldRetryOnTransientApiFailures() {
        doNothing().when(apiRateLimiter).checkAemetRateLimit();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(AemetInitialResponseDTO.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        CompletableFuture<TrainingActivity> resultFuture = aemetService.getValoresClimatologicosRangoFechas(trainingActivityRecent);
        TrainingActivity result = resultFuture.join();

        assertThat(result).isNotNull();
        assertThat(result.getTemperatura()).isNull();

        verify(restTemplate, times(3)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(AemetInitialResponseDTO.class));
    }
}
