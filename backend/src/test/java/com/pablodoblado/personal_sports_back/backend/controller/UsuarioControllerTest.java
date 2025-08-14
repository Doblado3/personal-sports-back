package com.pablodoblado.personal_sports_back.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablodoblado.personal_sports_back.backend.controllers.UsuarioController;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.mappers.UsuarioMapper;
import com.pablodoblado.personal_sports_back.backend.models.UsuarioRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.UsuarioResponseDTO;
import com.pablodoblado.personal_sports_back.backend.services.UsuarioService;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private UsuarioService usuarioService;

    @MockitoBean
    private UsuarioMapper usuarioMapper;
	
	@Autowired
	private ObjectMapper objectMapper;

    UsuarioRequestDTO usuarioRequestDTO;
    UsuarioResponseDTO usuarioResponseDTO;
    Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarioRequestDTO = UsuarioRequestDTO.builder()
                .nombre("Test User")
                .email("test@example.com")
                .password("password")
                .fechaNacimiento(LocalDateTime.now().minusYears(20))
                .build();

        usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nombre("Test User")
                .email("test@example.com")
                .password("password")
                .fechaNacimiento(LocalDateTime.now().minusYears(20))
                .build();
        
        usuarioResponseDTO = UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombre("Test User")
                .email("test@example.com")
                .fechaNacimiento(usuario.getFechaNacimiento())
                .build();
    }
	
	@Test
    void testSaveUsuario() throws Exception {
        given(usuarioMapper.usuarioRequestDTOtoUsuario(any(UsuarioRequestDTO.class))).willReturn(usuario);
        given(usuarioService.saveUsuario(any(Usuario.class))).willReturn(usuario);

        mockMvc.perform(post(UsuarioController.USUARIO_SAVE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void testFindAllUsuarios() throws Exception {
        List<UsuarioResponseDTO> usuarios = new ArrayList<>();
        usuarios.add(usuarioResponseDTO);

        given(usuarioService.findAll()).willReturn(usuarios);

        mockMvc.perform(get(UsuarioController.USUARIO_ALL_PATH)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(usuario.getId().toString())));
    }

    @Test
    void testGetUserById() throws Exception {
        given(usuarioService.findById(any(UUID.class))).willReturn(Optional.of(usuarioResponseDTO));

        mockMvc.perform(get(UsuarioController.USUARIO_BY_ID_PATH, usuario.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(usuario.getId().toString())));
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        given(usuarioService.findById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(UsuarioController.USUARIO_BY_ID_PATH, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateUsuario() throws Exception {
        given(usuarioMapper.usuarioRequestDTOtoUsuario(any(UsuarioRequestDTO.class))).willReturn(usuario);
        given(usuarioService.updateUsuario(any(UUID.class), any(Usuario.class))).willReturn(Optional.of(usuarioResponseDTO));

        mockMvc.perform(put(UsuarioController.USUARIO_UPDATE_PATH, usuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioRequestDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testUpdateUsuarioNotFound() throws Exception {
        when(usuarioService.updateUsuario(any(UUID.class), any(Usuario.class))).thenReturn(Optional.empty());

        mockMvc.perform(put(UsuarioController.USUARIO_UPDATE_PATH, UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUsuario() throws Exception {
        when(usuarioService.deleteUsuarioById(any(UUID.class))).thenReturn(true);

        mockMvc.perform(delete(UsuarioController.USUARIO_DELETE_PATH, UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteUsuarioNotFound() throws Exception {
        when(usuarioService.deleteUsuarioById(any(UUID.class))).thenReturn(false);

        mockMvc.perform(delete(UsuarioController.USUARIO_DELETE_PATH, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
	
}