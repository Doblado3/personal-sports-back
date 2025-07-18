package com.pablodoblado.personal_sports_back.backend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vuelta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vuelta {
	
	@Id
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_training_activity", nullable = false)
	private TrainingActivity trainingActivity;
	
	@Column(name = "velocidad_media")
	private Double velocidadMedia;
	
	@Column(name = "distancia")
	private Double distancia;
	
	@Column(name = "tiempo_activo")
	private Integer tiempoActivo;
	
	//Según la fecha de inicio obtendremos el orden de las vueltas dentro de una actividad
	@Column(name = "fecha_comienzo", nullable = false)
	private LocalDateTime fechaComienzo;
	
	@Column(name = "pulso_medio")
	private Integer pulsoMedio;
	
	@Column(name = "pulso_maximo")
	private Integer pulsoMaximo;
	
	@Column(name = "pulso_minimo")
	private Integer pulsoMinimo;
	
	@Column(name = "desnivel")
	private Integer desnivel;
	
	@Column(name = "numero_vuelta") 
    private Integer numeroVuelta;
	
	
	
	

}
