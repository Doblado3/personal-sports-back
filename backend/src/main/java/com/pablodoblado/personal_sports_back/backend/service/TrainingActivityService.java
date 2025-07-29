package com.pablodoblado.personal_sports_back.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType; 
import org.springframework.web.reactive.function.BodyInserters; 
import org.springframework.web.reactive.function.client.WebClientResponseException; 
import com.pablodoblado.personal_sports_back.backend.config.TrainingActivityMapper;
import com.pablodoblado.personal_sports_back.backend.dto.StravaApi.StravaDetailedActivityDTO;
import com.pablodoblado.personal_sports_back.backend.dto.StravaApi.StravaTokenResponse;
import com.pablodoblado.personal_sports_back.backend.dto.TrainingActivity.TrainingActivityResponseDTO;
import com.pablodoblado.personal_sports_back.backend.entity.CyclingActivity;
import com.pablodoblado.personal_sports_back.backend.entity.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import com.pablodoblado.personal_sports_back.backend.entity.enums.TipoActividad;
import com.pablodoblado.personal_sports_back.backend.repository.TrainingActivityRepository;
import com.pablodoblado.personal_sports_back.backend.repository.UsuarioRepository;

@Service
public class TrainingActivityService {

	private Logger log = LoggerFactory.getLogger(TrainingActivityService.class);

	private final TrainingActivityRepository trainingActivityRepository;

	private final WebClient webClientStrava;

	private final TrainingActivityMapper trainingActivityMapper;

	private final UsuarioRepository usuarioRepository;
	
	private final AemetService aemetService;
	
	    private final StravaTokenService stravaTokenService;
    private final ApiRateLimiter ApiRateLimiter;

	@Value("${strava.api.client-id}")
	private String stravaClientId;

	@Value("${strava.api.client-secret}")
	private String stravaClientSecret;

	@Autowired
	public TrainingActivityService(TrainingActivityRepository trainingActivityRepository, @Qualifier("webClientStrava") WebClient webClientStrava, TrainingActivityMapper trainingActivityMapper,
			UsuarioRepository usuarioRepository, AemetService aemetService, StravaTokenService stravaTokenService, ApiRateLimiter ApiRateLimiter) {
		
		this.usuarioRepository = usuarioRepository;
		this.trainingActivityMapper = trainingActivityMapper;
		this.trainingActivityRepository = trainingActivityRepository;
		this.webClientStrava = webClientStrava;
		this.aemetService = aemetService;
		this.stravaTokenService = stravaTokenService;
        this.ApiRateLimiter = ApiRateLimiter;

	}

	/**
	 * Metodo que llama al endpoint /athlete/activities de la API de Strava para devolver las actividades de un atleta identificado
	 * dentro de un determinado periodo de tiempo
	 * 
	 * @param before Optional: Activities before this Unix epoch timestamp (seconds).
     * @param after Optional: Activities after this Unix epoch timestamp (seconds).
     * @param page Page number for pagination.
     * @param perPage Number of activities per page (max 200).
     * 
     * @return A list of your application's TrainingActivityResponseDTOs.
     * 
     * * */
	public List<TrainingActivityResponseDTO> fetchAndSaveStravaActivities(UUID usuarioId, Long before, Long after, Integer page, Integer perPageResults) {
		
		//TO-DO: Podemos coger el usuario loggeado desde el front con loginService para pasarlo
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new RuntimeException("Usuario with ID " + usuarioId + " not found")); 

		// Comprobamos si el token está a punto de expirar y/o si el usuario no tiene sus credenciales
		// TO-DO: Registro del usuario como API developer
		long fiveMinutesFromNow = Instant.now().plusSeconds(300).getEpochSecond();
		if (usuario.getStravaAccessToken() == null ||
		    usuario.getStravaTokenExpiresAt() == null ||
		    usuario.getStravaTokenExpiresAt() < fiveMinutesFromNow) {
		    log.info("Proactively refreshing Strava token for user {} as it's expired or near expiration.", usuarioId);
		    usuario = stravaTokenService.refreshToken(usuario); 
		}


		StringBuilder uriBuilder = new StringBuilder("/athlete/activities"); //el propio webClient ya tiene definida la url base en config
		uriBuilder.append("?");

		if (before != null) {
			uriBuilder.append("before=").append(before).append("&");
		}

		if (after != null) {
			uriBuilder.append("after=").append(after).append("&");
		}

		uriBuilder.append("page=").append(page != null ? page: 1).append("&");
		uriBuilder.append("per_page=").append(perPageResults != null ? perPageResults: 30);
			
		List<StravaDetailedActivityDTO> listOfActivities;
			
		ApiRateLimiter.checkStravaRateLimit();

