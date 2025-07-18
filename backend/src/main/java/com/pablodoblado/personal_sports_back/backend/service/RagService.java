package com.pablodoblado.personal_sports_back.backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagService {
	
	private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

	
	private final ChatClient chatClient;
	private final VectorStore vectorStore;
	
	public RagService(ChatClient.Builder builder, VectorStore vectorStore) {
		this.chatClient = builder.build();
		this.vectorStore = vectorStore;
	}
	
	public String nutritionBooksAnswer(String question) {
		log.info("User question: " + question);
		
		//Retrieval
		
		List<Document> similarDocs = vectorStore.similaritySearch(
				SearchRequest.builder()
					.query(question)
					.topK(5)
					.similarityThreshold(0.2)
					.build()
		);
		
		if (similarDocs.isEmpty()) {
			log.info("No relevant chunks were found for question: " + question);
            return "I'm sorry, I couldn't find any relevant information in the documents to answer your question.";

		}
		
		String relevantText = similarDocs.stream()
				.map(Document::getText)
				.collect(Collectors.joining(System.lineSeparator()));
		
		similarDocs.forEach(doc -> log.info("Chunk ID: " + doc.getId() + ", Content: " + doc.getText().substring(0, Math.min(doc.getText().length(), 100)) + "..."));
		
		//Augment the prompt
		PromptTemplate promptTemplate = new PromptTemplate("""
                You are a nutritional coach.
                Based on the following context, answer the question thoroughly and concisely.
                If the answer is not available in the context, state that you don't have enough information.
                Do not make up answers.

                Context:
                {context}

                Question: {question}
                """);
		
		Prompt p = promptTemplate.create(
				Map.of("context", relevantText,
						"question", question)
				);

        

        // 3. Generate the answer using the LLM (Ollama chat model)
        System.out.println("Sending augmented prompt to LLM...");
        String response = chatClient.prompt(p).call().content();
        System.out.println("LLM Response received.");

        return response;
    
		
	}

}
