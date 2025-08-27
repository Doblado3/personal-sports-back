package com.pablodoblado.personal_sports_back.backend.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;
import com.pablodoblado.personal_sports_back.backend.mappers.TrainingActivityMapper;
import com.pablodoblado.personal_sports_back.backend.models.TrainingActivityRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.TrainingActivityResponseDTO;
import com.pablodoblado.personal_sports_back.backend.services.TrainingActivityService;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class TrainingActivityController {
	
	public static final String TRAINING_PATH = "/api/trainingActivity";
	
	public static final String TRAINING_ID_PATH = TRAINING_PATH + "/{id}";
	
	public static final String TRAINING_UPDATE_PATH = TRAINING_PATH + "/update/{id}";
	
	public static final String TRAINING_DELETE_PATH = TRAINING_PATH + "/delete/{id}";
	
	private final TrainingActivityService trainingActivityService;
	private final TrainingActivityMapper trainingActivityMapper;
	
	public TrainingActivityController(TrainingActivityService trainingActivityService,
			@Qualifier("trainingActivityMapperImpl") TrainingActivityMapper trainingActivityMapper) {
		this.trainingActivityService = trainingActivityService;
		this.trainingActivityMapper = trainingActivityMapper;
	}
	
	@GetMapping(TRAINING_PATH)
	public List<TrainingActivityResponseDTO> listActivitiesByParams(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dia,
			@RequestParam(required = false) TipoActividad tipo, 
			@RequestParam(required = false) Double rangoZonaMin,
			@RequestParam(required = false) Double rangoZonaMax) throws NotFoundException {
		
		return trainingActivityService.listActivities(dia, tipo, rangoZonaMin, rangoZonaMax).orElseThrow(NotFoundException::new);
		
	}
	
	@GetMapping(TRAINING_ID_PATH)
	public TrainingActivityResponseDTO getActivityById(@PathVariable Long id) throws NotFoundException {
		
		return trainingActivityService.findActivityById(id).orElseThrow(NotFoundException::new);
	}
	
	@PutMapping(TRAINING_UPDATE_PATH)
	public ResponseEntity<?> updateActivityById(@PathVariable Long id, @Validated @RequestBody TrainingActivityRequestDTO request) throws NotFoundException {
		
		TrainingActivity entidad = trainingActivityMapper.mapActivityRequestToEntity(request);
		Optional<TrainingActivityResponseDTO> respuesta = trainingActivityService.updateActivityById(id, entidad);
		
		if(respuesta == null || respuesta.isEmpty()) {
			
			throw new NotFoundException();
		}
		
		return new ResponseEntity(HttpStatus.NO_CONTENT);
		
	}
	
	@DeleteMapping(TRAINING_DELETE_PATH)
	public ResponseEntity<?> deleteActivityById(@PathVariable Long id) throws NotFoundException {
		
		if(!trainingActivityService.deleteActivityById(id)) {
			
			throw new NotFoundException();
		}
		
		return new ResponseEntity(HttpStatus.NO_CONTENT);
		
	}

}
