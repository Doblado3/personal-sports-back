package com.pablodoblado.personal_sports_back.backend.entities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum TipoActividad {

    RIDE("Ride"),
    RUNNING("Run"),
    TRAILRUNNING("TrailRun"),
    FUERZA("WeightTraining"),
    MOVILIDAD("Workout"),
    SENDERISMO("Hike"),
    MOUNTAINBIKERIDE("MountainBikeRide"),
    UNKNOWN("Unknown");

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