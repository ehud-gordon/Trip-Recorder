package com.example.tom_e91.finalproj.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.tom_e91.finalproj.MyApplication;
import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.adapters.CustomInfoWindowAdapter;
import com.example.tom_e91.finalproj.models.MyLocation;
import com.example.tom_e91.finalproj.models.MyMarker;
import com.example.tom_e91.finalproj.models.Repository;
import com.example.tom_e91.finalproj.models.Trip;
import com.example.tom_e91.finalproj.util.Constants;
import com.example.tom_e91.finalproj.util.MapHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

public class SummaryActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Constants
    private static final String LOG_TAG = "nadir " + SummaryActivity.class.getSimpleName();

    private Trip mCurrentTrip;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        Repository repository = ((MyApplication)getApplicationContext()).getRepository();
        mCurrentTrip = repository.getCurrentTrip();
        // Initialize Google Map, calls onMapReady
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.summary_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null) {
            Log.d(LOG_TAG, "onMapReady returns null");
            makeErrorDialogToHome("Google Maps unavailable");
            return;
        }
        mMap = googleMap;
        MyLocation firstLocation = mCurrentTrip.locations.get(0);
        LatLng firstLatLng = new LatLng(firstLocation.latitude, firstLocation.longitude);
        // Init Map
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, Constants.DEFAULT_ZOOM));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));

        // Set up Polyline
        Polyline polyline= MapHelper.initPolyline(mMap);
        polyline.setPoints(MapHelper.myLocationsToLatLngs(mCurrentTrip.locations));

        // Set up Markers
        for (MyMarker myMarker : mCurrentTrip.markers) {
            MapHelper.addMarkerToMap(mMap, myMarker);
        }
    }


    // ------------------------------- Helpers ------------------------------- //

    private void makeErrorDialogToHome(String msg) {
        new AlertDialog.Builder(this).setMessage(msg).setCancelable(false).setPositiveButton("Go back", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                startActivity(new Intent(SummaryActivity.this, HomeActivity.class));
                finish();
            }
        }).create().show();
    }
}
