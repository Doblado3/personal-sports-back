package com.pablodoblado.personal_sports_back.backend.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.models.UsuarioResponseDTO;

public interface UsuarioService {
	
	Usuario saveUsuario(Usuario usuario);
	
	Optional<Usuario> findUsuarioByEmail(String email);
	
	Optional<UsuarioResponseDTO> findById(UUID usuarioId);
	
	List<UsuarioResponseDTO> findAll();
	
	Optional<UsuarioResponseDTO> updateUsuario(UUID id, Usuario usuario);
	
	Boolean deleteUsuarioById(UUID usuarioId);

}
