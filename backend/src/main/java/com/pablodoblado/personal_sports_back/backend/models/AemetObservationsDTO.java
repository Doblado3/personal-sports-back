package com.pablodoblado.personal_sports_back.backend.dto.AemetApi;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AemetObservationsDTO {
	
	// Las observaciones proporcionan valores de las 12 horas previas al tiempo actual
	
	// Fecha hora final del periodo de observacion (AAAA-MM-DDTHH:MM:SS)
	// En nuestro caso, final de la actividad
	@JsonProperty(value = "fint")
	String fint;
	
	// Precipitacion acumulada durante los 60 minutos anteriores a fint
	@JsonProperty(value = "prec")
	Double prec;
	
	// Velocidad media del viento durante los 60 minutos anteriores a fint
	@JsonProperty(value = "vv")
	Double vv;
	
	// Humedad relativa instantánea del aire correspondiente a la fecha dada por fint
	@JsonProperty(value = "hr")
	Double hr;
	
	// Temperatura instantánea del aire correspondiente a la fecha data por fint
	@JsonProperty(value = "ta")
	Double ta;
	
	
	

}
