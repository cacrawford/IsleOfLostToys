package com.containerstore.lost.googlemaps;

import com.google.common.base.Objects;

import static java.lang.Math.*;
import static org.apache.commons.validator.GenericValidator.*;

public class LatAndLong {
    /** Constant used to convert from degrees to radians. */
    private static final double C = 57.3;

    private double latitude;
    private double longitude;

    public LatAndLong(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public static LatAndLong fromString(String item) {
        // Loosely assume that a string is lat & long if it can be split into two numbers
        // divided by a comma.
        String[] items = item.split(",");
        if (items.length != 2) {
            return null;
        }

        if (!isDouble(items[0]) || !isDouble(items[1])) {
            return null;
        }

        return new LatAndLong(Double.valueOf(items[0]), Double.valueOf(items[1]));
    }

    public static double getDistance(LatAndLong origin, LatAndLong dest) {
        return 3959 * acos(sin(origin.getLatitude() / C) * sin(dest.getLatitude() / C) +
                cos(origin.getLatitude() / C) * cos(dest.getLatitude() / C) *
                        cos(dest.getLongitude() / C - origin.getLongitude() / C));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(latitude, longitude);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }

        LatAndLong other = (LatAndLong) o;
        return latitude == other.latitude && longitude == other.longitude;
    }

    @Override
    public String toString() {
        return latitude + "," + longitude;
    }
}
