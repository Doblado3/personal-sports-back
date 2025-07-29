
package com.pablodoblado.personal_sports_back.backend.service;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.pablodoblado.personal_sports_back.backend.dto.TrainingActivity.TrainingActivityResponseDTO;
import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import com.pablodoblado.personal_sports_back.backend.repository.TrainingActivityRepository;
import com.pablodoblado.personal_sports_back.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@WireMockTest(httpPort = 8089)
public class TrainingActivityServiceTest {

    @Autowired
    private TrainingActivityService trainingActivityService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private TrainingActivityRepository trainingActivityRepository;

    @MockitoBean
    private AemetService aemetService;

   
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("strava.api.base-url", () -> "http://localhost:8089");
    }

    private Usuario usuario;
    private Usuario usuarioExpirado;

    @BeforeEach
    void setUp() {
    	
        usuario = new Usuario();
        usuario.setId(UUID.fromString("b2f22e50-321d-4816-a8bd-7a0670b72045"));
        usuario.setNombre("Test User");
        usuario.setStravaAthleteId(74275004L);
        usuario.setStravaAccessToken("test-token"); 
        usuario.setStravaRefreshToken("test-refresh-token");
        usuario.setStravaTokenExpiresAt(System.currentTimeMillis() / 1000 + 3600);
        
        usuarioExpirado = new Usuario();
        usuarioExpirado.setId(UUID.fromString("1d2559a9-1c63-4171-94b2-07be52a999d8"));
        usuarioExpirado.setNombre("Test User Expired");
        usuarioExpirado.setStravaAccessToken("test-token"); 
        usuarioExpirado.setStravaRefreshToken("test-refresh-token");
        usuarioExpirado.setStravaTokenExpiresAt(System.currentTimeMillis() / 1000 - 3600);
    }

    @Test
    void shouldFetchAndSaveStravaActivities() throws Exception {
  
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioRepository.findByStravaAthleteId(74275004L)).thenReturn(Optional.of(usuario));
        when(trainingActivityRepository.existsById(any())).thenReturn(false);
        when(trainingActivityRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(aemetService.getValoresClimatologicosRangoFechas(any())).thenAnswer(invocation -> invocation.getArgument(0));


        String responseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/stravaDetailedActivityDTO.json")));
        stubFor(get(urlPathEqualTo("/athlete/activities"))
                .withQueryParam("before", equalTo("1704067200"))
                .withQueryParam("after", equalTo("1672531200"))
                .withQueryParam("page", equalTo("1"))
                .withQueryParam("per_page", equalTo("30"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseBody)
                        .withStatus(200)));

        
        List<TrainingActivityResponseDTO> actualActivities = trainingActivityService.fetchAndSaveStravaActivities(
                usuario.getId(), 1704067200L, 1672531200L, 1, 30);

        assertThat(actualActivities).isNotNull();
        assertThat(actualActivities).hasSize(1);
        assertThat(actualActivities.get(0).getNombre()).isEqualTo("Morning Run");
    }
    
}

