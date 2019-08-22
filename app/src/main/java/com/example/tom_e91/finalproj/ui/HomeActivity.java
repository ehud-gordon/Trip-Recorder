package com.example.tom_e91.finalproj.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.tom_e91.finalproj.MyApplication;
import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.models.User;
import com.google.firebase.auth.FirebaseAuth;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    // Constants
    private static final String LOG_TAG = "nadir " + RegisterActivity.class.getSimpleName();

    // ------------------------------- LifeCycle ------------------------------- //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bind Views
        setContentView(R.layout.activity_home);
        findViewById(R.id.record_button).setOnClickListener(this);
        findViewById(R.id.view_previous_trips_button).setOnClickListener(this);

        // Example Usage of MyApplication
        User curUser = ((MyApplication) getApplicationContext()).getUser();
        Log.d(LOG_TAG, String.format("User email is: %s", curUser.getEmail()));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view_previous_trips_button:
                startActivity(new Intent(this, PreviousTripsActivity.class));
                break;

            case R.id.record_button:
                startActivity(new Intent(this, MapsActivity.class));
                break;
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
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
