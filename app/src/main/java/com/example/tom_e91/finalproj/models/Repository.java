package com.example.tom_e91.finalproj.models;

import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.tom_e91.finalproj.util.util_func;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a Singleton Repository class, that holds instances of the remote DB,
 * and reads and writes to the DB according to
 */
public class Repository {
    private static final String LOG_TAG = "nadir" + Repository.class.getSimpleName();

    private static Repository INSTANCE = null;
    private FirebaseFirestore remoteDB;

    // Location Data
    public MutableLiveData<List<MyLocation>> locationsLiveData = new MutableLiveData<>();
    public MutableLiveData<Location> currLocation = new MutableLiveData<>();
    public MutableLiveData<List<MyMarker>> markersLiveData = new MutableLiveData<>();

    // User Data
    private User mCurrentUser;
    private Trip mCurrentTrip;
    private DocumentReference mCurrentTripDocRef; // The Document that'll be updated in the remoteDB for this trip

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

    // ------------------------------- Getters & Setters ------------------------------- //

    private FirebaseFirestore getRemoteDB() {return remoteDB; }

    public void setCurrentUser(User currentUser) {
        mCurrentUser = currentUser;
    }

    public User getCurrentUser() { return mCurrentUser;}

    // ------------------------------- Current Trip ------------------------------- //

    /**
     * Creates new Trip Document in remoteDB and sets mCurrentTripDocRef
     */
    public void createNewTrip(final String tripName) {
        // Create Trip Document
        String uniqueTripName = tripName + Long.toString(System.currentTimeMillis());
        mCurrentTripDocRef = remoteDB.collection("Users").document(mCurrentUser.getEmail()).collection("Trips").document(uniqueTripName);

        // Init LiveData
        locationsLiveData.setValue(new ArrayList<MyLocation>());
        markersLiveData.setValue(new ArrayList<MyMarker>());

        // Create Trip Object
        mCurrentTrip = new Trip(tripName, new ArrayList<MyLocation>(), new ArrayList<MyMarker>());

        // Upload Trip object to DB
        mCurrentTripDocRef.set(mCurrentTrip);
    }

    // ------------------------------- DB Operations ------------------------------- //

    /**
     * adds new location to remote DB and updates MapsActivity
     */
    public void addLocation(@NonNull final Location newLocation) {
        final MyLocation newMyLocation = new MyLocation(newLocation);

        // If old point, don't update
        if (currLocation.getValue() != null) {
            if (!util_func.isNewLocation(new MyLocation(currLocation.getValue()), newMyLocation)) {
                Log.d(LOG_TAG, "addLocation() old Point");
                return;
            }
        }

        // Updates LiveData
        List<MyLocation> tempPoints = locationsLiveData.getValue();
        tempPoints.add(newMyLocation);
        locationsLiveData.setValue(tempPoints);

        // Updates remote DB
        mCurrentTripDocRef.update("locations", FieldValue.arrayUnion(newMyLocation));

        // Update last current location, must come at end
        currLocation.setValue(newLocation);
    }

    /**
     * add new marker to repository
     */
    public void addMarker(@NonNull final MyMarker myMarker) {
        Log.d(LOG_TAG, "addMarker() new marker");

        // Updates LiveData
        List<MyMarker> tempMarkers = markersLiveData.getValue();
        tempMarkers.add(myMarker);
        markersLiveData.setValue(tempMarkers);

        // Updates remote DB
        mCurrentTripDocRef.update("markers", FieldValue.arrayUnion(myMarker));
    }
}

