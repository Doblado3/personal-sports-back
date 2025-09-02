package com.pablodoblado.personal_sports_back.backend.controller;

import com.pablodoblado.personal_sports_back.backend.controllers.MetricaSaludController;
import com.pablodoblado.personal_sports_back.backend.entities.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.models.MetricaSaludRequestDTO;
import com.pablodoblado.personal_sports_back.backend.repositories.MetricaSaludRepository;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@ActiveProfiles("localpostgresql")
@SpringBootTest
public class MetricaSaludControllerIT {
	
	@Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    MetricaSaludRepository metricaSaludRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    MetricaSaludController metricaSaludController;
    
    private MetricaSalud initialMetrica;
    private Usuario user;
    private LocalDate fechaRegistro;
    
    @BeforeEach
    void setUp() {
    	
    	fechaRegistro = LocalDate.now();
    	
    	user = usuarioRepository.save(Usuario.builder()
                .nombre("Test User Save")
                .email("test.setup-" + UUID.randomUUID() + "@example.com")
                .password("password")
                .fechaNacimiento(LocalDateTime.now().minusYears(20))
                .build());
    	
    	initialMetrica = metricaSaludRepository.save(MetricaSalud.builder()
                .usuario(user)
                .fechaRegistro(fechaRegistro)
                .calidadSueno("Buena")
                .build());
    	
    }

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
        

        MetricaSaludRequestDTO request = MetricaSaludRequestDTO.builder()
                .fechaRegistro(fechaRegistro.plusDays(3))
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
    	
    	
    	UUID usuarioId = initialMetrica.getUsuario().getId();
    	LocalDate fechaRegistro = initialMetrica.getFechaRegistro();
    	
    	ResponseEntity<?> responseEntity = metricaSaludController.deleteRegistroByDate(usuarioId, fechaRegistro);
    	
    	assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
    	assertThat(metricaSaludRepository.findById(initialMetrica.getId()));
    	
    }
}

