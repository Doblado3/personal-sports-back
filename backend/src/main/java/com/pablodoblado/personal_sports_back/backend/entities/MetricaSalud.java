package com.pablodoblado.personal_sports_back.backend.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "metricasalud", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"id_usuario", "fecha_registro"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetricaSalud {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id")
	private UUID id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", nullable = false)
	private Usuario usuario;
	
	@Column(name = "fecha_registro", nullable = false)
	private LocalDate fechaRegistro;
	
	@Column(name = "horas_sueno_hours")
	private Integer horasSuenoHours;

	@Column(name = "horas_sueno_minutes")
	private Integer horasSuenoMinutes;
	
	@Column(name = "peso")
	private Double peso;
	
	
	@Column(name = "calidad_sueño")
	private String calidadSueno;
	
	@Column(name = "hrv_rmssd")
	private Double hrvRmssd;
	
	@Column(name = "hrv_sdnn")
	private Double hrvSdnn;
	
	@Column(name = "cardiaca_reposo")
	private Integer cardiacaReposo;
	
	//Escala del 1 al 5
	@Column(name = "descanso_subjetivo")
	private Integer descansoSubjetivo;
	
	//Escala del 1 al 5
	@Column(name = "estres_subjetivo")
	private Integer estresSubjetivo;
	
	@Column(name = "incidencias", columnDefinition = "TEXT")
	private String incidencias;
	
	@Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "estres_medido")
    private Integer estresMedido;

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
