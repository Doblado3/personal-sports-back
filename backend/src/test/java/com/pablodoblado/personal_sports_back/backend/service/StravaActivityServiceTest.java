package com.pablodoblado.personal_sports_back.backend.service;

import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.mappers.StravaActivityMapper;
import com.pablodoblado.personal_sports_back.backend.models.StravaDetailedActivityDTO;
import com.pablodoblado.personal_sports_back.backend.repositories.TrainingActivityRepository;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;
import com.pablodoblado.personal_sports_back.backend.services.AemetService;
import com.pablodoblado.personal_sports_back.backend.services.impls.ApiRateLimiterServiceImpl;
import com.pablodoblado.personal_sports_back.backend.services.impls.StravaActivityServiceImpl;
import com.pablodoblado.personal_sports_back.backend.services.impls.StravaTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StravaActivityServiceTest {

    @Mock
    private TrainingActivityRepository trainingActivityRepository;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AemetService aemetService;
    @Mock
    private StravaTokenServiceImpl stravaTokenService;
    @Mock
    private ApiRateLimiterServiceImpl apiRateLimiter;
    @Mock
    private StravaActivityMapper stravaActivityMapper;
    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private StravaActivityServiceImpl stravaActivityService;

    private Usuario usuario;
    private StravaDetailedActivityDTO activityDTO;

    @BeforeEach
    void setUp() {
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation ->
                invocation.getArgument(0, org.springframework.transaction.support.TransactionCallback.class).doInTransaction(null));

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setStravaAccessToken("valid-token");
        usuario.setStravaTokenExpiresAt(Instant.now().getEpochSecond() + 3600);

        activityDTO = new StravaDetailedActivityDTO();
        activityDTO.setId(123L);
        activityDTO.setNombre("Test Activity");
    }

    @Test
    void shouldFetchAndSaveNewActivities() {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of(activityDTO)));
        when(trainingActivityRepository.existsById(activityDTO.getId())).thenReturn(false);
        when(stravaActivityMapper.toTrainingActivity(any())).thenReturn(new TrainingActivity());
        when(aemetService.getValoresClimatologicosRangoFechas(any(TrainingActivity.class)))
                .thenAnswer(inv -> CompletableFuture.completedFuture(inv.getArgument(0)));

        Integer result = stravaActivityService.fetchAndSaveStravaActivities(usuario.getId(), null, null, 1, 1).join();

        assertThat(result).isEqualTo(1);
        ArgumentCaptor<List<TrainingActivity>> captor = ArgumentCaptor.forClass(List.class);
        verify(trainingActivityRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    void shouldRefreshTokenOn401AndSucceed() {
        Usuario refreshedUser = new Usuario();
        refreshedUser.setStravaAccessToken("refreshed-token");

        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .thenAnswer(invocation -> {
                HttpEntity<?> entity = invocation.getArgument(2);
                String authHeader = entity.getHeaders().getFirst("Authorization");
                if ("Bearer valid-token".equals(authHeader)) {
                    throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
                }
                if ("Bearer refreshed-token".equals(authHeader)) {
                    return ResponseEntity.ok(List.of(activityDTO));
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });

        when(stravaTokenService.refreshToken(usuario)).thenReturn(refreshedUser);
        when(trainingActivityRepository.existsById(activityDTO.getId())).thenReturn(false);
        when(stravaActivityMapper.toTrainingActivity(any())).thenReturn(new TrainingActivity());
        when(aemetService.getValoresClimatologicosRangoFechas(any(TrainingActivity.class)))
                .thenAnswer(inv -> CompletableFuture.completedFuture(inv.getArgument(0)));

        Integer result = stravaActivityService.fetchAndSaveStravaActivities(usuario.getId(), null, null, 1, 1).join();

        assertThat(result).isEqualTo(1);
        verify(stravaTokenService).refreshToken(usuario);
        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    void shouldReturnZeroWhenNoActivitiesAreFetched() {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        Integer result = stravaActivityService.fetchAndSaveStravaActivities(usuario.getId(), null, null, 1, 1).join();

        assertThat(result).isZero();
        verify(trainingActivityRepository, never()).saveAll(any());
    }

    @Test
    void shouldThrowExceptionWhenApiFailsWithNonRetriableError() {
    	
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        CompletableFuture<Integer> future = stravaActivityService.fetchAndSaveStravaActivities(usuario.getId(), null, null, 1, 1);

        java.util.concurrent.CompletionException exception = assertThrows(java.util.concurrent.CompletionException.class, future::join);

        assertThat(exception.getCause()).isInstanceOf(java.util.concurrent.CompletionException.class);
        

        Throwable rootCause = exception.getCause().getCause();
        assertThat(rootCause).isInstanceOf(HttpClientErrorException.class);
    }
}
