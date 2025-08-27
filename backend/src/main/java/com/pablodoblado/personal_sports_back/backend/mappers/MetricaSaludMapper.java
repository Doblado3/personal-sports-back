package com.pablodoblado.personal_sports_back.backend.mappers;

import org.mapstruct.Mapper;

import com.pablodoblado.personal_sports_back.backend.entities.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.models.MetricaSaludRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.MetricaSaludResponseDTO;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class})
public interface MetricaSaludMapper {
	
	MetricaSalud metricaSaludRequestToMetricaSalud(MetricaSaludRequestDTO metricaSaludRequestDTO);
	
	MetricaSaludResponseDTO metricaSaludToMetricaSaludResponseDTO(MetricaSalud metricaSalud);

}