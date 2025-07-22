package com.pablodoblado.personal_sports_back.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
	
	@Value("${strava.api.base-url}")
	private String Strava_base_URL;
	
	//Podemos especificar un mapa con las URIs que mas vayamos a usar
	@Bean
	public WebClient webClientStrava() {
		return WebClient.builder()
				.baseUrl(Strava_base_URL)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}

}
