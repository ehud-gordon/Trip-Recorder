package com.example.tom_e91.finalproj.models;

import com.example.tom_e91.finalproj.util.util_func;

import java.util.ArrayList;
import java.util.List;

public class Trip {
    public String tripName;
    public List<MyLocation> locations;
    public List<MyMarker> markers;
    public long startTime;
    public long endTime;
    public String durationString;

    // --------------------------------- Constructors --------------------------------- //

    public Trip() {}

    public Trip(String tripName) {
        this.tripName = tripName;
        locations = new ArrayList<MyLocation>();
        markers = new ArrayList<MyMarker>();
        startTime = System.currentTimeMillis();
    }

    public Trip(String tripName, List<MyLocation> locations, List<MyMarker> markers) {
        this.tripName = tripName;
        this.locations = locations;
        this.markers = markers;
    }

    // --------------------------------- Helpers --------------------------------- //
    public void end() {
        this.endTime = System.currentTimeMillis();
        this.durationString = util_func.getFormattedDurationString(this.startTime, this.endTime);
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getDurationString() {
        return durationString;
    }

    public void setDurationString(String durationString) {
        this.durationString = durationString;
    }
}
