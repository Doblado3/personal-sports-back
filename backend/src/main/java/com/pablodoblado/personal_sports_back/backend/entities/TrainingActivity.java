package com.pablodoblado.personal_sports_back.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;

import jakarta.persistence.*;

@Entity
@Table(name = "trainingactivity")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TrainingActivity {
	
	// El id de la actividad se obtiene del id de Strava
	@Id
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="id_usuario", nullable = false)
	private Usuario usuario;
	
	
	@Column(name = "nombre", nullable = false, length = 255)
	private String nombre;
	
	//metros
	@Column(name = "distancia")
	private Double distancia;
	
	@Column(name = "tipo", nullable = false)
	@Enumerated(EnumType.STRING)
	private TipoActividad tipo;
	
	//segundos
	@Column(name = "tiempo_total")
	private Integer tiempoTotal;
	
	@Column(name = "tiempo_activo")
	private Integer tiempoActivo;
	
	//metros
	@Column(name = "desnivel")
	private Double desnivel;
	
	@Column(name = "max_altitud")
	private Double maxAltitud;
	
	@Column(name = "min_altitud")
	private Double minAltitud;
	
	
	@Column(name = "temperatura")
	private Double temperatura;
	
	@Column(name = "viento")
	private Double viento;
	
	@Column(name = "humedad")
	private Double humedad;
	
	@Column(name = "lluvia")
	private Boolean lluvia;
	
	
	@Column(name = "fecha_comienzo", nullable = false)
	private LocalDateTime fechaComienzo;
	
	//Introducir mediante cuestionario
	@Column(name = "rpe_objetivo")
	private String rpeObjetivo;
	
	@Column(name = "rpe_Real")
	private String rpeReal;
	
	// m/s
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
	
	@Column(name = "start_latlng")
	private Double startLatlng;
	
	
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


