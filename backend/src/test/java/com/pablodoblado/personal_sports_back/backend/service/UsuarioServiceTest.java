package com.pablodoblado.personal_sports_back.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;
import com.pablodoblado.personal_sports_back.backend.services.impls.UsuarioServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {
	
	@Mock 
	private UsuarioRepository usuarioRepository;
	
	@InjectMocks
	private UsuarioServiceImpl usuarioService;
	
	private Usuario usuarioAntiguo;
	private Usuario nuevoUsuario;
	
	@BeforeEach
	void setUp() {
		usuarioAntiguo = new Usuario();
		usuarioAntiguo.setEmail("test@example.com");
		usuarioAntiguo.setPassword("password");
		usuarioAntiguo.setNombre("Test");
		
		nuevoUsuario = new Usuario();
		nuevoUsuario.setEmail("new@example.com");
		nuevoUsuario.setPassword("newPassword");
		nuevoUsuario.setNombre("Nuevo Usuario");
	}
	
	@Test
	void saveUsuario_shouldThrowException_whenEmailAlreadyExists() {
		when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuarioAntiguo));
		
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.saveUsuario(usuarioAntiguo);
        });

        assertTrue(thrown.getMessage().contains("El email ya está registrado"));
	}
	
	@Test
    void nuevoUsuario_shouldReturnUser_whenEmailIsUnique() {
        
        when(usuarioRepository.findByEmail(nuevoUsuario.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(nuevoUsuario);

        
        Usuario registeredUser = usuarioService.saveUsuario(nuevoUsuario);

        assertNotNull(registeredUser);
        assertEquals(nuevoUsuario.getEmail(), registeredUser.getEmail());
        
    }
	
	

}
