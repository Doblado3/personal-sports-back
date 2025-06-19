package com.pablodoblado.personal_sports_back.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pablodoblado.personal_sports_back.backend.entity.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.entity.Usuario;
import com.pablodoblado.personal_sports_back.backend.repository.MetricaSaludRepository;
import com.pablodoblado.personal_sports_back.backend.repository.UsuarioRepository;

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
			copia.setHorasSueño(registro.getHorasSueño());
			copia.setCalidadSueño(registro.getCalidadSueño());
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
	
	public Optional<MetricaSalud> getRegistroById(UUID id){
		return metricaSaludRepository.findById(id);
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
	
	public List<MetricaSalud> getRegistrosDiariosByUserInRange(UUID idUsuario, LocalDate starDate, LocalDate endDate){
		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new IllegalArgumentException("No se ha podido encontrar el usuario con id: " + idUsuario));
		return metricaSaludRepository.findByUsuarioAndFechaRegistroBetween(usuario, starDate, endDate);
		
	}
	
	public void deleteRegistroMetrica(UUID idRegistro) {
		metricaSaludRepository.deleteById(idRegistro);
	}
	
	

}
