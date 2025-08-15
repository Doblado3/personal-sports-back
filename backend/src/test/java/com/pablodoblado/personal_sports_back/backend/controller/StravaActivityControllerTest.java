package com.pablodoblado.personal_sports_back.backend.controller;

import com.pablodoblado.personal_sports_back.backend.controllers.StravaActivityController;
import com.pablodoblado.personal_sports_back.backend.services.impls.StravaActivityServiceImpl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(StravaActivityController.class)
public class StravaActivityControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private StravaActivityServiceImpl trainingActivityService;

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