package com.pablodoblado.personal_sports_back.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.pablodoblado.personal_sports_back.backend.entity.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import com.pablodoblado.personal_sports_back.backend.entity.enums.TipoActividad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@WireMockTest(httpPort = 8089)
public class AemetServiceTest {

    @Autowired
    private AemetService aemetService;


    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("aemet.api.base-url", () -> "http://localhost:8089");
    }

    private TrainingActivity trainingActivity;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
    	
    	usuario = new Usuario();
        usuario.setId(UUID.fromString("b2f22e50-321d-4816-a8bd-7a0670b72045"));
        usuario.setNombre("Test User");
        usuario.setStravaAthleteId(74275004L);
        
    	
        trainingActivity = new TrainingActivity();
        trainingActivity.setUsuario(usuario);
        trainingActivity.setNombre("Morning Run");
        trainingActivity.setTipo(TipoActividad.RUNNING);
        // Set activity date to be older than 12 hours to trigger historical API call
        trainingActivity.setFechaComienzo(LocalDateTime.now().minusDays(2));
        
    }

    @Test
    void shouldGetValoresClimatologicosRangoFechas() throws Exception {

        String initialResponseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/aemetInitialResponse.json")));
        String valuesResponseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/aemetValues.json")));

        stubFor(get(urlPathEqualTo("/valores/climatologicos/diarios/datos/fechaini/" + trainingActivity.getFechaComienzo().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'UTC'")) + "/fechafin/" + trainingActivity.getFechaComienzo().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'UTC'")) + "/estacion/4560Y"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(initialResponseBody)
                        .withStatus(200)));

        stubFor(get(urlPathEqualTo("/sh/e8b996d7"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(valuesResponseBody)
                        .withStatus(200)));

        TrainingActivity actualTrainingActivity = aemetService.getValoresClimatologicosRangoFechas(trainingActivity);

        assertThat(actualTrainingActivity).isNotNull();
        assertThat(actualTrainingActivity.getTemperatura()).isEqualTo(15.5);
        assertThat(actualTrainingActivity.getViento()).isEqualTo(10.0);
        assertThat(actualTrainingActivity.getHumedad()).isEqualTo(80.0);
        assertThat(actualTrainingActivity.getLluvia()).isFalse();
    }

    @Test
    void shouldGetClimatologicosObservacion() throws Exception {
        
        trainingActivity.setFechaComienzo(LocalDateTime.now().minusHours(1));

        String initialResponseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/aemetInitialResponse.json")));
        String observationResponseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/aemetObservations.json")));

        // Stub for the initial observation API call
        stubFor(get(urlPathEqualTo("/observacion/convencional/datos/estacion/4560Y"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(initialResponseBody)
                        .withStatus(200)));

        // Stub for the data URL returned by the initial response
        stubFor(get(urlPathEqualTo("/sh/e8b996d7"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(observationResponseBody)
                        .withStatus(200)));

        TrainingActivity actualTrainingActivity = aemetService.getValoresClimatologicosRangoFechas(trainingActivity);

        assertThat(actualTrainingActivity).isNotNull();
        assertThat(actualTrainingActivity.getTemperatura()).isEqualTo(25.0);
        assertThat(actualTrainingActivity.getViento()).isEqualTo(5.0);
        assertThat(actualTrainingActivity.getHumedad()).isEqualTo(70.0);
        assertThat(actualTrainingActivity.getLluvia()).isTrue();
    }
}