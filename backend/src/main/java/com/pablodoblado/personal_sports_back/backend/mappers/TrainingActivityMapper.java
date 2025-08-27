package com.pablodoblado.personal_sports_back.backend.mappers;


import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.SubclassMapping;

import com.pablodoblado.personal_sports_back.backend.entities.CyclingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.models.CyclingActivityRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.CyclingActivityResponseDTO;
import com.pablodoblado.personal_sports_back.backend.models.TrainingActivityRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.TrainingActivityResponseDTO;

@Mapper(componentModel = "spring", uses = {CyclingActivityMapper.class, VueltaMapper.class, DateMapper.class})
public interface TrainingActivityMapper {
	
	@SubclassMapping(source = CyclingActivityRequestDTO.class, target = CyclingActivity.class)
	@Mapping(target = "usuario", expression = "java(mapUsuarioIdToUsuario(trainingActivityRequest.getUsuarioId()))")
	@Mapping(target = "id", ignore = true)
	TrainingActivity mapActivityRequestToEntity(TrainingActivityRequestDTO trainingActivityRequest);
	
	@SubclassMapping(source = CyclingActivity.class, target = CyclingActivityResponseDTO.class)
	@Mapping(target = "usuarioId", source = "usuario.id")
	TrainingActivityResponseDTO mapActivityEntityToResponse(TrainingActivity trainingActivity);
	
	
	default Usuario mapUsuarioIdToUsuario(UUID  userId) {
		
		Usuario usuario = new Usuario();
		usuario.setId(userId);
		return usuario;
	}
	
	default TrainingActivity mapTrainingActivityIdToTrainingActivity(Long activityId) {
		TrainingActivity activity = new TrainingActivity();
		activity.setId(activityId);
		return activity;
	}
	

}
