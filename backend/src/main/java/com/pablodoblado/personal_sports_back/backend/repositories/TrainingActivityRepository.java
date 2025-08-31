 package com.pablodoblado.personal_sports_back.backend.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;

public interface TrainingActivityRepository extends JpaRepository<TrainingActivity, Long>, JpaSpecificationExecutor<TrainingActivity> {
	
	Optional<TrainingActivity> findByUsuarioAndTipo(Usuario usuario, TipoActividad tipo);
	
	// Actualizar actividades diarias
	List<TrainingActivity> findByUsuario_IdAndFechaComienzo(UUID idUsuario, LocalDateTime fechaComienzo);
		
	Optional<TrainingActivity> findAllByTipoAndFechaComienzoAndPulsoMedioBetween(TipoActividad tipoActividad, LocalDateTime fechaComienzo, Double rangoMinimo, Double rangoMaximo);
	
	Optional<TrainingActivity> findByUsuarioAndDistanciaBetween(Usuario usuario, Double distanciaMin, Double distanciaMax);
	
	void deleteByFechaComienzo(LocalDateTime fechaComienzo);
	
	// Filtrar actividades por año, mes, semana
	List<TrainingActivity> findAllByUsuario_IdAndFechaComienzoBetween(UUID idUsuario, LocalDateTime fechaIni, LocalDateTime fechaFin);

}
