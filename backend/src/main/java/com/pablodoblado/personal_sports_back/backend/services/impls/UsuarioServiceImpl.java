package com.pablodoblado.personal_sports_back.backend.services.impls;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.mappers.UsuarioMapper;
import com.pablodoblado.personal_sports_back.backend.models.UsuarioResponseDTO;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;
import com.pablodoblado.personal_sports_back.backend.services.UsuarioService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {
	
	private final UsuarioRepository usuarioRepository;
	
	private final UsuarioMapper usuarioMapper;
	
	@Override
	public Usuario saveUsuario(Usuario usuario) {
		
		if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
			throw new IllegalArgumentException("El email ya está registrado");
		}
		
		return usuarioRepository.save(usuario);
		
	}
	
	@Override
	public Optional<Usuario> findUsuarioByEmail(String email){
		return usuarioRepository.findByEmail(email);
	}
	
	
	@Override
	public List<UsuarioResponseDTO> findAll() {
		
		List<Usuario> usuarios = usuarioRepository.findAll();
		
		return usuarioRepository.findAll()
				.stream()
				.map(usuarioMapper::usuarioToUsuarioResponseDTO)
				.collect(Collectors.toList());
	}
	
	
	@Override
	public Optional<UsuarioResponseDTO> updateUsuario(UUID id, Usuario usuario) {
		
		return usuarioRepository.findById(id)
				.map(user -> {
					user.setNombre(usuario.getNombre());
					user.setApellidos(usuario.getApellidos());
					user.setEmail(usuario.getEmail());
					user.setFechaNacimiento(usuario.getFechaNacimiento());
					user.setGenero(usuario.getGenero());
					user.setPassword(usuario.getPassword());
					log.info("Updating existing user with id: " + user.getId());
					return usuarioMapper.usuarioToUsuarioResponseDTO(usuarioRepository.save(user));
				});
    }
	
	@Override
	public Boolean deleteUsuarioById(UUID usuarioId) {
		
		if(usuarioRepository.existsById(usuarioId)) {
			
			usuarioRepository.deleteById(usuarioId);
			return true;
		}
		
		return false;
	}

	@Override
	public Optional<UsuarioResponseDTO> findById(UUID usuarioId) {
		
		return Optional.ofNullable(usuarioMapper.usuarioToUsuarioResponseDTO(usuarioRepository.findById(usuarioId)
				.orElse(null)));
	}

}
