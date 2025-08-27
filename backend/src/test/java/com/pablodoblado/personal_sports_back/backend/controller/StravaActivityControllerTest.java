package com.pablodoblado.personal_sports_back.backend.controller;

import com.pablodoblado.personal_sports_back.backend.controllers.StravaActivityController;
import com.pablodoblado.personal_sports_back.backend.services.StravaActivityService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(StravaActivityController.class)
public class StravaActivityControllerTest {
	

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StravaActivityService stravaService;

    @Test
    public void shouldAcknowledgeRequestAndProcessInBackground() throws Exception {
    	
    	UUID usuarioId = UUID.randomUUID();
    	
        when(stravaService.fetchAndSaveStravaActivities(any(UUID.class), any(Long.class), any(Long.class), any(Integer.class), any(Integer.class)))
                .thenReturn(CompletableFuture.completedFuture(1));

        mockMvc.perform(post(StravaActivityController.FETCH_ACTIVITIES_USER, usuarioId)
                        .param("before", "1704067200")
                        .param("after", "1672531200")
                        .param("page", "1")
                        .param("perPageResults", "30"))
        .andExpect(status().isAccepted())
        .andExpect(header().exists("Location"));        
    }
}