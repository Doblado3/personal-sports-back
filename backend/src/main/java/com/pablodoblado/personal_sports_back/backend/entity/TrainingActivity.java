package com.pablodoblado.personal_sports_back.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

import com.pablodoblado.personal_sports_back.backend.entity.enums.TipoActividad;

import jakarta.persistence.*;

@Entity
@Table(name = "trainingactivity")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TrainingActivity {
	
	// No asignamos estrategia de generación ya que lo obtendremos de la API de Strava
	@Id
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="id_usuario", nullable = false)
	private Usuario usuario;
	
	//Nombre asociado a la actividad
	@Column(name = "nombre", nullable = false, length = 255)
	private String nombre;
	
	//Distance of the activity in meters
	@Column(name = "distancia")
	private Double distancia;
	
	@Column(name = "tipo", nullable = false)
	@Enumerated(EnumType.STRING)
	private TipoActividad tipo;
	
	@Column(name = "tiempo_total")
	private Integer tiempoTotal;
	
	@Column(name = "tiempo_activo")
	private Integer tiempoActivo;
	
	@Column(name = "desnivel")
	private Double desnivel;
	
	@Column(name = "max_altitud")
	private Double maxAltitud;
	
	@Column(name = "min_altitud")
	private Double minAltitud;
	
	/* Condiciones meteorológicas */
	@Column(name = "temperatura")
	private Double temperatura;
	
	@Column(name = "viento")
	private Double viento;
	
	@Column(name = "humedad")
	private Integer humedad;
	
	@Column(name = "lluvia")
	private Boolean lluvia;
	
	//La fecha final se puede obtener con el tiempo de actividad
	@Column(name = "fecha_comienzo", nullable = false)
	private LocalDateTime fechaComienzo;
	
	//Introducir mediante cuestionario
	@Column(name = "rpe_objetivo")
	private String rpeObjetivo;
	
	@Column(name = "rpe_Real")
	private String rpeReal;
	
	//Ritmo
	@Column(name = "velocidad_media")
	private Double velocidadMedia;
	
	@Column(name = "velocidad_maxima")
	private Double velocidadMaxima;
	
	@Column(name = "feedback", columnDefinition = "TEXT")
	private String feedback;
	
	@Column(name = "hidratos")
	private Double hidratos;
	
	@Column(name = "litros_agua")
	private Double litrosAgua;
	
	@Column(name = "pulso_medio")
	private Double pulsoMedio;
	
	@Column(name = "pulso_maximo")
	private Double pulsoMaximo;
	
	
	@OneToMany(mappedBy="trainingActivity", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Vuelta> vueltas;
	
	@Column(name = "kilo_julios")
	private Double kiloJulios;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
	
	
	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

}


