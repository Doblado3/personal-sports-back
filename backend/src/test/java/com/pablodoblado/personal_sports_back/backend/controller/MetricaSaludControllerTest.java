package com.pablodoblado.personal_sports_back.backend.controller;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablodoblado.personal_sports_back.backend.controllers.MetricaSaludController;
import com.pablodoblado.personal_sports_back.backend.entities.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.mappers.MetricaSaludMapper;
import com.pablodoblado.personal_sports_back.backend.models.MetricaSaludRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.MetricaSaludResponseDTO;
import com.pablodoblado.personal_sports_back.backend.services.MetricaSaludService;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.core.Is.is;




@WebMvcTest(MetricaSaludController.class)
public class MetricaSaludControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricaSaludService metricaSaludService;

    @Autowired
    ObjectMapper objectMapper;
    
    @MockitoBean
    private MetricaSaludMapper metricaSaludMapper;

    
    @Test
    void testSaveMetricaSalud() throws Exception {
    	
    	MetricaSaludRequestDTO request = MetricaSaludRequestDTO.builder()
    			.peso(61.2)
    			.calidadSueno("Mala")
    			.fechaRegistro(LocalDate.of(2025, 11, 10))
    			.build();
    	
    	MetricaSaludResponseDTO response = MetricaSaludResponseDTO.builder()
    			.usuarioId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    			.fechaRegistro(LocalDate.now())
    			.build();
    	
    	given(metricaSaludMapper.metricaSaludRequestToMetricaSalud(any(MetricaSaludRequestDTO.class))).willReturn(new MetricaSalud());
    	given(metricaSaludService.saveMetricaDiaria(any(UUID.class), any(MetricaSalud.class))).willReturn(response);
    	
    	mockMvc.perform(post(MetricaSaludController.METRICASALUD_SAVE_PATH, response.getUsuarioId())
    			.accept(MediaType.APPLICATION_JSON)
    			.contentType(MediaType.APPLICATION_JSON)
    				.content(objectMapper.writeValueAsString(request)))
    			.andExpect(status().isCreated())
    			.andExpect(header().exists("Location"));
    }
    
    @Test
    void testSaveMetricaSaludNotNullConstraint() throws Exception {
    	
    	MetricaSaludRequestDTO request = MetricaSaludRequestDTO.builder()
    			.peso(61.2)
    			.fechaRegistro(LocalDate.of(2025, 11, 10))
    			.build();
    	
    	MetricaSaludResponseDTO response = MetricaSaludResponseDTO.builder()
    			.usuarioId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    			.fechaRegistro(LocalDate.now())
    			.build();
    	
    	given(metricaSaludMapper.metricaSaludRequestToMetricaSalud(any(MetricaSaludRequestDTO.class))).willReturn(new MetricaSalud());
    	given(metricaSaludService.saveMetricaDiaria(any(UUID.class), any(MetricaSalud.class))).willReturn(response);
    	
    	MvcResult result = mockMvc.perform(post(MetricaSaludController.METRICASALUD_SAVE_PATH, response.getUsuarioId())
    			.accept(MediaType.APPLICATION_JSON)
    			.contentType(MediaType.APPLICATION_JSON)
    				.content(objectMapper.writeValueAsString(request)))
    			.andExpect(status().isBadRequest())
    			.andReturn();
    	
    	System.out.println(result.getResponse().getContentAsString());
    	
    }
    
    @Test
    void testSaveMetricaSaludNotBlankConstraint() throws Exception {
    	
    	MetricaSaludRequestDTO request = MetricaSaludRequestDTO.builder()
    			.peso(61.2)
    			.calidadSueno("")
    			.fechaRegistro(LocalDate.of(2025, 11, 10))
    			.build();
    	
    	MetricaSaludResponseDTO response = MetricaSaludResponseDTO.builder()
    			.usuarioId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    			.fechaRegistro(LocalDate.now())
    			.build();
    	
    	given(metricaSaludMapper.metricaSaludRequestToMetricaSalud(any(MetricaSaludRequestDTO.class))).willReturn(new MetricaSalud());
    	given(metricaSaludService.saveMetricaDiaria(any(UUID.class), any(MetricaSalud.class))).willReturn(response);
    	
    	MvcResult result = mockMvc.perform(post(MetricaSaludController.METRICASALUD_SAVE_PATH, response.getUsuarioId())
    			.accept(MediaType.APPLICATION_JSON)
    			.contentType(MediaType.APPLICATION_JSON)
    				.content(objectMapper.writeValueAsString(request)))
    			.andExpect(status().isBadRequest())
    			.andReturn();
    	
    	System.out.println(result.getResponse().getContentAsString());
    	
    }
    
    @Test
    void testUpdateMetricaSalud() throws Exception {
    	
    	MetricaSaludRequestDTO request = MetricaSaludRequestDTO.builder()
    			.peso(61.2)
    			.calidadSueno("Mala")
    			.fechaRegistro(LocalDate.of(2025, 11, 10))
    			.build();
    	
    	MetricaSaludResponseDTO response = MetricaSaludResponseDTO.builder()
    			.usuarioId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    			.fechaRegistro(LocalDate.now())
    			.build();
    	
    	given(metricaSaludMapper.metricaSaludRequestToMetricaSalud(any(MetricaSaludRequestDTO.class))).willReturn(new MetricaSalud());
    	given(metricaSaludService.updateMetricaSalud(any(UUID.class), any(MetricaSalud.class))).willReturn(Optional.of(response));
    	
    	mockMvc.perform(put(MetricaSaludController.METRICASALUD_UPDATE_PATH, response.getUsuarioId())
    			.accept(MediaType.APPLICATION_JSON)
    			.contentType(MediaType.APPLICATION_JSON)
    			.content(objectMapper.writeValueAsString(request)))
    			.andExpect(status().isNoContent());
    	
    	verify(metricaSaludService).updateMetricaSalud(any(UUID.class), any(MetricaSalud.class));
    }
    
    @Test
    void testFindAllRegistrosByUser() throws Exception {
    	
    	
    	UUID testUserId = UUID.randomUUID();
    	MetricaSalud mockEntity = MetricaSalud.builder()
    			.id(UUID.randomUUID())
    		    .fechaRegistro(LocalDate.now())
    		    .calidadSueno("Mala")
    		    .build();

    	MetricaSaludResponseDTO mockDto = MetricaSaludResponseDTO.builder()
    			.usuarioId(UUID.randomUUID())
    		    .fechaRegistro(mockEntity.getFechaRegistro())
    		    .build();

    	List<MetricaSalud> entityList = List.of(mockEntity);
    	List<MetricaSaludResponseDTO> dtoList = List.of(mockDto);
    	
    	given(metricaSaludService.getAllRegistrosForUsuario(testUserId)).willReturn(Optional.of(entityList));
    	
    	mockMvc.perform(get(MetricaSaludController.METRICASALUD_ALL_PATH, testUserId)
    	        .accept(MediaType.APPLICATION_JSON))
    	        .andExpect(status().isOk())
    	        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    	        .andExpect(jsonPath("$.length()").value(dtoList.size()));
    	
    }
    
    @Test
    void testFindAllRegistrosByUserNotFound() throws Exception {
    	
    	UUID testUserId = UUID.randomUUID();
    	
    	given(metricaSaludService.getAllRegistrosForUsuario(testUserId)).willReturn(Optional.empty());
    	
    	mockMvc.perform(get(MetricaSaludController.METRICASALUD_ALL_PATH, testUserId)
    	        .accept(MediaType.APPLICATION_JSON))
    	        .andExpect(status().isNotFound());
    }
    
    @Test
    void testFindByUsuarioAndFechaRegistro() throws Exception {
    	
    	UUID usuarioId =UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    	LocalDate fechaRegistro = LocalDate.now();
    	
    	MetricaSalud mockEntity = MetricaSalud.builder()
    			.id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    		    .fechaRegistro(fechaRegistro)
    		    .calidadSueno("Mala")
    		    .build();
    	
    	MetricaSaludResponseDTO mockDto = MetricaSaludResponseDTO.builder()
    			.usuarioId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    		    .fechaRegistro(mockEntity.getFechaRegistro())
    		    .calidadSueno(mockEntity.getCalidadSueno())
    		    .build();
    	
    	given(metricaSaludService.getRegistroByUsuarioAndDate(usuarioId, fechaRegistro)).willReturn(Optional.of(mockEntity));
        given(metricaSaludMapper.metricaSaludToMetricaSaludResponseDTO(mockEntity)).willReturn(mockDto);

    	
    	mockMvc.perform(get(MetricaSaludController.METRICASALUD_USUARIO_FECHA_PATH, usuarioId, fechaRegistro)
    		    .accept(MediaType.APPLICATION_JSON))
    		    .andExpect(status().isOk())
    		    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    		    .andExpect(jsonPath("$.calidadSueno", is(mockDto.getCalidadSueno())));
    	
    }
    
    @Test
    void testFindByUsuarioAndFechaRegistroNotFound() throws Exception {
    	
    	UUID usuarioId =UUID.randomUUID();
    	LocalDate fechaRegistro = LocalDate.now();
    	
    	given(metricaSaludService.getRegistroByUsuarioAndDate(usuarioId, fechaRegistro)).willReturn(Optional.empty());
    	
    	mockMvc.perform(get(MetricaSaludController.METRICASALUD_USUARIO_FECHA_PATH, usuarioId, fechaRegistro)
    			.accept(MediaType.APPLICATION_JSON))
    			.andExpect(status().isNotFound());
    	
    }
    
    @Test
    void testFindAllRegistrosByDateRange() throws Exception {
    	
    	LocalDate fechaInicio = LocalDate.now();

    	
    	MetricaSalud mockEntity = MetricaSalud.builder()
    			.id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    		    .fechaRegistro(LocalDate.now())
    		    .calidadSueno("Mala")
    		    .build();
    	
    	MetricaSalud mockEntity2 = MetricaSalud.builder()
    			.id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    		    .fechaRegistro(LocalDate.now())
    		    .calidadSueno("Mala")
    		    .build();

    	MetricaSaludResponseDTO mockDto = MetricaSaludResponseDTO.builder()
    			.usuarioId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    		    .fechaRegistro(mockEntity.getFechaRegistro())
    		    .build();
    	
    	MetricaSaludResponseDTO mockDto2 = MetricaSaludResponseDTO.builder()
    			.usuarioId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    		    .fechaRegistro(mockEntity.getFechaRegistro())
    		    .build();

    	List<MetricaSalud> entityList = List.of(mockEntity, mockEntity2);
    	List<MetricaSaludResponseDTO> dtoList = List.of(mockDto, mockDto2);
    	
    	UUID usuarioId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    	LocalDate fechaFin = LocalDate.now().plusDays(1L);
    	
    	given(metricaSaludService.getRegistrosDiariosByUserInRange(usuarioId, fechaInicio, fechaFin)).willReturn(Optional.of(entityList));
    	
    	mockMvc.perform(get(MetricaSaludController.METRICASALUD_ALL_FECHA_RANGE_PATH, usuarioId, fechaInicio, fechaFin)
    			.accept(MediaType.APPLICATION_JSON))
		        .andExpect(status().isOk())
		        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
		        .andExpect(jsonPath("$.length()").value(dtoList.size()));
    	
    }
    
    @Test
    void testFindAllRegistrosByDateRangeNotFound() throws Exception {
    	
    	UUID usuarioId = UUID.randomUUID();
    	LocalDate fechaInicio = LocalDate.now();
    	LocalDate fechaFin = LocalDate.now();
    	
    	given(metricaSaludService.getRegistrosDiariosByUserInRange(usuarioId, fechaInicio, fechaFin)).willReturn(Optional.empty());
    	
    	mockMvc.perform(get(MetricaSaludController.METRICASALUD_ALL_FECHA_RANGE_PATH, usuarioId, fechaInicio, fechaFin)
    			.accept(MediaType.APPLICATION_JSON))
		        .andExpect(status().isNotFound());
    }
    
    @Test
    void testDeleteMetricaSalud() throws Exception {
    	
    	UUID userId = UUID.randomUUID();
    	LocalDate fechaRegistro = LocalDate.now();
    	
    	given(metricaSaludService.deleteRegistroMetrica(userId, fechaRegistro)).willReturn(true);
    	
    	mockMvc.perform(delete(MetricaSaludController.METRICASALUD_DELETE_PATH, userId, fechaRegistro)
    			.accept(MediaType.APPLICATION_JSON))
    			.andExpect(status().isNoContent());
    	
    	verify(metricaSaludService).deleteRegistroMetrica(userId, fechaRegistro);
    	
    	
    }
    
    @Test
    void testDeleteMetricaSaludNotFound() throws Exception {
    	
    	UUID userId = UUID.randomUUID();
    	LocalDate fechaRegistro = LocalDate.now();
    	
    	given(metricaSaludService.deleteRegistroMetrica(userId, fechaRegistro)).willReturn(false);
    	
    	mockMvc.perform(delete(MetricaSaludController.METRICASALUD_DELETE_PATH, userId, fechaRegistro)
    			.accept(MediaType.APPLICATION_JSON))
    			.andExpect(status().isNotFound());
    	
    	
    }
    
    
    @Test
    void testFindAllPaginatedRegistrosByUser() throws Exception {
    	
    	UUID usuarioId = UUID.randomUUID();
        
        MetricaSalud mockEntity1 = MetricaSalud.builder().id(UUID.randomUUID())
        		.calidadSueno("Regular")
        		.build();
        MetricaSalud mockEntity2 = MetricaSalud.builder().id(UUID.randomUUID())
        		.calidadSueno("Regular")
        		.build();
        
        List<MetricaSalud> entityList = List.of(mockEntity1, mockEntity2);
        
        MetricaSaludResponseDTO mockDto1 = MetricaSaludResponseDTO.builder().usuarioId(usuarioId)
        		.calidadSueno(mockEntity1.getCalidadSueno())
        		.build();
        MetricaSaludResponseDTO mockDto2 = MetricaSaludResponseDTO.builder().usuarioId(usuarioId)
        		.calidadSueno(mockEntity2.getCalidadSueno())
        		.build();
        
        Page<MetricaSalud> pageEntity = new PageImpl<>(entityList, PageRequest.of(0, 2), entityList.size());
        Page<MetricaSaludResponseDTO> pageDto = new PageImpl<>(List.of(mockDto1, mockDto2), PageRequest.of(0, 2), 2);

        given(metricaSaludService.getPaginatedRegistrosForUsuario(eq(usuarioId), any(PageRequest.class), any())).willReturn(pageEntity);
        given(metricaSaludMapper.metricaSaludToMetricaSaludResponseDTO(mockEntity1)).willReturn(mockDto1);
        given(metricaSaludMapper.metricaSaludToMetricaSaludResponseDTO(mockEntity2)).willReturn(mockDto2);

        mockMvc.perform(get(MetricaSaludController.METRICASALUD_ALL_PAGINATED_PATH, usuarioId)
                .contentType(MediaType.APPLICATION_JSON)
                .param("filter", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(pageDto.getSize()));
    }

    @Test
    void testFindAllPaginatedRegistrosByUserFilter() throws Exception {
    	
    	UUID usuarioId = UUID.randomUUID();
        
        MetricaSalud mockEntity1 = MetricaSalud.builder().id(UUID.randomUUID())
        		.calidadSueno("Regular")
        		.build();
        MetricaSalud mockEntity2 = MetricaSalud.builder().id(UUID.randomUUID())
        		.calidadSueno("Regular")
        		.build();
        
        List<MetricaSalud> entityList = List.of(mockEntity1, mockEntity2);
        
        MetricaSaludResponseDTO mockDto1 = MetricaSaludResponseDTO.builder().usuarioId(usuarioId)
        		.calidadSueno(mockEntity1.getCalidadSueno())
        		.build();
        MetricaSaludResponseDTO mockDto2 = MetricaSaludResponseDTO.builder().usuarioId(usuarioId)
        		.calidadSueno(mockEntity2.getCalidadSueno())
        		.build();
        
        Page<MetricaSalud> pageEntity = new PageImpl<>(entityList, PageRequest.of(0, 2), entityList.size());
        Page<MetricaSaludResponseDTO> pageDto = new PageImpl<>(List.of(mockDto1, mockDto2), PageRequest.of(0, 2), 2);

        given(metricaSaludService.getPaginatedRegistrosForUsuario(eq(usuarioId), any(PageRequest.class), eq("someFilter"))).willReturn(pageEntity);
        given(metricaSaludMapper.metricaSaludToMetricaSaludResponseDTO(mockEntity1)).willReturn(mockDto1);
        given(metricaSaludMapper.metricaSaludToMetricaSaludResponseDTO(mockEntity2)).willReturn(mockDto2);

        mockMvc.perform(get(MetricaSaludController.METRICASALUD_ALL_PAGINATED_PATH, usuarioId)
                .contentType(MediaType.APPLICATION_JSON)
                .param("filter", "someFilter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(pageDto.getSize()));
    }
    
    @Test
    void testFindAllPaginatedRegistrosByUserFilterNotFound() throws Exception {
    	
    	UUID usuarioId = UUID.randomUUID();
        
        MetricaSalud mockEntity1 = MetricaSalud.builder().id(UUID.randomUUID())
        		.calidadSueno("Regular")
        		.build();
        MetricaSalud mockEntity2 = MetricaSalud.builder().id(UUID.randomUUID())
        		.calidadSueno("Regular")
        		.build();
        
        List<MetricaSalud> entityList = List.of(mockEntity1, mockEntity2);
        
        MetricaSaludResponseDTO mockDto1 = MetricaSaludResponseDTO.builder().usuarioId(usuarioId)
        		.calidadSueno(mockEntity1.getCalidadSueno())
        		.build();
        MetricaSaludResponseDTO mockDto2 = MetricaSaludResponseDTO.builder().usuarioId(usuarioId)
        		.calidadSueno(mockEntity2.getCalidadSueno())
        		.build();
        
        Page<MetricaSalud> pageEntity = new PageImpl<>(entityList, PageRequest.of(0, 2), entityList.size());
        Page<MetricaSaludResponseDTO> pageDto = new PageImpl<>(List.of(mockDto1, mockDto2), PageRequest.of(0, 2), 2);

        given(metricaSaludService.getPaginatedRegistrosForUsuario(eq(usuarioId), any(PageRequest.class), eq("someFilter"))).willReturn(pageEntity);
        given(metricaSaludMapper.metricaSaludToMetricaSaludResponseDTO(mockEntity1)).willReturn(mockDto1);
        given(metricaSaludMapper.metricaSaludToMetricaSaludResponseDTO(mockEntity2)).willReturn(mockDto2);

        mockMvc.perform(get(MetricaSaludController.METRICASALUD_ALL_PAGINATED_PATH, usuarioId)
                .contentType(MediaType.APPLICATION_JSON)
                .param("filter", "someFi"))
                .andExpect(status().isNotFound());
    }

    
}