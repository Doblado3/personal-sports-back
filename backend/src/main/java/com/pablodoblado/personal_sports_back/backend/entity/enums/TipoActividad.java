package com.pablodoblado.personal_sports_back.backend.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoActividad {
	
	RIDE("Ride"), RUNNING("Run"), TRAILRUNNING("TrailRun"), FUERZA("WeightTraining"), MOVILIDAD("WorkOut"), SENDERISMO("Hike");
	
	private final String stravaValue;
	
	TipoActividad(String stravaValue) {
		this.stravaValue = stravaValue;
	}
	
    @JsonValue
    public String getStravaValue() {
        return stravaValue;
    }

    @JsonCreator
    public static TipoActividad fromStravaValue(String text) {
        for (TipoActividad tipo : TipoActividad.values()) {
            if (String.valueOf(tipo.stravaValue).equalsIgnoreCase(text)) { 
                return tipo;
            }
        }
        // Tener en cuenta tipos de actividades que todavía no he considerado
        throw new IllegalArgumentException("Unknown Strava activity type: " + text);
    }

}
