package com.pablodoblado.personal_sports_back.backend.chatControllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pablodoblado.personal_sports_back.backend.services.impls.RagService;

@RestController
@RequestMapping("/api/nutritionRag")
public class NutritionRagController {
	
	private final RagService ragService;
	
	public NutritionRagController(RagService ragService) {
		this.ragService = ragService;
	}
	
	@GetMapping("/ask")
	public String askQuestion(@RequestParam String query) {
		return ragService.nutritionBooksAnswer(query);
	}

}