		try {
			
			String currentAccessToken = usuario.getStravaAccessToken();
			if (currentAccessToken == null || currentAccessToken.isEmpty()) { // Added isEmpty check
				throw new RuntimeException("No Strava access token available for user " + usuarioId + ". Please connect to Strava first.");
			}

			listOfActivities = webClientStrava.get()
					.uri(uriBuilder.toString())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + currentAccessToken)
					.retrieve()
                    .toEntity(new ParameterizedTypeReference<List<StravaDetailedActivityDTO>>() {})
                    .map(responseEntity -> {
                        ApiRateLimiter.updateStravaRateLimit(responseEntity.getHeaders());
                        return responseEntity.getBody();
                    })
					.block();

		} catch (WebClientResponseException.Unauthorized e) { // Error encontrado con Postman
			
			log.warn("Strava API call failed with 401 UNAUTHORIZED for user {}. Attempting to refresh token and retry.", usuarioId);
			usuario = stravaTokenService.refreshToken(usuario);
			String refreshedAccessToken = usuario.getStravaAccessToken();
			
			if(refreshedAccessToken == null || refreshedAccessToken.isEmpty()) {
				throw new RuntimeException("Failed to obtain a valid access token after refresh for user: " + usuario.getId());
			}
			
			            listOfActivities = webClientStrava.get()
                    .uri(uriBuilder.toString())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer: " + refreshedAccessToken)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<StravaDetailedActivityDTO>>() {})
                    .map(responseEntity -> {
                        ApiRateLimiter.updateStravaRateLimit(responseEntity.getHeaders());
                        return responseEntity.getBody();
                    })
                    .block();
			
		} catch (Exception e) {
			log.error("Error fetching Strava activities for user {}: {}", usuarioId, e.getMessage(), e);
			throw new RuntimeException("Error fetching Strava activities for user " + usuarioId, e);
		}
		
		
		if (listOfActivities == null || listOfActivities.isEmpty()) {
		    log.info("No new activities fetched from Strava for user {}.", usuarioId);
		    return List.of();
		}
		
		//Evitamos almacenar actividades ya existentes
		List<TrainingActivity> activitiesToSave = listOfActivities.stream()
				.map(this::mapStravaActivityToTrainingActivityEntity)
				.filter(activity -> !trainingActivityRepository.existsById(activity.getId()))
				.toList();

		if (!CollectionUtils.isEmpty(activitiesToSave)) { 
			
			
			
		    List<TrainingActivity> saveActivities = trainingActivityRepository.saveAll(activitiesToSave);
		    log.info("Saved {} new Strava activities for user {}.", saveActivities.size(), usuarioId);
		    
		    return saveActivities.stream()
		            .map(trainingActivityMapper::mapActivityEntityToResponse)
		            .toList();
		} else {
		    log.info("No new activities to save for user {}.", usuarioId);
		    return List.of(); 
		}
	}

	//De momento prefiero implementar el metodo de forma privada que hacer un nuevo mapper
	private TrainingActivity mapStravaActivityToTrainingActivityEntity(StravaDetailedActivityDTO dtoStrava) {
		TrainingActivity trainingActivity;

		if(TipoActividad.RIDE.equals(dtoStrava.getTipo())) { 

			CyclingActivity cyclingActivity = new CyclingActivity();
			cyclingActivity.setPotenciometro(dtoStrava.getPotenciometro());
			cyclingActivity.setCadencia(dtoStrava.getCadence());
			cyclingActivity.setVatiosMedios(dtoStrava.getVatiosMedios());
			cyclingActivity.setVatiosMaximos(dtoStrava.getVatiosMaximos());
			trainingActivity = cyclingActivity;

		}else {
			trainingActivity = new TrainingActivity();
		}

		// Asignamos el ID a la clase padre
		trainingActivity.setId(dtoStrava.getId()); 

		Usuario usuario = usuarioRepository.findByStravaAthleteId(dtoStrava.getAthleteId())
				.orElseThrow(() -> new RuntimeException("Usuario with athleteId: " + dtoStrava.getAthleteId() + " not found.")); 

		trainingActivity.setUsuario(usuario);
		trainingActivity.setTipo(dtoStrava.getTipo());
		trainingActivity.setNombre(dtoStrava.getNombre());
		trainingActivity.setDistancia(dtoStrava.getDistancia());
		trainingActivity.setTiempoTotal(dtoStrava.getTiempoTotal());
		trainingActivity.setTiempoActivo(dtoStrava.getTiempoActivo());
		trainingActivity.setDesnivel(dtoStrava.getDesnivel());
		trainingActivity.setMaxAltitud(dtoStrava.getMaxAltitud());
		trainingActivity.setMinAltitud(dtoStrava.getMinAltitud());
		trainingActivity.setFechaComienzo(dtoStrava.getFechaComienzo());
		trainingActivity.setVelocidadMaxima(dtoStrava.getVelocidadMaxima());
		trainingActivity.setVelocidadMedia(dtoStrava.getVelocidadMedia());
		trainingActivity.setKiloJulios(dtoStrava.getKiloJulios());
		trainingActivity.setPulsoMedio(dtoStrava.getPulsoMedio());
		trainingActivity.setPulsoMaximo(dtoStrava.getPulsoMaximo());
		
		// Las actividades de fuerza no recogen la posicion de partida
		if (dtoStrava.getLatlng() != null && !dtoStrava.getLatlng().isEmpty()) {
			trainingActivity.setStartLatlng(dtoStrava.getLatlng().get(0));
		}
		
		trainingActivity = aemetService.getValoresClimatologicosRangoFechas(trainingActivity);
		
		
		

		return trainingActivity;
	}

	
}