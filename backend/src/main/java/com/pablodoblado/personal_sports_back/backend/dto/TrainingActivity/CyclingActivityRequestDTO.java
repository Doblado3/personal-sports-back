package com.pablodoblado.personal_sports_back.backend.dto.TrainingActivity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

//Hereda los campos y validaciones de TrainingActivityRequestDTO

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class CyclingActivityRequestDTO extends TrainingActivityRequestDTO {
	
	@NotNull(message = "Debes indicar si has usado potenciometro o las medidas son estimadas.")
	private Boolean potenciometro;
	
	@Min(0)
	private Double cadencia;
	
	
	@Min(0)
	private Double vatiosMedios;
	
	@Min(0)
	private Double vatiosMaximos;
	
	

}
