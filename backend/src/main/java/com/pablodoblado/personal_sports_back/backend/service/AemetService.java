package com.pablodoblado.personal_sports_back.backend.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablodoblado.personal_sports_back.backend.dto.AemetApi.AemetInitialResponseDTO;
import com.pablodoblado.personal_sports_back.backend.dto.AemetApi.AemetObservationsDTO;
import com.pablodoblado.personal_sports_back.backend.dto.AemetApi.AemetValuesDTO;
import com.pablodoblado.personal_sports_back.backend.entity.TrainingActivity;


@Service
public class AemetService {
	
	// Clase que define la lógica de las llamadas Http a la API de Aemet
	// Para obtener los DTOs de observaciones y valores, como minimo
	
	private Logger log = LoggerFactory.getLogger(AemetService.class);
	
	private WebClient webClientAemet;
	
	private final ObjectMapper objectMapper;
	private final ApiRateLimiter apiRateLimiter;
	
	
	@Value("${identificador.estacion.Alajar}")
	private String identificadorEstacionAlajar;
	
	
	public AemetService(@Qualifier("webClientAemet") WebClient webClient,  ObjectMapper objectMapper, ApiRateLimiter apiRateLimiter) {
		this.webClientAemet = webClient;
		this.objectMapper = objectMapper;
		this.apiRateLimiter = apiRateLimiter;
	}
	
