package com.pablodoblado.personal_sports_back.backend.services.impls;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pablodoblado.personal_sports_back.backend.entities.DocumentEntity;
import com.pablodoblado.personal_sports_back.backend.entities.DocumentVectorStoreEntity;
import com.pablodoblado.personal_sports_back.backend.entities.DocumentVectorStoreId;
import com.pablodoblado.personal_sports_back.backend.repositories.DocumentRepository;
import com.pablodoblado.personal_sports_back.backend.repositories.DocumentVectorStoreRepository;

import jakarta.annotation.PostConstruct;

@Service
public class DocumentService {
	
	private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
	
	private final DocumentRepository documentRepository;
	private final DocumentVectorStoreRepository documentVectorStoreRepository;
	private final VectorStore vectorStore;
	private final ResourceLoader resourceLoader;
	
	@Value("classpath:/data/advanced-sports-nutrition-2nd-edition-9781450401616-1450401619_compress.pdf")
	private Resource pdf1Resource;
	
	@Value("classpath:/data/Nancy_Clark_s_Sports_Nutrition_Guidebook_-__Malestrom_.pdf")
	private Resource pdf2Resource;
	
	@Autowired
	public DocumentService(DocumentRepository documentRepository, DocumentVectorStoreRepository documentVectorStoreRepository,
			VectorStore vectorStore, ResourceLoader resourceLoader) {
		this.documentRepository = documentRepository;
		this.documentVectorStoreRepository = documentVectorStoreRepository;
		this.vectorStore = vectorStore;
		this.resourceLoader = resourceLoader;
		
	}
	
	@PostConstruct
	@Transactional
	//TO-DO: Automatizar el proceso de carga por parte del usuario en el cliente
	public void ingestAllConfiguredDocuments() {
		
		List<Resource> pdfResources = new ArrayList<>();
		pdfResources.add(pdf1Resource);
		pdfResources.add(pdf2Resource);
		
		for (Resource pdfResource : pdfResources) {
            try {
            	
                ingestDocument(pdfResource);
                
            } catch (IOException e) {
            	
                log.warn("Failed to ingest document " + pdfResource.getFilename() + ": " + e.getMessage());
                
            }
        }
        System.out.println("All configured documents ingestion complete.");
	}
	
	private void ingestDocument(Resource pdfResource) throws IOException {
		
		String filePath = pdfResource.getURI().toString();
		String filename = pdfResource.getFilename();
		
		Optional<DocumentEntity> existingDocument = documentRepository.findByFilePath(filePath);
		
		if(existingDocument.isPresent()) {
			log.warn("Document already ingested");
			return;
		}
		
		
		//Document Metadata
		DocumentEntity documentEntity = new DocumentEntity();
		documentEntity.setName(filename);
		documentEntity.setType(Files.probeContentType(Paths.get(pdfResource.getURI()))); //MIME type
		documentEntity.setSize(pdfResource.contentLength());
		documentEntity.setUploadedAt(LocalDateTime.now());
		documentEntity.setFilePath(filePath);
		documentEntity = documentRepository.save(documentEntity); //Need the id
		log.info("DocumentEntity with id: " + documentEntity.getId() + "saved.");
		
		//Spring AI's PdfDocumentReader
		PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource);
		List<Document> rawDocuments = pdfReader.get();
		
		//Splitting phase
		
		int chunkSize = 1024;
		int minChunkSizeChars = 500; 
		int minChunkLenghtToEmbed = 1; //Discard tokens shorter than this
		int maxNumChunks = 10000;
		boolean keepSeparator = true;
		
		TokenTextSplitter textSplitter = new TokenTextSplitter(
				chunkSize,
				minChunkSizeChars,
				minChunkLenghtToEmbed,
				maxNumChunks,
				keepSeparator
		);
		
		log.info("Splitting chunks...");
		List<Document> chunks = textSplitter.apply(rawDocuments);
		
		for (int i = 0; i < chunks.size(); i++) {
			
            Document chunk = chunks.get(i);
            chunk.getMetadata().put("document_id", documentEntity.getId());
            chunk.getMetadata().put("document_name", documentEntity.getName());
            chunk.getMetadata().put("chunk_index", i); 
        }
		
		//Aquí se generan los embeddings antes del almacenamiento
		log.info("Storing Chunks...");
		vectorStore.add(chunks);
		
		List<DocumentVectorStoreEntity> documentVectorStoreEntities = new ArrayList<>();
        for (Document chunk : chunks) {
        	
            DocumentVectorStoreEntity dse = new DocumentVectorStoreEntity();
            dse.setId(new DocumentVectorStoreId(chunk.getId(), documentEntity.getId()));
            dse.setDocument(documentEntity); 
            dse.setChunkIndex((Integer) chunk.getMetadata().get("chunk_index")); 
            documentVectorStoreEntities.add(dse);
        }
        documentVectorStoreRepository.saveAll(documentVectorStoreEntities);
        log.info("Document ingestion for " + filename + "succesfully completed.");
	}

}
