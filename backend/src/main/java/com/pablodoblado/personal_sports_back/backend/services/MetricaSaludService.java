package com.pablodoblado.personal_sports_back.backend.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pablodoblado.personal_sports_back.backend.entities.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.models.MetricaSaludResponseDTO;

public interface MetricaSaludService {
	
	MetricaSaludResponseDTO saveMetricaDiaria(UUID usuarioId, MetricaSalud metricaSalud) throws NotFoundException;
	
	Optional<MetricaSaludResponseDTO> updateMetricaSalud(UUID usuarioId, MetricaSalud metricaSalud) throws NotFoundException;
	
	Optional<MetricaSalud> getRegistroByUsuarioAndDate(UUID idUsuario, LocalDate fechaRegistro) throws NotFoundException;
	
	Optional<List<MetricaSalud>> getAllRegistrosForUsuario(UUID idUsuario) throws NotFoundException;
	
	Page<MetricaSalud> getPaginatedRegistrosForUsuario(UUID idUsuario, Pageable pageable, String filter) throws NotFoundException;
	
	Optional<List<MetricaSalud>> getRegistrosDiariosByUserInRange(UUID idUsuario, LocalDate starDate, LocalDate endDate) throws NotFoundException;
	
	Boolean deleteRegistroMetrica(UUID usuarioId, LocalDate fechaRegistro) throws NotFoundException;

}
