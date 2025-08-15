package com.pablodoblado.personal_sports_back.backend.controller;

import com.pablodoblado.personal_sports_back.backend.controllers.MetricaSaludController;
import com.pablodoblado.personal_sports_back.backend.entities.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.models.MetricaSaludRequestDTO;
import com.pablodoblado.personal_sports_back.backend.repositories.MetricaSaludRepository;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class MetricaSaludControllerIT {

    @Autowired
    MetricaSaludRepository metricaSaludRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    MetricaSaludController metricaSaludController;

    @Test
    void testUpdateMetricaNotFound() {
        UUID nonExistentUserId = UUID.randomUUID();
        MetricaSaludRequestDTO request = MetricaSaludRequestDTO.builder()
                .fechaRegistro(LocalDate.now())
                .calidadSueno("Any quality")
                .build();

        assertThrows(NotFoundException.class, () -> {
            metricaSaludController.updateMetricaSalud(nonExistentUserId, request);
        });
    }

    @Transactional
    @Rollback
    @Test
    void testSaveMetricaDiaria() throws NotFoundException {
        Usuario user = usuarioRepository.save(Usuario.builder()
                .nombre("Test User Save")
                .email("test.save@example.com")
                .password("password")
                .fechaNacimiento(LocalDateTime.now().minusYears(20))
                .build());

        LocalDate fechaRegistro = LocalDate.now();
        MetricaSaludRequestDTO request = MetricaSaludRequestDTO.builder()
                .fechaRegistro(fechaRegistro)
                .calidadSueno("Buena")
                .build();

        ResponseEntity<?> responseEntity = metricaSaludController.saveMetricaDiaria(user.getId(), request);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] location = responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID usuarioId = UUID.fromString(location[4]);
        LocalDate fechaRegistroGuardado = LocalDate.parse(location[5]);

        MetricaSalud metricaSalud = metricaSaludRepository.findByUsuarioAndFechaRegistro(user, fechaRegistroGuardado).get();

        assertThat(metricaSalud).isNotNull();
        assertThat(usuarioId).isEqualTo(user.getId());
    }


    @Transactional
    @Rollback
    @Test
    void testUpdateMetricaSalud() throws NotFoundException {
        Usuario user = usuarioRepository.save(Usuario.builder()
                .nombre("Test User Update")
                .email("test.update@example.com")
                .password("password")
                .fechaNacimiento(LocalDateTime.now().minusYears(25))
                .build());

        LocalDate fechaRegistro = LocalDate.now();
        MetricaSalud initialMetrica = MetricaSalud.builder()
                .usuario(user)
                .fechaRegistro(fechaRegistro)
                .calidadSueno("Good")
                .build();
        metricaSaludRepository.save(initialMetrica);

        MetricaSaludRequestDTO request = MetricaSaludRequestDTO.builder()
                .fechaRegistro(fechaRegistro)
                .calidadSueno("UPDATED")
                .build();

        ResponseEntity<?> responseEntity = metricaSaludController.updateMetricaSalud(user.getId(), request);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        MetricaSalud updatedMetrica = metricaSaludRepository.findByUsuarioAndFechaRegistro(user, fechaRegistro).get();

        assertThat(updatedMetrica).isNotNull();
        assertThat(updatedMetrica.getCalidadSueno()).isEqualTo("UPDATED");
        assertThat(updatedMetrica.getFechaRegistro()).isEqualTo(fechaRegistro);
    }
    
    @Test
    void testDeleteMetricaSaludNotFound() throws NotFoundException {
    	
    	UUID random = UUID.randomUUID();
    	LocalDate fechaRandom = LocalDate.of(2004, 02, 20);
    	
    	assertThrows(NotFoundException.class, () -> {
            metricaSaludController.deleteRegistroByDate(random, fechaRandom);
        });
    	
    	
    	
    }
    
    @Transactional
    @Rollback
    @Test
    void testDeleteMetricaSaludByFechaRegistro() throws NotFoundException {
    	
    	MetricaSalud metricaSalud = metricaSaludRepository.findAll().get(0);
    	UUID usuarioId = metricaSalud.getUsuario().getId();
    	LocalDate fechaRegistro = metricaSalud.getFechaRegistro();
    	
    	ResponseEntity<?> responseEntity = metricaSaludController.deleteRegistroByDate(usuarioId, fechaRegistro);
    	
    	assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
    	assertThat(metricaSaludRepository.findById(metricaSalud.getId()));
    	
    }
}

