package com.pablodoblado.personal_sports_back.backend.services.impls;

import com.pablodoblado.personal_sports_back.backend.entities.CyclingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;
import com.pablodoblado.personal_sports_back.backend.mappers.TrainingActivityMapper;
import com.pablodoblado.personal_sports_back.backend.models.StravaDetailedActivityDTO;
import com.pablodoblado.personal_sports_back.backend.repositories.TrainingActivityRepository;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;
import com.pablodoblado.personal_sports_back.backend.services.TrainingActivityService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class TrainingActivityServiceImpl implements TrainingActivityService {
	
	// TO-DO(Refactoring): TrainingActivity should refer only to managing the database table. Everything else needs other impls(strava calls, refresh tokens, etc)

    private final TrainingActivityRepository trainingActivityRepository;
    
    @Qualifier("webClientStrava")
    private final WebClient webClientStrava;
    
    private final UsuarioRepository usuarioRepository;
    
    private final AemetService aemetService;
    
    private final StravaTokenService stravaTokenService;
    
    private final ApiRateLimiterService apiRateLimiter;

    
    
    public Mono<Void> fetchAndSaveStravaActivities(UUID usuarioId, Long before, Long after, Integer page, Integer perPageResults) {
        return Mono.fromCallable(() -> usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario with ID " + usuarioId + " not found")))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(this::refreshTokenIfNecessary)
                .flatMap(user -> {
                    String uri = buildStravaActivitiesUri(before, after, page, perPageResults);
                    return fetchActivitiesFromStrava(user, uri)
                            .flatMap(activities -> processAndSaveActivities(activities, user));
                });
    }

    private Mono<Usuario> refreshTokenIfNecessary(Usuario usuario) {
        long fiveMinutesFromNow = Instant.now().plusSeconds(300).getEpochSecond();
        if (usuario.getStravaAccessToken() == null ||
            usuario.getStravaTokenExpiresAt() == null ||
            usuario.getStravaTokenExpiresAt() < fiveMinutesFromNow) {
            log.info("Proactively refreshing Strava token for user {} as it's expired or near expiration.", usuario.getId());
            return Mono.fromCallable(() -> stravaTokenService.refreshToken(usuario))
                    .subscribeOn(Schedulers.boundedElastic());
        }
        return Mono.just(usuario);
    }

    private Mono<List<StravaDetailedActivityDTO>> fetchActivitiesFromStrava(Usuario user, String uri) {
        return apiRateLimiter.checkStravaRateLimit()
                .then(Mono.defer(() -> {
                    String accessToken = user.getStravaAccessToken();
                    if (accessToken == null || accessToken.isEmpty()) {
                        return Mono.error(new RuntimeException("No Strava access token for user " + user.getId()));
                    }
                    return makeStravaApiCall(accessToken, uri)
                            .onErrorResume(WebClientResponseException.Unauthorized.class, e -> refreshTokenAndRetry(user, uri));
                }));
    }

    private Mono<List<StravaDetailedActivityDTO>> makeStravaApiCall(String accessToken, String uri) {
        return webClientStrava.get().uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<StravaDetailedActivityDTO>>() {})
                .map(responseEntity -> {
                    apiRateLimiter.updateStravaRateLimit(responseEntity.getHeaders());
                    return responseEntity.getBody();
                });
    }

    private Mono<List<StravaDetailedActivityDTO>> refreshTokenAndRetry(Usuario user, String uri) {
        log.warn("Strava API call failed with 401 UNAUTHORIZED for user {}. Refreshing token and retrying.", user.getId());
        return Mono.fromCallable(() -> stravaTokenService.refreshToken(user))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(refreshedUser -> {
                    if (refreshedUser.getStravaAccessToken() == null || refreshedUser.getStravaAccessToken().isEmpty()) {
                        return Mono.error(new RuntimeException("Failed to get valid token after refresh for user: " + user.getId()));
                    }
                    return makeStravaApiCall(refreshedUser.getStravaAccessToken(), uri);
                });
    }

    private Mono<Void> processAndSaveActivities(List<StravaDetailedActivityDTO> activities, Usuario user) {
        if (CollectionUtils.isEmpty(activities)) {
            log.info("No new activities from Strava.");
            return Mono.empty();
        }
        return Flux.fromIterable(activities)
                .flatMap(dto -> mapAndCheckExistence(dto, user))
                .collectList()
                .flatMap(this::saveActivities);
    }

    private Mono<TrainingActivity> mapAndCheckExistence(StravaDetailedActivityDTO dto, Usuario user) {
        return mapStravaDtoToEntity(dto, user)
                .flatMap(activity -> Mono.fromCallable(() -> trainingActivityRepository.existsById(activity.getId()))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(exists -> exists ? Mono.empty() : Mono.just(activity)));
    }

    private Mono<Void> saveActivities(List<TrainingActivity> activities) {
        if (!CollectionUtils.isEmpty(activities)) {
            return Mono.fromRunnable(() -> trainingActivityRepository.saveAll(activities))
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnSuccess(v -> log.info("Saved {} new Strava activities.", activities.size()))
                    .then();
        }
        log.info("No new activities to save.");
        return Mono.empty();
    }

    private String buildStravaActivitiesUri(Long before, Long after, Integer page, Integer perPageResults) {
        StringBuilder uriBuilder = new StringBuilder("/athlete/activities?");
        if (before != null) uriBuilder.append("before=").append(before).append("&");
        if (after != null) uriBuilder.append("after=").append(after).append("&");
        uriBuilder.append("page=").append(page != null ? page : 1).append("&");
        uriBuilder.append("per_page=").append(perPageResults != null ? perPageResults : 30);
        return uriBuilder.toString();
    }

    private Mono<TrainingActivity> mapStravaDtoToEntity(StravaDetailedActivityDTO dto, Usuario user) {
        TrainingActivity activity = (dto.getTipo() == TipoActividad.RIDE || dto.getTipo() == TipoActividad.MOUNTAINBIKERIDE) ? new CyclingActivity() : new TrainingActivity();
        activity.setUsuario(user);
        // Mapping basic fields
        activity.setId(dto.getId());
        activity.setNombre(dto.getNombre());
        activity.setDistancia(dto.getDistancia());
        activity.setTiempoTotal(dto.getTiempoTotal());
        activity.setTiempoActivo(dto.getTiempoActivo());
        activity.setDesnivel(dto.getDesnivel());
        activity.setMaxAltitud(dto.getMaxAltitud());
        activity.setMinAltitud(dto.getMinAltitud());
        activity.setFechaComienzo(dto.getFechaComienzo());
        activity.setVelocidadMaxima(dto.getVelocidadMaxima());
        activity.setVelocidadMedia(dto.getVelocidadMedia());
        activity.setKiloJulios(dto.getKiloJulios());
        activity.setPulsoMedio(dto.getPulsoMedio());
        activity.setPulsoMaximo(dto.getPulsoMaximo());
        if (dto.getLatlng() != null && !dto.getLatlng().isEmpty()) {
            activity.setStartLatlng(dto.getLatlng().get(0));
        }

        if (activity instanceof CyclingActivity) {
            CyclingActivity cyclingActivity = (CyclingActivity) activity;
            cyclingActivity.setPotenciometro(dto.getPotenciometro());
            cyclingActivity.setCadencia(dto.getCadence());
            cyclingActivity.setVatiosMedios(dto.getVatiosMedios());
            cyclingActivity.setVatiosMaximos(dto.getVatiosMaximos());
        }

        return aemetService.getValoresClimatologicosRangoFechas(activity);
    }
}
