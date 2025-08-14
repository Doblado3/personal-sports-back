package com.pablodoblado.personal_sports_back.backend.controller;

import com.pablodoblado.personal_sports_back.backend.controllers.UsuarioController;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.models.UsuarioRequestDTO;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UsuarioControllerIT {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    UsuarioController usuarioController;

    @Transactional
    @Rollback
    @Test
    void testSaveUsuario() {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .nombre("Test User Save")
                .email("test.save@example.com")
                .password("password")
                .fechaNacimiento(LocalDateTime.now().minusYears(20))
                .build();

        ResponseEntity<?> responseEntity = usuarioController.save(request);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] location = responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID usuarioId = UUID.fromString(location[3]);

        Usuario usuario = usuarioRepository.findById(usuarioId).get();
        assertThat(usuario).isNotNull();
    }

    @Test
    void testFindAll() {
        List<?> list = (List<?>) usuarioController.findAll().getBody();
        assertThat(list.size()).isGreaterThan(0);
    }

    @Test
    void testFindById() throws NotFoundException {
        Usuario usuario = usuarioRepository.findAll().get(0);
        ResponseEntity<?> responseEntity = usuarioController.getUserById(usuario.getId());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    }

    @Test
    void testFindByIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            usuarioController.getUserById(UUID.randomUUID());
        });
    }

    @Transactional
    @Rollback
    @Test
    void testUpdateUsuario() throws NotFoundException {
    	
        Usuario usuario = usuarioRepository.findAll().get(0);
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .nombre("UPDATED")
                .email(usuario.getEmail())
                .password(usuario.getPassword())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .build();

        ResponseEntity<?> responseEntity = usuarioController.updateUsuarioById(usuario.getId(), request);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        Usuario updatedUsuario = usuarioRepository.findById(usuario.getId()).get();
        assertThat(updatedUsuario.getNombre()).isEqualTo("UPDATED");
    }

    @Test
    void testUpdateUsuarioNotFound() {
        assertThrows(NotFoundException.class, () -> {
            usuarioController.updateUsuarioById(UUID.randomUUID(), UsuarioRequestDTO.builder().build());
        });
    }

    @Transactional
    @Rollback
    @Test
    void testDeleteUsuario() throws NotFoundException {
        Usuario usuario = usuarioRepository.findAll().get(0);
        ResponseEntity<?> responseEntity = usuarioController.delete(usuario.getId());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(usuarioRepository.findById(usuario.getId())).isEmpty();
    }

    @Test
    void testDeleteUsuarioNotFound() {
        assertThrows(NotFoundException.class, () -> {
            usuarioController.delete(UUID.randomUUID());
        });
    }
}