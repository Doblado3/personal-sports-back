package com.pablodoblado.personal_sports_back.backend.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id")
	private UUID id;
	
	@Column(name = "strava_athlete_id", unique = true)
	private Long stravaAthleteId;
	
	@Column(name = "strava_access_token", length = 512)
    private String stravaAccessToken;

    @Column(name = "strava_refresh_token", length = 512)
    private String stravaRefreshToken;

    @Column(name = "strava_token_expires_at") 
    private Long stravaTokenExpiresAt;
	
	@Column(name = "email", unique = true, nullable = false, length = 255)
	private String email;
	
	@Column(name = "password", nullable = false, length = 255)
	private String password;
	
	@Column(name = "nombre", nullable = false, length = 100)
	private String nombre;
	
	@Column(name = "apellidos", length = 100)
	private String apellidos;
	
	@Column(name = "fecha_nacimiento", nullable = false)
	private LocalDateTime fechaNacimiento;
	
	@Column(name = "genero", length = 10)
	private String genero;
	
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
	
	@Builder.Default
	@OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
	private Set<MetricaSalud> metricas = new HashSet<>();
	
	public void addMetrica(MetricaSalud metrica) {
		this.metricas.add(metrica);
		metrica.setUsuario(this);
	}
	
	public void removeMetrica(MetricaSalud metrica) {
		this.metricas.remove(metrica);
		metrica.setUsuario(null);
	}
	
	@Builder.Default
	@OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
	private Set<TrainingActivity> actividades = new HashSet<>();
	
	public void addActivity(TrainingActivity activity) {
		this.actividades.add(activity);
		activity.setUsuario(this);
	}
	
	public void removeActivity(TrainingActivity activity) {
		this.actividades.remove(activity);
		activity.setUsuario(null);
	}




}
