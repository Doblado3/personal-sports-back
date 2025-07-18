package com.pablodoblado.personal_sports_back.backend.chatControllers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pablodoblado.personal_sports_back.backend.tools.MetricaSaludTools;

import reactor.core.publisher.Flux;

@RestController
public class GenericChatController {
	
	private final ChatClient chatClient;
	
	private final MetricaSaludTools metricaSaludTools;
	
	@Autowired
	public GenericChatController(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, MetricaSaludTools metricaSaludTools) {
		this.chatClient = chatClientBuilder
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
				.build();
		this.metricaSaludTools = metricaSaludTools;
	}
	
	
	
	@GetMapping("/chat")
	public String chat() {
		
		PromptTemplate pt = new PromptTemplate("""
				<start_of_turn>user Hello World!<end_of_turn><start_of_turn>model
				""");
		
		return chatClient.prompt(pt.create())
				.call()
				.content();
	}
	
	@GetMapping("/stream")
	public Flux<String> stream() {
		PromptTemplate pt = new PromptTemplate("""
				<start_of_turn>user 
				Can you give me 3 ideas of travelling plans for the next month?<end_of_turn>
				<start_of_turn>model
				""");
		
		return chatClient.prompt(pt.create())
				.stream()
				.content();
	}
	
	@GetMapping("/format")
	public ChatResponse format() {
		PromptTemplate pt = new PromptTemplate("""
				<start_of_turn>user
				Could you plan my diet for today?<end_of_turn>
				<start_of_turn>model
				""");
		
		return chatClient.prompt(pt.create())
				.call()
				.chatResponse();
	}
	
	@GetMapping("/memory")
	public String memory(@RequestParam String message) {
		
		UserMessage userMessage = new UserMessage(message);
		Prompt prompt = new Prompt(userMessage);
		
		return chatClient.prompt(prompt)
				.call()
				.content();
	}
	
	//gemma3:12b does not support tools
	@GetMapping("/tools")
	public String tools() {
		return chatClient.prompt()
				.user("How is my total sleep evolution for this week going?")
				.tools(metricaSaludTools)
				.call()
				.content();
	}

}
