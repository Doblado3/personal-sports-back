package com.pablodoblado.personal_sports_back.backend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.Vuelta;
import com.pablodoblado.personal_sports_back.backend.models.VueltaRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.VueltaResponseDTO;

@Mapper(uses = {DateMapper.class})
public interface VueltaMapper {
	
	@Mapping(target = "id", ignore = true)
    @Mapping(target = "trainingActivity", expression = "java(mapTrainingActivityIdToTrainingActivity(dto.getTrainingActivityId()))")
    Vuelta mapRequestToEntity(VueltaRequestDTO dto);

    
    @Mapping(target = "trainingActivityId", source = "trainingActivity.id")
    VueltaResponseDTO mapEntityToResponse(Vuelta entity);

    // This is a helper method to create a `TrainingActivity` entity from just its ID.
    // MapStruct will automatically use this method when needed.
    default TrainingActivity mapTrainingActivityIdToTrainingActivity(Long activityId) {
        if (activityId == null) {
            return null;
        }
        TrainingActivity trainingActivity = new TrainingActivity();
        trainingActivity.setId(activityId);
        return trainingActivity;
    }

}
