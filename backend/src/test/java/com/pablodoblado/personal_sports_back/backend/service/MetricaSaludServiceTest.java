package com.pablodoblado.personal_sports_back.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.pablodoblado.personal_sports_back.backend.entities.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.repositories.MetricaSaludRepository;
import com.pablodoblado.personal_sports_back.backend.services.impls.MetricaSaludServiceImpl;

@ExtendWith(MockitoExtension.class)
public class MetricaSaludServiceTest {

    @Mock
    private MetricaSaludRepository metricaSaludRepository;

    @InjectMocks
    private MetricaSaludServiceImpl metricaSaludService;

    private UUID testUserId;
    private List<MetricaSalud> metricas;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        Usuario testUser = new Usuario();
        testUser.setId(testUserId);

        metricas = new ArrayList<>();
        metricas.add(createMetrica(testUser, LocalDate.of(2023, 2, 10), 71.8, "Regular", "Mala"));
        metricas.add(createMetrica(testUser, LocalDate.of(2023, 2, 5), 72.1, "Buena", "Todo bien"));
        metricas.add(createMetrica(testUser, LocalDate.of(2023, 1, 20), 70.0, "Mala", "Insomnio"));
        metricas.add(createMetrica(testUser, LocalDate.of(2023, 1, 15), 71.0, "Regular", "Dolor de cabeza"));
        metricas.add(createMetrica(testUser, LocalDate.of(2023, 1, 10), 70.5, "Buena", "Sin incidencias importantes"));
    }

    private MetricaSalud createMetrica(Usuario user, LocalDate fecha, double peso, String calidadSueno, String incidencias) {
        MetricaSalud metrica = new MetricaSalud();
        metrica.setUsuario(user);
        metrica.setFechaRegistro(fecha);
        metrica.setPeso(peso);
        metrica.setCalidadSueno(calidadSueno);
        metrica.setIncidencias(incidencias);
        return metrica;
    }

    @Test
    void testPaginatedRegistrosForUsuario_NoFilter() {
        PageRequest pageable = PageRequest.of(0, 3, Sort.by("fechaRegistro").descending());
        Page<MetricaSalud> page = new PageImpl<>(metricas.subList(0, 3), pageable, metricas.size());

        when(metricaSaludRepository.findByUsuarioId(eq(testUserId), any(PageRequest.class))).thenReturn(page);

        Page<MetricaSalud> resultPage = metricaSaludService.getPaginatedRegistrosForUsuario(testUserId, pageable, "");

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(5);
        assertThat(resultPage.getContent()).hasSize(3);
        assertThat(resultPage.getContent().get(0).getFechaRegistro()).isEqualTo(LocalDate.of(2023, 2, 10));
        assertThat(resultPage.getContent().get(1).getFechaRegistro()).isEqualTo(LocalDate.of(2023, 2, 5));
    }

    @Test
    void testPaginatedRegistrosForUsuario_WithFilter() {
        PageRequest pageable = PageRequest.of(0, 3, Sort.by("fechaRegistro").descending());
        List<MetricaSalud> filteredMetricas = List.of(metricas.get(2));
        Page<MetricaSalud> page = new PageImpl<>(filteredMetricas, pageable, filteredMetricas.size());

        when(metricaSaludRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<MetricaSalud> resultPage = metricaSaludService.getPaginatedRegistrosForUsuario(testUserId, pageable, "Mala");

        assertThat(resultPage).isNotNull();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getContent().get(0).getCalidadSueno()).isEqualTo("Mala");
    }
}
