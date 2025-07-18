package com.pablodoblado.personal_sports_back.backend.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.pablodoblado.personal_sports_back.backend.entity.enums.TipoActividad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "cyclingactivity")
@Data
@NoArgsConstructor
@SuperBuilder
public class CyclingActivity extends TrainingActivity {
		
	@Column(name = "potenciometro", nullable = false)
	private Boolean potenciometro;
		
	@Column(name = "cadencia")
	private Double cadencia;
		
	@Column(name = "kilo_julios")
	private Double kiloJulios;
		
	@Column(name = "vatios_medios")
	private Double vatiosMedios;
		
	@Column(name = "vatios_maximos")
	private Double vatiosMaximos;
	
	public CyclingActivity(Long id, Usuario usuario, String nombre, Double distancia,
            TipoActividad tipo, Integer tiempoTotal, Integer tiempoActivo, Integer desnivel, Double maxAltitud,
            Double minAltitud, Double temperatura, Double viento, Integer humedad, Boolean lluvia,
            LocalDateTime fechaComienzo, String rpeObjetivo, String rpeReal, Double velocidadMedia, Double velocidadMaxima, 
            String feedback, Double calorias, Double hidratos, Double litrosAgua, Integer pulsoMedio, Integer pulsoMaximo, Integer pulsoMinimo, 
            List<Vuelta> vueltas, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean potenciometro, Double cadencia, Double kiloJulios, 
            Double vatiosMedios, Double vatiosMaximos) {

		super(id, usuario, nombre, distancia, tipo, tiempoTotal, tiempoActivo, desnivel, maxAltitud, minAltitud,
		      temperatura, viento, humedad, lluvia, fechaComienzo,  rpeObjetivo, rpeReal, velocidadMedia, velocidadMaxima, 
		      feedback, calorias, hidratos, litrosAgua, pulsoMedio, pulsoMaximo, pulsoMinimo, vueltas, createdAt, updatedAt);
		
		this.potenciometro = potenciometro;
		this.cadencia = cadencia;
		this.kiloJulios = kiloJulios;
		this.vatiosMedios = vatiosMedios;
		this.vatiosMaximos = vatiosMaximos;
	}
	


}
