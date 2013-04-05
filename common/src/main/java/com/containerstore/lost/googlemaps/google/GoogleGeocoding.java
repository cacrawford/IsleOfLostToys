package com.containerstore.lost.googlemaps.google;

import com.containerstore.common.base.exception.BusinessException;
import com.containerstore.lost.googlemaps.LatAndLong;
import com.containerstore.lost.googlemaps.MapGeocoding;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import static com.containerstore.lost.googlemaps.google.GoogleDefs.*;

@Component("GoogleGeocoding")
public class GoogleGeocoding implements MapGeocoding {
    private final RestTemplate restTemplate;

    public GoogleGeocoding() {
        this.restTemplate = new RestTemplate();
    }

    public GoogleGeocoding(@Qualifier("orderServicesServiceRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public URI buildURI(String address) {
        try {
            return new URI(GOOGLE_API + GEOCODING_URI
                    + "?address=" + URLEncoder.encode(address, "UTF-8")
                    + "&sensor=false");
        } catch (UnsupportedEncodingException shouldNeverHappen) {
            throw new AssertionError(shouldNeverHappen);
        } catch (URISyntaxException shouldNeverHappen) {
            throw new AssertionError(shouldNeverHappen);
        }
    }

    @Override
    public LatAndLong getLocation(String address) {
        String response = restTemplate.getForObject(buildURI(address), String.class);

        JSONObject responseObject = (JSONObject) JSONSerializer.toJSON(response);
        String status = responseObject.getString("status");
        if (!"OK".equalsIgnoreCase(status)) {
            throw new BusinessException("Google Maps call returned status %s", status);
        }

        JSONArray results = responseObject.getJSONArray("results");
        if (results.size() < 1) {
            return null;
        }

        JSONObject addrObj = results.getJSONObject(0);
        JSONObject geometry = addrObj.getJSONObject("geometry");
        JSONObject location = geometry.getJSONObject("location");

        return new LatAndLong(location.getDouble("lat"), location.getDouble("lng"));
    }
}
