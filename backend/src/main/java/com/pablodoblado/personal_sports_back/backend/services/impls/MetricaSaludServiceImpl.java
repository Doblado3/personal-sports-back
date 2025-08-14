package com.pablodoblado.personal_sports_back.backend.services.impls;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.pablodoblado.personal_sports_back.backend.entities.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
import com.pablodoblado.personal_sports_back.backend.mappers.MetricaSaludMapper;
import com.pablodoblado.personal_sports_back.backend.models.MetricaSaludResponseDTO;
import com.pablodoblado.personal_sports_back.backend.repositories.MetricaSaludRepository;
import com.pablodoblado.personal_sports_back.backend.repositories.UsuarioRepository;
import com.pablodoblado.personal_sports_back.backend.services.MetricaSaludService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricaSaludServiceImpl implements MetricaSaludService {
	
	private final MetricaSaludRepository metricaSaludRepository;
	
	private final UsuarioRepository usuarioRepository;
	
	private final MetricaSaludMapper metricaSaludMapper;
	
	@Override
	public MetricaSaludResponseDTO saveMetricaDiaria(UUID idUsuario, MetricaSalud metricaSalud) throws NotFoundException {
		
		Usuario usuario = searchForUser(idUsuario);
		
		if(metricaSaludRepository.findByUsuarioAndFechaRegistro(usuario, metricaSalud.getFechaRegistro()).isPresent()) {
			
			log.warn("Intento de guardar una instancia del objeto MetricaSalud ya existente en la base de datos");
			throw new IllegalStateException("A daily metric for this user and date already exists");
		}
		
		log.info("Guardando nueva instancia del objeto MetricaSalud");
		metricaSalud.setUsuario(usuario);
		return metricaSaludMapper.metricaSaludToMetricaSaludResponseDTO(metricaSaludRepository.save(metricaSalud));
		
	}
	
	@Override
	public Optional<MetricaSaludResponseDTO> updateMetricaSalud(UUID idUsuario, MetricaSalud metricaSalud) throws NotFoundException{
		
		Usuario usuario = searchForUser(idUsuario);
		
		return metricaSaludRepository.findByUsuarioAndFechaRegistro(usuario, metricaSalud.getFechaRegistro())
				.map(copia -> {
					
					copia.setUsuario(usuario);
					copia.setHorasSuenoHours(metricaSalud.getHorasSuenoHours());
					copia.setHorasSuenoMinutes(metricaSalud.getHorasSuenoMinutes());
					copia.setPeso(metricaSalud.getPeso());
					copia.setCalidadSueno(metricaSalud.getCalidadSueno());
					copia.setHrvRmssd(metricaSalud.getHrvRmssd());
					copia.setHrvSdnn(metricaSalud.getHrvSdnn());
					copia.setCardiacaReposo(metricaSalud.getCardiacaReposo());
					copia.setEstresSubjetivo(metricaSalud.getEstresSubjetivo());
					copia.setDescansoSubjetivo(metricaSalud.getDescansoSubjetivo());
					copia.setEstresMedido(metricaSalud.getEstresMedido());
					copia.setIncidencias(metricaSalud.getIncidencias());
					log.info("Actualizando instancia del objeto MetricaSalud");
					return metricaSaludMapper.metricaSaludToMetricaSaludResponseDTO(metricaSaludRepository.save(copia));
					
				});
		
	}
	
	
	
	@Override
	public Optional<MetricaSalud> getRegistroByUsuarioAndDate(UUID idUsuario, LocalDate fechaRegistro) throws NotFoundException{
		
		Usuario usuario = searchForUser(idUsuario);
		
		return metricaSaludRepository.findByUsuarioAndFechaRegistro(usuario, fechaRegistro);
	}
	
	@Override
	public Optional<List<MetricaSalud>> getAllRegistrosForUsuario(UUID idUsuario) throws NotFoundException{
		
		Usuario usuario = searchForUser(idUsuario);
		
		return Optional.of(metricaSaludRepository.findByUsuarioOrderByFechaRegistro(usuario));
		
	}
	
	@Override
	public Page<MetricaSalud> getPaginatedRegistrosForUsuario(UUID idUsuario, Pageable pageable, String filter) {
		
		if (filter != null && !filter.trim().isEmpty()) {
			
			log.info("Filtrando resultados de metricas de salud");
		
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
			
			log.info("Devolviendo registros de metricas de salud paginados sin filtrar");
			
			return metricaSaludRepository.findByUsuarioId(idUsuario, pageable);
		}
	}
	
	@Override
	public Optional<List<MetricaSalud>> getRegistrosDiariosByUserInRange(UUID idUsuario, LocalDate starDate, LocalDate endDate) throws NotFoundException{
		
		Usuario usuario = searchForUser(idUsuario);
		
		return Optional.of(metricaSaludRepository.findByUsuarioAndFechaRegistroBetween(usuario, starDate, endDate));
		
	}
	
	@Override
	public Boolean deleteRegistroMetrica(UUID usuarioId, LocalDate fechaRegistro) throws NotFoundException {
		
		Usuario usuario = searchForUser(usuarioId);

		Optional<MetricaSalud> metricaOpt = metricaSaludRepository.findByUsuarioAndFechaRegistro(usuario, fechaRegistro);
		if(metricaOpt.isPresent()) {
			metricaSaludRepository.delete(metricaOpt.get());
			return true;
		}
		
		return false;
	}
	
	private Usuario searchForUser(UUID idUsuario) throws NotFoundException {
		
		return usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new NotFoundException());
		
	}
	
	

}
