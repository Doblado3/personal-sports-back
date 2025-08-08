package com.pablodoblado.personal_sports_back.backend.models;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioResponseDTO {

    private UUID id;
    private Long stravaAthleteId;
    private String email;
    private String nombre;
    private String apellidos;
    private String genero;
    private LocalDateTime fechaNacimiento;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
