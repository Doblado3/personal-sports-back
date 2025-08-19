package com.pablodoblado.personal_sports_back.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
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
@SpringBootTest
@ActiveProfiles("localpostgresql")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TrainingActivityRepositoryIT {
	
	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
	
	@Autowired
	UsuarioRepository usuarioRepository;
	
	@Autowired
	TrainingActivityRepository trainingActivityRepository;
	
	LocalDateTime fecha;
	Usuario savedUsuario;
	
	
	@BeforeEach
	void setUp() {
		
		Usuario usuario = Usuario.builder()
                .nombre("Test User")
                .email("test6375463@example.com")
                .password("password")
                .fechaNacimiento(LocalDateTime.now().minusYears(20))
                .build();
		
		savedUsuario = usuarioRepository.save(usuario);		
		
		fecha = LocalDateTime.of(2025, Month.APRIL, 20, 0, 0);
		
		TrainingActivity trainingActivity = TrainingActivity.builder()
				.id(3L)
				.usuario(savedUsuario)
				.nombre("actividad de test")
				.tipo(TipoActividad.RUNNING)
				.fechaComienzo(fecha)
				.pulsoMedio(142.0)
				.build();
		
		trainingActivityRepository.save(trainingActivity);
	}
	
	@Test
	void testListActivities() {
		
		List<TrainingActivity> list = trainingActivityRepository.findAll();
		
		assertThat(postgres.isCreated()).isTrue();
		assertThat(postgres.isRunning()).isTrue();
		assertThat(list.size()).isGreaterThan(0);
	}
	
	@Test
	void testFindAllByTipoAndFechaComienzoAndPulsoMedioBetween() {
		
		Optional<TrainingActivity> activity = trainingActivityRepository.findAllByTipoAndFechaComienzoAndPulsoMedioBetween(TipoActividad.RUNNING, fecha, 140.0, 150.0);
		
		assertThat(activity).isNotEmpty();
		
		
		
	}
	
	

}
