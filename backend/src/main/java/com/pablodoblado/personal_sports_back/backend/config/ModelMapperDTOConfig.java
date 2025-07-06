package com.pablodoblado.personal_sports_back.backend.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pablodoblado.personal_sports_back.backend.dto.MetricaSaludRequestDTO;
import com.pablodoblado.personal_sports_back.backend.dto.MetricaSaludResponseDTO;
import com.pablodoblado.personal_sports_back.backend.entity.MetricaSalud;

@Configuration
public class ModelMapperDTOConfig {

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();

		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STANDARD); 

		// Del DTO a la Entidad 
		modelMapper.createTypeMap(MetricaSaludRequestDTO.class, MetricaSalud.class)
		    .addMappings(mapper -> {
		        
		    });


		// De la Entidad al DTO
		modelMapper.createTypeMap(MetricaSalud.class, MetricaSaludResponseDTO.class)
			.addMappings(mapper -> {
				// Mapeamos el id del usuario del objeto Usuario
				mapper.map(src -> src.getUsuario().getId(), MetricaSaludResponseDTO::setUsuarioId);

				// El resto de atributos se mappean automáticamente 
			});

		return modelMapper;
	}
}
