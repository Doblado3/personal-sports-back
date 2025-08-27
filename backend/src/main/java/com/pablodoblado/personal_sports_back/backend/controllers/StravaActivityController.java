package com.pablodoblado.personal_sports_back.backend.controllers;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.pablodoblado.personal_sports_back.backend.services.StravaActivityService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
@Validated
public class StravaActivityController {
	
	public static final String STRAVA_PATH = "/api/trainingActivities";
	public static final String FETCH_ACTIVITIES_USER = STRAVA_PATH + "/fetchStravaActivities/{usuarioId}";

    private final StravaActivityService stravaService;
    
   

    /**
     * Reactive endpoint method to, calling Strava servers, fetch user strava activities
     * The method get the activities and saved them to our database for later processing porpuses
     * That's way the PostMapping tag instead of a GetMapping one
     * It returns a 201 HTTP Created and the Location from which the user can obtain his activities
     * 
     * param **before**: epoch timestamp to use for filtering activities that have taken place BEFORE a certain time
     * param **after**: epoch timestamp to use for filtering activities that have taken place after a certain time
     * 
     * Understand that the after date must be earlier than the before one
     * */
    
    @PostMapping(FETCH_ACTIVITIES_USER)
    public ResponseEntity<Void> fetchAndSaveStravaActivities(@PathVariable UUID usuarioId,
                                                            @RequestParam Long before, @RequestParam Long after,
                                                            @RequestParam(required = false, defaultValue = "1") Integer page, 
                                                            @RequestParam(required = false, defaultValue = "30") @Min(1) @Max(200) Integer perPageResults) {
    	
    	if(before < after) {
    		
    		return ResponseEntity.badRequest().build();
    	}
    	
    	CompletableFuture<Integer> future = stravaService.fetchAndSaveStravaActivities(usuarioId, before, after, page, perPageResults);

    	future.thenAccept(numSaved -> {
    	    
    	    log.info("Successfully fetched and saved {} activities for user {}", numSaved, usuarioId);
    	    
    	}).exceptionally(ex -> {
    	    
    	    log.error("Failed to fetch and save activities for user {}", usuarioId, ex);
    	    return null; 
    	});
    	
    	// Location from where to get the fetched activities
    	HttpHeaders headers = new HttpHeaders();
    	headers.add("Location", TrainingActivityController.TRAINING_ID_PATH +  "/" + usuarioId);
    	
    	return new ResponseEntity<>(headers, HttpStatus.ACCEPTED);
    	
    }
    
}