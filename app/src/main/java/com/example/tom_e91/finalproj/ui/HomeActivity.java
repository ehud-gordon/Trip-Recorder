package com.example.tom_e91.finalproj.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.tom_e91.finalproj.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    // Constants
    private static final String LOG_TAG = "nadir " + RegisterActivity.class.getSimpleName();
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private final static int ERROR_DIALOG_REQUEST = 1001;
    private final static int PERMISSIONS_REQUEST_ENABLE_GPS = 1002;

    // Permissions
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    private boolean mLocationPermissionGranted = false;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bind Views
        setContentView(R.layout.activity_home);
        findViewById(R.id.record_button).setOnClickListener(this);
        findViewById(R.id.view_previous_trips_button).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkMapServices();
    }
    // TODO move to Map
    private void checkMapServices() {
        if (checkGooglePlayServices()) {
            if (checkGPSProvider()) {
                // Request Location permission
                getLocationPermission();
            }
        }
    }

    private boolean checkGooglePlayServices() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(HomeActivity.this);

        if(available == ConnectionResult.SUCCESS){
            // Everything is fine and the user can make map requests
            Log.d(LOG_TAG, "checkGooglePlayServices(): has Google Play Services");
            return true;
        } else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(LOG_TAG, "checkGooglePlayService(): UserResolvableError");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(HomeActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Log.d(LOG_TAG, "checkGooglePlayServices(): ConnectionResult is unsuccessful");
            Toast.makeText(this, "Google API Unavailable", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private boolean checkGPSProvider() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(LOG_TAG, "checkMapServices() No GPS Provider");
            // Ask User to enable GPS provider
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Location is Off. Do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
            return false;
        }
        else {
            return true;
        }
    }

    private void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS:
                getLocationPermission();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int res: grantResults) {
            if (res != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_PERMISSION);
            }
        }
        mLocationPermissionGranted = true;

    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
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

    @Override public void onClick(View view) {
        // TODO handle case clicked "don't ask me again for permissions"
        if (!mLocationPermissionGranted) {
            checkMapServices();
            return;
        }
        switch (view.getId()) {
            case R.id.view_previous_trips_button:
                startActivity(new Intent(this, PreviousTripsActivity.class));

            case R.id.record_button:
                startActivity(new Intent(this, MapActivity.class));
                break;
        }
    }


}
