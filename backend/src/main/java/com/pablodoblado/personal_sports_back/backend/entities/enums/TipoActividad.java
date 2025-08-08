package com.pablodoblado.personal_sports_back.backend.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum TipoActividad {

    RIDE("Ride"),
    RUNNING("Run"),
    TRAILRUNNING("TrailRun"),
    FUERZA("WeightTraining"),
    MOVILIDAD("Workout"),
    SENDERISMO("Hike"),
    MOUNTAINBIKERIDE("MountainBikeRide"),
    UNKNOWN("Unknown");

    private static final Logger log = LoggerFactory.getLogger(TipoActividad.class);
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
        log.warn("Unknown Strava activity type: {}. Returning UNKNOWN.", text);
        return UNKNOWN;
    }
}