	/**
	 * Metodo que llama al endpoint /valores/climatologicos/diarios/datos/fechaini/{fechaIniStr}/fechaFin/{fechaFinStr}/estacion/{idema} de la API de Aemet openData 
	 * para devolver los valores climatologicos historicos del rango de fechas y estacion seleccionada
	 * 
	 * @param trainingActivityList: Lista de DTO's con actividades sin informacion climatologica asociada.
     * 
     * @return Una lista de TrainingActivityResponseDTOs pero con los datos climatologicos rellenos.
     * 
     * * */
	public TrainingActivity getValoresClimatologicosRangoFechas(TrainingActivity trainingActivity) {
		
		apiRateLimiter.checkAemetRateLimit();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'UTC'");
		String fechaIniStr = trainingActivity.getFechaComienzo().format(formatter);
		
		String fechaFinStr = trainingActivity.getFechaComienzo().plusDays(1).format(formatter);
		
		// Alajar y Almaden (estaciones idema) estan bastante proximos, no tiene demasiado sentido diferenciar uno de otro
		String idema = identificadorEstacionAlajar;
		
		
		// Si la fecha de inicio de la activididad esta dentro de las ultimas 12 horas
		// Llamamos al endpoint de valores de la API de aemet
		Instant now = Instant.now();
		Instant twelveHoursAgo = now.minus(12, ChronoUnit.HOURS);
		Instant startTime = trainingActivity.getFechaComienzo().toInstant(ZoneOffset.UTC);
		
		try {
			
		    
			if (startTime.isAfter(twelveHoursAgo)) {
				
				log.info("This Activity has begun before 12 hours from now, calling the observations Aemet API endpoint");
				StringBuilder uriBuilder = new StringBuilder("/observacion/convencional/datos");
				if (idema != null) uriBuilder.append("/estacion/").append(idema);
				
				AemetInitialResponseDTO initialResponse = webClientAemet.get()
						.uri(uriBuilder.toString())
						.header("cache-control", "no-cache")
						.retrieve()
						.bodyToMono(AemetInitialResponseDTO.class)
						.block();
				
				List<AemetObservationsDTO> valuesMeteo = webClientAemet.get()
						.uri(initialResponse.getDatos())
						.retrieve()
						.bodyToMono(new ParameterizedTypeReference<List<AemetObservationsDTO>>() {})
						.block();
				
				if(valuesMeteo != null && !valuesMeteo.isEmpty()) {
					
                    AemetObservationsDTO closestObservation = findClosestObservation(valuesMeteo, startTime);
                    
                    if (closestObservation != null) {
                    	
                        trainingActivity.setTemperatura(closestObservation.getTa());
                        trainingActivity.setViento(closestObservation.getVv());
                        trainingActivity.setHumedad(closestObservation.getHr());
                        trainingActivity.setLluvia(closestObservation.getPrec() != 0.0 ? true: false);

                        
                    }
                }
				
				
			} else {
			
				// Logica para llamadas historicas (añadir logica try/catch)
				log.info("Calling the values Aemet API endpoint");
				
				StringBuilder uriBuilder = new StringBuilder("/valores/climatologicos/diarios/datos");
				if (fechaIniStr != null) uriBuilder.append("/fechaini/").append(fechaIniStr);
				if (fechaFinStr != null) uriBuilder.append("/fechafin/").append(fechaFinStr);
				if (idema != null) uriBuilder.append("/estacion/").append(idema);
				
				AemetInitialResponseDTO initialResponse = webClientAemet.get()
						.uri(uriBuilder.toString())
						.header("cache-control", "no-cache")
						.retrieve()
						.bodyToMono(AemetInitialResponseDTO.class)
						.block();
				
				if (initialResponse != null) {
				
					String datosMeteoString = webClientAemet.get()
		                    .uri(initialResponse.getDatos())
		                    .retrieve()
		                    .bodyToMono(String.class)
		                    .block();
		            
		            // 2. Deserializa manualmente el String a una lista de DTOs.
		            List<AemetValuesDTO> datosMeteoList = objectMapper.readValue(datosMeteoString, new TypeReference<List<AemetValuesDTO>>(){});
		            
		          
					
					if (datosMeteoList != null && !datosMeteoList.isEmpty()) {
						
						AemetValuesDTO datosMeteo = datosMeteoList.get(0);
						
						// Hay JSONs que no registran ciertos valores, de forma aleatoria
						// Guardo como null ya que guardar un 0 tendria un significado implicito, que no es correcto
					    trainingActivity.setHumedad(Optional.ofNullable(datosMeteo.getHrmedia())
					                                        .map(s -> Double.parseDouble(s.replace(',', '.')))
					                                        .orElse(null)); 
					    
					    trainingActivity.setTemperatura(Optional.ofNullable(datosMeteo.getTmed())
					                                        .map(s -> Double.parseDouble(s.replace(',', '.')))
					                                        .orElse(null));
					    
					    trainingActivity.setViento(Optional.ofNullable(datosMeteo.getVelmedia())
					                                        .map(s -> Double.parseDouble(s.replace(',', '.')))
					                                        .orElse(null));
					    
					    trainingActivity.setLluvia(Optional.ofNullable(datosMeteo.getPrec())
					                                        .map(s -> Double.parseDouble(s.replace(',', '.')))
					                                        .map(prec -> prec != 0.0) 
					                                        .orElse(false)); 
					}
				}
			}
				
			} catch (Exception e) {
	            log.error("Error al obtener o procesar los datos de Aemet: {}", e.getMessage(), e);

				
			}
		
		
		
		log.info("Returning trainingActivity: {}", trainingActivity);
		return trainingActivity;
				
		
		
		
	}
	
	/**
	 * Metodo que busca la medida de la API Aemet con la hora mas cercana
	 * a la hora de comienzo de la actividad de la API Strava
	 * 
	 * @param observations: lista con los datos de observacion de las ultimas 12 horas
	 * @param targetTime: hora de comienzo de la actividad
	 * 
	 * @return: instancia AemetObservationsDTO de la que obtendremos los datos meteorologicos
	 * **/
    private AemetObservationsDTO findClosestObservation(List<AemetObservationsDTO> observations, Instant targetTime) {
    	
        DateTimeFormatter aemetDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

        return observations.stream()
        		.filter(o -> Objects.nonNull(o.getFint()))
                .min((o1, o2) -> {
                    Instant time1 = OffsetDateTime.parse(o1.getFint(), aemetDateTimeFormatter).toInstant();
                    Instant time2 = OffsetDateTime.parse(o2.getFint(), aemetDateTimeFormatter).toInstant();
                    long diff1 = Math.abs(ChronoUnit.SECONDS.between(time1, targetTime));
                    long diff2 = Math.abs(ChronoUnit.SECONDS.between(time2, targetTime));
                    return Long.compare(diff1, diff2);
                })
                .orElse(null);
    }

}
