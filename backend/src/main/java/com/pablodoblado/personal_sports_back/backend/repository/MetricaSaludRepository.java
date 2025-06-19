package com.pablodoblado.personal_sports_back.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pablodoblado.personal_sports_back.backend.entity.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.entity.Usuario;

public interface MetricaSaludRepository extends JpaRepository<MetricaSalud, UUID> {
	
	Optional<MetricaSalud> findByUsuarioAndFechaRegistro(Usuario usuario, LocalDate fechaRegistro);
	
	List<MetricaSalud> findByUsuarioOrderByFechaRegistro(Usuario usuario);
	
	List<MetricaSalud> findByUsuarioAndFechaRegistroBetween(Usuario usuario, LocalDate starDate, LocalDate endDate);

}
