package com.containerstore.lost.googlemaps.google;

import com.containerstore.lost.googlemaps.MapDistanceRequest;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collection;

import static com.containerstore.lost.googlemaps.google.GoogleDefs.*;
import static com.google.common.collect.Lists.*;

@Component("GoogleDistanceRequest")
public class GoogleDistanceRequest implements MapDistanceRequest {
    private Collection<String> origins;
    private Collection<String> destinations;
    private boolean sensor;
    private Mode mode;
    private String language;
    private Avoid avoid;
    private Units units;

    @Override
    public Collection<String> getOrigins() {
        return origins;
    }

    @Override
    public String getDestination() {
        return destinations.iterator().next();
    }

    public Collection<String> getDestinations() {
        return destinations;
    }

    public URI build() throws URISyntaxException, UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append(GOOGLE_API);
        sb.append(DISTANCE_URI);
        sb.append("?origins=");
        sb.append(buildLocationString(origins));
        sb.append("&destinations=");
        sb.append(buildLocationString(destinations));
        sb.append("&sensor=").append(sensor ? "true" : "false");

        // Optional parameters
        sb.append(mode == null ? "" : "&mode=" + mode.toString().toLowerCase());
        sb.append(language == null ? "" : "&language=" + language.toLowerCase());
        sb.append(avoid == null ? "" : "&avoid=" + avoid.toString().toLowerCase());
        sb.append(units == null ? "" : "&units=" + units.toString().toLowerCase());

        return new URI(sb.toString());
    }

    @Override
    public MapDistanceRequest withOrigin(String origin) {
        origins = newArrayList();
        origins.add(origin);
        return this;
    }

    @Override
    public MapDistanceRequest withOrigins(Collection<String> origins) {
        this.origins = newArrayList(origins);
        return this;
    }

    @Override
    public MapDistanceRequest withDestination(String destination) {
        destinations = Lists.newArrayList();
        destinations.add(destination);
        return this;
    }

    public GoogleDistanceRequest withDestinations(Collection<String> destinations) {
        this.destinations = newArrayList(destinations);
        return this;
    }

    public GoogleDistanceRequest withSensor(boolean sensor) {
        this.sensor = sensor;
        return this;
    }

    public GoogleDistanceRequest withMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public GoogleDistanceRequest withLanguage(String language) {
        this.language = language;
        return this;
    }

    public GoogleDistanceRequest withAvoid(Avoid avoid) {
        this.avoid = avoid;
        return this;
    }

    public GoogleDistanceRequest withUnits(Units units) {
        this.units = units;
        return this;
    }

    private String buildLocationString(Collection<String> locations) throws UnsupportedEncodingException {
        return URLEncoder.encode(Joiner.on('|').join(locations), "UTF-8");
    }
}
