package com.example.tom_e91.finalproj.models;

import android.arch.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.tom_e91.finalproj.util.Constants;
import com.example.tom_e91.finalproj.util.util_func;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
    public MutableLiveData<List<Location>> locations = new MutableLiveData<>();
    public MutableLiveData<Location> currLocation = new MutableLiveData<>();
    public MutableLiveData<List<MarkerTag>> markersLiveData = new MutableLiveData<>();

    // User Data
    private User curUser;
    private DocumentReference currentTripDocRef; // The document that'll be updated in the remoteDB for this trip
    private boolean hasAddedMarker = false;

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
     * Creates new Trip Document in remoteDB and sets currentTripDocRef
     */
    public void createNewTrip() {
        currentTripDocRef =  remoteDB.collection("Users").document(curUser.getEmail()).collection("Trips").document();
    }

    // ------------------------------- DB Operations ------------------------------- //

    // Locations //

    /**
     *  A call to the remoteDB to get all current trip locations
     */
    // TODO is this necessary?
    public void getLocations() {
        currentTripDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d(LOG_TAG, "onSuccess()  getLocations");
                Map<String, Object> map = documentSnapshot.getData();
                if (map != null) {
                    Log.d(LOG_TAG, "onSuccess()  getLocations map is not null");
                    locations.setValue((List<Location>)(map.get("Locations")));
                } else {
                    Log.d(LOG_TAG, "onSuccess()  getLocations map is!! null");
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

        // If first Location update we receive
        if (locations.getValue() == null) {
            Log.d(LOG_TAG, "addLocation() first time");
            // Updates LiveData
            locations.setValue(new ArrayList<Location>(){{add(newLocation);}});
            // Updates remote DB
            Map<String, Object> map = new HashMap<>();
            map.put("Locations", Arrays.asList(newLocation));
            currentTripDocRef.set(map, SetOptions.merge());
        }

        // Else if not first Location update
        else {
            // Update Locations only if moved from previous location
            if (util_func.isNewLocation(currLocation.getValue(),newLocation)) {
                Log.d(LOG_TAG, "addLocation() new Point");
                // Updates LiveData
                List<Location> tempPoints = locations.getValue();
                tempPoints.add(newLocation);
                locations.setValue(tempPoints);
                // Updates remote DB
                currentTripDocRef.update("Locations", FieldValue.arrayUnion(newLocation));
            } else {
                Log.d(LOG_TAG, "addLocation() Old point");
            }
        }

        // Update last current location
        currLocation.setValue(newLocation);
    }

    // Markers //

    public void addMarker(MarkerTag markerTag) {
        if (!hasAddedMarker) {
            Log.d(LOG_TAG, "addMarker() first time");
            // Updates remote DB
            Map<String, Object> map = new HashMap<>();
            map.put("Markers", Arrays.asList(markerTag));
            currentTripDocRef.set(map, SetOptions.merge());
            hasAddedMarker = true;
        } else {
            Log.d(LOG_TAG, "addMarker() to array");
            // Updates remote DB
            currentTripDocRef.update("Markers", FieldValue.arrayUnion(markerTag));
        }
    }

    public void getMarkers() {
        currentTripDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d(LOG_TAG, "getMarkers() success");
                Map<String, Object> map = documentSnapshot.getData();
                if (map == null) {
                    Log.d(LOG_TAG, "getMarkers() map is null");
                    return;
                }
                List<HashMap<String, Object>> objectList = (List<HashMap<String, Object>>) map.get("Markers");

                if (objectList == null || objectList.size() == 0) {
                    Log.d(LOG_TAG, "getMarkers() empty");
                    return;
                }
                else { // Have markers to create
                    // Convert Objects to MarkerTag
                    List<MarkerTag> markerTagList = new ArrayList<>();
                    for (HashMap<String, Object> markerMap : objectList) {
                        MarkerTag markerTag = fromHashMapToMarkerTag(markerMap);
                        markerTagList.add(markerTag);
                    }

                    Log.d(LOG_TAG, "getMarkers() has markersLiveData");
                    markersLiveData.setValue(markerTagList);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(LOG_TAG, "getMarkers() fail!!");
            }
        });
    }

    private MarkerTag fromHashMapToMarkerTag (Map <String, Object> map) {
        String tag = (String) map.get("tag");
        Location location = (Location) map.get("location");
        MarkerTag markerTag = new MarkerTag(tag, location);
        if (tag.equals(Constants.camera)) {
            Bitmap bitmap = (Bitmap) map.get("bitmap");
            String imagePath = (String) map.get("imagePath");
            markerTag.setBitmap(bitmap).setImagePath(imagePath);
        }
        return markerTag;
    }

}

