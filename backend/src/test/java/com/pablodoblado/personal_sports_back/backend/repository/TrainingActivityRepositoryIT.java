package com.pablodoblado.personal_sports_back.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;
import com.pablodoblado.personal_sports_back.backend.repositories.TrainingActivityRepository;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;

import org.testcontainers.containers.PostgreSQLContainer;


@Testcontainers
@ActiveProfiles("localpostgresql")
@SpringBootTest
public class TrainingActivityRepositoryIT {
	
	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
	
	@Autowired
	UsuarioRepository usuarioRepository;
	
	@Autowired
	TrainingActivityRepository trainingActivityRepository;
	
	private LocalDateTime fecha;
	private Usuario savedUsuario;
	private List<TrainingActivity> list;
	Optional<TrainingActivity> activity;
	
	
	@BeforeEach
	void setUp() {
		
		savedUsuario = usuarioRepository.save(Usuario.builder()
                .nombre("Test User")
                .email("test.setup-" + UUID.randomUUID() + "@example.com")
                .password("password")
                .fechaNacimiento(LocalDateTime.now().minusYears(20))
                .build());
			
		
		fecha = LocalDateTime.of(2025, Month.APRIL, 20, 0, 0);
		
		TrainingActivity trainingActivity = trainingActivityRepository.save(TrainingActivity.builder()
				.id(3L)
				.usuario(savedUsuario)
				.nombre("actividad de test")
				.tipo(TipoActividad.RUNNING)
				.fechaComienzo(fecha)
				.pulsoMedio(142.0)
				.build());
		
		list = trainingActivityRepository.findAll();
		
		activity = trainingActivityRepository.findAllByTipoAndFechaComienzoAndPulsoMedioBetween(TipoActividad.RUNNING, fecha, 140.0, 150.0);
	}
	
	@Test
	void testListActivities() {
		
		
		assertThat(postgres.isCreated()).isTrue();
		assertThat(postgres.isRunning()).isTrue();
		assertThat(list.size()).isGreaterThan(0);
	}
	
	@Test
	void testFindAllByTipoAndFechaComienzoAndPulsoMedioBetween() {
				
		assertThat(activity).isNotEmpty();
		
		
		
	}
	
	

}
