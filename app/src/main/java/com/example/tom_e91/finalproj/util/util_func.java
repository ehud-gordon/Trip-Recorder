package com.example.tom_e91.finalproj.util;

import android.location.Location;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

public class util_func {
    public static boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    public static LatLng locToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}
