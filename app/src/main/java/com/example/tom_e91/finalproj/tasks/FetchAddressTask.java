package com.example.tom_e91.finalproj.tasks;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.example.tom_e91.finalproj.models.MyLocation;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * AsyncTask for reverse geocoding coordinates into a physical address.
 */
public class FetchAddressTask extends AsyncTask<MyLocation, Void, String>{
    private final String LOG_TAG = "nadir" + FetchAddressTask.class.getSimpleName();

    private Context mContext;
    private OnTaskCompleted mListener;

    public FetchAddressTask(Context applicationContext, OnTaskCompleted listener) {
        mContext = applicationContext;
        mListener = listener;
    }

    @Override protected String doInBackground(MyLocation... myLocations) {
        // Set up the geocoder
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

        // Get the passed in location
        MyLocation location = myLocations[0];
        List<Address> addresses = null;
        String resultAddress = "";

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems
            Log.e(LOG_TAG, "GeoCoding error ", ioException);
            return resultAddress;
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values
            Log.e(LOG_TAG, String.format("Illegal lat long used, Lat: %f, Lng: %f", location.getLatitude(), location.getLongitude()), illegalArgumentException);
            return resultAddress;
        }

        // If no addresses found, print an error message.
        if (addresses == null || addresses.size() == 0) {
                Log.e(LOG_TAG, "No addressed found");
        } else {
            // If an address is found, read it into resultAddress
            Address address = addresses.get(0);

            String thoroughfare = address.getThoroughfare();
            if (thoroughfare != null && !thoroughfare.isEmpty()) {
                String subT = (address.getSubThoroughfare() == null) ? "" : address.getSubThoroughfare();
                String subLoc = (address.getSubLocality() == null) ? "" : address.getSubLocality();
                String locality = (address.getLocality() == null) ? "" : address.getLocality();
                resultAddress = subT + " " + thoroughfare + ", " + subLoc + " " + locality;
            } else {
                resultAddress = address.getAddressLine(0);
            }
        }
        return resultAddress;
    }

    @Override protected void onPostExecute(String address) {
        super.onPostExecute(address);
        mListener.onTaskCompleted(address);
    }

    public interface OnTaskCompleted {
        void onTaskCompleted(String result);
    }
}
