package com.pablodoblado.personal_sports_back.backend.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DocumentContentDTO extends DocumentDTO {
	
	private String downloadUrl;

}
