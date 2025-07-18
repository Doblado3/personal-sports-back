package com.pablodoblado.personal_sports_back.backend.chatControllers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nutritionChat")
public class NutritionChatController {
	
	private final ChatClient chatClient;
	
	@Value("classpath:/images/foto_comida_1.jpg")
	Resource first_image;
	
	public NutritionChatController(ChatClient.Builder builder) {
		this.chatClient = builder.build();
	}
	
	
	//Probar con platos reales mios
	@GetMapping("/getCalories")
	public String getCalories() {
		return chatClient.prompt()
				.user(u -> {
					u.text("Could you tell me how many calories, grams of carbohidrates, proteins and fat have this dish?");
					u.media(MimeTypeUtils.IMAGE_JPEG, first_image);
				})
				.call()
				.content();
	}
	
	//Probando RAG con libros de nutricion
	
	
	
	

}
