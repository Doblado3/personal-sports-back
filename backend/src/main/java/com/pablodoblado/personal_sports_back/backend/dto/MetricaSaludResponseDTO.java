package com.pablodoblado.personal_sports_back.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricaSaludResponseDTO {
	
	private UUID usuarioId; 
    private LocalDate fechaRegistro;
    private Integer horasSuenoHours;
    private Integer horasSuenoMinutes;
    private Double peso;
    private String calidadSueno;
    private Double hrvRmssd;
    private Double hrvSdnn;
    private Integer cardiacaReposo;
    private String estresSubjetivo;
    private Integer estresMedido;
    private String descansoSubjetivo;
    private String incidencias; 

}
