package com.pablodoblado.personal_sports_back.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pablodoblado.personal_sports_back.backend.entity.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import com.pablodoblado.personal_sports_back.backend.entity.enums.TipoActividad;

public interface TrainingActivityRepository extends JpaRepository<TrainingActivity, Long> {
	
	Optional<TrainingActivity> findByUsuarioAndTipo(Usuario usuario, TipoActividad tipo);
	
	Optional<TrainingActivity> findByFechaComienzo(LocalDateTime fechaComienzo);
	
	Optional<TrainingActivity> findByUsuarioAndDistanciaBetween(Usuario usuario, Double distanciaMin, Double distanciaMax);
	
	void deleteByFechaComienzo(LocalDateTime fechaComienzo);

}
