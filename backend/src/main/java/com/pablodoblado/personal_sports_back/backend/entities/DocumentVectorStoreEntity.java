package com.pablodoblado.personal_sports_back.backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "document_vector_store")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVectorStoreEntity {
	
	@EmbeddedId
	private DocumentVectorStoreId id;
	
	// Many-to-one (chunks-documents)
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("documentId")
	@JoinColumn(name = "document_id", referencedColumnName = "id",nullable = false, insertable = false, updatable = false)
	private DocumentEntity document;
	
	@Column(name = "chunk_index")
	private Integer chunkIndex;

}
