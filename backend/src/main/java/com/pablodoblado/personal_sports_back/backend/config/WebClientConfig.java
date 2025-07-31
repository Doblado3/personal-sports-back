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
	private String stravaBaseUrl;
	
	@Value("${aemet.api.base-url}")
	private String aemetBaseUrl;
	
	@Value("${aemet.api.opendata.api_key}")
	private String aemetApiKey;
	
	//Podemos especificar un mapa con las URIs que mas vayamos a usar
	@Bean
	public WebClient webClientStrava() {
		return WebClient.builder()
				.baseUrl(stravaBaseUrl)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}
	
	@Bean
	public WebClient webClientAemet() {
		return WebClient.builder()
				.baseUrl(aemetBaseUrl)
				.defaultHeader("api_key", aemetApiKey)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}
	
	
	

}
