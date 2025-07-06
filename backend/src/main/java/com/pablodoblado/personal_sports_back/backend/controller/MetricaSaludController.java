package com.pablodoblado.personal_sports_back.backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@GetMapping("/{registroId}")
	public MetricaSalud findById(@PathVariable UUID registroId) {
			
		return metricaSaludService.getRegistroById(registroId).get();
		
		
	}
	
	@DeleteMapping("/delete/{registroId}") 
	public void deleteRegistroById(@PathVariable UUID registroId){
		metricaSaludService.deleteRegistroMetrica(registroId);
	}

}
