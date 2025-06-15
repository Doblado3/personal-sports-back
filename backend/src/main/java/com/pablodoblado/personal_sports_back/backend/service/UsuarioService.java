package com.pablodoblado.personal_sports_back.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import com.pablodoblado.personal_sports_back.backend.repository.UsuarioRepository;

@Service
public class UsuarioService {
	
	private final UsuarioRepository usuarioRepository;
	
	//Usamos la inyeccion de dependencias mediante constructores
	public UsuarioService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}
	
	public Usuario saveUsuario(Usuario usuario) {
		
		if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
			throw new IllegalArgumentException("Ya existe un usuario con este correo");
		}
		
		return usuarioRepository.save(usuario);
		
	}
	
	public Optional<Usuario> findUsuarioByEmail(String email){
		return usuarioRepository.findByEmail(email);
	}
	
	public Usuario findById(UUID id) {
		Optional<Usuario> usuario = usuarioRepository.findById(id);
		
		if(usuario.isEmpty()) {
			throw new RuntimeException("El usuario no existe");
		}
		
		return usuario.get();
		
	}
	
	public List<Usuario> findAll() {
		return usuarioRepository.findAll();
	}
	
	public Usuario updateUsuario(UUID id, Usuario usuario) {
		//No actualiza la password
        return usuarioRepository.findById(id).map(user -> {
        	user.setNombre(usuario.getNombre());
        	user.setApellidos(usuario.getApellidos());
        	user.setFechaNacimiento(usuario.getFechaNacimiento());
        	user.setGenero(usuario.getGenero());
            return usuarioRepository.save(user);
        }).orElseThrow(() -> new IllegalArgumentException("El usuario con id: " + id + " no existe"));
    }
	
	public void deleteUsuario(Usuario usuario) {
		usuarioRepository.delete(usuario);
	}

}
