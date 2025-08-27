package com.pablodoblado.personal_sports_back.backend.services.impls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.models.StravaTokenResponse;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;
import com.pablodoblado.personal_sports_back.backend.services.StravaTokenService;


@Service
public class StravaTokenServiceImpl implements StravaTokenService {

    private final RestTemplate restTemplate;
    
    private final UsuarioRepository usuarioRepository;

    @Value("${strava.api.client-id}")
    private String stravaClientId;

    @Value("${strava.api.client-secret}")
    private String stravaClientSecret;

    @Autowired
    public StravaTokenServiceImpl(RestTemplate restTemplate, UsuarioRepository usuarioRepository) {
        this.restTemplate = restTemplate;
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario refreshToken(Usuario usuario) {
        if (usuario.getStravaRefreshToken() == null || usuario.getStravaRefreshToken().isBlank()) {
            throw new RuntimeException("No strava refresh token available for user: " + usuario.getId());
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", stravaClientId);
            formData.add("client_secret", stravaClientSecret);
            formData.add("refresh_token", usuario.getStravaRefreshToken());
            formData.add("grant_type", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            
            StravaTokenResponse tokenResponse = restTemplate.postForObject("/oauth/token", request, StravaTokenResponse.class);

            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                usuario.setStravaAccessToken(tokenResponse.getAccessToken());
                usuario.setStravaRefreshToken(tokenResponse.getRefreshToken());
                usuario.setStravaTokenExpiresAt(tokenResponse.getExpiresAt());
                return usuarioRepository.save(usuario);
            } else {
                throw new RuntimeException("Failed refreshing Strava token for user: " + usuario.getId() + ". Response was empty.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error during Strava token refresh for user: " + usuario.getId(), e);
        }
    }
}
