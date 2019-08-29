package com.example.tom_e91.finalproj.models;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.tom_e91.finalproj.tasks.FetchAddressTask;
import com.example.tom_e91.finalproj.util.Constants;
import com.example.tom_e91.finalproj.util.util_func;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

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
    private Context mAppContext;

    // Location Data
    public MutableLiveData<List<MyLocation>> locationsLiveData = new MutableLiveData<>();
    public MutableLiveData<Location> currLocation = new MutableLiveData<>();
    public MutableLiveData<List<MyMarker>> markersLiveData = new MutableLiveData<>();

    // User Data
    private User mCurrentUser;
    private Trip mCurrentTrip;
    private CollectionReference mTripsColReference;
    private DocumentReference mCurrentTripDocRef; // The Document that'll be updated in the remoteDB for this trip

    // ------------------------------- Constructors ------------------------------- //

    // Private Ctor for singleton
    private Repository(FirebaseFirestore remoteDB, Context appContext) {
        this.remoteDB = remoteDB;
        mAppContext = appContext;
    }

    public static synchronized Repository getInstance(FirebaseFirestore remoteDB, Context appContext) {
        if (INSTANCE == null) {
            INSTANCE = new Repository(remoteDB, appContext);
        }
        return INSTANCE;
    }

    // ------------------------------- Getters & Setters ------------------------------- //

    public void setCurrentUser(User currentUser) {
        mCurrentUser = currentUser;
        mTripsColReference =  remoteDB.collection("Users").document(mCurrentUser.getEmail()).collection("Trips");
    }

    public User getCurrentUser() { return mCurrentUser;}

    // ------------------------------- Current Trip ------------------------------- //

    /**
     * Creates new Trip Document in remoteDB and sets mCurrentTripDocRef
     */
    public void createNewTrip(final String tripName) {
        // Create Trip Document
        String curDateTime = util_func.millisToDateTimeString(System.currentTimeMillis());
        String uniqueTripName = tripName + " " + curDateTime;
        mCurrentTripDocRef = mTripsColReference.document(uniqueTripName);

        // Init LiveData
        locationsLiveData.setValue(new ArrayList<MyLocation>());
        markersLiveData.setValue(new ArrayList<MyMarker>());

        // Create Trip Object
        mCurrentTrip = new Trip(tripName);

        // Upload Trip object to DB
        mCurrentTripDocRef.set(mCurrentTrip);
    }

    public void endTrip() {
        mCurrentTrip.end();
        mCurrentTripDocRef.set(mCurrentTrip, SetOptions.mergeFields("endTime", "durationString", "distanceTraveled", "firstPhotoPath"));
        mCurrentTrip.locations = locationsLiveData.getValue();
        mCurrentTrip.markers = markersLiveData.getValue();
    }

    public Trip getCurrentTrip() {
        return mCurrentTrip;
    }

    public void setCurrentTrip(final Trip trip) {
        mCurrentTrip = trip;
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

        // Update Trip
        if (currLocation.getValue() != null)
            mCurrentTrip.distanceTraveled += currLocation.getValue().distanceTo(newLocation);

        // Update last current location, must come at end
        currLocation.setValue(newLocation);



    }

    /**
     * add new marker to repository
     */
    public void addMarker(@NonNull final MyMarker myMarker) {
        Log.d(LOG_TAG, "addMarker() new marker");

        // Update Trip
        if (myMarker.tag.equals(Constants.camera)) {
            if (mCurrentTrip.firstPhotoPath == null) {
                mCurrentTrip.firstPhotoPath = myMarker.imagePath;
            }
        }

        new FetchAddressTask(mAppContext, new FetchAddressTask.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(String address) {
                if (!address.isEmpty())
                    myMarker.address = address;
                else
                    Log.d(LOG_TAG, "address is empty");
                // Updates LiveData
                List<MyMarker> tempMarkers = markersLiveData.getValue();
                tempMarkers.add(myMarker);
                markersLiveData.setValue(tempMarkers);

                // Updates remote DB
                mCurrentTripDocRef.update("markers", FieldValue.arrayUnion(myMarker));
            }
        }).execute(myMarker.location);


    }

    public void getAllTrips(OnCompleteListener<QuerySnapshot> onCompleteListener) {
        mTripsColReference.get().addOnCompleteListener(onCompleteListener).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG_TAG, "getAllTrips() failed");
            }
        });
    }
}

