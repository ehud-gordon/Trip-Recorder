package com.example.tom_e91.finalproj.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tom_e91.finalproj.MyApplication;
import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.adapters.CustomInfoWindowAdapter;
import com.example.tom_e91.finalproj.models.MyLocation;
import com.example.tom_e91.finalproj.models.MyMarker;
import com.example.tom_e91.finalproj.models.Repository;
import com.example.tom_e91.finalproj.services.LocationUpdatesService;
import com.example.tom_e91.finalproj.util.Constants;
import com.example.tom_e91.finalproj.util.util_func;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, LocationSource {

    // Constants
    private static final String LOG_TAG = "nadir " + MapsActivity.class.getSimpleName();

    // Maps
    private GoogleMap mMap;
    private OnLocationChangedListener mLocationChangedListener;

    // Views
    EditText noteEditText;
    Button noteFinishButton;

    // Trip
    private Polyline mPolyline;
    private MyLocation mCurrLocation;
    private String mCurrPhotoPath;
    private Integer noteVisibility = View.GONE;
    private String NOTE_VISIBILITY_KEY = "NOTE_VISIBILITY_KEY";

    // Repository
    Repository repository;

    // --------------------------------- Service --------------------------------- //
    // The BroadcastReceiver used to listen to broadcasts from the service.
    private MyReceiver myReceiver;
    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;
    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.requestLocationUpdates();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    // ------------------------------- LifeCycle ------------------------------- //

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate()");
        repository = ((MyApplication)getApplicationContext()).getRepository();
        if (savedInstanceState != null) {
            noteVisibility = savedInstanceState.getInt(NOTE_VISIBILITY_KEY);
        }
        bindViews();

        // Check required Location permission
        if (checkMapServices()) {
            Log.d(LOG_TAG, "onCreate(), checkMapServices() True");

            // Service //
            myReceiver = new MyReceiver();

            initActivity();

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NOTE_VISIBILITY_KEY, noteVisibility);
    }

    private void bindViews() {
        // Hide title bar
        getSupportActionBar().hide();
        setContentView(R.layout.activity_maps);
        noteEditText = findViewById(R.id.note_edit_text);
        noteEditText.setVisibility(noteVisibility);
        noteFinishButton = findViewById(R.id.note_finish_button);
        noteFinishButton.setOnClickListener(this);
        noteFinishButton.setVisibility(noteVisibility);
        findViewById(R.id.map_stop_button).setOnClickListener(this);
        findViewById(R.id.map_star).setOnClickListener(this);
        findViewById(R.id.map_camera).setOnClickListener(this);
        findViewById(R.id.map_marker).setOnClickListener(this);
        findViewById(R.id.map_note).setOnClickListener(this);
    }

    @Override protected void onStart() {
        super.onStart();
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        mMap.setLocationSource(null);
        super.onStop();
    }

    private void initActivity() {
        Log.d(LOG_TAG, "initActivity()");

        // Initialize Google Map, calls onMapReady
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setupObservers() {

        repository.locations.observe(this, new Observer<List<MyLocation>>() {
            @Override
            public void onChanged(@Nullable List<MyLocation> myLocations) {
                if (myLocations != null && myLocations.size() > 0) {
                    Log.d(LOG_TAG, "locations from DB larger than 0");
                    List<LatLng> latLngList = util_func.myLocationsToLatLngs(myLocations);
                    updateCamera(myLocations.get(myLocations.size() - 1));
                    mPolyline.setPoints(latLngList);
                } else Log.d(LOG_TAG, "locations from DB is size 0");
            }
        });

        // Current Location Observer
        repository.currLocation.observe(this, new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                mCurrLocation = new MyLocation(location);
                mLocationChangedListener.onLocationChanged(location);
            }
        });

        repository.markersLiveData.observe(this, new Observer<List<MyMarker>>() {
            @Override
            public void onChanged(@Nullable List<MyMarker> myMarkers) {
                if (myMarkers != null && myMarkers.size() > 0) {
                    Log.d(LOG_TAG,"markersLiveData observe update");
                    for (MyMarker myMarker : myMarkers) {
                        addMarkerToMap(myMarker);
                    }
                } else {
                    Log.d(LOG_TAG, "markersLiveData observe size 0!");
                }
            }
        });

        repository.getMarkers();


    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case Constants.INTENT_ENABLE_GPS_CODE:
                if (resultCode == RESULT_OK) {
                    Log.d(LOG_TAG, "onActivityResult(), ENABLE_GPS_CODE OK");
                    requestLocationPermission();
                } else {
                    makeErrorDialogToHome("Tracking a trip requires Location services");
                    Log.d(LOG_TAG, "onActivityResult(), ENABLE_GPS_CODE failed");
                }
                break;

            case Constants.INTENT_IMAGE_CAPTURE_CODE:
                if (resultCode == RESULT_OK) {
                    Log.d(LOG_TAG, "onActivityResult(), RESULT_OK");

                    // Get Bitmap
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");

                    // Create marker with the resulting bitmap image
                    MyMarker myMarker = new MyMarker(Constants.camera, mCurrLocation).setBitmap(imageBitmap);
                    addMarkerToDbAndMap(myMarker);
                } else {
                    Toast.makeText(this, "No image was captured", Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "onActivityResult(), RESULT failed");
                }

            case Constants.INTENT_APP_SETTINGS_CODE:
                Log.d(LOG_TAG, String.format("onActivityResult(), APP_SETTINGS, result: %d", resultCode));
                if (isLocationPermissionGranted()) {
                    initActivity();
                } else makeErrorDialogToHome("Tracking a trip requires Location services.");
        }
    }

    // ------------------------------- Map Setup ------------------------------- //

    @SuppressWarnings("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap == null) {
            Log.d(LOG_TAG, "onMapReady() returned null");
            makeErrorDialogToHome("Google Maps unavailable");
            return;
        }
        // Init Map
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Constants.DEFAULT_LAT_LNG, Constants.DEFAULT_ZOOM));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));
        mMap.setLocationSource(this);
        mMap.setMyLocationEnabled(true);

        mPolyline = initPolyline();

        // Start Observing for changes
        setupObservers();


    }

    public void addMarkerToDbAndMap(MyMarker myMarker) {
        repository.addMarker(myMarker);
        addMarkerToMap(myMarker);
    }

    public void addMarkerToMap(MyMarker marker) {
        Log.d(LOG_TAG, "addMarkerToMap() for " + marker.location);
        LatLng markerLatLng = marker.location.toLatLng();
        switch (marker.tag) {
            case Constants.marker:
                mMap.addMarker(new MarkerOptions().position(markerLatLng)).setTag(marker);
                break;

            case Constants.camera:
                mMap.addMarker(new MarkerOptions().position(markerLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_color25))).setTag(marker);
                break;

            case Constants.note:
                mMap.addMarker(new MarkerOptions().position(markerLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.note20))).setTag(marker);
                break;

            case Constants.star:
                break;
        }
    }

    private Polyline initPolyline() {
        Polyline polyline = mMap.addPolyline(new PolylineOptions());
        polyline.setColor(Constants.COLOR_GREEN_ARGB);
        polyline.setEndCap(new CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 30));
        return polyline;
    }

    private void updateCamera(MyLocation myLocation) {
        LatLng currLatLng = myLocation.toLatLng();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currLatLng));
    }

    // ------------------------------- Sidebar ------------------------------- //

    @Override public void onClick(View view) {
        switch (view.getId()) {

            case R.id.map_stop_button:
                mService.removeLocationUpdates();
                startActivity(new Intent(this, SummaryActivity.class));
                finish();
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
                noteVisibility = View.VISIBLE;
                break;

            case R.id.note_finish_button:
                finishButtonOnClick();
                break;

            case R.id.map_marker:
                MyMarker myMarker = new MyMarker(Constants.marker, mCurrLocation);
                addMarkerToDbAndMap(myMarker);
                break;

        }
    }

    public void finishButtonOnClick() {
        String content = noteEditText.getText().toString();
        if (content.isEmpty()) {
            Toast.makeText(this, "Write something!", Toast.LENGTH_SHORT).show();
        } else {
            noteEditText.setVisibility(View.INVISIBLE);
            noteFinishButton.setVisibility(View.INVISIBLE);
            noteVisibility = View.GONE;
            MyMarker myMarker = new MyMarker(Constants.note, mCurrLocation).setNoteContent(content);
            addMarkerToDbAndMap(myMarker);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, Constants.INTENT_IMAGE_CAPTURE_CODE);
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
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.tom_e91.finalproj.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, Constants.INTENT_IMAGE_CAPTURE_CODE);
            }
        }
    }

    private void getRecommendations() {
        // TODO
    }

    // ------------------------------- utilities ------------------------------- //

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // Create File
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    private void makeErrorDialogToHome(String msg) {
        new AlertDialog.Builder(this).setMessage(msg).setCancelable(false).setPositiveButton("Go back", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                startActivity(new Intent(MapsActivity.this, HomeActivity.class));
                finish();
            }
        }).create().show();
    }

    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            Log.d(LOG_TAG, "startInstalledAppDetailsActivity() context is null");
            return;
        }
        final Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivityForResult(i, Constants.INTENT_APP_SETTINGS_CODE);
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
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, Constants.ERROR_DIALOG_REQUEST);
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
                    startActivityForResult(enableGpsIntent, Constants.INTENT_ENABLE_GPS_CODE);
                }
            }).create().show();
            return false;
        } else {
//            Log.d(LOG_TAG, "checkGPSProvider() Provider Enabled, returns true");
            return true;
        }
    }
    private boolean requestLocationPermission() {
        boolean locationPermissionGranted = isLocationPermissionGranted();
//        Log.d(LOG_TAG, String.format("requestLocationPermission(), location granted %b", locationPermissionGranted));
        if (!locationPermissionGranted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.PERMISSION_LOCATION_CODE);
            return false;
        } else return true;
    }
    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.PERMISSION_LOCATION_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        boolean shouldShowRationale = shouldShowRequestPermissionRationale(permissions[i]);
                        Log.d(LOG_TAG, String.format("onRequestPermissionsResult() %s: show rationale: %b", permissions[i], shouldShowRationale));
                        if (shouldShowRationale) {
                            Log.d(LOG_TAG, "onRequestPermissionsResult(), ask again");
                            ActivityCompat.requestPermissions(this, new String[]{permissions[i]}, Constants.PERMISSION_LOCATION_CODE);
                        } else {
                            Log.d(LOG_TAG, "onRequestPermissionsResult(), do not!! ask again");
                            new AlertDialog.Builder(this).setMessage("Tracking a trip requires location permissions").setCancelable(false).setPositiveButton("Go to App settings", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    startInstalledAppDetailsActivity(MapsActivity.this);
                                }
                            }).setNegativeButton("Go back", new DialogInterface.OnClickListener() {
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
    private boolean isLocationPermissionGranted() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // ------------------------------- Location Source ------------------------------- //


    @Override public void activate(OnLocationChangedListener onLocationChangedListener) {
        mLocationChangedListener = onLocationChangedListener;
    }

    @Override public void deactivate() {
        mLocationChangedListener = null;
    }

    // ------------------------------- Service ------------------------------- //

    // TODO Not Currently Used
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null)
                Log.d(LOG_TAG, "Receiver Update " + location);
        }
    }
}
