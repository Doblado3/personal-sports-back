package com.pablodoblado.personal_sports_back.backend.dto.TrainingActivity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.pablodoblado.personal_sports_back.backend.entity.enums.TipoActividad;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public class TrainingActivityResponseDTO {
	
	private Long id;
	private UUID usuarioId;
	private String nombre;
	private Double distancia;
	private TipoActividad tipo;
	private Integer tiempoTotal;
	private Integer tiempoActivo;
	private Double desnivel;
	private Double maxAltitud;
	private Double minAltitud;
	private Double temperatura;
	private Double viento;
	private Integer humedad;
	private Boolean lluvia;
	private OffsetDateTime fechaComienzo;
	private String rpeObjetivo;
	private String rpeReal;
	private Double velocidadMedia;
	private Double velocidadMaxima;
	private String feedback;
	private Double kiloJulios;
	private Double hidratos;
	private Double litrosAgua;
	private Double pulsoMedio;
	private Double pulsoMaximo;
	private List<VueltaResponseDTO> vueltas;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
	

}
