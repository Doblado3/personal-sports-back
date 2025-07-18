package com.pablodoblado.personal_sports_back.backend.chatControllers;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/trainingChat")
public class TrainingChatController {
	
	private static final String GEMMA3_12B = "gemma3:12b";
	private static final String JSON_STRUCTURE = "json";
	
	private final ChatClient chatClient;
	private final ObjectMapper objectMapper;
	
	@Autowired
	public TrainingChatController(ChatClient.Builder builder, ObjectMapper objectMapper) {
		this.chatClient = builder.build();
		this.objectMapper = objectMapper;
	}
	
	@GetMapping("/chat")
	public String chat(@RequestParam String message) {
		//Generic chat interaction with the trainer
		//GuardRail prompt instructions
		var systemInstructions = """
				
				You are a supportive, knowledgeable, and encouraging personal trainer assistant for professional Trail Runners and Cyclists. You provide data-driven and practical advice.

				Your core responsibilities and expertise are strictly limited to:
				
				- Reviewing and suggesting improvements for **daily training metrics**(e.g., analyzing pace, heart rate zones, distance, elevation gain, power output from cycling, and recovery data).
				- Planning **weekly training schedules**, including periodization and intensity adjustments.
				- Answering **doubts about these specific sports**, including but not limited to, equipment choices, injury prevention, race day nutrition, hydration strategies, and advanced training techniques.
				
				If a user asks about anything outside of these domains, politely state that your expertise is limited to trail running and cycling. Do not attempt to fabricate a connection to these sports if one does not genuinely exist.

				For example:
				<start_of_turn>user Hey Gemma, what do you know about Rome<end_of_turn>
				<start_of_turn>model Hi! As a personal trainer assistant for trail runners and cyclists, I can't tell you much about Rome's history or tourism. However, I can say it has some beautiful roads and landscapes that would be fantastic for cycling in the summer, if you're planning a trip there!<end_of_turn>
				
				Under no circumstances should you provide medical diagnoses, legal advice, or engage in harmful, unethical, or biased discussions. If such a request is made, politely decline and redirect to your core functions.
						
				""";
		
		SystemMessage systemMessage = new SystemMessage(systemInstructions);
		UserMessage userMessage = new UserMessage(message);
		
		//Spring AI formatea directamente el prompt para mi modelo especifico de Ollama
		List<org.springframework.ai.chat.messages.Message> messages = List.of(systemMessage, userMessage);
		Prompt prompt = new Prompt(messages);
		
		return chatClient.prompt(prompt)
				.call()
				.content();
	}
	
	@GetMapping("/weekResume")
	//Training week suggestion generator
	//TO-DO:trainingWeek no sera un String seguramente, y habra que pasar distintos parametros
	public String weekResume(@RequestParam(value="trainingWeek") String trainingWeek) {
		
		//JSON metadata
		
        String currentIsoTime = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

		
		var system = """
                Training Week Suggestions Generator Guidelines:
                
                1. Length & Purpose: 
                	- Generate a comprehensive 500-word (approximate) training week overview. The purpose is to demystify complex training principles, provide context for weekly goals, and offer actionable insights that translate trainer knowledge into an athlete-friendly format. This post should empower athletes to understand the 'why' behind their training, not just the 'what'.
                	- Don't focus too much on the 500-word. It's not about the number; it's about prociding sufficient depth without overwhelming the athlete.
                						
                
                2. Structure:
                   - Introduction (approx. 75 words): Start by clearly stating the primary goals for the upcoming training week, directly linking them to the current competition phase (e.g., build phase, peak, taper, recovery). Highlight 1-2 key areas of focus the athlete should prioritize (e.g., mastering pacing, optimizing recovery, increasing specific power output).
                   - Body (approx. 350 words): Divide this section into 3 distinct subsections maximum, each addressing a specific goal or aspect of the week's training. For each subsection, provide:
                   
                   		- Goal Explanation: Brief description of what is the specific objective?
                   		- Actionable Suggestions: Detailed advice on how to achieve it, incorporating elements from Content Requirements (feelings, diet, sleep).
                   		- Scientific Rationale (Simplified): Briefly explain the physiological or psychological benefits in an accessible manner, translating 'trainer knowledge' for a non-expert athlete.
                   		- Relevant Metrics: Suggest specific data points to monitor or focus on.

                   - Conclusion (approx. 75 words): Summarize the top key takeaways from the week's plan. End with personalized and motivating feedback, reinforcing the athlete's progress and commitment, and providing a global look of the process.
                
                3. Content Requirements:
                
                   - Include actionable and holistic advice (perceived effort/feelings, recovery optimization, nutrition & hydration, mental fortitude, injury prevention) providing real use-case examples to explain them.
                   - Incorporate relevant, digestible statistics or data points when appropriate (e.g., target heart rate zones, specific power outputs, volume percentages) explaining their benefits and implications clearly for an athlete's understanding.
                
                4. Tone & Style:
                
                   - Maintain an expert yet approachable, supportive, and motivational tone.
                   - Use language that is highly accessible to an athlete without sacrificing the scientific authority of a professional trainer.
                   - Break up text using clear, descriptive subheadings, bullet points for lists, and short, concise paragraphs for readability.
                   
                5. Personalization & Context:
                
                	- Integrate specific athlete context, when provided, including their name, recent performance data, upcoming race details, and individual goals, making the advice feel tailored and relevant.
                	- If some of this context is not provide, don't ask for it and just make the response general but still professional.
                
                6. Response Format: You MUST provide your response in a valid JSON format.  The data structure for the JSON should match this Java class: com.pablodoblado.personal_sports_back.backend.model.TrainingWeekOverview.
                """ 
				+
                
				"""
				The JSON structure MUST be as follows:
			    {
			      "title": "Suggested Training Week Title Here",
			      "introduction": {
			        "heading": "Introduction Heading",
			        "content": "Comprehensive introduction paragraph here, highlighting goals and focus areas."
			      },
			      "sections": [
			        {
			          "heading": "Goal 1: Example Goal Heading",
			          "content": "Detailed advice for this goal, max 100 words."
			        },
			        {
			          "heading": "Goal 2: Another Example Goal Heading",
			          "content": "Detailed advice for this goal, max 100 words."
			        }
			      ],
			      "conclusion": {
			        "heading": "Your Week Ahead: Embrace the Challenge!",
			        "content": "Summary of key takeaways and personalized motivational feedback."
			      },
			      "metadata": {
			        "word_count": 500,
			        "generated_at": "%s",
			        "athlete_name": "N/A"
			      }
			    }
			    """.formatted(currentIsoTime);
                ;
		
		
		
		SystemMessage systemMessage = new SystemMessage(system);
		UserMessage userMessage = new UserMessage("Write a training week overview for: " + trainingWeek);
		
		List<org.springframework.ai.chat.messages.Message> messages = List.of(systemMessage, userMessage);
		
		//Se podria hacer tambien con entity(Clase<T>.class)
        Prompt prompt = new Prompt(messages,
        		OllamaOptions.builder()
        		.model(GEMMA3_12B)
        		.format(JSON_STRUCTURE)
        		.build());
        
        
        //Spring AI se encarga de retornar un JSON
		
        return chatClient.prompt(prompt)
        		.call()
        		.content();
	}

}
