package com.pablodoblado.personal_sports_back.backend.services;


import java.util.concurrent.CompletableFuture;

import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;


public interface AemetService {
	
	CompletableFuture<TrainingActivity> getValoresClimatologicosRangoFechas(TrainingActivity trainingActivity);
	

}
