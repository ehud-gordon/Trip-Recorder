package com.example.tom_e91.finalproj.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.tom_e91.finalproj.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    // Constants
    private static final String LOG_TAG = "nadir " + MapsActivity.class.getSimpleName();
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

    // Maps
    GoogleMap mMap;

    // Location
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mCurrLocation;
    private LocationCallback mLocationCallback;

    // Points
    LatLng p1 = new LatLng(31.773448, 35.199189);
    LatLng p2 = new LatLng(31.774876, 35.198846);
    LatLng p3 = new LatLng(31.775825, 35.198535);
    LatLng p4 = new LatLng(31.776536, 35.198353);
    private final LatLng mDefaultLocation = p1; // A default location
    private static final int DEFAULT_ZOOM = 18;
    // Trip
    private Polyline mPolyline;



    @Override
//    @SuppressWarnings("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViews();
        // Initialize the FusedLocationClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Initialize Google Map, calls onMapReady when ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void bindViews() {
        setContentView(R.layout.activity_maps);
        findViewById(R.id.map_stop_button).setOnClickListener(this);
        findViewById(R.id.map_star).setOnClickListener(this);
        findViewById(R.id.map_camera).setOnClickListener(this);
        findViewById(R.id.map_note).setOnClickListener(this);
        findViewById(R.id.map_marker).setOnClickListener(this);
    }

    @Override public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap == null) {
            Log.d(LOG_TAG, "onMapReady() returned null");
            Toast.makeText(this, "Google Maps unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_PERMISSION);
            return;
        }
        // request Location updates
        mLocationCallback = getOnUpdateLocationCallBack();
        mFusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, null);
    }

    public void setMarker(LatLng latLng, String markerTitle, int icon) {
        if (mMap != null) {
            if (icon == 0) {
                mMap.addMarker(new MarkerOptions().position(latLng).title(markerTitle));
            }
            else {
                mMap.addMarker(new MarkerOptions().position(latLng).title(markerTitle).icon(BitmapDescriptorFactory.fromResource(icon)));
            }


            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
        else {
            Log.d(LOG_TAG, "Map object is null");
        }
    }

    private LatLng locToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void checkMovement(Location newLocation) {
        LatLng newLatLng = locToLatLng(newLocation);
        Log.d(LOG_TAG, String.format("update nadirloc: lat: %f , lng: %f", newLatLng.latitude, newLatLng.longitude));
        // First update
        if (mCurrLocation == null) {
            Toast.makeText(this, "first Point!", Toast.LENGTH_SHORT).show();
            mCurrLocation = newLocation;
            mPolyline = mMap.addPolyline(new PolylineOptions().color(0xff388E3C).add(newLatLng));

        } else {
            if (isNewLocation(mCurrLocation, newLocation)) {
                Toast.makeText(this, "new Point", Toast.LENGTH_SHORT).show();
                mCurrLocation = newLocation;
                // Add a point to route
                List<LatLng> points = mPolyline.getPoints();
                points.add(newLatLng);
                mPolyline.setPoints(points);
            }
        }
    }

    private LocationCallback getOnUpdateLocationCallBack() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location newLocation = locationResult.getLastLocation();
                checkMovement(newLocation);
            }
        };
    }

    @SuppressWarnings("MissingPermission")
    @Override public void onClick(View view) {
        LatLng newLatLng = locToLatLng(mCurrLocation);
        switch (view.getId()) {
            case R.id.map_stop_button:
                // TODO finish activity
                stopMap();
                break;
            case R.id.map_camera:
                setMarker(newLatLng, "", R.drawable.camera20);
                break;
            case R.id.map_star:
                getRecommendations();
                break;
            case R.id.map_note:
                setMarker(locToLatLng(mCurrLocation), "", R.drawable.note20);
                break;
            case R.id.map_marker:
                setMarker(locToLatLng(mCurrLocation), "", 0);
                break;
        }
    }
    private void stopMap() {
        // TODO save Map to JSON
        Intent toSummaryActivityIntent = new Intent(this, SummaryActivity.class);
        // TODO add to intent's bundle an indicator for this map's JSON file
        startActivity(new Intent(this, MapsActivity.class));
    }

    private void getRecommendations() {
        // TODO
    }
    private boolean isNewLocation(Location currPos, Location newPos) {
        double tol = 0.0001;
        return (Math.abs(currPos.getLatitude() - newPos.getLatitude()) > tol
                || Math.abs(currPos.getLongitude() - newPos.getLongitude()) > tol);
    }
    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }
    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }
    private void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_PERMISSION);
        }
    }
}
