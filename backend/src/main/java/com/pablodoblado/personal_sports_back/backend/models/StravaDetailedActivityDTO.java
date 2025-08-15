package com.pablodoblado.personal_sports_back.backend.models;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StravaDetailedActivityDTO {
	
	@JsonProperty(value = "id")
	Long id;
	
	@JsonProperty(value = "athlete")
	StravaAthleteDTO athlete;
	
	@JsonProperty(value = "name")
	String nombre;
	
	@JsonProperty(value = "distance")
	Double distancia;
	
	@JsonProperty(value = "sport_type")
	TipoActividad tipo;
	
	@JsonProperty(value = "elapsed_time")
	Integer tiempoTotal; //segundos
	
	@JsonProperty(value = "moving_time")
	Integer tiempoActivo;
	
	@JsonProperty(value = "total_elevation_gain")
	Double desnivel;
	
	@JsonProperty(value = "elev_high")
	Double maxAltitud;
	
	@JsonProperty(value = "elev_low")
	Double minAltitud;
	
	@JsonProperty(value = "start_date")
	LocalDateTime fechaComienzo;
	
	@JsonProperty(value = "average_speed")
	Double velocidadMedia;
	
	@JsonProperty(value = "max_speed")
	Double velocidadMaxima;
	
	@JsonProperty(value = "kilojoules")
	Double kiloJulios;
	
	@JsonProperty(value = "average_heartrate")
	Double pulsoMedio;
	
	@JsonProperty(value = "max_heartrate")
	Double pulsoMaximo;
	
	@JsonProperty(value = "average_watts")
	Double vatiosMedios;
	
	@JsonProperty(value = "max_watts")
	Double vatiosMaximos;
	
	@JsonProperty(value = "average_cadence")
	Double cadence;
	
	@JsonProperty(value = "device_watts")
	Boolean potenciometro;
	
	@JsonProperty(value = "start_latlng")
	List<Double> latlng;
	
	public Long getAthleteId() {
		return athlete != null ? athlete.getId(): null;
	}
	
	

}
