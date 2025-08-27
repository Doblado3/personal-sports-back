package com.pablodoblado.personal_sports_back.backend.services;

import com.pablodoblado.personal_sports_back.backend.entities.Usuario;

public interface StravaTokenService {
	
	Usuario refreshToken(Usuario usuario);

}
