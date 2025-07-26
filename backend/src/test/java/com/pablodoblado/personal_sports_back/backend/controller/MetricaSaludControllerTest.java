package com.pablodoblado.personal_sports_back.backend.controller;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.hasSize;


import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.pablodoblado.personal_sports_back.backend.dto.MetricaSaludResponseDTO;
import com.pablodoblado.personal_sports_back.backend.entity.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.service.MetricaSaludService;
import org.modelmapper.ModelMapper;

@WebMvcTest(MetricaSaludController.class)
public class MetricaSaludControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricaSaludService metricaSaludService;

    @MockitoBean
    private ModelMapper modelMapper;


    private List<MetricaSaludResponseDTO> metricasDTO;
    private List<MetricaSalud> metricasEntity;

    @BeforeEach
    void setUp() {
        metricasDTO = new ArrayList<>();
        metricasDTO.add(new MetricaSaludResponseDTO());
        metricasDTO.add(new MetricaSaludResponseDTO());

        metricasEntity = new ArrayList<>();
        metricasEntity.add(new MetricaSalud());
        metricasEntity.add(new MetricaSalud());
    }

    @Test
    void testGetMetricas() throws Exception {
        Page<MetricaSalud> pageEntity = new PageImpl<>(metricasEntity, PageRequest.of(0, 2), metricasEntity.size());

        when(metricaSaludService.getPaginatedRegistrosForUsuario(any(), any(PageRequest.class), any())).thenReturn(pageEntity);
        when(modelMapper.map(any(MetricaSalud.class), eq(MetricaSaludResponseDTO.class))).thenReturn(new MetricaSaludResponseDTO());

        mockMvc.perform(get("/api/registroDiario/findAll/paginated/{idUsuario}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .param("filter", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void testGetMetricas_withFilter() throws Exception {
        Page<MetricaSalud> pageEntity = new PageImpl<>(metricasEntity.subList(0, 1), PageRequest.of(0, 1), 1);

        when(metricaSaludService.getPaginatedRegistrosForUsuario(any(), any(PageRequest.class), eq("someFilter"))).thenReturn(pageEntity);
        when(modelMapper.map(any(MetricaSalud.class), eq(MetricaSaludResponseDTO.class))).thenReturn(new MetricaSaludResponseDTO());

        mockMvc.perform(get("/api/registroDiario/findAll/paginated/{idUsuario}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .param("filter", "someFilter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void testGetMetricas_serviceThrowsIllegalArgumentException() throws Exception {
        when(metricaSaludService.getPaginatedRegistrosForUsuario(any(), any(PageRequest.class), any())).thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/api/registroDiario/findAll/paginated/{idUsuario}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .param("filter", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetMetricas_serviceThrowsGenericException() throws Exception {
        when(metricaSaludService.getPaginatedRegistrosForUsuario(any(), any(PageRequest.class), any())).thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(get("/api/registroDiario/findAll/paginated/{idUsuario}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .param("filter", ""))
                .andExpect(status().isInternalServerError());
    }
}