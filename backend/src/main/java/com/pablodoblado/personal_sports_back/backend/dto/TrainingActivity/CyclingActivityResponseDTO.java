package com.pablodoblado.personal_sports_back.backend.dto.TrainingActivity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class CyclingActivityResponseDTO extends TrainingActivityResponseDTO {
	
	private Boolean potenciometro;
	private Double cadencia;
	private Double kiloJulios;
	private Double vatiosMedios;
	private Double vatiosMaximos;

}
