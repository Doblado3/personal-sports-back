
package com.pablodoblado.personal_sports_back.backend.service;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;
import com.pablodoblado.personal_sports_back.backend.services.StravaTokenService;
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
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@WireMockTest(httpPort = 8089)
public class StravaTokenServiceTest {

    @Autowired
    private StravaTokenService stravaTokenService; 

    @MockitoBean 
    private UsuarioRepository usuarioRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        
        registry.add("strava.api.base-url", () -> "http://localhost:8089");
    }

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.fromString("b2f22e50-321d-4816-a8bd-7a0670b72045"));
        usuario.setNombre("Test User");
        usuario.setStravaRefreshToken("test-refresh-token");
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String responseBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/stravaTokenResponseDTO.json")));

        stubFor(post(urlEqualTo("/oauth/token"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .withRequestBody(containing("refresh_token=test-refresh-token"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(responseBody)
                        .withStatus(200)));

        
        Usuario updatedUsuario = stravaTokenService.refreshToken(usuario);

        
        assertThat(updatedUsuario).isNotNull();
        assertThat(updatedUsuario.getStravaAccessToken()).isEqualTo("new-access-token");
        assertThat(updatedUsuario.getStravaRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(updatedUsuario.getStravaTokenExpiresAt()).isEqualTo(1704067200L);
    }

    @Test
    void throwExceptionWhenRefreshingTokens() {
        
        stubFor(post(urlEqualTo("/oauth/token"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error from Strava")));

        
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            stravaTokenService.refreshToken(usuario);
        });

        // Assert that our service wrapped the error correctly.
        assertThat(thrown.getMessage()).contains("Error during Strava token refresh for user");
    }
}
