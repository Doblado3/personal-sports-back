package com.pablodoblado.personal_sports_back.backend.specifications;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;

import jakarta.persistence.criteria.Predicate;

public class TrainingActivitySpecifications {
	
	public static Specification<TrainingActivity> findByDiaTipoZonas(LocalDateTime dia, TipoActividad tipo, Double minZoneRange, Double maxZoneRange) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dia != null) {
                predicates.add(criteriaBuilder.equal(root.get("fechaComienzo"), dia));
            }

            if (tipo != null) {
                predicates.add(criteriaBuilder.equal(root.get("tipo"), tipo));
            }

            if (minZoneRange != null && maxZoneRange != null) {
                predicates.add(criteriaBuilder.between(root.get("pulsoMedio"), minZoneRange, maxZoneRange));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
