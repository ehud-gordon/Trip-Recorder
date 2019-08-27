package com.example.tom_e91.finalproj.util;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.EditText;

import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.models.MyLocation;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class util_func {
    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";


    public static boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // ------------------------------- Location ------------------------------- //

    public static LatLng locationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static boolean isNewLocation(MyLocation currPos, MyLocation newPos) {
        double tol = 0.0001;
        return (Math.abs(currPos.getLatitude() - newPos.getLatitude()) > tol || Math.abs(currPos.getLongitude() - newPos.getLongitude()) > tol);
    }

    public static List<LatLng> myLocationsToLatLngs(List<MyLocation> locations) {
        List<LatLng> latLngList = new ArrayList<>();
        for (MyLocation myLocation : locations)
            latLngList.add(myLocation.toLatLng());
        return latLngList;
    }

    // ------------------------------- Service ------------------------------- //

    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /** Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state. */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    public static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }



}
