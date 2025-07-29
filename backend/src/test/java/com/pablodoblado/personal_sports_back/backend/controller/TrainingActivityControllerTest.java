
package com.pablodoblado.personal_sports_back.backend.controller;


import com.pablodoblado.personal_sports_back.backend.dto.TrainingActivity.TrainingActivityResponseDTO;
import com.pablodoblado.personal_sports_back.backend.service.TrainingActivityService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(TrainingActivityController.class)
public class TrainingActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private TrainingActivityService trainingActivityService;
    
    private List<TrainingActivityResponseDTO> trainingActivities;
        
    @BeforeEach
    void setUp() {
    	
    	trainingActivities = new ArrayList<>();
    	TrainingActivityResponseDTO activity = new TrainingActivityResponseDTO();
    	activity.setId(10L);
    	activity.setNombre("Morning Run");
    	trainingActivities.add(activity);

    	
    }


    @Test
    public void shouldFetchAndSaveStravaActivities() throws Exception {
        
    	
    	when(trainingActivityService.fetchAndSaveStravaActivities(any(), any(), any(), any(), any())).thenReturn(trainingActivities);
    	

        mockMvc.perform(post("/api/trainingActivities/fetchStravaActivities/{usuarioId}", UUID.randomUUID())
        		.contentType(MediaType.APPLICATION_JSON)
                        .param("before", "1704067200")
                        .param("after", "1672531200")
                        .param("page", "1")
                        .param("perPageResults", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].nombre").value("Morning Run"));
    }
    
    @Test
    public void shouldThrowErrorFromService() throws Exception {
    	
    	when(trainingActivityService.fetchAndSaveStravaActivities(any(), any(), any(), any(), any())).thenThrow(new RuntimeException("Ha ocurrido un error inesperado"));
    	
    	mockMvc.perform(post("/api/trainingActivities/fetchStravaActivities/{usuarioId}", UUID.randomUUID())
        		.contentType(MediaType.APPLICATION_JSON)
                        .param("before", "1704067200")
                        .param("after", "1672531200")
                        .param("page", "1")
                        .param("perPageResults", "30"))
                .andExpect(status().isInternalServerError());
    	
    }
}

