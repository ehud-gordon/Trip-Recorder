package com.example.tom_e91.finalproj.models;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;


public class MyLocation {
    public double latitude, longitude;
    public long timestamp;

    // ------------------------------- Constructors ------------------------------- //

    /** Empty Constructor for Firebase*/
    MyLocation() {}

    public MyLocation(double latitude, double longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public MyLocation(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.timestamp = location.getTime();
    }

    // ------------------------------- Utils ------------------------------- //

    public LatLng toLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    @Override
    public String toString() {
        return String.format("Lat: %f, Lng:%f, timestamp: %d", this.latitude, this.longitude, this.timestamp);
    }

    // ------------------------------- Getters & Setters ------------------------------- //

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
