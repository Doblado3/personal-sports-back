package com.pablodoblado.personal_sports_back.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pablodoblado.personal_sports_back.backend.entities.DocumentVectorStoreEntity;
import com.pablodoblado.personal_sports_back.backend.entities.DocumentVectorStoreId;

@Repository
public interface DocumentVectorStoreRepository extends JpaRepository<DocumentVectorStoreEntity, DocumentVectorStoreId> {

}
