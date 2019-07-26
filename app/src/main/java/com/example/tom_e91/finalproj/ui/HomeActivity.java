package com.example.tom_e91.finalproj.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.tom_e91.finalproj.R;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String LOG_TAG = "nadir " + RegisterActivity.class.getSimpleName();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findViewById(R.id.record_button).setOnClickListener(this);
        findViewById(R.id.view_previous_trips_button).setOnClickListener(this);
    }

    @Override public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_previous_trips_button:
                Intent previousTripsIntent = new Intent(this, PreviousTripsActivity.class);
                startActivity(previousTripsIntent);

            case R.id.record_button:
                // TODO go to Map Activity
                break;
        }
    }
}
