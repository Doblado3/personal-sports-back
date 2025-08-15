package com.pablodoblado.personal_sports_back.backend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pablodoblado.personal_sports_back.backend.entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
	
	//Spring Data JPA se ocupa de save(), findById(), findAll()...
	
	//Spring Data adivina la query con el nombre del metodo
	Optional<Usuario> findByEmail(String email);
	
	Optional<Usuario> findByStravaAthleteId(Long stravaId);

}
