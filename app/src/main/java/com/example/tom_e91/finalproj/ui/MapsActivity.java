package com.example.tom_e91.finalproj.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.adapters.CustomInfoWindowAdapter;
import com.example.tom_e91.finalproj.models.MarkerTag;
import com.example.tom_e91.finalproj.util.util_func;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    // Constants
    private static final String LOG_TAG = "nadir " + MapsActivity.class.getSimpleName();
    private static final int PERMISSION_LOCATION_CODE = 1;
    private final static int ERROR_DIALOG_REQUEST = 500;
    private static final int INTENT_IMAGE_CAPTURE_CODE = 1000;
    private final static int INTENT_ENABLE_GPS_CODE = 1001;
    private final static int INTENT_APP_SETTINGS_CODE = 1002;

    // Maps
    GoogleMap mMap;

    // Points googleplex
    LatLng p1 = new LatLng(37.422, -122.084);
    LatLng p2 = new LatLng(37.4223, -122.084);
    LatLng p3 = new LatLng(37.4226, -122.084);
    LatLng p4 = new LatLng(37.4229, -122.084);

    private final LatLng mDefaultLocation = p1; // A default location
    private static final int DEFAULT_ZOOM = 18;

    // Views
    EditText noteEditText;
    Button noteFinishButton;

    // Permissions
    private boolean mStoragePermissionGranted = false;

    // Location
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mCurrLocation;
    private LocationCallback mLocationCallback;

    // Trip
    private Polyline mPolyline;
    private String mCurrPhotoPath;

    // ------------------------------- LifeCycle ------------------------------- //

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViews();
        Log.d(LOG_TAG, "onCreate()");
        // Get required Location permission
        if (checkMapServices()) {
            Log.d(LOG_TAG, "onCreate(), checkMapServices() True");
            initActivity();
        }
    }

    private void initActivity() {
        Log.d(LOG_TAG, "initActivity()");
        // Initialize the FusedLocationClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Google Map, calls onMapReady
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void bindViews() {
        setContentView(R.layout.activity_maps);
        noteEditText = findViewById(R.id.note_edit_text);
        noteFinishButton = findViewById(R.id.note_finish_button);
        findViewById(R.id.map_stop_button).setOnClickListener(this);
        findViewById(R.id.map_star).setOnClickListener(this);
        findViewById(R.id.map_camera).setOnClickListener(this);
        findViewById(R.id.map_note).setOnClickListener(this);
        findViewById(R.id.map_marker).setOnClickListener(this);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case INTENT_ENABLE_GPS_CODE:
                if (resultCode == RESULT_OK) {
                    Log.d(LOG_TAG, "onActivityResult(), ENABLE_GPS_CODE OK");
                    requestLocationPermission();
                }
                else {
                    makeErrorDialogToHome("Tracking a trip requires Location services");
                    Log.d(LOG_TAG, "onActivityResult(), ENABLE_GPS_CODE failed");
                }
                break;

            case INTENT_IMAGE_CAPTURE_CODE:
                if (resultCode == RESULT_OK) {
                    Log.d(LOG_TAG, "onActivityResult(), RESULT_OK");
                    // Create marker with the resulting bitmap image
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    MarkerTag tag = new MarkerTag(getString(R.string.tag_camera)).setBitmap(imageBitmap);
                    LatLng latLng = util_func.locToLatLng(mCurrLocation);
                    setMarker(latLng, "Loc", R.drawable.camera_color25, tag);
                }
                else {
                    Toast.makeText(this, "No image was captured", Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "onActivityResult(), RESULT failed");
                }

            case INTENT_APP_SETTINGS_CODE:
                Log.d(LOG_TAG, String.format("onActivityResult(), APP_SETTINGS, result: %d", resultCode));
                if (isLocationPermissionGranted()) {
                        initActivity();
                } else
                    makeErrorDialogToHome("Tracking a trip requires Location services.");
        }
    }

    // ------------------------------- Permissions ------------------------------- //

    private boolean checkMapServices() {
        return checkGooglePlayServices() && checkGPSProvider() && requestLocationPermission();
    }

    private boolean checkGooglePlayServices() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(LOG_TAG, "checkGooglePlayService(): UserResolvableError");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Log.d(LOG_TAG, "checkGooglePlayServices(): Unavailable");
            makeErrorDialogToHome("Google Play Services Unavailable");
        }
        return false;
    }

    private boolean checkGPSProvider() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            makeErrorDialogToHome("No Location Manager detected");
            Log.d(LOG_TAG, "No LocationManager");
            return false;
        }
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(LOG_TAG, "checkGPSProvider() GPS Provider disabled");
            // Ask user to enable GPS provider
            new AlertDialog.Builder(this).setMessage("Location is Off. Do you want to enable it?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int id) {
                    Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGpsIntent, INTENT_ENABLE_GPS_CODE);
                }}).create().show();
            return false;
        }
        else {
            Log.d(LOG_TAG, "checkGPSProvider() Provider Enabled, returns true");
            return true;
        }
    }

    private boolean requestLocationPermission() {
        boolean locationPermissionGranted = isLocationPermissionGranted();
        Log.d(LOG_TAG, String.format("requestLocationPermission(), location granted %b", locationPermissionGranted));
        if (!locationPermissionGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_CODE);
            return false;
        }
        else
            return true;
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_LOCATION_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        boolean shouldShowRationale = shouldShowRequestPermissionRationale(permissions[i]);
                        Log.d(LOG_TAG, String.format("onRequestPermissionsResult() %s: show rationale: %b", permissions[i], shouldShowRationale));
                        if (shouldShowRationale) {
                            Log.d(LOG_TAG, "onRequestPermissionsResult(), ask again");
                            ActivityCompat.requestPermissions(this, new String[]{permissions[i]}, PERMISSION_LOCATION_CODE);
                        }
                        else {
                            Log.d(LOG_TAG, "onRequestPermissionsResult(), do not!! ask again");
                            new AlertDialog.Builder(this).setMessage("Tracking a trip requires location permissions").setCancelable(false)
                                    .setPositiveButton("Go to App settings", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    startInstalledAppDetailsActivity(MapsActivity.this);
                                }}).setNegativeButton("Go back", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(MapsActivity.this, HomeActivity.class));
                                }
                            }).create().show();
                            return;
                        }
                    }
                }
                if (isLocationPermissionGranted()) {
                    Log.d(LOG_TAG, "onRequestPermissionsResult(), go to initActivity()");
                    initActivity(); // If all permissions were granted, init activity
                }
            }
    }

    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            Log.d(LOG_TAG, "startInstalledAppDetailsActivity() context is null");
            return;
        }
        final Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivityForResult(i, INTENT_APP_SETTINGS_CODE);
    }

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // ------------------------------- Map Setup ------------------------------- //

    @SuppressWarnings("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap == null) {
            Log.d(LOG_TAG, "onMapReady() returned null");
           makeErrorDialogToHome( "Google Maps unavailable");
            return;
        }
        // Init camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));

        // request Location updates
        mLocationCallback = getOnUpdateLocationCallBack();
        mFusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, null);
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
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

    private void stopMap() {
        // TODO save Map to JSON
        Intent toSummaryActivityIntent = new Intent(this, SummaryActivity.class);
        // TODO add to intent's bundle an indicator for this map's JSON file
        startActivity(new Intent(this, MapsActivity.class));
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    // ------------------------------- Location -------------------------------//

    public void setMarker(LatLng latLng, String markerTitle, int icon, MarkerTag tag) {
        if (icon == 0) { // Default marker
            mMap.addMarker(new MarkerOptions().position(latLng).title(markerTitle)).setTag(tag);
        } else { // Custom icon
            mMap.addMarker(new MarkerOptions().position(latLng).title(markerTitle).icon(BitmapDescriptorFactory.fromResource(icon))).setTag(tag);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void checkMovement(Location newLocation) {
        LatLng newLatLng = util_func.locToLatLng(newLocation);
        Log.d(LOG_TAG, String.format("update loc: lat: %f , lng: %f", newLatLng.latitude, newLatLng.longitude));
        // First update
        if (mCurrLocation == null) {
            Toast.makeText(this, "first Point!", Toast.LENGTH_SHORT).show();
            mCurrLocation = newLocation;
            mPolyline = mMap.addPolyline(new PolylineOptions().color(0xff388E3C).add(newLatLng));

        } else if (isNewLocation(mCurrLocation, newLocation)) {
            Toast.makeText(this, "new Point", Toast.LENGTH_SHORT).show();
            mCurrLocation = newLocation;
            // Add a point to route
            List<LatLng> points = mPolyline.getPoints();
            points.add(newLatLng);
            mPolyline.setPoints(points);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(util_func.locToLatLng(mCurrLocation)));
    }

    private boolean isNewLocation(Location currPos, Location newPos) {
        double tol = 0.0001;
        return (Math.abs(currPos.getLatitude() - newPos.getLatitude()) > tol || Math.abs(currPos.getLongitude() - newPos.getLongitude()) > tol);
    }


    // ------------------------------- Sidebar ------------------------------- //

    @Override public void onClick(View view) {
        switch (view.getId()) {
            case R.id.map_stop_button:
                stopMap();
                break;

            case R.id.map_camera:
                dispatchTakePictureIntent();
                break;

            case R.id.map_star:
                getRecommendations();
                break;

            case R.id.map_note:
                noteEditText.setVisibility(View.VISIBLE);
                noteFinishButton.setVisibility(View.VISIBLE);
                break;

            case R.id.map_marker:
                setMarker(util_func.locToLatLng(mCurrLocation), "", 0, new MarkerTag("marker"));
                break;

        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, INTENT_IMAGE_CAPTURE_CODE);
        }
    }

    private void dispatchTakePictureIntentFullSize() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d(LOG_TAG, "dispatchTakePictureIntent(), IOException" + ex.toString());
            }
            if (photoFile != null) {
                mCurrPhotoPath = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(this,"com.example.tom_e91.finalproj.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, INTENT_IMAGE_CAPTURE_CODE);
            }
        }
    }

    private File createImageFile() throws IOException{
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // Create File
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    public void finishButtonOnClick(View view) {
        String content = noteEditText.getText().toString();
        if (content.isEmpty()) {
            Toast.makeText(this, "Write something!", Toast.LENGTH_SHORT).show();
        } else {
            noteEditText.getText().clear();
            setMarker(util_func.locToLatLng(mCurrLocation), content, R.drawable.note20, new MarkerTag(getString(R.string.tag_note)));
            noteEditText.setVisibility(View.INVISIBLE);
            noteFinishButton.setVisibility(View.INVISIBLE);
        }
    }

    private void getRecommendations() {
        // TODO
    }

    // ------------------------------- utilities ------------------------------- //

    private void makeErrorDialogToHome(String msg) {
        new AlertDialog.Builder(this).setMessage(msg).setCancelable(false).setPositiveButton("Go back", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                startActivity(new Intent(MapsActivity.this, HomeActivity.class));
                finish();
            }}).create().show();
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
