package com.containerstore.lost.googlemaps.google;

import com.containerstore.lost.googlemaps.MapDistanceResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.DataFormatException;

import static com.google.common.collect.Maps.*;

public class GoogleDistanceResult implements MapDistanceResult {
    private static final Logger LOG = Logger.getLogger(GoogleDistanceResult.class);

    private static class Distance {
        private final String text;
        private final double value;

        Distance(JSONObject object) {
            text = object.getString("text");
            value = object.getDouble("value");
        }

        String getDescription() {
            return text;
        }

        double getAmount() {
            return value;
        }

        double getAmountInMiles() {
            return value * 0.00062137119;
        }
    }

    private static class Duration {
        private final String text;
        private final double value;

        Duration(JSONObject object) {
            text = object.getString("text");
            value = object.getDouble("value");
        }

        String getDescription() {
            return text;
        }

        double getAmount() {
            return value;
        }
    }

    private static class Element {
        private final boolean isOk;

        private Distance distance = null;
        private Duration duration = null;

        Element(JSONObject object) {
            isOk = object.getString("status").equalsIgnoreCase("OK");
            if (isOk) {
                distance = new Distance(object.getJSONObject("distance"));
                duration = new Duration(object.getJSONObject("duration"));
            }
        }

        double getDistanceAmount() {
            return distance == null ? -1 : distance.getAmountInMiles();
        }

        double getDurationAmount() {
            return duration == null ? -1 : duration.getAmount();
        }

        String getDistanceDescription() {
            return distance == null ? "" : distance.getDescription();
        }

        String getDurationDescription() {
            return duration == null ? "" : duration.getDescription();
        }
    }

    private static class Elements extends ArrayList<Element> {
        Elements(JSONArray array) {
            for (int i = 0; i < array.size(); i++) {
                add(new Element(array.getJSONObject(i)));
            }
        }
    }

    private static class Row {
        private final Elements elements;

        Row(JSONObject object) {
            elements = new Elements(object.getJSONArray("elements"));
        }

        Element getElement(int index) {
            return elements == null ? null : elements.get(index);
        }

        int size() {
            return elements.size();
        }
    }

    private static class Rows extends ArrayList<Row> {
        Rows(JSONArray array) {
            for (int i = 0; i < array.size(); i++) {
                add(new Row(array.getJSONObject(i)));
            }
        }
    }

    private Map<String, Row> originRows;
    private Map<String, String> originAddresses;
    private Map<String, String> destinationAddresses;
    private boolean isOk;

    private GoogleDistanceResult() {
        isOk = false;

        destinationAddresses = newLinkedHashMap();
        originAddresses = newLinkedHashMap();
        originRows = newLinkedHashMap();
    }

    private void marshalJson(Collection<String> origins, Collection<String> destinations, JSONObject object)
            throws DataFormatException {

        destinationAddresses = newLinkedHashMap();
        originAddresses = newLinkedHashMap();
        originRows = newLinkedHashMap();

        isOk = object.getString("status").equalsIgnoreCase("ok");
        if (!isOk) {
            LOG.error("Google Distance Matrix query returned error: " + object.getString("status"));
            return;
        }

        JSONArray destinationsJson = object.getJSONArray("destination_addresses");
        JSONArray originsJson = object.getJSONArray("origin_addresses");
        Rows rows = new Rows(object.getJSONArray("rows"));

        if (destinations.size() != destinationsJson.size()) {
            throw new DataFormatException("Addresses returned different size than incoming origins");
        }

        if (origins.size() != rows.size() || origins.size() != originsJson.size()) {
            throw new DataFormatException("Rows and/or addresses returned different size than incoming destinations");
        }

        int i = 0;
        for (String each : destinations) {
            destinationAddresses.put(each, destinationsJson.getString(i++));
        }

        i = 0;
        for (String each : origins) {
            originAddresses.put(each, originsJson.getString(i));
            originRows.put(each, rows.get(i++));
        }
    }

    private int getDestinationIndex(String destination) {
        int i = 0;

        for (String each : destinationAddresses.keySet()) {
            if (destination.equals(each)) {
                return i;
            }
            i++;
        }

        return -1;
    }

    private Element getElement(int destinationIndex, String origin) {
        Row row = originRows.get(origin);
        if (row == null) {
            return null;
        }

        return row.getElement(destinationIndex);
    }

    private Element getElement(String origin, String destination) {
        int destinationIndex = getDestinationIndex(destination);
        return getElement(destinationIndex, origin);
    }

    @Override
    public Map<String, Double> getDistancesToDestination(String destination) {
        Map<String, Double> distanceMap = newLinkedHashMap();

        for (String origin : originRows.keySet()) {
            Element e = getElement(origin, destination);
            if (e != null) {
                distanceMap.put(origin, e.getDistanceAmount());
            }
        }

        return distanceMap;
    }

    @Override
    public Map<String, Double> getDistancesFromOrigin(String origin) {
        Map<String, Double> distanceMap = newLinkedHashMap();

        Row row = originRows.get(origin);
        if (row == null) {
            return null;
        }

        if (row.size() != destinationAddresses.size()) {
            // error, should be equal
            return null;
        }

        Iterator<String> iter = destinationAddresses.keySet().iterator();
        for (int i = 0; i < row.size(); i++) {
            Element e = row.getElement(i);
            distanceMap.put(iter.next(), e.getDistanceAmount());
        }

        return distanceMap;
    }

    @Override
    public double getDistance(String origin, String destination) {
        Element e = getElement(origin, destination);
        return e == null ? -1 : e.getDistanceAmount();
    }

    @Override
    public double getDuration(String origin, String destination) {
        Element e = getElement(origin, destination);
        return e == null ? -1 : e.getDurationAmount();
    }

    public static GoogleDistanceResultBuilder result() {
        return new GoogleDistanceResultBuilder();
    }

    public static class GoogleDistanceResultBuilder {
        private JSONObject jsonObject;
        private Collection<String> origins;
        private Collection<String> destinations;

        private GoogleDistanceResultBuilder() {
        }

        public GoogleDistanceResultBuilder withOrigins(Collection<String> origins) {
            this.origins = origins;
            return this;
        }

        public GoogleDistanceResultBuilder withJSONObject(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
            return this;
        }

        public GoogleDistanceResultBuilder withDestinations(Collection<String> destinations) {
            this.destinations = destinations;
            return this;
        }

        public GoogleDistanceResult build() {
            try {
                GoogleDistanceResult result = new GoogleDistanceResult();
                result.marshalJson(origins, destinations, jsonObject);
                return result;
            } catch (Exception e) {
                return new GoogleDistanceResult();
            }
        }
    }
}
