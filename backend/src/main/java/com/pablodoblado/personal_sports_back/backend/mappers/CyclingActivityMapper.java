package com.pablodoblado.personal_sports_back.backend.mappers;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.pablodoblado.personal_sports_back.backend.entities.CyclingActivity;
import com.pablodoblado.personal_sports_back.backend.models.CyclingActivityRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.CyclingActivityResponseDTO;

@Mapper(uses = {DateMapper.class}, componentModel = "spring")
public interface CyclingActivityMapper extends TrainingActivityMapper {
	
	@InheritConfiguration(name = "mapActivityRequestToEntity")
	@Mapping(target = "potenciometro", source = "trainingActivityRequest.potenciometro")
	@Mapping(target = "cadencia", source = "trainingActivityRequest.cadencia")
	@Mapping(target = "vatiosMedios", source = "trainingActivityRequest.vatiosMedios")
	@Mapping(target = "vatiosMaximos", source = "trainingActivityRequest.vatiosMaximos")
	CyclingActivity mapActivityRequestToEntity(CyclingActivityRequestDTO trainingActivityRequest);
	
	@InheritConfiguration(name = "mapActivityEntityToResponse")
	@Mapping(target = "potenciometro", source = "entity.potenciometro")
	@Mapping(target = "cadencia", source = "entity.cadencia")
	@Mapping(target = "vatiosMedios", source = "entity.vatiosMedios")
	@Mapping(target = "vatiosMaximos", source = "entity.vatiosMaximos")
	CyclingActivityResponseDTO mapActivityEntityToResponse(CyclingActivity entity);

}
