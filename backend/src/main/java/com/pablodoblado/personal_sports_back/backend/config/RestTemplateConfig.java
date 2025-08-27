package com.pablodoblado.personal_sports_back.backend.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
	
	@Value("${strava.api.base-url}")
	private String stravaBaseUrl;
	
	@Value("${aemet.api.base-url}")
	private String aemetBaseUrl;
	
	@Value("${aemet.api.opendata.api_key}")
	private String aemetApiKey;
	
	@Bean
	@Primary
	public RestTemplate restTemplateStrava(RestTemplateBuilder builder) {
		return builder
				.rootUri(stravaBaseUrl)
				.connectTimeout(Duration.ofSeconds(10))
				.readTimeout(Duration.ofSeconds(10))
				.build();
	}
	
	@Bean
	@Qualifier("aemetRestTemplate")
	public RestTemplate aemetRestTemplate(RestTemplateBuilder builder) {
		return builder
				.rootUri(aemetBaseUrl)
				.connectTimeout(Duration.ofSeconds(10))
				.readTimeout(Duration.ofSeconds(10))
				.additionalInterceptors((request, body, execution) -> {
					request.getHeaders().set("api_key", aemetApiKey);
					return execution.execute(request, body);
				})
				.build();
	}

	@Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

}
