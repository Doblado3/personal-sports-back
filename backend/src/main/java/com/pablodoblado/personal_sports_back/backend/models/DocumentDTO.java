package com.pablodoblado.personal_sports_back.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
	
	private Integer id;
	private String name;
	private String type;
	private Long size;
	private LocalDateTime uploadedAt;

}
