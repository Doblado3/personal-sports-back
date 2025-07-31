package com.pablodoblado.personal_sports_back.backend.repository;

import com.pablodoblado.personal_sports_back.backend.entity.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MetricaSaludRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MetricaSaludRepository metricaSaludRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setNombre("testuser");
        usuario.setEmail("testuser@example.com");
        usuario.setFechaNacimiento(LocalDateTime.now());
        usuario.setPassword("password");
        entityManager.persist(usuario);

        MetricaSalud metrica1 = new MetricaSalud();
        metrica1.setUsuario(usuario);
        metrica1.setFechaRegistro(LocalDate.of(2023, 1, 1));
        metrica1.setCalidadSueno("Buena");
        entityManager.persist(metrica1);

        MetricaSalud metrica2 = new MetricaSalud();
        metrica2.setUsuario(usuario);
        metrica2.setFechaRegistro(LocalDate.of(2023, 1, 2));
        metrica2.setCalidadSueno("Mala");
        entityManager.persist(metrica2);
    }

    @Test
    void testFindByUsuarioId() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("fechaRegistro").descending());
        Page<MetricaSalud> result = metricaSaludRepository.findByUsuarioId(usuario.getId(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getFechaRegistro()).isEqualTo(LocalDate.of(2023, 1, 2));
    }

    @Test
    void testFindByUsuarioAndFechaRegistro() {
        MetricaSalud metrica = metricaSaludRepository.findByUsuarioAndFechaRegistro(usuario, LocalDate.of(2023, 1, 1)).orElse(null);
        assertThat(metrica).isNotNull();
        assertThat(metrica.getCalidadSueno()).isEqualTo("Buena");
    }

    @Test
    void testFindByUsuarioOrderByFechaRegistro() {
        List<MetricaSalud> metricas = metricaSaludRepository.findByUsuarioOrderByFechaRegistro(usuario);
        assertThat(metricas).hasSize(2);
        assertThat(metricas.get(0).getFechaRegistro()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(metricas.get(1).getFechaRegistro()).isEqualTo(LocalDate.of(2023, 1, 2));
    }

    @Test
    void testFindByUsuarioAndFechaRegistroBetween() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 1);
        List<MetricaSalud> metricas = metricaSaludRepository.findByUsuarioAndFechaRegistroBetween(usuario, startDate, endDate);
        assertThat(metricas).hasSize(1);
        assertThat(metricas.get(0).getFechaRegistro()).isEqualTo(LocalDate.of(2023, 1, 1));
    }

    @Test
    void testDeleteByFechaRegistro() {
        metricaSaludRepository.deleteByFechaRegistro(LocalDate.of(2023, 1, 1));
        List<MetricaSalud> metricas = metricaSaludRepository.findAll();
        assertThat(metricas).hasSize(1);
        assertThat(metricas.get(0).getFechaRegistro()).isEqualTo(LocalDate.of(2023, 1, 2));
    }

    @Test
    void testFindAllWithSpecification() {
        Specification<MetricaSalud> spec = (root, query, cb) -> cb.equal(root.get("calidadSueno"), "Buena");
        Pageable pageable = PageRequest.of(0, 10);
        Page<MetricaSalud> result = metricaSaludRepository.findAll(spec, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCalidadSueno()).isEqualTo("Buena");
    }
}