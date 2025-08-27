package com.pablodoblado.personal_sports_back.backend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.pablodoblado.personal_sports_back.backend.entities.CyclingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.models.StravaDetailedActivityDTO;

@Mapper(componentModel = "spring")
public interface StravaActivityMapper {
	
	@Mapping(target = "usuario", ignore = true)
	@Mapping(target = "startLatlng", expression = "java(dto.getLatlng() != null && !dto.getLatlng().isEmpty() ? dto.getLatlng().get(0) : null)")
	TrainingActivity toTrainingActivity(StravaDetailedActivityDTO dto);
	
	@Mapping(target = "usuario", ignore = true)
	@Mapping(target = "startLatlng", expression = "java(dto.getLatlng() != null && !dto.getLatlng().isEmpty() ? dto.getLatlng().get(0) : null)")
	@Mapping(target = "cadencia", source = "cadence")
	CyclingActivity toCyclingActivity(StravaDetailedActivityDTO dto);

}
