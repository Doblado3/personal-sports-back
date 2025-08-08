package com.pablodoblado.personal_sports_back.backend.models;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioRequestDTO {

    private String email;
    private String password;
    private String nombre;
    private String apellidos;
    private LocalDateTime fechaNacimiento;
    private String genero;
    
}
