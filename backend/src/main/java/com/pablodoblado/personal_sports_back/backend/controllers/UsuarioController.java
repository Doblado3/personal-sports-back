package com.pablodoblado.personal_sports_back.backend.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.mappers.UsuarioMapper;
import com.pablodoblado.personal_sports_back.backend.models.UsuarioRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.UsuarioResponseDTO;
import com.pablodoblado.personal_sports_back.backend.services.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UsuarioController {
	
	private final UsuarioService usuarioService;
	
	private final UsuarioMapper usuarioMapper;
	
	public static final String USUARIO_PATH = "/api/usuario";
	
	public static final String USUARIO_SAVE_PATH = USUARIO_PATH + "/save";
	
	public static final String USUARIO_ALL_PATH = USUARIO_PATH + "/findAll";
	
	public static final String USUARIO_BY_ID_PATH = USUARIO_PATH + "/{id}";
	
	public static final String USUARIO_UPDATE_PATH = USUARIO_PATH + "/update/{id}";
	
	public static final String USUARIO_DELETE_PATH = USUARIO_PATH + "/delete/{id}";
	
	
	
	
	@PostMapping(USUARIO_SAVE_PATH)
	public ResponseEntity<?> save(@Validated @RequestBody UsuarioRequestDTO usuario) {
		
		Usuario usuarioEntity = usuarioMapper.usuarioRequestDTOtoUsuario(usuario);
		Usuario nuevoUsuario = usuarioService.saveUsuario(usuarioEntity);
		
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("Location", USUARIO_PATH + "/" + nuevoUsuario.getId().toString());
		
		return new ResponseEntity(headers, HttpStatus.CREATED);
		
		
	}
	
	@GetMapping(USUARIO_ALL_PATH)
	public ResponseEntity<?> findAll() {
		
		List<UsuarioResponseDTO> usuarios = usuarioService.findAll();
		return new ResponseEntity<>(usuarios, HttpStatus.OK);
			
		
		
	}
	
	@GetMapping(USUARIO_BY_ID_PATH)
	public ResponseEntity<?> getUserById(@PathVariable UUID id) throws NotFoundException {
		 
		UsuarioResponseDTO usuario = usuarioService.findById(id).orElseThrow(NotFoundException::new);
	    return new ResponseEntity(usuario, HttpStatus.OK);
	        
	    }
	
	@PutMapping(USUARIO_UPDATE_PATH)
	public ResponseEntity<?> updateUsuarioById(@PathVariable UUID id, @Validated @RequestBody UsuarioRequestDTO usuario) throws NotFoundException {
		
		Usuario entidad = usuarioMapper.usuarioRequestDTOtoUsuario(usuario);
		
		Optional<UsuarioResponseDTO> respuesta = usuarioService.updateUsuario(id, entidad);
		
		if(respuesta == null || respuesta.isEmpty()) {
			throw new NotFoundException();
		}
		
		return new ResponseEntity<>(respuesta, HttpStatus.NO_CONTENT);
			
		
		
	}
	
	@DeleteMapping(USUARIO_DELETE_PATH)
	public ResponseEntity delete(@PathVariable UUID id) throws NotFoundException {
		
		if(!usuarioService.deleteUsuarioById(id)) {
			
			throw new NotFoundException();
		}
		
		return new ResponseEntity(HttpStatus.NO_CONTENT);
		
	}
	

}
