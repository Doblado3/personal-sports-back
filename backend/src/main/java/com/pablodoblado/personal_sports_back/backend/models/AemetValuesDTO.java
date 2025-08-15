package com.pablodoblado.personal_sports_back.backend.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AemetValuesDTO {
	
	// Los valores proporcionan datos historicos
	
	// fecha del dia
	@JsonProperty(value = "fecha")
	String fecha;
	
	// Temperatura media diaria en grados celsius
	@JsonProperty(value = "tmed")
	String tmed;
	
	// Precipitacion diaria de 07 a 07 en mm
	@JsonProperty(value = "prec")
	String prec;
	
	// Velocidad media del viento en m/s
	@JsonProperty(value = "velmedia")
	String velmedia;
	
	// Humedad relativa media diaria (%)
	@JsonProperty(value = "hrMedia")
	String hrmedia;
	

}
