package com.pablodoblado.personal_sports_back.backend.mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

@Mapper(componentModel = "spring")
public interface TrainingActivityMapper {
	
	@Mapping(target = "usuario", expression = "java(mapUsuarioIdToUsuario(trainingActivityRequest.getUsuarioId()))")
	@Mapping(target = "fechaComienzo", source = "trainingActivityRequest.fechaComienzo", dateFormat = "dd-MM-yyyy HH:mm:ss")
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "vueltas", source = "vueltas")
	TrainingActivity mapActivityRequestToEntity(TrainingActivityRequestDTO trainingActivityRequest);
	
	@Mapping(target = "usuarioId", source = "usuario.id")
	@Mapping(target = "fechaComienzo", source = "trainingActivity.fechaComienzo", dateFormat = "dd-MM-yyyy HH:mm:ss")
	@Mapping(target = "vueltas", source = "vueltas")
	TrainingActivityResponseDTO mapActivityEntityToResponse(TrainingActivity trainingActivity);
	
	/*
	//Service-layer cleaner with @SubClassMapping: Eliminamos el check if-instanceof
	@SubclassMapping(source = CyclingActivityRequestDTO.class, target = CyclingActivity.class)
	TrainingActivity toEntity(TrainingActivityRequestDTO trainingActivityRequest);
	
	@SubclassMapping(source = CyclingActivity.class, target = CyclingActivityResponseDTO.class)
	TrainingActivityResponseDTO toDto(TrainingActivity trainingActivity);
	*/
	
	//fechaComienzo of DTO to fechaComienzo of entity
	//fechaComienzo no es nullable pero nos curamos en salud
	default OffsetDateTime map(LocalDateTime localDateTime) {
		return localDateTime != null? localDateTime.atOffset(ZoneOffset.UTC): null;
		
	}
	
	default LocalDateTime map(OffsetDateTime offsetDateTime) {
		return offsetDateTime != null? offsetDateTime.toLocalDateTime(): null;
	}
	
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
