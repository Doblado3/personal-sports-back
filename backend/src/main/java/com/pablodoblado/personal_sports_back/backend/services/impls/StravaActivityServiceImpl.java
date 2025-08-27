package com.pablodoblado.personal_sports_back.backend.services.impls;

import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;
import com.pablodoblado.personal_sports_back.backend.mappers.StravaActivityMapper;
import com.pablodoblado.personal_sports_back.backend.models.StravaDetailedActivityDTO;
import com.pablodoblado.personal_sports_back.backend.repositories.TrainingActivityRepository;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;
import com.pablodoblado.personal_sports_back.backend.services.AemetService;
import com.pablodoblado.personal_sports_back.backend.services.StravaActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StravaActivityServiceImpl implements StravaActivityService {

    private final TrainingActivityRepository trainingActivityRepository;
    private final RestTemplate restTemplate;
    private final UsuarioRepository usuarioRepository;
    private final AemetService aemetService;
    private final StravaTokenServiceImpl stravaTokenService;
    private final ApiRateLimiterServiceImpl apiRateLimiter;
    private final StravaActivityMapper stravaActivityMapper;
    private final TransactionTemplate transactionTemplate;

    @Async("asyncExecutor")
    @Override
    public CompletableFuture<Integer> fetchAndSaveStravaActivities(UUID usuarioId, Long before, Long after, Integer page, Integer perPageResults) {
        return findAndRefreshToken(usuarioId)
                .thenCompose(user -> {
                    String uri = buildStravaActivitiesUri(before, after, page, perPageResults);
                    return fetchActivitiesFromStrava(user, uri)
                            .thenCompose(activities -> {
                                if (CollectionUtils.isEmpty(activities)) {
                                    log.info("No new activities were found from Strava.");
                                    return CompletableFuture.completedFuture(0);
                                }
                                log.info("Fetched {} activities from Strava for user {}. Starting processing...", activities.size(), user.getId());
                                return processAndSaveActivities(activities, user);
                            });
                });
    }

    private CompletableFuture<Usuario> findAndRefreshToken(UUID usuarioId) {
        return CompletableFuture.supplyAsync(() -> {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("User not found for id: " + usuarioId));

            long fiveMinutesFromNow = Instant.now().plusSeconds(300).getEpochSecond();
            if (usuario.getStravaAccessToken() == null || usuario.getStravaTokenExpiresAt() == null ||
                    usuario.getStravaTokenExpiresAt() < fiveMinutesFromNow) {
                log.info("Proactively refreshing Strava token for user {} as it's expired or near expiration.", usuario.getId());
                return stravaTokenService.refreshToken(usuario);
            }
            return usuario;
        });
    }

    private CompletableFuture<List<StravaDetailedActivityDTO>> fetchActivitiesFromStrava(Usuario user, String uri) {
        return CompletableFuture.supplyAsync(() -> makeStravaApiCall(user.getStravaAccessToken(), uri))
                .exceptionally(ex -> {
                    if (isUnauthorizedException(ex)) {
                        log.warn("Strava API call failed with 401. Refreshing token and retrying.", ex);
                        return refreshTokenAndRetry(user, uri);
                    }
                    throw new CompletionException("Failed to fetch Strava activities", ex);
                });
    }

    private List<StravaDetailedActivityDTO> refreshTokenAndRetry(Usuario user, String uri) {
        log.info("Executing token refresh and retry for user {}", user.getId());
        Usuario refreshedUser = stravaTokenService.refreshToken(user);
        if (refreshedUser.getStravaAccessToken() == null) {
            throw new RuntimeException("Failed to get valid token after refresh for user: " + user.getId());
        }
        return makeStravaApiCall(refreshedUser.getStravaAccessToken(), uri);
    }

    public List<StravaDetailedActivityDTO> makeStravaApiCall(String accessToken, String uri) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<StravaDetailedActivityDTO>> responseEntity = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        apiRateLimiter.updateStravaRateLimit(responseEntity.getHeaders());
        return responseEntity.getBody();
    }

    private boolean isUnauthorizedException(Throwable ex) {
        Throwable cause = ex.getCause();
        if (ex instanceof HttpClientErrorException) {
            return ((HttpClientErrorException) ex).getStatusCode().value() == 401;
        }
        if (cause instanceof HttpClientErrorException) {
            return ((HttpClientErrorException) cause).getStatusCode().value() == 401;
        }
        return false;
    }

    private CompletableFuture<Integer> processAndSaveActivities(List<StravaDetailedActivityDTO> activities, Usuario user) {
        List<CompletableFuture<Optional<TrainingActivity>>> futures = activities.stream()
                .map(dto -> mapAndCheckExistence(dto, user))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApplyAsync(v -> transactionTemplate.execute(status -> {
                    List<TrainingActivity> newActivities = futures.stream()
                            .map(CompletableFuture::join)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());

                    if (!newActivities.isEmpty()) {
                        log.info("Saving {} new Strava activities for user {}.", newActivities.size(), user.getId());
                        trainingActivityRepository.saveAll(newActivities);
                        return newActivities.size();
                    }
                    log.info("No new activities to save for user {}.", user.getId());
                    return 0;
                }));
    }

    private CompletableFuture<Optional<TrainingActivity>> mapAndCheckExistence(StravaDetailedActivityDTO dto, Usuario user) {
        return CompletableFuture.supplyAsync(() -> {
                    if (trainingActivityRepository.existsById(dto.getId())) {
                        return Optional.<TrainingActivity>empty();
                    }
                    TrainingActivity activity = mapStravaDtoToEntity(dto, user);
                    return Optional.of(activity);
                })
                .thenCompose(optionalActivity -> optionalActivity
                        .map(activity -> aemetService.getValoresClimatologicosRangoFechas(activity)
                                .thenApply(Optional::of))
                        .orElse(CompletableFuture.completedFuture(Optional.empty())));
    }

    private String buildStravaActivitiesUri(Long before, Long after, Integer page, Integer perPageResults) {
        StringBuilder uriBuilder = new StringBuilder("/athlete/activities?");
        if (before != null) uriBuilder.append("before=").append(before).append("&");
        if (after != null) uriBuilder.append("after=").append(after).append("&");
        uriBuilder.append("page=").append(page != null ? page : 1).append("&");
        uriBuilder.append("per_page=").append(perPageResults != null ? perPageResults : 30);
        return uriBuilder.toString();
    }

    private TrainingActivity mapStravaDtoToEntity(StravaDetailedActivityDTO dto, Usuario user) {
        TrainingActivity activity;
        if (dto.getTipo() == TipoActividad.RIDE || dto.getTipo() == TipoActividad.MOUNTAINBIKERIDE) {
            activity = stravaActivityMapper.toCyclingActivity(dto);
        } else {
            activity = stravaActivityMapper.toTrainingActivity(dto);
        }
        activity.setUsuario(user);
        return activity;
    }
}
