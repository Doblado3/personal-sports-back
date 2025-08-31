package com.pablodoblado.personal_sports_back.backend.controller;

import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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
	
	private UUID testId;
	private Long mockId1;
	private Long mockId2;
	private LocalDateTime fecha;
	
	private TrainingActivityRequestDTO request;
	private TrainingActivityResponseDTO response;
	private TrainingActivityResponseDTO response2;
	private List<TrainingActivityResponseDTO> list;
	
	@BeforeEach
	void setUp() {
		
		testId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
		mockId1 = 1L;
		mockId2 = 2L;
		fecha = LocalDateTime.now();
		
		request = TrainingActivityRequestDTO.builder()
				.tipo(TipoActividad.MOVILIDAD)
				.usuarioId(testId)
				.nombre("usuario test")
				.pulsoMedio(123.2)
				.fechaComienzo(OffsetDateTime.of(fecha.toLocalDate(), LocalTime.MIN, ZoneOffset.UTC))
				.build();
		
		response = TrainingActivityResponseDTO.builder()
				.tipo(TipoActividad.MOVILIDAD)
				.id(mockId1)
				.usuarioId(testId)
				.nombre("usuario test")
				.pulsoMedio(123.2)
				.fechaComienzo(OffsetDateTime.of(fecha.toLocalDate(), LocalTime.MIN, ZoneOffset.UTC))
				.build();
		
		response2 = TrainingActivityResponseDTO.builder()
                .fechaComienzo(OffsetDateTime.of(fecha.toLocalDate(), LocalTime.MIN, ZoneOffset.UTC))
                .tipo(TipoActividad.MOVILIDAD)
                .id(mockId2)
                .nombre("usuario test 2")
                .pulsoMedio(145.0)
                .build();
		
		list = List.of(response, response2);
	}
	
	@Test
	void testGetActivitiesForUsuarioAndFecha() throws Exception {
		
		given(trainingActivityService.findActivitiesByUsuarioAndDate(testId, fecha)).willReturn(Optional.of(list));
		
		mockMvc.perform(get(TrainingActivityController.TRAINING_USER_ID_PATH, testId)
				.queryParam("fecha", fecha.toString())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(list.size())));
		
	}
	
	@Test
	void testGetActivitiesForUsuarioAndFechaNotFound() throws Exception {
		
		LocalDateTime random = LocalDateTime.now().plusDays(5);
		
		given(trainingActivityService.findActivitiesByUsuarioAndDate(testId, random)).willReturn(Optional.empty());
		
		mockMvc.perform(get(TrainingActivityController.TRAINING_USER_ID_PATH, testId)
				.queryParam("fecha", random.toString())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}
	
	@Test
	void testGetActivitiesForUsuarioAndFechaBadRequest() throws Exception {
		
		given(trainingActivityService.findActivitiesByUsuarioAndDate(testId, fecha)).willReturn(Optional.of(list));
				
				mockMvc.perform(get(TrainingActivityController.TRAINING_USER_ID_PATH, testId)
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isBadRequest());
					
	}
	
	@Test
	void testUpdateTrainingActivity() throws Exception {
		
		
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
		
		
		given(trainingActivityService.findActivityById(response.getId())).willReturn(Optional.of(response));
		
		mockMvc.perform(get(TrainingActivityController.TRAINING_ID_PATH, response.getId())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(Math.toIntExact(response.getId()))));
	}
	
	@Test
	void testGetActivityByIdNotFound() throws Exception {
		
		
		given(trainingActivityService.findActivityById(response.getId())).willReturn(Optional.empty());
		
		mockMvc.perform(get(TrainingActivityController.TRAINING_ID_PATH, response.getId())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}
	
	@Test
	void testDeleteActivityById() throws Exception {
		
		given(trainingActivityService.deleteActivityById(response.getId())).willReturn(true);
		
		mockMvc.perform(delete(TrainingActivityController.TRAINING_DELETE_PATH, response.getId())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
		
	}
	
	@Test 
	void testDeleteActivityByIdNotFound() throws Exception {
		
		given(trainingActivityService.deleteActivityById(response.getId())).willReturn(false);
		
		mockMvc.perform(delete(TrainingActivityController.TRAINING_DELETE_PATH, response.getId())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}
	
	@Test
	void testListActivitiesByParams() throws Exception {
		

		
		given(trainingActivityService.listActivities(response.getFechaComienzo().toLocalDateTime(), response.getTipo(), 120.0, 150.0)).willReturn(Optional.of(list));
		
		mockMvc.perform(get(TrainingActivityController.TRAINING_PATH)
				.queryParam("dia", response.getFechaComienzo().toLocalDateTime().toString())
				.queryParam("tipo", response.getTipo().toString())
				.queryParam("rangoZonaMin", "120.0")
				.queryParam("rangoZonaMax", "150.0"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.size()", is(2)));

		
	}
	
	@Test
	void testListActivitiesByParamsNotFound() throws Exception {
		

		
		given(trainingActivityService.listActivities(response.getFechaComienzo().toLocalDateTime(), TipoActividad.RIDE, 120.0, 150.0)).willReturn(Optional.empty());
		
		mockMvc.perform(get(TrainingActivityController.TRAINING_PATH)
				.queryParam("dia", response.getFechaComienzo().toLocalDateTime().toString())
				.queryParam("tipo", TipoActividad.RIDE.toString())
				.queryParam("rangoZonaMin", "120.0")
				.queryParam("rangoZonaMax", "150.0"))
	        .andExpect(status().isNotFound());

		
	}

}
