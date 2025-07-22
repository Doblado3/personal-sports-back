package com.pablodoblado.personal_sports_back.backend.dto.StravaApi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StravaTokenResponse {
	
	private String accessToken;
	private String refreshToken;
	private Long expiresAt;

}
