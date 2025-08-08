package com.pablodoblado.personal_sports_back.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.pablodoblado.personal_sports_back.backend.entity.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import com.pablodoblado.personal_sports_back.backend.repository.MetricaSaludRepository;
import com.pablodoblado.personal_sports_back.backend.repository.UsuarioRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class MetricaSaludService {
	
	private final MetricaSaludRepository metricaSaludRepository;
	
	private final UsuarioRepository usuarioRepository;
	
	@Autowired
	public MetricaSaludService(MetricaSaludRepository metricaSaludRepository, UsuarioRepository usuarioRepository) {
		this.metricaSaludRepository = metricaSaludRepository;
		this.usuarioRepository = usuarioRepository;
	}
	
	public MetricaSalud saveOrUpdateMetricaDiaria(UUID idUsuario, MetricaSalud registro) {
		
		//La fecha de registro se pasa en el Body, por si el usuario quiere introducir datos para fechas pasadas
		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new IllegalArgumentException("No se ha encontrado el usuario con id :" + idUsuario));
		
		Optional<MetricaSalud> registroExistente = metricaSaludRepository.findByUsuarioAndFechaRegistro(usuario, registro.getFechaRegistro());
		
		//Actualizamos el registro si ya existe
		if(registroExistente.isPresent()) {
			MetricaSalud copia = registroExistente.get();
			copia.setHorasSuenoHours(registro.getHorasSuenoHours());
			copia.setHorasSuenoMinutes(registro.getHorasSuenoMinutes());
			copia.setPeso(registro.getPeso());
			copia.setCalidadSueno(registro.getCalidadSueno());
			copia.setHrvRmssd(registro.getHrvRmssd());
			copia.setHrvSdnn(registro.getHrvSdnn());
			copia.setCardiacaReposo(registro.getCardiacaReposo());
			copia.setEstresSubjetivo(registro.getEstresSubjetivo());
			copia.setDescansoSubjetivo(registro.getDescansoSubjetivo());
			copia.setEstresMedido(registro.getEstresMedido());
			copia.setIncidencias(registro.getIncidencias());
			return metricaSaludRepository.save(copia);
			
		} else {
			//Asociamos el nuevo registro al usuario
			registro.setUsuario(usuario);
			return metricaSaludRepository.save(registro);
		}
		
	}
	
	public Optional<MetricaSalud> getRegistroByUsuarioAndDate(UUID idUsuario, LocalDate fechaRegistro){
		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new IllegalArgumentException("No se ha podido encontrar el usuario con id: " + idUsuario));
		return metricaSaludRepository.findByUsuarioAndFechaRegistro(usuario, fechaRegistro);
	}
	
	public List<MetricaSalud> getAllRegistrosForUsuario(UUID idUsuario){
		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new IllegalArgumentException("No se ha podido encontrar el usuario con id: " + idUsuario));
		return metricaSaludRepository.findByUsuarioOrderByFechaRegistro(usuario);
		
	}
	
	public Page<MetricaSalud> getPaginatedRegistrosForUsuario(UUID idUsuario, Pageable pageable, String filter) {
		if (filter != null && !filter.trim().isEmpty()) {
		//Automatizacion por parte de Spring Data
		Specification<MetricaSalud> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			
			predicates.add(criteriaBuilder.equal(root.get("usuario").get("id"), idUsuario));
			
			String lowerCaseFilter = filter.trim().toLowerCase();
			Predicate filterPredicate = criteriaBuilder.or(
						
	                   criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.function("STR", String.class, root.get("fechaRegistro"))), "%" + lowerCaseFilter + "%"),                    
	                   criteriaBuilder.like(criteriaBuilder.lower(root.get("calidadSueno")), "%" + lowerCaseFilter + "%"),	       
	                   criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.function("STR", String.class,root.get("descansoSubjetivo"))), "%" + lowerCaseFilter + "%")
	                    
	               );
	               predicates.add(filterPredicate);

			
	               return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		
			
			};
		
			return metricaSaludRepository.findAll(spec, pageable);
		
		} else {
			
			return metricaSaludRepository.findByUsuarioId(idUsuario, pageable);
		}
	}
	
	public List<MetricaSalud> getRegistrosDiariosByUserInRange(UUID idUsuario, LocalDate starDate, LocalDate endDate){
		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new IllegalArgumentException("No se ha podido encontrar el usuario con id: " + idUsuario));
		return metricaSaludRepository.findByUsuarioAndFechaRegistroBetween(usuario, starDate, endDate);
		
	}
	
	public void deleteRegistroMetrica(UUID usuarioId, LocalDate fechaRegistro) {
		//TO-DO: Implementar logica de autenticacion
		metricaSaludRepository.deleteByFechaRegistro(fechaRegistro);
	}
	
	

}
