package com.pablodoblado.personal_sports_back.backend.services;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface StravaActivityService {
	
	Mono<Void> fetchAndSaveStravaActivities(UUID usuarioId, Long before, Long after, Integer page, Integer perPageResults);

}
