package com.pablodoblado.personal_sports_back.backend.service;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.repositories.TrainingActivityRepository;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;
import com.pablodoblado.personal_sports_back.backend.services.impls.AemetService;
import com.pablodoblado.personal_sports_back.backend.services.impls.ApiRateLimiterService;
import com.pablodoblado.personal_sports_back.backend.services.impls.StravaTokenService;
import com.pablodoblado.personal_sports_back.backend.services.impls.TrainingActivityServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@WireMockTest(httpPort = 8089)
public class TrainingActivityServiceTest {

    @Autowired
    private TrainingActivityServiceImpl trainingActivityService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private TrainingActivityRepository trainingActivityRepository;

    @MockitoBean
    private AemetService aemetService;

    @MockitoBean
    private StravaTokenService stravaTokenService;

    @MockitoBean
    private ApiRateLimiterService apiRateLimiterService;

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
        usuario.setStravaAccessToken("test-token");
        usuario.setStravaRefreshToken("test-refresh-token");
        usuario.setStravaTokenExpiresAt(System.currentTimeMillis() / 1000 + 3600);

        usuarioExpirado = new Usuario();
        usuarioExpirado.setId(UUID.fromString("1d2559a9-1c63-4171-94b2-07be52a999d8"));
        usuarioExpirado.setNombre("Test User Expired");
        usuarioExpirado.setStravaAccessToken("expired-token");
        usuarioExpirado.setStravaRefreshToken("test-refresh-token");
        usuarioExpirado.setStravaTokenExpiresAt(System.currentTimeMillis() / 1000 - 3600);

        when(apiRateLimiterService.checkStravaRateLimit()).thenReturn(Mono.empty());
        when(aemetService.getValoresClimatologicosRangoFechas(any(TrainingActivity.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(trainingActivityRepository.saveAll(any())).thenReturn(Collections.emptyList());
    }

    @Test
    void shouldFetchAndSaveStravaActivities() throws IOException {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(trainingActivityRepository.existsById(any())).thenReturn(false);

        String responseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/stravaDetailedActivityDTO.json")));
        stubFor(get(urlPathEqualTo("/athlete/activities"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseBody)
                        .withStatus(200)));

        Mono<Void> result = trainingActivityService.fetchAndSaveStravaActivities(usuario.getId(), 1L, 2L, 1, 30);

        StepVerifier.create(result).verifyComplete();

        ArgumentCaptor<List<TrainingActivity>> captor = ArgumentCaptor.forClass(List.class);
        verify(trainingActivityRepository).saveAll(captor.capture());
        List<TrainingActivity> savedActivities = captor.getValue();

        assertThat(savedActivities).isNotNull();
        assertThat(savedActivities).hasSize(1);
        assertThat(savedActivities.get(0).getNombre()).isEqualTo("Morning Run");
    }

    @Test
    void shouldRefreshTokenAndFetchActivitiesWhenTokenIsExpired() throws IOException {
        when(usuarioRepository.findById(usuarioExpirado.getId())).thenReturn(Optional.of(usuarioExpirado));
        when(trainingActivityRepository.existsById(any())).thenReturn(false);

        Usuario refreshedUser = new Usuario();
        refreshedUser.setId(usuarioExpirado.getId());
        refreshedUser.setStravaAccessToken("new-refreshed-token");
        when(stravaTokenService.refreshToken(any(Usuario.class))).thenReturn(refreshedUser);

        String responseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/stravaDetailedActivityDTO.json")));
        stubFor(get(urlPathEqualTo("/athlete/activities"))
                .withHeader("Authorization", equalTo("Bearer new-refreshed-token"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseBody)
                        .withStatus(200)));

        Mono<Void> result = trainingActivityService.fetchAndSaveStravaActivities(usuarioExpirado.getId(), 1L, 2L, 1, 30);

        StepVerifier.create(result).verifyComplete();
        verify(stravaTokenService).refreshToken(any(Usuario.class));
    }

    @Test
    void shouldRefreshTokenOn401AndRetry() throws IOException {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(trainingActivityRepository.existsById(any())).thenReturn(false);

        Usuario refreshedUser = new Usuario();
        refreshedUser.setId(usuario.getId());
        refreshedUser.setStravaAccessToken("new-refreshed-token-on-401");
        when(stravaTokenService.refreshToken(any(Usuario.class))).thenReturn(refreshedUser);

        String responseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/stravaDetailedActivityDTO.json")));

        stubFor(get(urlPathEqualTo("/athlete/activities"))
                .withHeader("Authorization", equalTo("Bearer " + usuario.getStravaAccessToken()))
                .willReturn(aResponse().withStatus(401)));

        stubFor(get(urlPathEqualTo("/athlete/activities"))
                .withHeader("Authorization", equalTo("Bearer " + refreshedUser.getStravaAccessToken()))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseBody)
                        .withStatus(200)));

        Mono<Void> result = trainingActivityService.fetchAndSaveStravaActivities(usuario.getId(), 1L, 2L, 1, 30);

        StepVerifier.create(result).verifyComplete();
        verify(stravaTokenService).refreshToken(any(Usuario.class));
    }
}