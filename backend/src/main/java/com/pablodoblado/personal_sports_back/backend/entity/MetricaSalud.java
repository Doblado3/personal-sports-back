package com.pablodoblado.personal_sports_back.backend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

//Añadimos una restricción para que un usuario solo añada un registro por día
@Entity
@Table(name = "metricasalud", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"id_usuario", "fecha_registro"})
})
@Data
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
	
	@Column(name = "horas_sueño")
	private Double horasSueño;
	
	//Del 0 al 10
	@Column(name = "calidad_sueño")
	private Integer calidadSueño;
	
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
