package com.pablodoblado.personal_sports_back.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import com.pablodoblado.personal_sports_back.backend.service.UsuarioService;

@RestController
@RequestMapping("/api")
public class UsuarioController {
	
	private final UsuarioService usuarioService;
	
	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}
	
	@PostMapping("/save")
	public Usuario save(@RequestBody Usuario usuario) {
		return usuarioService.saveUsuario(usuario);
		
	}
	
	@GetMapping("/findAll")
	public List<Usuario> findAll(){
		return usuarioService.findAll();
	}
	
	@GetMapping("/findById")
	public Usuario findById(UUID id){
		return usuarioService.findById(id);
	}
	
	@PutMapping("/update")
	public Usuario update(@RequestParam UUID id,@RequestBody Usuario usuario) {
		return usuarioService.updateUsuario(id, usuario);
	}
	
	@DeleteMapping("/delete")
	public void delete(@RequestBody Usuario usuario) {
		usuarioService.deleteUsuario(usuario);
		
	}
	

}
