package com.pablodoblado.personal_sports_back.backend.services.impls;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;
import com.pablodoblado.personal_sports_back.backend.mappers.TrainingActivityMapper;
import com.pablodoblado.personal_sports_back.backend.models.TrainingActivityResponseDTO;
import com.pablodoblado.personal_sports_back.backend.repositories.TrainingActivityRepository;
import com.pablodoblado.personal_sports_back.backend.services.TrainingActivityService;
import com.pablodoblado.personal_sports_back.backend.specifications.TrainingActivitySpecifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TrainingActivityServiceImpl implements TrainingActivityService {
	
	private final TrainingActivityRepository activityRepository;
    private final TrainingActivityMapper trainingActivityMapper;

    @Autowired
    public TrainingActivityServiceImpl(
        TrainingActivityRepository activityRepository,
        @Qualifier("trainingActivityMapperImpl") TrainingActivityMapper trainingActivityMapper
    ) {
        this.activityRepository = activityRepository;
        this.trainingActivityMapper = trainingActivityMapper;
    }

	

	@Override
	public Optional<List<TrainingActivityResponseDTO>> listActivities(LocalDateTime dia, TipoActividad tipo,
			Double minZoneRange, Double maxZoneRange) {
		
		Specification<TrainingActivity> spec = TrainingActivitySpecifications.findByDiaTipoZonas(dia, tipo, minZoneRange, maxZoneRange);
	    List<TrainingActivity> activitiesList = activityRepository.findAll(spec);
	
	    if (activitiesList.isEmpty()) {
	        return Optional.empty();
	    }
	    
	    
	    return Optional.of(activitiesList.stream()
	            .map(activity -> trainingActivityMapper.mapActivityEntityToResponse(activity))
	            .collect(Collectors.toList()));
	}

	@Override
	public Optional<TrainingActivityResponseDTO> findActivityById(Long id) {
		return Optional.ofNullable(trainingActivityMapper.mapActivityEntityToResponse(activityRepository.findById(id)
				.orElse(null)));
	}

	@Override
	public Boolean deleteActivityById(Long id) {
		
		if(activityRepository.existsById(id)) {
			activityRepository.deleteById(id);
			
			return true;
		}
		
		return false;
	}

	@Override
	public Optional<TrainingActivityResponseDTO> updateActivityById(Long id, TrainingActivity trainingActivity) {
		return activityRepository.findById(id)
				.map(act -> {
					
					act.setDesnivel(trainingActivity.getDesnivel());
					act.setDistancia(trainingActivity.getDistancia());
					act.setFechaComienzo(trainingActivity.getFechaComienzo());
					act.setFeedback(trainingActivity.getFeedback());
					act.setHidratos(trainingActivity.getHidratos());
					act.setHumedad(trainingActivity.getHumedad());
					act.setKiloJulios(trainingActivity.getKiloJulios());
					act.setLitrosAgua(trainingActivity.getLitrosAgua());
					act.setLluvia(trainingActivity.getLluvia());
					act.setMaxAltitud(trainingActivity.getMaxAltitud());
					act.setMinAltitud(trainingActivity.getMinAltitud());
					act.setNombre(trainingActivity.getNombre());
					act.setPulsoMaximo(trainingActivity.getPulsoMaximo());
					act.setPulsoMedio(trainingActivity.getPulsoMedio());
					act.setRpeObjetivo(trainingActivity.getRpeObjetivo());
					act.setRpeReal(trainingActivity.getRpeReal());
					act.setStartLatlng(trainingActivity.getStartLatlng());
					act.setTemperatura(trainingActivity.getTemperatura());
					act.setTiempoActivo(trainingActivity.getTiempoActivo());
					act.setTiempoTotal(trainingActivity.getTiempoTotal());
					act.setTipo(trainingActivity.getTipo());
					act.setVelocidadMaxima(trainingActivity.getVelocidadMaxima());
					act.setVelocidadMedia(trainingActivity.getVelocidadMedia());
					act.setViento(trainingActivity.getViento());
					act.setVueltas(trainingActivity.getVueltas());
										log.info("Actualizando instancia del objeto TrainingActivity");
					TrainingActivity savedActivity = activityRepository.save(act);
					return trainingActivityMapper.mapActivityEntityToResponse(savedActivity);
					
				});
	}
	
	@Override
	public Optional<List<TrainingActivityResponseDTO>> findActivitiesByUsuarioAndDate(UUID idUsuario, LocalDateTime fecha) {
		
		List<TrainingActivity> activitiesList = activityRepository.findByUsuario_IdAndFechaComienzo(idUsuario, fecha);
		
	    if (activitiesList.isEmpty()) {
	        return Optional.empty();
	    }
	    
	    
	    List<TrainingActivityResponseDTO> resultado = activitiesList.stream()
	            .map(activity -> trainingActivityMapper.mapActivityEntityToResponse(activity))
	            .collect(Collectors.toList());
	    
	    return Optional.of(resultado);
		
	}



	@Override
	public Optional<List<TrainingActivityResponseDTO>> findActivitiesByUsuarioDateRange(UUID idUsuario,
			LocalDateTime fechaIni, LocalDateTime fechaFin) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

}
