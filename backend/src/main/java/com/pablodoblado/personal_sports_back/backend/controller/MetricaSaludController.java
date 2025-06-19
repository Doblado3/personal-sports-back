package com.pablodoblado.personal_sports_back.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pablodoblado.personal_sports_back.backend.entity.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.service.MetricaSaludService;
import com.pablodoblado.personal_sports_back.backend.service.UsuarioService;

@RestController
@RequestMapping("/api/registroDiario")
public class MetricaSaludController {
	
	private final MetricaSaludService metricaSaludService;
	
	
	public MetricaSaludController(MetricaSaludService metricaSaludService, UsuarioService usuarioService) {
		this.metricaSaludService = metricaSaludService;
	}
	
	@PostMapping("/saveOrUpdate/{idUsuario}")
	public ResponseEntity<?> saveOrUpdate(@PathVariable UUID idUsuario, @RequestBody MetricaSalud registro){
		try {
		
			MetricaSalud nuevoRegistro = metricaSaludService.saveOrUpdateMetricaDiaria(idUsuario, registro);
			return new ResponseEntity<>(nuevoRegistro, HttpStatus.OK);
			
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
			return new ResponseEntity<>(registros, HttpStatus.OK);
			
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
