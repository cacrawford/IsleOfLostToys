package com.containerstore.lost.googlemaps;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MapQuery {
    private static final Logger LOG = Logger.getLogger(MapQuery.class);

    private final MapDistanceMatrix distanceMatrix;
    private final MapGeocoding geocoding;
    private final MapDistanceRequest request;

    @Autowired
    public MapQuery(MapDistanceMatrix distanceMatrix, MapGeocoding geocoding, MapDistanceRequest request) {
        this.distanceMatrix = distanceMatrix;
        this.geocoding = geocoding;
        this.request = request;
    }

    public MapDistanceRequest createRequest() {
        try {
            return request.getClass().newInstance();
        } catch (Exception e) {
            LOG.error("Exception creating new MapDistanceRequest instance: " + request.getClass().toString());
            return null;
        }
    }

    public LatAndLong getLocation(String address) {
        return geocoding.getLocation(address);
    }

    public MapDistanceResult queryDistance(MapDistanceRequest request) {
        return distanceMatrix.query(request);
    }
}
