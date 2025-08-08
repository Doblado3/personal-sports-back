package com.pablodoblado.personal_sports_back.backend.service;

import com.pablodoblado.personal_sports_back.backend.config.TrainingActivityMapper;
import com.pablodoblado.personal_sports_back.backend.dto.StravaApi.StravaDetailedActivityDTO;
import com.pablodoblado.personal_sports_back.backend.entity.CyclingActivity;
import com.pablodoblado.personal_sports_back.backend.entity.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import com.pablodoblado.personal_sports_back.backend.entity.enums.TipoActividad;
import com.pablodoblado.personal_sports_back.backend.repository.TrainingActivityRepository;
import com.pablodoblado.personal_sports_back.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TrainingActivityService {

    private final Logger log = LoggerFactory.getLogger(TrainingActivityService.class);
    private final TrainingActivityRepository trainingActivityRepository;
    private final WebClient webClientStrava;
    private final UsuarioRepository usuarioRepository;
    private final AemetService aemetService;
    private final StravaTokenService stravaTokenService;
    private final ApiRateLimiterService apiRateLimiter;

    @Autowired
    public TrainingActivityService(TrainingActivityRepository trainingActivityRepository, @Qualifier("webClientStrava") WebClient webClientStrava, TrainingActivityMapper trainingActivityMapper,
                                 UsuarioRepository usuarioRepository, AemetService aemetService, StravaTokenService stravaTokenService, ApiRateLimiterService apiRateLimiter) {
        this.usuarioRepository = usuarioRepository;
        this.trainingActivityRepository = trainingActivityRepository;
        this.webClientStrava = webClientStrava;
        this.aemetService = aemetService;
        this.stravaTokenService = stravaTokenService;
        this.apiRateLimiter = apiRateLimiter;
    }

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
