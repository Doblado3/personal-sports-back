package com.pablodoblado.personal_sports_back.backend.entities;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "name", nullable = false)
	private String name; //nombre del archivo original
	
	@Column(name = "type", nullable = false)
	private String type;
	
	@Column(name = "size")
	private Long size; //bytes
	
	@Column(name = "uploaded_at", nullable = false)
	private LocalDateTime uploadedAt;
	
	@Column(name = "file_path", nullable = false)
	private String filePath; //classpath:/data...
	
	//Join table con las entradas de la tabla de vectores
	@OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<DocumentVectorStoreEntity> documentVectorStoreEntities;

}
