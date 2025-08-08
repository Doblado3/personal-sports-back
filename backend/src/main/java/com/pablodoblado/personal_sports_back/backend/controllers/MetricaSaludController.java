package com.pablodoblado.personal_sports_back.backend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pablodoblado.personal_sports_back.backend.dto.MetricaSaludRequestDTO;
import com.pablodoblado.personal_sports_back.backend.dto.MetricaSaludResponseDTO;
import com.pablodoblado.personal_sports_back.backend.entity.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.service.MetricaSaludService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/registroDiario")
@CrossOrigin(origins = "http://localhost:4200") //Front- end calls
public class MetricaSaludController {
	
	private final MetricaSaludService metricaSaludService;
	
	private final ModelMapper modelMapper;
	
	@Autowired
	public MetricaSaludController(MetricaSaludService metricaSaludService, ModelMapper modelMapper) {
		this.metricaSaludService = metricaSaludService;
		this.modelMapper = modelMapper;
	}
	
	/**
     * Endpoint to create or update a daily health metric for a specific user.
     * The request body contains the metric data, including the date.
     *
     * @param idUsuario The UUID of the user.
     * @param request   The DTO containing the daily health metric data.
     * @return ResponseEntity with the saved/updated metric (as a DTO) or an error message.
     */
	@PostMapping("/saveOrUpdate/{idUsuario}")
	public ResponseEntity<?> saveOrUpdate(@PathVariable UUID idUsuario, @Valid @RequestBody MetricaSaludRequestDTO registro){
		try {
			
			MetricaSalud entity = modelMapper.map(registro, MetricaSalud.class);
			MetricaSalud nuevoRegistro = metricaSaludService.saveOrUpdateMetricaDiaria(idUsuario, entity);
			MetricaSaludResponseDTO respuesta = modelMapper.map(nuevoRegistro, MetricaSaludResponseDTO.class);
			
			return new ResponseEntity<>(respuesta, HttpStatus.OK);
			
		}  catch (IllegalArgumentException e) {
			
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT); 
            
        } catch (Exception e) {
            return new ResponseEntity<>("Ha ocurrido un error inesperado : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
        }
	}
	
	@GetMapping("/findAll/{idUsuario}")
	public ResponseEntity<?> findAllRegistrosByUser(@PathVariable UUID idUsuario){
		
		try {
			
			List<MetricaSalud> registros = metricaSaludService.getAllRegistrosForUsuario(idUsuario);
			List<MetricaSaludResponseDTO> respuesta = registros.stream()
					.map(registro -> modelMapper.map(registro, MetricaSaludResponseDTO.class))
					.collect(Collectors.toList());
			
			return new ResponseEntity<>(respuesta, HttpStatus.OK);
			
		}  catch (IllegalArgumentException e) {
			
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT); 
            
        } catch (Exception e) {
            return new ResponseEntity<>("Ha ocurrido un error inesperado : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // Return 500
        }
	}
	
	@GetMapping("/findAll/paginated/{idUsuario}")
	public ResponseEntity<Page<MetricaSaludResponseDTO>> findAllPaginatedRegistrosByUser(
			@PathVariable UUID idUsuario,
			@PageableDefault(page = 0, size = 10, sort = "fechaRegistro,desc") Pageable pageable,
			@RequestParam(required = false) String filter){
		
		try {
			
			Page<MetricaSalud> registros = metricaSaludService.getPaginatedRegistrosForUsuario(idUsuario, pageable, filter);
			//Spring Data Page ofrece un metodo que se encarga de el mapeo de clases
			Page<MetricaSaludResponseDTO> respuesta = registros.map(registro ->
					modelMapper.map(registro, MetricaSaludResponseDTO.class));
			
			return new ResponseEntity<>(respuesta, HttpStatus.OK);
			
		}  catch (IllegalArgumentException e) {
			
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); 
            
        } catch (Exception e) {
        	
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // Return 500
        }
	}
	
	@GetMapping("/findByUsuarioAndFecha/{usuarioId}/{fechaRegistro}")
	public ResponseEntity<?> findByUsuarioAndFechaRegistro(@PathVariable UUID usuarioId, @PathVariable LocalDate fechaRegistro) {
		
		try {
			
			MetricaSalud registro = metricaSaludService.getRegistroByUsuarioAndDate(usuarioId, fechaRegistro).get();
			MetricaSaludResponseDTO respuesta = modelMapper.map(registro, MetricaSaludResponseDTO.class);
			
			return new ResponseEntity<>(respuesta, HttpStatus.OK);
			
		} catch (IllegalArgumentException e) {
			
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
			
		} catch (Exception e) {
			
			return new ResponseEntity<>("Ha ocurrido un error inesperado : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
		
		
	}
	
	@GetMapping("/allByDateRange/{usuarioId}/{fechaInicio}/{fechaFin}")
	public ResponseEntity<?> findAllRegistrosByDateRange(@PathVariable UUID usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
		
		//Comprobar autenticacion del usuario cuando este implementada
		
		try {
			
			List<MetricaSalud> registros = metricaSaludService.getRegistrosDiariosByUserInRange(usuarioId, fechaInicio, fechaFin);
			List<MetricaSaludResponseDTO> respuesta = registros.stream()
					.map(registro -> modelMapper.map(registro, MetricaSaludResponseDTO.class))
					.toList();
			
			return new ResponseEntity<>(respuesta, HttpStatus.OK);
			
		} catch (IllegalArgumentException e) {
			
			return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
			
		} catch (Exception e) {
			
			return new ResponseEntity<>("Ha ocurrido un error inesperado : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
	}
	
	@DeleteMapping("/delete/{usuarioId}/{fechaRegistro}") 
	public ResponseEntity<Void> deleteRegistroByDate(@PathVariable UUID usuarioId, @PathVariable LocalDate fechaRegistro){
		
		//Coger el usuario aquí cuando tenga autenticacion
		
		try {
			
			metricaSaludService.deleteRegistroMetrica(usuarioId, fechaRegistro);
			return ResponseEntity.noContent().build(); //204
			
		} catch (EmptyResultDataAccessException e) {
			
			return ResponseEntity.notFound().build(); //404
			
		} catch (Exception e) {
			
			System.err.println("Error al eliminar el registro: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
	}

}
