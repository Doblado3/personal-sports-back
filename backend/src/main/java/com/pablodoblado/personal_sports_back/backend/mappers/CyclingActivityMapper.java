package com.pablodoblado.personal_sports_back.backend.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.pablodoblado.personal_sports_back.backend.entities.CyclingActivity;
import com.pablodoblado.personal_sports_back.backend.models.CyclingActivityRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.CyclingActivityResponseDTO;

@Mapper
public interface CyclingActivityMapper {
	
	@Mapping(target = "potenciometro", source = "potenciometro")
	@Mapping(target = "cadencia", source = "cadencia")
	@Mapping(target = "vatiosMedios", source = "vatiosMedios")
	@Mapping(target = "vatiosMaximos", source = "vatiosMaximos")
	CyclingActivity map(CyclingActivityRequestDTO dto);
	
	
	@InheritInverseConfiguration(name = "map")
	CyclingActivityResponseDTO map(CyclingActivity entity);

}
