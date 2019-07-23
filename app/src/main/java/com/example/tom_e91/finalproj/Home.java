package com.example.tom_e91.finalproj;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class Home extends AppCompatActivity {
    FloatingActionButton recordButton;
    Button viewPrevTripsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        recordButton = findViewById(R.id.record_button);
        viewPrevTripsButton = findViewById(R.id.view_previous_trips_button);
    }

    public void onRecordButtonClick(View view) {
        // TODO Go to Map Activity
    }


    public void onPreviousTripsButtonClick(View view) {
        Intent previousTripsIntent = new Intent(this, PreviousTrips.class);
        startActivity(previousTripsIntent);
        // TODO create intent to get to Previous Trips Activity
    }
}
