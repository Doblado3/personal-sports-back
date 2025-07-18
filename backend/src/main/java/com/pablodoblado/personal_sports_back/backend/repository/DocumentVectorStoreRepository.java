package com.pablodoblado.personal_sports_back.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pablodoblado.personal_sports_back.backend.entity.DocumentVectorStoreEntity;
import com.pablodoblado.personal_sports_back.backend.entity.DocumentVectorStoreId;

@Repository
public interface DocumentVectorStoreRepository extends JpaRepository<DocumentVectorStoreEntity, DocumentVectorStoreId> {

}
