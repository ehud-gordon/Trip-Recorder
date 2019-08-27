package com.example.tom_e91.finalproj.models;

import android.location.Location;

import java.util.List;

public class Trip {
    public int trip_id;
    public List<Location> locations;

    // --------------------------------- Constructors --------------------------------- //

    public Trip() {}

    public Trip(List<Location> locations) {
        this.locations = locations;
    }

    // --------------------------------- Getters and Setters --------------------------------- //

    public List<Location> getLocations() {
        return locations;
    }
    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public void setTrip_id(int trip_id) {
        this.trip_id = trip_id;
    }
    public int getTrip_id() {
        return trip_id;
    }



}
