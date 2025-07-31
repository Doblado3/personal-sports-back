package com.pablodoblado.personal_sports_back.backend.controller;

import com.pablodoblado.personal_sports_back.backend.service.TrainingActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(TrainingActivityController.class)
public class TrainingActivityControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TrainingActivityService trainingActivityService;

    @Test
    public void shouldAcknowledgeRequestAndProcessInBackground() {
        when(trainingActivityService.fetchAndSaveStravaActivities(any(UUID.class), any(Long.class), any(Long.class), any(Integer.class), any(Integer.class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/trainingActivities/fetchStravaActivities/{usuarioId}")
                        .queryParam("before", "1704067200")
                        .queryParam("after", "1672531200")
                        .queryParam("page", "1")
                        .queryParam("perPageResults", "30")
                        .build(UUID.randomUUID()))
                .exchange()
                .expectStatus().isAccepted();
    }
}