package com.example.tom_e91.finalproj.models;

import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.tom_e91.finalproj.util.util_func;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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
        mCurrentMarkerCollectionRef = mCurrentTripDocRef.collection("Markers");
    }

    // ------------------------------- DB Operations ------------------------------- //

    /**
     *  A call to the remoteDB to get all current trip locations
     */
    // TODO is this necessary?
    public void getLocations() {
        mCurrentTripDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d(LOG_TAG, "onSuccess()  getLocations");
                Map<String, Object> map = documentSnapshot.getData();
                if (map != null && map.get("Locations") != null) {
                    Log.d(LOG_TAG, "onSuccess()  getLocations map is not null");
                    List<MyLocation> myLocations = (List<MyLocation>) map.get("Locations");
                    locations.setValue(myLocations);
                }
                else {
                    Log.d(LOG_TAG, "onSuccess()  getLocations map or locations is null !!");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG_TAG, "repository getLocations() failed");
            }
        });
    }


    /**
     *  adds new location to remote DB and updates MapsActivity
     */
    public void addLocation(final Location newLocation) {
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
            } else { // If not new location
                Log.d(LOG_TAG, "addLocation() Old point");
            }
        }

        // Update last current location, must come at end of function
        currLocation.setValue(newLocation);


    }

    // Markers //

    public void addMarker(MyMarker myMarker) {
        mCurrentMarkerCollectionRef.add(myMarker);
    }

    public void getMarkers() {
        mCurrentMarkerCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<MyMarker> myMarkerList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(LOG_TAG, document.getId() + " great success ");
                        MyMarker myMarker = document.toObject(MyMarker.class);
                        myMarkerList.add(myMarker);
                    }
                    // Update LiveData
                    markersLiveData.setValue(myMarkerList);
                } else {
                    Log.d(LOG_TAG, "getMarkers Error getting documents: ", task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG_TAG, "getMarkers() failed");
            }
        });
    }
}

