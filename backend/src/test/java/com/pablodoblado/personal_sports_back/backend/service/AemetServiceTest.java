package com.pablodoblado.personal_sports_back.backend.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;
import com.pablodoblado.personal_sports_back.backend.services.impls.AemetService;
import com.pablodoblado.personal_sports_back.backend.services.impls.ApiRateLimiterService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@WireMockTest(httpPort = 8089)
public class AemetServiceTest {

    @Autowired
    private AemetService aemetService;

    @MockitoBean
    private ApiRateLimiterService apiRateLimiterService;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("aemet.api.base-url", () -> "http://localhost:8089");
        registry.add("identificador.estacion.Alajar", () -> "4560Y");
    }

    private TrainingActivity trainingActivityHistorical;
    private TrainingActivity trainingActivityRecent;

    @BeforeEach
    void setUp() {
        when(apiRateLimiterService.checkAemetRateLimit()).thenReturn(Mono.empty());

        Usuario usuario = new Usuario();
        usuario.setId(UUID.fromString("b2f22e50-321d-4816-a8bd-7a0670b72045"));
        usuario.setNombre("Test User");

        trainingActivityHistorical = new TrainingActivity();
        trainingActivityHistorical.setId(1L);
        trainingActivityHistorical.setUsuario(usuario);
        trainingActivityHistorical.setNombre("Historical Run");
        trainingActivityHistorical.setTipo(TipoActividad.RUNNING);
        trainingActivityHistorical.setFechaComienzo(LocalDateTime.now().minusDays(2));

        trainingActivityRecent = new TrainingActivity();
        trainingActivityRecent.setId(2L);
        trainingActivityRecent.setUsuario(usuario);
        trainingActivityRecent.setNombre("Recent Run");
        trainingActivityRecent.setTipo(TipoActividad.RUNNING);
        trainingActivityRecent.setFechaComienzo(LocalDateTime.now().minusHours(1));
    }

    @Test
    void shouldGetHistoricalWeatherValues() throws IOException {
        String initialResponseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/aemetInitialResponse.json")));
        String valuesResponseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/aemetValues.json")));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'UTC'");
        String fechaIniStr = trainingActivityHistorical.getFechaComienzo().format(formatter);
        String fechaFinStr = trainingActivityHistorical.getFechaComienzo().plusDays(1).format(formatter);

        stubFor(get(urlPathMatching("/valores/climatologicos/diarios/datos/fechaini/" + fechaIniStr + "/fechafin/" + fechaFinStr + "/estacion/4560Y"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(initialResponseBody)
                        .withStatus(200)));

        stubFor(get(urlEqualTo("/sh/e8b996d7"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(valuesResponseBody)
                        .withStatus(200)));

        Mono<TrainingActivity> result = aemetService.getValoresClimatologicosRangoFechas(trainingActivityHistorical);

        StepVerifier.create(result)
                .assertNext(activity -> {
                    assertThat(activity).isNotNull();
                    assertThat(activity.getTemperatura()).isEqualTo(15.5);
                    assertThat(activity.getViento()).isEqualTo(10.0);
                    assertThat(activity.getHumedad()).isEqualTo(80.0);
                    assertThat(activity.getLluvia()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    void shouldGetRecentWeatherObservations() throws IOException {
        String initialResponseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/aemetInitialResponse.json")));
        String observationResponseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/aemetObservations.json")));

        stubFor(get(urlEqualTo("/observacion/convencional/datos/estacion/4560Y"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(initialResponseBody)
                        .withStatus(200)));

        stubFor(get(urlEqualTo("/sh/e8b996d7"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(observationResponseBody)
                        .withStatus(200)));

        Mono<TrainingActivity> result = aemetService.getValoresClimatologicosRangoFechas(trainingActivityRecent);

        StepVerifier.create(result)
                .assertNext(activity -> {
                    assertThat(activity).isNotNull();
                    assertThat(activity.getTemperatura()).isEqualTo(25.0);
                    assertThat(activity.getViento()).isEqualTo(5.0);
                    assertThat(activity.getHumedad()).isEqualTo(70.0);
                    assertThat(activity.getLluvia()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnActivityWithoutWeather_WhenAemetApiFails() {
        stubFor(get(anyUrl()).willReturn(aResponse().withStatus(500)));

        Mono<TrainingActivity> result = aemetService.getValoresClimatologicosRangoFechas(trainingActivityRecent);

        StepVerifier.create(result)
                .assertNext(activity -> {
                    assertThat(activity).isNotNull();
                    assertThat(activity.getTemperatura()).isNull();
                    assertThat(activity.getViento()).isNull();
                    assertThat(activity.getHumedad()).isNull();
                    assertThat(activity.getLluvia()).isNull();
                })
                .verifyComplete();

        WireMock.verify(4, getRequestedFor(anyUrl()));
    }
}
