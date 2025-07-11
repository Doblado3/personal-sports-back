package com.pablodoblado.personal_sports_back.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.pablodoblado.personal_sports_back.backend.entity.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import com.pablodoblado.personal_sports_back.backend.repository.MetricaSaludRepository;
import com.pablodoblado.personal_sports_back.backend.repository.UsuarioRepository;

@DataJpaTest
@Import(MetricaSaludService.class) //Just because MetricaSaludService its not an interface
public class MetricaSaludServiceTest {
	
	@Autowired
	private MetricaSaludRepository metricaSaludRepository;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private TestEntityManager entityManager;
	
	//DataJpaTest doesn't tipically load services beans
	@Autowired
	private MetricaSaludService metricaSaludService;
	
	private UUID testUserId;
	private UUID anotherUserId;
	
	@BeforeEach
	void setUp() {
		
		metricaSaludRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Limpieza mas robusta
        entityManager.flush();
        entityManager.clear();
        
        Usuario testUser = new Usuario();
        testUser.setNombre("testuser");
        testUser.setEmail("emailprimerus@gmail.com");
        testUser.setFechaNacimiento(LocalDateTime.of(LocalDate.of(2002, 2, 2), LocalTime.MIDNIGHT));
        testUser.setPassword("password");
        
        usuarioRepository.save(testUser);
        this.testUserId = testUser.getId();
        
        
        Usuario anotherUser = new Usuario();
        anotherUser.setNombre("anotheruser");
        anotherUser.setEmail("emailsegundous@gmail.com");
        anotherUser.setFechaNacimiento(LocalDateTime.of(LocalDate.of(2004, 2, 2), LocalTime.MIDNIGHT));
        anotherUser.setPassword("anotherPassword");

        
        usuarioRepository.save(anotherUser);
        this.anotherUserId = anotherUser.getId();
        
        saveMetricaSalud(testUser, LocalDate.of(2023, 1, 10), 70.5, "Buena", "Sin incidencias importantes");
        saveMetricaSalud(testUser, LocalDate.of(2023, 1, 15), 71.0, "Regular", "Dolor de cabeza");
        saveMetricaSalud(testUser, LocalDate.of(2023, 1, 20), 70.0, "Mala", "Insomnio");
        saveMetricaSalud(testUser, LocalDate.of(2023, 2, 5), 72.1, "Buena", "Todo bien");
        saveMetricaSalud(testUser, LocalDate.of(2023, 2, 10), 71.8, "Regular", "Mala");

	}
	
	private void saveMetricaSalud(Usuario user, LocalDate fecha, double peso, String calidadSueno, String incidencias) {
        MetricaSalud metrica = new MetricaSalud();
        metrica.setUsuario(user);
        metrica.setFechaRegistro(fecha);
        metrica.setPeso(peso);
        metrica.setCalidadSueno(calidadSueno);
        metrica.setIncidencias(incidencias);
        metricaSaludRepository.save(metrica);
    }
	
	// Clases de Test:
	@Test
	void testPaginatedRegistrosForUsuario_NoFilter() {
		
		PageRequest pageable = PageRequest.of(0, 3, Sort.by("fechaRegistro").descending());
		String filter = "";
		
		Page<MetricaSalud> resultPage = metricaSaludService.getPaginatedRegistrosForUsuario(testUserId, pageable, filter);
		
		assertThat(resultPage).isNotNull();
		assertThat(resultPage.getTotalElements()).isEqualTo(5); 
        assertThat(resultPage.getContent()).hasSize(3); 
        assertThat(resultPage.getContent().get(0).getFechaRegistro()).isEqualTo(LocalDate.of(2023, 2, 10)); 
        assertThat(resultPage.getContent().get(1).getFechaRegistro()).isEqualTo(LocalDate.of(2023, 2, 5));
		
		
	}
	
	@Test
	void testPaginatedRegistrosForUsuario_WithFilterMatchingMultipleFields() {
		
		PageRequest pageable = PageRequest.of(0, 3, Sort.by("fechaRegistro").descending());
		String filter = "Mala";
		
		Page<MetricaSalud> resultPage = metricaSaludService.getPaginatedRegistrosForUsuario(testUserId, pageable, filter);
		
		assertThat(resultPage).isNotNull();
		//Ojo que el filtro no compara con incidencias, debe devolver 1
		assertThat(resultPage.getTotalElements()).isEqualTo(1);
		assertThat(resultPage.getContent()).hasSize(1);
		assertThat(resultPage.getContent().get(0).getCalidadSueno()).isEqualTo("Mala");

		
	}

}
