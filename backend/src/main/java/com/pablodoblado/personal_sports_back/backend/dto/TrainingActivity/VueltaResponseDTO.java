package com.pablodoblado.personal_sports_back.backend.dto.TrainingActivity;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VueltaResponseDTO {
	
	private Long id;
	private Long trainingActivityId;
	private Double velocidadMedia;
	private Double distancia;
	private Integer tiempoActivo;
	private OffsetDateTime fechaComienzo;
	private Integer pulsoMedio;
	private Integer pulsoMaximo;
	private Integer pulsoMinimo;
	private Integer desnivel;
	private Integer numeroVuelta;

}
