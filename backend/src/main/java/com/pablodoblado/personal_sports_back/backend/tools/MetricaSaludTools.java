package com.pablodoblado.personal_sports_back.backend.tools;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import com.pablodoblado.personal_sports_back.backend.entity.MetricaSalud;
import com.pablodoblado.personal_sports_back.backend.service.MetricaSaludService;

@Component
public class MetricaSaludTools {
	
	private final MetricaSaludService metricaSaludService;
	
	public MetricaSaludTools(MetricaSaludService metricaSaludService) {
		this.metricaSaludService = metricaSaludService;
	}
	
	@Tool(description = "Get the total sleep evolution for the user in this week. If there are missing entries, ignore them.")
	public String getTimeSleepByWeek() {
		UUID idUsuario = UUID.fromString("b2f22e50-321d-4816-a8bd-7a0670b72045");
		LocalDate today = LocalDate.now();
		LocalDate firstDayOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate lastDayOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		
		List<MetricaSalud> registros =  metricaSaludService.getRegistrosDiariosByUserInRange(idUsuario, firstDayOfWeek, lastDayOfWeek);
		
		OptionalDouble averageSleepHours = registros.stream()
				.filter(r -> r.getHorasSuenoHours() != null && r.getHorasSuenoMinutes() != null)
				.mapToDouble(r -> (double)r.getHorasSuenoHours() * 60 + r.getHorasSuenoMinutes())
				.average();
		
		if (averageSleepHours.isPresent()) {
			
            double meanMinutes = averageSleepHours.getAsDouble(); 
            double meanHours = meanMinutes / 60.0; 
            
            return String.format("The mean hours of sleep for user %s this week is %.2f hours.", idUsuario, meanHours);
        } else {
            return "No valid sleep entries found (hours and minutes must be present) to calculate the mean for user " + idUsuario + " this week.";
        }
	}

}
