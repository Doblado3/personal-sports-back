package com.pablodoblado.personal_sports_back.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.pablodoblado.personal_sports_back.backend.entity.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.entity.Usuario;

public interface MetricaSaludRepository extends JpaRepository<MetricaSalud, UUID>, JpaSpecificationExecutor<MetricaSalud> {
	
	Optional<MetricaSalud> findByUsuarioAndFechaRegistro(Usuario usuario, LocalDate fechaRegistro);
	
	List<MetricaSalud> findByUsuarioOrderByFechaRegistro(Usuario usuario);
	
	List<MetricaSalud> findByUsuarioAndFechaRegistroBetween(Usuario usuario, LocalDate starDate, LocalDate endDate);
	
	Optional<MetricaSalud> findByFechaRegistro(LocalDate fechaRegistro);
	
	//Genera la consulta SQL tomando los valores para las clausulas LIMIT/OFFSET del Objeto Pageable
	Page<MetricaSalud> findByUsuarioId(UUID idUsuario, Pageable pageable);
	
	void deleteByFechaRegistro(LocalDate fechaRegistro);

}
