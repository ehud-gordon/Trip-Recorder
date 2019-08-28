package com.example.tom_e91.finalproj.models;

import java.util.List;

public class Trip {
    public String tripName;
    public List<MyLocation> locations;
    public List<MyMarker> markers;

    // --------------------------------- Constructors --------------------------------- //

    public Trip() {}

    public Trip(String tripName, List<MyLocation> locations, List<MyMarker> markers) {
        this.tripName = tripName;
        this.locations = locations;
        this.markers = markers;
    }

    // --------------------------------- Getters and Setters --------------------------------- //


    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public List<MyLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<MyLocation> locations) {
        this.locations = locations;
    }

    public List<MyMarker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<MyMarker> markers) {
        this.markers = markers;
    }
}
