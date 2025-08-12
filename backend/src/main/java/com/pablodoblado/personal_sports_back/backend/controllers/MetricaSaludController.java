package com.pablodoblado.personal_sports_back.backend.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.pablodoblado.personal_sports_back.backend.entities.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.mappers.MetricaSaludMapper;
import com.pablodoblado.personal_sports_back.backend.models.MetricaSaludRequestDTO;
import com.pablodoblado.personal_sports_back.backend.models.MetricaSaludResponseDTO;
import com.pablodoblado.personal_sports_back.backend.services.MetricaSaludService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class MetricaSaludController {
	
	private final MetricaSaludService metricaSaludService;
	
	private final MetricaSaludMapper metricaSaludMapper;
	
	public static final String METRICASALUD_PATH = "/api/registroDiario";
	
	public static final String METRICASALUD_SAVE_PATH = METRICASALUD_PATH + "/save/{idUsuario}";
	
	public static final String METRICASALUD_UPDATE_PATH = METRICASALUD_PATH + "/update/{idUsuario}";
	
	public static final String METRICASALUD_ALL_PATH = METRICASALUD_PATH + "/findAll/{idUsuario}";

	public static final String METRICASALUD_ALL_PAGINATED_PATH = METRICASALUD_PATH + "/findAll/paginated/{idUsuario}";
	
	public static final String METRICASALUD_ALL_FECHA_RANGE_PATH = METRICASALUD_PATH + "/allByDateRange/{usuarioId}/{fechaInicio}/{fechaFin}";
	
	public static final String METRICASALUD_USUARIO_FECHA_PATH = METRICASALUD_PATH + "/findByUsuarioAndFecha/{usuarioId}/{fechaRegistro}";
	
	public static final String METRICASALUD_DELETE_PATH = METRICASALUD_PATH + "/delete/{usuarioId}/{fechaRegistro}";

	
	/**
     * Endpoint to create or update a daily health metric for a specific user.
     * The request body contains the metric data, including the date.
     *
     * @param idUsuario The UUID of the user.
     * @param request   The DTO containing the daily health metric data.
     * @return ResponseEntity with the saved/updated metric (as a DTO) or an error message.
     */
	
	@PostMapping(METRICASALUD_SAVE_PATH)
	public ResponseEntity<?> saveMetricaDiaria(@PathVariable UUID idUsuario, @Validated @RequestBody MetricaSaludRequestDTO registro){
		try {
			
			MetricaSalud entity = metricaSaludMapper.metricaSaludRequestToMetricaSalud(registro);
			MetricaSaludResponseDTO nuevoRegistro = metricaSaludService.saveMetricaDiaria(idUsuario, entity);
			
			HttpHeaders headers = new HttpHeaders();
			headers.add("Location", METRICASALUD_PATH + "/findByUsuarioAndFecha/" + idUsuario + "/" + nuevoRegistro.getFechaRegistro().toString());
			
			return new ResponseEntity<>(headers, HttpStatus.CREATED);
			
		}  catch (IllegalStateException e) {
			
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT); 
            
        }
	}
	
	@PutMapping(METRICASALUD_UPDATE_PATH)
	public ResponseEntity<?> updateMetricaSalud(@PathVariable UUID idUsuario, @Valid @RequestBody MetricaSaludRequestDTO registro) throws NotFoundException{
		
		MetricaSalud entidad = metricaSaludMapper.metricaSaludRequestToMetricaSalud(registro);
		
		Optional<MetricaSaludResponseDTO> update = metricaSaludService.updateMetricaSalud(idUsuario, entidad);
		
		if(update == null || update.isEmpty()) {
			throw new NotFoundException();
		}
		
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		
	}
	
	@GetMapping(METRICASALUD_ALL_PATH)
	public ResponseEntity<?> findAllRegistrosByUser(@PathVariable UUID idUsuario) throws NotFoundException{
			
		List<MetricaSaludResponseDTO> respuesta = metricaSaludService.getAllRegistrosForUsuario(idUsuario)
				.map(registros -> registros.stream()
						.map(metricaSaludMapper::metricaSaludToMetricaSaludResponseDTO)
						.collect(Collectors.toList()))
				.orElseThrow(NotFoundException::new);
				
			
		return new ResponseEntity<>(respuesta, HttpStatus.OK);
			
	}
	
	@GetMapping(METRICASALUD_ALL_PAGINATED_PATH)
	public ResponseEntity<Page<MetricaSaludResponseDTO>> findAllPaginatedRegistrosByUser(
			@PathVariable UUID idUsuario,
			@PageableDefault(page = 0, size = 10, sort = "fechaRegistro,desc") Pageable pageable,
			@RequestParam(required = false) String filter) throws Exception {
		
		
			
		Page<MetricaSalud> registros = metricaSaludService.getPaginatedRegistrosForUsuario(idUsuario, pageable, filter);
		
		if (registros == null || registros.isEmpty()) {
	        throw new NotFoundException();
	    }
			
		Page<MetricaSaludResponseDTO> respuesta = registros.map(registro ->
			
				metricaSaludMapper.metricaSaludToMetricaSaludResponseDTO(registro));
			
		return new ResponseEntity<>(respuesta, HttpStatus.OK);
			
		  
	}
	
	@GetMapping(METRICASALUD_USUARIO_FECHA_PATH)
	public ResponseEntity<?> findByUsuarioAndFechaRegistro(@PathVariable UUID usuarioId, @PathVariable LocalDate fechaRegistro) throws NotFoundException {
		
		Optional<MetricaSalud> optionalMetrica = metricaSaludService.getRegistroByUsuarioAndDate(usuarioId, fechaRegistro);

	    if (optionalMetrica == null || optionalMetrica.isEmpty()) {
	        throw new NotFoundException();
	    }
	    
	    MetricaSalud metrica = optionalMetrica.get();
	    MetricaSaludResponseDTO respuesta = metricaSaludMapper.metricaSaludToMetricaSaludResponseDTO(metrica);
	        
	    return new ResponseEntity<>(respuesta, HttpStatus.OK);
			
		
		
		
	}
	
	@GetMapping(METRICASALUD_ALL_FECHA_RANGE_PATH)
	public ResponseEntity<?> findAllRegistrosByDateRange(@PathVariable UUID usuarioId, @PathVariable LocalDate fechaInicio, @PathVariable LocalDate fechaFin) throws NotFoundException {
		
			
		List<MetricaSaludResponseDTO> respuesta = metricaSaludService.getRegistrosDiariosByUserInRange(usuarioId, fechaInicio, fechaFin)
				.map(registros -> registros.stream()
						.map(metricaSaludMapper::metricaSaludToMetricaSaludResponseDTO)
						.collect(Collectors.toList()))
				.orElseThrow(NotFoundException::new);
		
			
		return new ResponseEntity<>(respuesta, HttpStatus.OK);
			
	}
	
	@DeleteMapping(METRICASALUD_DELETE_PATH) 
	public ResponseEntity<?> deleteRegistroByDate(@PathVariable UUID usuarioId, @PathVariable LocalDate fechaRegistro) throws NotFoundException{
		
		
		if(!metricaSaludService.deleteRegistroMetrica(usuarioId, fechaRegistro)) {
			
			throw new NotFoundException();
		}
		
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		
	}

}
