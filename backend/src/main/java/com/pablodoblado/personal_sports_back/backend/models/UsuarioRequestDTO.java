package com.pablodoblado.personal_sports_back.backend.models;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioRequestDTO {
	
	@NotBlank
	@NotNull
    private String email;
	
	@NotNull
	@NotBlank
    private String password;
	
	@NotNull
	@NotBlank
    private String nombre;
    private String apellidos;
    
    @NotNull
    private LocalDateTime fechaNacimiento;
    private String genero;
    
}
