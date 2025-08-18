package com.pablodoblado.personal_sports_back.backend.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;
import com.pablodoblado.personal_sports_back.backend.models.TrainingActivityResponseDTO;

public interface TrainingActivityService {
	
	Optional<List<TrainingActivityResponseDTO>> listActivities(LocalDateTime dia, TipoActividad tipo, Double minZoneRange, Double maxZoneRange);
	
	Optional<TrainingActivityResponseDTO> findActivityById(Long id);
	
	Boolean deleteActivityById(Long id);
	
	Optional<TrainingActivityResponseDTO> updateActivityById(Long id, TrainingActivity trainingActivity);
	
	

}
