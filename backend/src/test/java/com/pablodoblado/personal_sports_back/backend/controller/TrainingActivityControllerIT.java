package com.pablodoblado.personal_sports_back.backend.controller;

import com.pablodoblado.personal_sports_back.backend.controllers.TrainingActivityController;
import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;
import com.pablodoblado.personal_sports_back.backend.models.TrainingActivityRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.TrainingActivityResponseDTO;
import com.pablodoblado.personal_sports_back.backend.repositories.TrainingActivityRepository;
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

import java.time.LocalDateTime;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@ActiveProfiles("localpostgresql")
@SpringBootTest
public class TrainingActivityControllerIT {
	
	@Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    TrainingActivityController trainingActivityController;
    
    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    TrainingActivityRepository trainingActivityRepository;
    
    private TrainingActivity trainingActivity;
    private LocalDateTime fecha;
    private Usuario usuario;
    
    @BeforeEach
    void setUp() {
    	
    	fecha = LocalDateTime.now();
    	
    	usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Test User Setup")
                .email("test.setup-" + UUID.randomUUID() + "@example.com")
                .password("password")
                .fechaNacimiento(LocalDateTime.now().minusYears(30))
                .build());
    	
    	trainingActivity = trainingActivityRepository.save(TrainingActivity.builder()
			.tipo(TipoActividad.MOVILIDAD)
			.id(2L)
			.usuario(usuario)
			.nombre("actividad test")
			.distancia(25.0)
			.fechaComienzo(fecha)
			.build());
    }

    @Test
    void testGetActivityByIdNotFound() {
        assertThrows(NotFoundException.class, () -> {
            trainingActivityController.getActivityById(9999L);
        });
    }

    @Test
    void testUpdateActivityNotFound() {
        assertThrows(NotFoundException.class, () -> {
            trainingActivityController.updateActivityById(9999L, TrainingActivityRequestDTO.builder().build());
        });
    }

    @Test
    void testDeleteActivityNotFound() {
        assertThrows(NotFoundException.class, () -> {
            trainingActivityController.deleteActivityById(9999L);
        });
    }

    @Transactional
    @Rollback
    @Test
    void testListActivities() throws NotFoundException {
        List<TrainingActivityResponseDTO> dtos = trainingActivityController.listActivitiesByParams(null, null, null, null);
        assertThat(dtos.size()).isEqualTo(trainingActivityRepository.count());
    }

    @Transactional
    @Rollback
    @Test
    void testGetActivityById() throws NotFoundException {
        
        TrainingActivityResponseDTO dto = trainingActivityController.getActivityById(trainingActivity.getId());
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(trainingActivity.getId());
    }

    @Transactional
    @Rollback
    @Test
    void testUpdateActivity() throws NotFoundException {

        TrainingActivityRequestDTO requestDTO = TrainingActivityRequestDTO.builder()
                .nombre("UPDATED")
                .tipo(TipoActividad.MOVILIDAD)
                .distancia(25.0)
                .fechaComienzo(OffsetDateTime.of(trainingActivity.getFechaComienzo(), ZoneOffset.MIN))
                .build();

        ResponseEntity<?> responseEntity = trainingActivityController.updateActivityById(trainingActivity.getId(), requestDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        TrainingActivity updatedActivity = trainingActivityRepository.findById(trainingActivity.getId()).get();
        assertThat(updatedActivity.getNombre()).isEqualTo("UPDATED");
        assertThat(updatedActivity.getTipo()).isEqualTo(TipoActividad.MOVILIDAD);
    }

    @Transactional
    @Rollback
    @Test
    void testDeleteActivity() throws NotFoundException {
        
        Long activityId = trainingActivity.getId();

        ResponseEntity<?> responseEntity = trainingActivityController.deleteActivityById(activityId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(trainingActivityRepository.findById(activityId)).isEmpty();
    }
}