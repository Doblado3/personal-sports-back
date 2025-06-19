package com.pablodoblado.personal_sports_back.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import com.pablodoblado.personal_sports_back.backend.service.UsuarioService;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {
	
	private final UsuarioService usuarioService;
	
	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}
	
	@PostMapping("/save")
	public ResponseEntity<?> save(@RequestBody Usuario usuario) {
		try {
			Usuario nuevoUsuario = usuarioService.saveUsuario(usuario);
			return new ResponseEntity<>(nuevoUsuario, HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT); 
            
        } catch (Exception e) {
            return new ResponseEntity<>("Ha ocurrido un error inesperado : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // Return 500
        }
		
	}
	
	@GetMapping("/findAll")
	public List<Usuario> findAll(){
		return usuarioService.findAll();
	}
	
	 @GetMapping("/{id}")
	    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
	        try {
	            Usuario usuario = usuarioService.findById(id); 
	            return new ResponseEntity<>(usuario, HttpStatus.OK); 
	        } catch (IllegalArgumentException e) { 
	            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
	        } catch (Exception e) {
	            
	            return new ResponseEntity<>("Ha ocurrido un error inesperado: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	        }
	    }
	
	@PutMapping("/update/{id}")
	public ResponseEntity<?> update(@PathVariable UUID id,@RequestBody Usuario usuario) {
		try {
			
			Usuario usuarioActualizado = usuarioService.updateUsuario(id, usuario);
			return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
			
		} catch(IllegalArgumentException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
			
		} catch(Exception e) {
			return new ResponseEntity<>("Error inesperado: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@DeleteMapping("/delete")
	public void delete(@RequestBody Usuario usuario) {
		usuarioService.deleteUsuario(usuario);
		
	}
	

}
