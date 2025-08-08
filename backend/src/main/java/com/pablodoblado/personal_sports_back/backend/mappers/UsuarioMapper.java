package com.pablodoblado.personal_sports_back.backend.mappers;

import org.mapstruct.Mapper;

import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.models.UsuarioRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.UsuarioResponseDTO;

@Mapper
public interface UsuarioMapper {
	
	Usuario usuarioRequestDTOtoUsuario(UsuarioRequestDTO usuarioRequestDTO);
	
	UsuarioResponseDTO usuarioToUsuarioResponseDTO(Usuario usuario);

}
