package com.pablodoblado.personal_sports_back.backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pablodoblado.personal_sports_back.backend.entities.DocumentEntity;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Integer> {
	Optional<DocumentEntity> findByFilePath(String filePath);

}
