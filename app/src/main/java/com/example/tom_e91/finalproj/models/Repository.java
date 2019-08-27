package com.example.tom_e91.finalproj.models;

import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.util.Log;

import com.example.tom_e91.finalproj.util.util_func;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a Singleton Repository class, that holds instances of the remote DB,
 * and reads and writes to the DB according to
 */
public class Repository {
    private static final String LOG_TAG = "nadir" + Repository.class.getSimpleName();


    private static Repository INSTANCE = null;
    private FirebaseFirestore remoteDB;

    // Location Data
    public MutableLiveData<List<MyLocation>> locations = new MutableLiveData<>();
    public MutableLiveData<Location> currLocation = new MutableLiveData<>();
    public MutableLiveData<List<MyMarker>> markersLiveData = new MutableLiveData<>();

    // User Data
    private User curUser;
    private DocumentReference mCurrentTripDocRef; // The Document that'll be updated in the remoteDB for this trip
    private CollectionReference mCurrentMarkerCollectionRef; // The Marker Collection that'll be updated in the remoteDB for this trip

    // ------------------------------- Constructors ------------------------------- //

    private Repository(FirebaseFirestore remoteDB) {
        this.remoteDB = remoteDB;
    }

    public static synchronized Repository getInstance(FirebaseFirestore remoteDB) {
        if (INSTANCE == null) {
            INSTANCE = new Repository(remoteDB);
        }
        return INSTANCE;
    }

    // ------------------------------- Current User------------------------------- //

    public void setCurUser(User currentUser) {
        curUser = currentUser;
    }

    /**
     * Creates new Trip Document in remoteDB and sets mCurrentTripDocRef
     */
    public void createNewTrip(final String tripName) {
        // Create Trip Document

        String uniqueTripName = tripName + Long.toString(System.currentTimeMillis());
        mCurrentTripDocRef =  remoteDB.collection("Users").document(curUser.getEmail()).collection("Trips").document(uniqueTripName);
        mCurrentTripDocRef.set(new HashMap<String, Object>() {{ put("Trip Name", tripName); }}, SetOptions.merge());

        // Create Marker Collection in Trip Document
//        mCurrentMarkerCollectionRef = mCurrentTripDocRef.collection("Markers");
    }

    // ------------------------------- DB Operations ------------------------------- //

    /**
     *  adds new location to remote DB and updates MapsActivity
     */
    public void addLocation(final Location newLocation) {
        if (newLocation == null) {
            Log.d(LOG_TAG, "addLocation, newlocation is null");
            return;
        }
        final MyLocation newMyLocation = new MyLocation(newLocation);

        // If first Location update we receive
        if (locations.getValue() == null) {
            Log.d(LOG_TAG, "addLocation() first time");

            // Updates LiveData
            locations.setValue(new ArrayList<MyLocation>(){{add(newMyLocation);}});

            // Updates remote DB
            Map<String, Object> map = new HashMap<>();
            map.put("Locations", Arrays.asList(newLocation));
            mCurrentTripDocRef.set(map, SetOptions.merge());

            // Update last current location, must come at end
            currLocation.setValue(newLocation);
        }

        // Else if not first Location update
        else {
            // Update Locations only if moved from previous location
            MyLocation currMyLocation = new MyLocation(currLocation.getValue());
            if (util_func.isNewLocation(currMyLocation, newMyLocation)) {
                Log.d(LOG_TAG, "addLocation() new Point");

                // Updates LiveData
                List<MyLocation> tempPoints = locations.getValue();
                tempPoints.add(newMyLocation);
                locations.setValue(tempPoints);

                // Updates remote DB
                mCurrentTripDocRef.update("Locations", FieldValue.arrayUnion(newMyLocation));

                // Update last current location, must come at end
                currLocation.setValue(newLocation);

            } else { // If not new location
                Log.d(LOG_TAG, "addLocation() Old point");
            }
        }




    }

    public void addMarker(final MyMarker myMarker) {
        if (myMarker == null) {
            Log.d(LOG_TAG, "addMarker(), myMarker is null");
            return;
        }
        // If first Marker update we receive
        if (markersLiveData.getValue() == null) {
            Log.d(LOG_TAG, "addMarker() first time");

            // Updates LiveData
            markersLiveData.setValue(new ArrayList<MyMarker>(){{add(myMarker);}});

            // Updates remote DB
            Map<String, Object> map = new HashMap<>();
            map.put("Markers", Arrays.asList(myMarker));
            mCurrentTripDocRef.set(map, SetOptions.merge());

        }

        // Else if not first marker update
        else {
            Log.d(LOG_TAG, "addMarker() new marker");
            // Updates LiveData
            List<MyMarker> tempMarkers = markersLiveData.getValue();
            tempMarkers.add(myMarker);
            markersLiveData.setValue(tempMarkers);

            // Updates remote DB
            mCurrentTripDocRef.update("Markers", FieldValue.arrayUnion(myMarker));
        }
    }
}

