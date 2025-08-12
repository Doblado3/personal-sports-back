package com.pablodoblado.personal_sports_back.backend.models;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.*;

@Data
@Builder
public class MetricaSaludRequestDTO {
	
	@NotNull(message = "La fecha de registro no puede ser nula")
	private LocalDate fechaRegistro;
	
	@Min(value = 0, message = "Las horas de sueño no pueden ser negativas.")
    @Max(value = 24, message = "Las horas de sueño no pueden exceder 24.")
    private Integer horasSuenoHours;

    @Min(value = 0, message = "Los minutos de sueño no pueden ser negativos.")
    @Max(value = 59, message = "Los minutos de sueño no pueden exceder 59.")
    private Integer horasSuenoMinutes;
    
    @Min(value = 0, message = "No es posible no pesar nada.")
    private Double peso;

	
	@NotNull(message = "La calidad del sueño no puede ser nula")
	private String calidadSueno;
	
	private Double hrvRmssd;
    private Double hrvSdnn;
    private Integer cardiacaReposo; 
    private Integer estresSubjetivo;
    private Integer estresMedido;
    private Integer descansoSubjetivo;
    private String incidencias; 

}
