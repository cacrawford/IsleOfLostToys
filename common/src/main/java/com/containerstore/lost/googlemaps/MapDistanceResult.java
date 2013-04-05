package com.containerstore.lost.googlemaps;

import java.util.Map;

public interface MapDistanceResult {
    Map<String, Double> getDistancesToDestination(String destination);

    Map<String, Double> getDistancesFromOrigin(String origin);

    double getDistance(String origin, String destination);

    double getDuration(String origin, String destination);
}
