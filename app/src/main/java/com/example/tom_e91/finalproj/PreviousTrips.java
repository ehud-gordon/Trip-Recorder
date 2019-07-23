package com.example.tom_e91.finalproj;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.LinkedList;

public class PreviousTrips extends AppCompatActivity {
    final LinkedList<Trip> tripList = new LinkedList<>();
    PreviousTripsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_trips);

        // Create initial trips
        for (int i = 0; i < 10; ++i) { tripList.addLast(new Trip(i, Integer.toString(i))); }

        // Set up RecyclerView
        RecyclerView previousTripsRecyclerView = findViewById(R.id.previous_trips_rv);
        adapter = new PreviousTripsListAdapter(this, tripList);
        previousTripsRecyclerView.setAdapter(adapter);
        previousTripsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }
}
