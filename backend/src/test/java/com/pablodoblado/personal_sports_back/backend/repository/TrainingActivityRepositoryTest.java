package com.pablodoblado.personal_sports_back.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.pablodoblado.personal_sports_back.backend.entity.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import com.pablodoblado.personal_sports_back.backend.entity.enums.TipoActividad;

import jakarta.persistence.EntityManager;

@DataJpaTest
public class TrainingActivityRepositoryTest {
	
	@Autowired
	private EntityManager entityManager;
	
	@Autowired
	private TrainingActivityRepository trainingActivityRepository;
	
	private Usuario usuario;
	private TrainingActivity trainingActivity;
	private LocalDateTime startTime;
	
	@BeforeEach
	void setUp() {
		
		this.startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		
		usuario = new Usuario();
        usuario.setNombre("testuser");
        usuario.setEmail("testuser@example.com");
        usuario.setFechaNacimiento(LocalDateTime.now());
        usuario.setPassword("password");
        entityManager.persist(usuario);
        entityManager.flush();
        
        trainingActivity = new TrainingActivity();
        trainingActivity.setId(36822349223L);
        trainingActivity.setUsuario(usuario);
        trainingActivity.setNombre("Morning Run");
        trainingActivity.setTipo(TipoActividad.RUNNING);
        trainingActivity.setFechaComienzo(startTime);
        trainingActivity.setDistancia(40.0);
        entityManager.persist(trainingActivity);
		entityManager.flush();
	}
	
	@Test
    void testFindByUsuarioAndTipo() {
        TipoActividad tipo = TipoActividad.RUNNING;
        TrainingActivity foundActivity = trainingActivityRepository.findByUsuarioAndTipo(usuario, tipo).orElse(null);

        assertThat(foundActivity).isNotNull();
        assertThat(foundActivity.getId()).isEqualTo(trainingActivity.getId());
        assertThat(foundActivity.getNombre()).isEqualTo(trainingActivity.getNombre());
    }

    @Test
    void testFindByFechaComienzo() {
        TrainingActivity foundActivity = trainingActivityRepository.findByFechaComienzo(startTime).orElse(null);

        assertThat(foundActivity).isNotNull();
        assertThat(foundActivity.getId()).isEqualTo(trainingActivity.getId());
        assertThat(foundActivity.getFechaComienzo()).isEqualTo(trainingActivity.getFechaComienzo());
    }

    @Test
    void testFindByUsuarioAndDistanciaBetween() {
        Double distanciaMin = 20.0;
        Double distanciaMax = 70.0;
        TrainingActivity foundActivity = trainingActivityRepository.findByUsuarioAndDistanciaBetween(usuario, distanciaMin, distanciaMax).orElse(null);

        assertThat(foundActivity).isNotNull();
        assertThat(foundActivity.getId()).isEqualTo(trainingActivity.getId());
        assertThat(foundActivity.getDistancia()).isEqualTo(trainingActivity.getDistancia());
    }

    @Test
    void testDeleteByFechaComienzo() {

        assertThat(trainingActivityRepository.findByFechaComienzo(startTime)).isPresent();

        trainingActivityRepository.deleteByFechaComienzo(startTime);

        assertThat(trainingActivityRepository.findByFechaComienzo(startTime)).isNotPresent();
    }

}
