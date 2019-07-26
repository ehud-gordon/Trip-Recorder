package com.example.tom_e91.finalproj.data;

public class Trip {
    int trip_id;

    String trip_title;
    Trip() {}

    public Trip(int trip_id, String trip_title) {
        this.trip_id = trip_id;
        this.trip_title = trip_title;
    }
    public void setTrip_id(int trip_id) {
        this.trip_id = trip_id;
    }

    public void setTrip_title(String trip_title) {
        this.trip_title = trip_title;
    }

    public int getTrip_id() {
        return trip_id;
    }

    public String getTrip_title() {
        return trip_title;
    }
}
