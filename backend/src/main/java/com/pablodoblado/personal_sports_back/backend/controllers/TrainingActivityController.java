package com.pablodoblado.personal_sports_back.backend.controller;

import com.pablodoblado.personal_sports_back.backend.service.TrainingActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@RestController
@RequestMapping("/api/trainingActivities")
@CrossOrigin(origins = "http://localhost:4200")
public class TrainingActivityController {

    private static final Logger log = LoggerFactory.getLogger(TrainingActivityController.class);
    private final TrainingActivityService trainingActivityService;

    public TrainingActivityController(TrainingActivityService trainingActivityService) {
        this.trainingActivityService = trainingActivityService;
    }

    /**
     * Endpoint method to fetch user strava activities
     * The method get the activities and saved them to our database for later processing porpuses
     * That's way the PostMapping tag instead of a GetMapping one
     * */
    @PostMapping("/fetchStravaActivities/{usuarioId}")
    public Mono<ResponseEntity<Void>> fetchStravaActivities(@PathVariable UUID usuarioId,
                                                            @RequestParam Long before, @RequestParam Long after,
                                                            @RequestParam Integer page, @RequestParam Integer perPageResults) {

        trainingActivityService.fetchAndSaveStravaActivities(usuarioId, before, after, page, perPageResults)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        null,
                        error -> log.error("Error fetching Strava activities for user {} in background", usuarioId, error)
                );

        return Mono.just(ResponseEntity.accepted().build());
    }
}