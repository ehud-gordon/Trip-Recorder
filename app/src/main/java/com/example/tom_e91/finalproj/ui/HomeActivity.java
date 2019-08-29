package com.example.tom_e91.finalproj.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.tom_e91.finalproj.MyApplication;
import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.models.Repository;
import com.example.tom_e91.finalproj.util.util_func;
import com.google.firebase.auth.FirebaseAuth;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    // Constants
    private static final String LOG_TAG = "nadir " + HomeActivity.class.getSimpleName();
    private static final String TRIP_LAYOUT_BOOL_KEY = "TRIP_LAYOUT_BOOL_KEY";
    Repository repository;

    private boolean isTripNameLayout = false;

    // Views
    private EditText mTripNameEditText;
    private LinearLayout mRecordLayout;
    private LinearLayout mTripNameLayout;

    // ------------------------------- LifeCycle ------------------------------- //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViews();

        // Setup value of isTripNameLayout
        if (savedInstanceState !=  null) {
            isTripNameLayout = savedInstanceState.getBoolean(TRIP_LAYOUT_BOOL_KEY);
        }
        updateVisibilityOfLayouts();

        // Set up views
        mRecordLayout.setVisibility(View.VISIBLE);

        // TODO weird
        util_func.setRequestingLocationUpdates(this,false);

        // Set up repository
        repository = ((MyApplication)getApplicationContext()).getRepository();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TRIP_LAYOUT_BOOL_KEY, isTripNameLayout);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_previous_trips_button:
                startActivity(new Intent(this, PreviousTripsActivity.class));
                break;

            case R.id.record_button:
                findViewById(R.id.home_record_layout).setVisibility(View.GONE);
                findViewById(R.id.home_trip_name_layout).setVisibility(View.VISIBLE);
                isTripNameLayout = true;
                break;

            case R.id.home_trip_name_button:
                String tripName = mTripNameEditText.getText().toString();
                if (tripName.isEmpty()) {
                    mTripNameEditText.setError("Required!");
                    return;
                } else {
                    mTripNameEditText.setError(null);
                    // Create new trip in DB
                    repository.createNewTrip(tripName);
                    // Go to MapsActivity
                    startActivity(new Intent(this, MapsActivity.class));
                    isTripNameLayout = false;
                    finish();
                    break;
                }
        }
    }

    // ------------------------------- UI ------------------------------- //
    private void bindViews() {
        // Bind Views
        setContentView(R.layout.activity_home);
        findViewById(R.id.record_button).setOnClickListener(this);
        findViewById(R.id.view_previous_trips_button).setOnClickListener(this);
        findViewById(R.id.home_trip_name_button).setOnClickListener(this);
        mTripNameEditText = findViewById(R.id.home_trip_name_et);
        mRecordLayout = findViewById(R.id.home_record_layout);
        mTripNameLayout = findViewById(R.id.home_trip_name_layout);
    }

    private void updateVisibilityOfLayouts() {
        if (isTripNameLayout) {
            mRecordLayout.setVisibility(View.GONE);
            mTripNameLayout.setVisibility(View.VISIBLE);
        } else {
            mRecordLayout.setVisibility(View.VISIBLE);
            mTripNameLayout.setVisibility(View.GONE);
        }
    }

    // ------------------------------- Menu ------------------------------- //

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signOut() {
        // TODO check if need to update user in MyApplication
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, EmailPasswordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
