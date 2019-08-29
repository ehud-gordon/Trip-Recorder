package com.example.tom_e91.finalproj.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.tom_e91.finalproj.MyApplication;
import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.adapters.PreviousTripsListAdapter;
import com.example.tom_e91.finalproj.models.Repository;
import com.example.tom_e91.finalproj.models.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;

public class PreviousTripsActivity extends AppCompatActivity {
    private final static String LOG_TAG = "nadir" + PreviousTripsActivity.class.getSimpleName();

    List<Trip> tripList = new LinkedList<>();
    PreviousTripsListAdapter adapter;
    private Repository repository;
    private OnCompleteListener<QuerySnapshot> onCompleteListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_trips);
        repository = ((MyApplication)(getApplicationContext())).getRepository();
        onCompleteListener = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Trip trip = document.toObject(Trip.class);
                        tripList.add(trip);
                    }
                    setupRecyclerView();
                } else {
                    Log.d(LOG_TAG, "Error getting documents: ", task.getException());
                }

            }
        };

        repository.getAllTrips(onCompleteListener);


    }

    private void setupRecyclerView() {
        // Set up RecyclerView
        if (tripList.isEmpty()) {
            Toast.makeText(this, "No trips to display!", Toast.LENGTH_LONG).show();
        }
        RecyclerView previousTripsRecyclerView = findViewById(R.id.previous_trips_rv);
        adapter = new PreviousTripsListAdapter(this, tripList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        previousTripsRecyclerView.setAdapter(adapter);
        previousTripsRecyclerView.setLayoutManager(layoutManager);
    }
}
