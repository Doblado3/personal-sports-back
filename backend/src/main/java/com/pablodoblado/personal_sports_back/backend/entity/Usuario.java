package com.pablodoblado.personal_sports_back.backend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data //Lombok genera getters, setters, toString, equals y hashCode
@NoArgsConstructor
@AllArgsConstructor
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
	
	//Llamadas LifeCycle para los TimeStamps
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
