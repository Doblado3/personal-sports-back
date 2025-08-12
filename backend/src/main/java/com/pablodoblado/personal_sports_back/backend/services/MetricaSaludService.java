package com.pablodoblado.personal_sports_back.backend.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pablodoblado.personal_sports_back.backend.entities.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.models.MetricaSaludResponseDTO;

public interface MetricaSaludService {
	
	MetricaSaludResponseDTO saveMetricaDiaria(UUID usuarioId, MetricaSalud metricaSalud);
	
	Optional<MetricaSaludResponseDTO> updateMetricaSalud(UUID usuarioId, MetricaSalud metricaSalud);
	
	Optional<MetricaSalud> getRegistroByUsuarioAndDate(UUID idUsuario, LocalDate fechaRegistro);
	
	Optional<List<MetricaSalud>> getAllRegistrosForUsuario(UUID idUsuario);
	
	Page<MetricaSalud> getPaginatedRegistrosForUsuario(UUID idUsuario, Pageable pageable, String filter);
	
	Optional<List<MetricaSalud>> getRegistrosDiariosByUserInRange(UUID idUsuario, LocalDate starDate, LocalDate endDate);
	
	Boolean deleteRegistroMetrica(UUID usuarioId, LocalDate fechaRegistro);

}
