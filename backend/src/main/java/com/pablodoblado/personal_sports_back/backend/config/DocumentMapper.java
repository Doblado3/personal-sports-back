package com.pablodoblado.personal_sports_back.backend.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.pablodoblado.personal_sports_back.backend.dto.DocumentDTO;
import com.pablodoblado.personal_sports_back.backend.entity.DocumentEntity;

@Component
public class DocumentMapper {
	
	public DocumentDTO toDto(DocumentEntity entity) {
		if (entity == null) {
			return null;
		}
		
		DocumentDTO dto = new DocumentDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setSize(entity.getSize());
		dto.setType(entity.getType());
		dto.setUploadedAt(entity.getUploadedAt());
		return dto;
		
	}
	
	public List<DocumentDTO> toDtoList(List<DocumentEntity> entities) {
		return entities.stream()
				.map(this::toDto)
				.toList();
	}
	
	//public DocumentEntity toEntity(DocumentDTO dto {...}

}
