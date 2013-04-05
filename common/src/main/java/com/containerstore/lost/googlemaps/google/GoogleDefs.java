package com.containerstore.lost.googlemaps.google;

public class GoogleDefs {
    public static final String GOOGLE_API = "http://maps.googleapis.com/maps/api";
    public static final String DISTANCE_URI = "/distancematrix/json";
    public static final String GEOCODING_URI = "/geocode/json";

    public enum Mode {
        DRIVING,
        WALKING,
        BICYCLING
    }

    public enum Avoid {
        TOLLS,
        HIGHWAYS
    }

    public enum Units {
        METRIC,
        IMPERIAL
    }

    private GoogleDefs() {
        throw new UnsupportedOperationException();
    }
}
