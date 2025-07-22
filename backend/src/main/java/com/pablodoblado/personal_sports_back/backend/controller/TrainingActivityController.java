package com.pablodoblado.personal_sports_back.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pablodoblado.personal_sports_back.backend.dto.TrainingActivity.TrainingActivityResponseDTO;
import com.pablodoblado.personal_sports_back.backend.service.TrainingActivityService;

@RestController
@RequestMapping("/api/trainingActivities")
@CrossOrigin(origins = "http://localhost:4200")
public class TrainingActivityController {
	
	private final TrainingActivityService trainingActivityService;
	
	public TrainingActivityController(TrainingActivityService trainingActivityService) {
		this.trainingActivityService = trainingActivityService;
	}
	
	/** Endpoint method to fetch user strava activities
	 * */
	@PostMapping("/fetchStravaActivities/{usuarioId}")
	public ResponseEntity<?> fetchStravaActivities(@PathVariable  UUID usuarioId, 
			@RequestParam Long before, @RequestParam Long after,
			@RequestParam Integer page, @RequestParam Integer perPageResults) {
		try {
			
			List<TrainingActivityResponseDTO> respuesta = trainingActivityService.fetchAndSaveStravaActivities(usuarioId, before, after, page, perPageResults);
			return new ResponseEntity<>(respuesta, HttpStatus.OK);

			
		} catch (Exception e) {
			
			return new ResponseEntity<>("Ha ocurrido un error inesperado : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
		}
		
		
	}

}
