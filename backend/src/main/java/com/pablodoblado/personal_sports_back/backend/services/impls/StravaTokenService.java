package com.pablodoblado.personal_sports_back.backend.services.impls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.models.StravaTokenResponse;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;

@Service
public class StravaTokenService {
	
	private final WebClient webClientStrava;
	private final UsuarioRepository usuarioRepository;
	
	@Value("${strava.api.client-id}")
	private String stravaClientId;
	
	@Value("${strava.api.client-secret}")
	private String stravaClientSecret;
	
	@Autowired
	public StravaTokenService(@Qualifier("webClientStrava") WebClient webClientStrava, UsuarioRepository usuarioRepository) {
		this.webClientStrava = webClientStrava;
		this.usuarioRepository = usuarioRepository;
	}
	
	
	public Usuario refreshToken(Usuario usuario) {
		if (usuario.getStravaRefreshToken() == null || usuario.getStravaRefreshToken().isBlank()) {
			throw new RuntimeException("No strava refresh token available for user: " + usuario.getId());
		}
		
		try {
			LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
			formData.add("client_id", stravaClientId);
			formData.add("client_secret", stravaClientSecret);
			formData.add("refresh_token", usuario.getStravaRefreshToken());
			formData.add("grant_type", "refresh_token");
			
			StravaTokenResponse tokenResponse = webClientStrava.post()
					.uri("/oauth/token")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(BodyInserters.fromFormData(formData))
					.retrieve()
					.bodyToMono(StravaTokenResponse.class)
					.block();
			
			if(tokenResponse != null && tokenResponse.getAccessToken() != null) {
				usuario.setStravaAccessToken(tokenResponse.getAccessToken());
				usuario.setStravaRefreshToken(tokenResponse.getRefreshToken());
				usuario.setStravaTokenExpiresAt(tokenResponse.getExpiresAt());
				return usuarioRepository.save(usuario);
			} else {
				
				throw new RuntimeException("Failed refreshing Strava token for user: " + usuario.getId());
			}
			
		} catch( Exception e) {
			throw new RuntimeException("Error during Strava token refresh or retry for user: " + usuario.getId(), e);
		}
	}

}
