package com.pablodoblado.personal_sports_back.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import com.pablodoblado.personal_sports_back.backend.service.UsuarioService;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private UsuarioService usuarioService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private Usuario testUser;
	private Usuario anotherUser;
	
	private List<Usuario> listaUsuariosTest;
	
	private final String controllerBaseUrl = "/api/usuario";
	
	
	
	@BeforeEach
	public void setup() {
		
		testUser = new Usuario();
		testUser.setId(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"));
        testUser.setNombre("testuser");
        testUser.setEmail("emailprimerus@gmail.com");
        testUser.setFechaNacimiento(LocalDateTime.of(LocalDate.of(2002, 2, 2), LocalTime.MIDNIGHT));
        testUser.setPassword("password");
        
        anotherUser = new Usuario();
        anotherUser.setId(UUID.fromString("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12"));
        anotherUser.setNombre("anotheruser");
        anotherUser.setEmail("emailsegundous@gmail.com");
        anotherUser.setFechaNacimiento(LocalDateTime.of(LocalDate.of(2004, 2, 2), LocalTime.MIDNIGHT));
        anotherUser.setPassword("anotherPassword");
        
        listaUsuariosTest = new ArrayList<>();
        listaUsuariosTest.add(testUser);
        listaUsuariosTest.add(anotherUser);

        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
		
	}
	
	@Test
	void whenSaveMethodIsCalled_thenReturnOkMessage() throws Exception {
		
		when(usuarioService.saveUsuario(any(Usuario.class))).thenReturn(testUser);
		
		mockMvc.perform(post(controllerBaseUrl + "/save").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(testUser)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
		
	}
	
	@Test
	void whenFindAllMethodIsCalled_thenReturnAListOfUsers() throws Exception {
		
		when(usuarioService.findAll()).thenReturn(listaUsuariosTest);
		
		mockMvc.perform(get(controllerBaseUrl + "/findAll")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(listaUsuariosTest.size())));
	}
	
	@Test
	void whenServiceThrowsException_thenReturnInternalServerError() throws Exception {
		when(usuarioService.findAll()).thenThrow(new RuntimeException("Something went wrong in the service!"));

		
		mockMvc.perform(get(controllerBaseUrl + "/findAll")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError()); 
	}
	
	@Test
	void whenGetUsuarioByIdIsCalled_thenReturnThatUser() throws Exception {
		
		UUID id = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
		
		when(usuarioService.findById(eq(id))).thenReturn(testUser);
		
		mockMvc.perform(get(controllerBaseUrl + "/{id}", id)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	void whenGetUsuarioByIdIsCalled_thenNotFound() throws Exception {
		
		UUID id = UUID.fromString("c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13");
		
		when(usuarioService.findById(eq(id))).thenReturn(null);
		
		mockMvc.perform(get(controllerBaseUrl + "/{id}", id)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}
	
}
