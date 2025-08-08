package com.pablodoblado.personal_sports_back.backend.entity;

import java.util.Objects;

import jakarta.persistence.*;
import lombok.*;

//Spring Data JPA composite primary key

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVectorStoreId {
	
	@Column(name = "vector_store_id", nullable = false)
	private String vectorStoreId;
	
	@Column(name = "document_id", nullable = false)
	private Integer documentId;
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentVectorStoreId that = (DocumentVectorStoreId) o;
        return Objects.equals(vectorStoreId, that.vectorStoreId) &&
               Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vectorStoreId, documentId);
    }

}
