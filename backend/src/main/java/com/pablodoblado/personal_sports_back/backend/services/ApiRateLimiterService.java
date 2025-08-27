package com.pablodoblado.personal_sports_back.backend.services;

import org.springframework.http.HttpHeaders;

public interface ApiRateLimiterService {
	
	void checkStravaRateLimit();
	
	void updateStravaRateLimit(HttpHeaders headers);
	
	void checkAemetRateLimit();

}
