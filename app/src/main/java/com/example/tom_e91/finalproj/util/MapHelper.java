package com.example.tom_e91.finalproj.util;

import android.support.annotation.NonNull;

import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.models.MyLocation;
import com.example.tom_e91.finalproj.models.MyMarker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapHelper {
    public static void addMarkerToMap(GoogleMap map, @NonNull MyMarker marker) {
        LatLng markerLatLng = marker.location.toLatLng();
        String title = marker.getTime() + " " + marker.getAddress();
        switch (marker.tag) {
            case Constants.marker:
                map.addMarker(new MarkerOptions().position(markerLatLng).title(title)).setTag(marker);
                break;

            case Constants.camera:
                map.addMarker(new MarkerOptions().position(markerLatLng).title(title)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_color25))).setTag(marker);
                break;

            case Constants.note:
                map.addMarker(new MarkerOptions().position(markerLatLng).title(title)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.note20))).setTag(marker);
                break;

            case Constants.star:
                break;
        }
    }

    public static Polyline initPolyline(GoogleMap map) {
        Polyline polyline = map.addPolyline(new PolylineOptions());
        polyline.setColor(Constants.COLOR_GREEN_ARGB);
        polyline.setEndCap(new CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 30));
        return polyline;
    }

    public static List<LatLng> myLocationsToLatLngs(List<MyLocation> locations) {
        List<LatLng> latLngList = new ArrayList<>();
        for (MyLocation myLocation : locations)
            latLngList.add(myLocation.toLatLng());
        return latLngList;
    }
}
