package com.containerstore.lost.googlemaps.google;

import com.containerstore.lost.googlemaps.MapDistanceMatrix;
import com.containerstore.lost.googlemaps.MapDistanceRequest;
import com.containerstore.lost.googlemaps.MapDistanceResult;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("GoogleDistanceMatrix")
public class GoogleDistanceMatrix implements MapDistanceMatrix {
    private static final Logger LOG = Logger.getLogger(GoogleDistanceMatrix.class);

    private final RestTemplate restTemplate;

    public GoogleDistanceMatrix() {
        this.restTemplate = new RestTemplate();
    }

    public GoogleDistanceMatrix(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public MapDistanceResult query(MapDistanceRequest request) {
        GoogleDistanceRequest distanceRequest = ((GoogleDistanceRequest) request)
                .withSensor(false)
                .withMode(GoogleDefs.Mode.DRIVING)
                .withUnits(GoogleDefs.Units.IMPERIAL);

        try {
            String json = restTemplate.getForObject(distanceRequest.build(), String.class);

            JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(json);

            return GoogleDistanceResult.result()
                    .withJSONObject(jsonObject)
                    .withOrigins(distanceRequest.getOrigins())
                    .withDestinations(distanceRequest.getDestinations())
                    .build();
        } catch (Exception e) {
            LOG.error("Exception querying URI: " + e.getMessage());
        }

        return GoogleDistanceResult.result().build();
    }
}
