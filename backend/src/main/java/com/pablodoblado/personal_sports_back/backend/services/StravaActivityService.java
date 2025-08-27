package com.pablodoblado.personal_sports_back.backend.services;


import java.util.UUID;
import java.util.concurrent.CompletableFuture;




public interface StravaActivityService {
	
	/**
	 * Method that fetch the activities from Strava and save them to the database
	 * Returns the number of activities saved
	 * */
	CompletableFuture<Integer> fetchAndSaveStravaActivities(UUID usuarioId, Long before, Long after, Integer page, Integer perPageResults);


}
