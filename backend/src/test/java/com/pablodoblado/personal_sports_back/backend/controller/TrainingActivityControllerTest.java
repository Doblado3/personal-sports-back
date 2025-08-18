package com.pablodoblado.personal_sports_back.backend.controller;

import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.given;


import com.pablodoblado.personal_sports_back.backend.controllers.TrainingActivityController;
import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;
import com.pablodoblado.personal_sports_back.backend.mappers.TrainingActivityMapper;
import com.pablodoblado.personal_sports_back.backend.models.TrainingActivityRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.TrainingActivityResponseDTO;
import com.pablodoblado.personal_sports_back.backend.services.TrainingActivityService;

import java.util.List;
import java.util.Optional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.hamcrest.core.Is.is;



@WebMvcTest(TrainingActivityController.class)
public class TrainingActivityControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private TrainingActivityService trainingActivityService;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@MockitoBean(name = "trainingActivityMapperImpl")
	private TrainingActivityMapper trainingActivityMapper;
	
	public static final UUID testId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	
	public static final Long mockId1 = 1L;
	public static final Long mockId2 = 2L;
	
	@Test
	void testUpdateTrainingActivity() throws Exception {
		
		TrainingActivityRequestDTO request = TrainingActivityRequestDTO.builder()
				.tipo(TipoActividad.MOVILIDAD)
				.usuarioId(testId)
				.nombre("usuario test")
				.fechaComienzo(OffsetDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneOffset.UTC))
				.build();
		
		TrainingActivityResponseDTO response = TrainingActivityResponseDTO.builder()
				.tipo(TipoActividad.MOVILIDAD)
				.id(mockId1)
				.usuarioId(testId)
				.nombre("usuario test")
				.fechaComienzo(OffsetDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneOffset.UTC))
				.build();
		
		given(trainingActivityMapper.mapActivityRequestToEntity(any(TrainingActivityRequestDTO.class))).willReturn(new TrainingActivity());
		given(trainingActivityService.updateActivityById(any(Long.class), any(TrainingActivity.class))).willReturn(Optional.of(response));
		
		mockMvc.perform(put(TrainingActivityController.TRAINING_UPDATE_PATH, mockId1)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNoContent());
		
		verify(trainingActivityService).updateActivityById(any(Long.class), any(TrainingActivity.class));
		
	}
	
	@Test
	void testUpdateTrainingActivityNotFound() throws Exception {
		
		TrainingActivityRequestDTO request = TrainingActivityRequestDTO.builder()
				.tipo(TipoActividad.MOVILIDAD)
				.usuarioId(testId)
				.nombre("usuario test")
				.fechaComienzo(OffsetDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneOffset.UTC))
				.build();
		
		given(trainingActivityMapper.mapActivityRequestToEntity(any(TrainingActivityRequestDTO.class))).willReturn(new TrainingActivity());
		given(trainingActivityService.updateActivityById(any(Long.class), any(TrainingActivity.class))).willReturn(Optional.empty());
		
		mockMvc.perform(put(TrainingActivityController.TRAINING_UPDATE_PATH, mockId1)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());
	}
	
	@Test
	void testGetActivityById() throws Exception {
		
		
		TrainingActivityResponseDTO response = TrainingActivityResponseDTO.builder()
				.tipo(TipoActividad.MOVILIDAD)
				.id(mockId1)
				.usuarioId(testId)
				.nombre("usuario test")
				.fechaComienzo(OffsetDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneOffset.UTC))
				.build();
		
		given(trainingActivityService.findActivityById(response.getId())).willReturn(Optional.of(response));
		
		mockMvc.perform(get(TrainingActivityController.TRAINING_ID_PATH, response.getId())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(Math.toIntExact(response.getId()))));
	}
	
	@Test
	void testGetActivityByIdNotFound() throws Exception {
		
		
		TrainingActivityResponseDTO response = TrainingActivityResponseDTO.builder()
				.tipo(TipoActividad.MOVILIDAD)
				.id(mockId1)
				.usuarioId(testId)
				.nombre("usuario test")
				.fechaComienzo(OffsetDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneOffset.UTC))
				.build();
		
		given(trainingActivityService.findActivityById(response.getId())).willReturn(Optional.empty());
		
		mockMvc.perform(get(TrainingActivityController.TRAINING_ID_PATH, response.getId())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	void testDeleteActivityById() throws Exception {
		
		TrainingActivityResponseDTO response = TrainingActivityResponseDTO.builder()
				.tipo(TipoActividad.MOVILIDAD)
				.id(mockId1)
				.usuarioId(testId)
				.nombre("usuario test")
				.fechaComienzo(OffsetDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneOffset.UTC))
				.build();
		
		given(trainingActivityService.deleteActivityById(response.getId())).willReturn(true);
		
		mockMvc.perform(delete(TrainingActivityController.TRAINING_DELETE_PATH, response.getId())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
		
	}
	
	@Test 
	void testDeleteActivityByIdNotFound() throws Exception {
		
		TrainingActivityResponseDTO response = TrainingActivityResponseDTO.builder()
				.tipo(TipoActividad.MOVILIDAD)
				.id(mockId1)
				.usuarioId(testId)
				.nombre("usuario test")
				.fechaComienzo(OffsetDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneOffset.UTC))
				.build();
		
		given(trainingActivityService.deleteActivityById(response.getId())).willReturn(false);
		
		mockMvc.perform(delete(TrainingActivityController.TRAINING_DELETE_PATH, response.getId())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}
	
	@Test
	void testListActivitiesByParams() throws Exception {
		
		TrainingActivityResponseDTO response1 = TrainingActivityResponseDTO.builder()
				.tipo(TipoActividad.MOVILIDAD)
				.id(mockId1)
				.usuarioId(testId)
				.nombre("usuario test")
				.pulsoMedio(123.2)
				.fechaComienzo(OffsetDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneOffset.UTC))
				.build();
		
		TrainingActivityResponseDTO response2 = TrainingActivityResponseDTO.builder()
                .fechaComienzo(OffsetDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneOffset.UTC))
                .tipo(TipoActividad.MOVILIDAD)
                .id(mockId2)
                .nombre("usuario test 2")
                .pulsoMedio(145.0)
                .build();
		
		List<TrainingActivityResponseDTO> list = List.of(response1,response2);

		
		given(trainingActivityService.listActivities(response1.getFechaComienzo().toLocalDateTime(), response1.getTipo(), 120.0, 150.0)).willReturn(Optional.of(list));
		
		mockMvc.perform(get(TrainingActivityController.TRAINING_PATH)
				.queryParam("dia", response1.getFechaComienzo().toLocalDateTime().toString())
				.queryParam("tipo", response1.getTipo().toString())
				.queryParam("rangoZonaMin", "120.0")
				.queryParam("rangoZonaMax", "150.0"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.size()", is(2)));

		
	}
	
	@Test
	void testListActivitiesByParamsNotFound() throws Exception {
		
		TrainingActivityResponseDTO response1 = TrainingActivityResponseDTO.builder()
				.tipo(TipoActividad.SENDERISMO)
				.id(mockId1)
				.usuarioId(testId)
				.nombre("usuario test")
				.pulsoMedio(123.2)
				.fechaComienzo(OffsetDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneOffset.UTC))
				.build();
		
		TrainingActivityResponseDTO response2 = TrainingActivityResponseDTO.builder()
                .fechaComienzo(OffsetDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneOffset.UTC))
                .tipo(TipoActividad.FUERZA)
                .id(mockId2)
                .nombre("usuario test 2")
                .pulsoMedio(145.0)
                .build();
		
		List<TrainingActivityResponseDTO> list = List.of(response1,response2);

		
		given(trainingActivityService.listActivities(response1.getFechaComienzo().toLocalDateTime(), TipoActividad.RIDE, 120.0, 150.0)).willReturn(Optional.empty());
		
		mockMvc.perform(get(TrainingActivityController.TRAINING_PATH)
				.queryParam("dia", response1.getFechaComienzo().toLocalDateTime().toString())
				.queryParam("tipo", TipoActividad.RIDE.toString())
				.queryParam("rangoZonaMin", "120.0")
				.queryParam("rangoZonaMax", "150.0"))
	        .andExpect(status().isNotFound());

		
	}

}
