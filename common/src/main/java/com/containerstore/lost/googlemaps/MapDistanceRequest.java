package com.containerstore.lost.googlemaps;

import java.util.Collection;

public interface MapDistanceRequest {
    MapDistanceRequest withOrigin(String origin);

    MapDistanceRequest withOrigins(Collection<String> origins);

    MapDistanceRequest withDestination(String destination);

    String getDestination();

    Collection<String> getOrigins();
}
