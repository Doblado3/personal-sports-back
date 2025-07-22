package com.pablodoblado.personal_sports_back.backend.dto.TrainingActivity;

import java.time.OffsetDateTime;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VueltaRequestDTO {
	
	@NotNull(message = "La vuelta debe estar asociada a una actividad.")
	private Long trainingActivity;
	
	@Min(0)
	private Double velocidadMedia;
	
	@Min(0)
	private Double distancia;
	
	@Min(0)
	private Integer tiempoActivo;
	
	@NotNull(message = "La fecha de comienzo de la vuelta no puede ser nula.")
	private OffsetDateTime fechaComienzo;
	
	@Min(0)
	private Double pulsoMedio;
	
	@Min(0)
	private Double pulsoMaximo;
	
	
	private Double desnivel;
	
	@Min(1)
	private Integer numeroVuelta;

}
