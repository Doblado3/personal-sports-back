package com.pablodoblado.personal_sports_back.backend.models;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TrainingActivityRequestDTO {
	
	@NotNull(message = "Debes introducir un nombre para guardar la actividad.")
	private String nombre;
	
	@NotNull(message = "El usuario no puede ser nulo")
	private UUID usuarioId;
	
	@Min(0)
	private Double distancia;
	
	@NotNull(message = "Debes indicar qué tipo de actividad has realizado.")
	private TipoActividad tipo;
	
	//Comprobar que tiempoActivo <= tiempoTotal en capa servicio
	@Min(0)
	private Integer tiempoTotal;
	
	@Min(0)
	private Integer tiempoActivo;
	
	private Double desnivel;
	
	private Double maxAltitud;
	
	private Double minAltitud;
	
	@Max(60)
	@Min(-20)
	private Double temperatura;
	
	private Double viento;
	
	private Boolean lluvia;
	
	//En porcentaje
	@Max(100)
	@Min(0)
	private Double humedad;
	
	@NotNull(message = "La fecha de comienzo no puede ser nula.")
	private OffsetDateTime fechaComienzo;
	
	//Escala de 0-10
	@Max(10)
	@Min(0)
	private String rpeObjetivo;
	
	@Max(10)
	@Min(0)
	private String rpeReal;
	
	@Min(0)
	private Double velocidadMedia;
	
	@Min(0)
	private Double velocidadMaxima;
	
	private String feedback;
	
	@Min(0)
	private Double kiloJulios;
	
	private Double hidratos;
	
	private Double litrosAgua;
	
	@Min(0)
	private Double pulsoMedio;
	
	@Min(0)
	private Double pulsoMaximo;
	
	
	private List<VueltaRequestDTO> vueltas;

}
