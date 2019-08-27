package com.example.tom_e91.finalproj.util;

import com.google.android.gms.maps.model.LatLng;

public class Constants {
    // Bundle
    public static final String TRIP_NAME_KEY = "TRIP_NAME";

    // Permissions
    public static final int PERMISSION_LOCATION_CODE = 1;

    // Dialogs
    public static final int ERROR_DIALOG_REQUEST = 500;

    // Intents
    public static final int INTENT_IMAGE_CAPTURE_CODE = 1000;
    public static final int INTENT_ENABLE_GPS_CODE = 1001;
    public static final int INTENT_APP_SETTINGS_CODE = 1002;

    // Points Googleplex
    public static final LatLng p1 = new LatLng(37.422, -122.084);
    public static final LatLng p2 = new LatLng(37.4223, -122.084);
    public static final LatLng p3 = new LatLng(37.4226, -122.084);
    public static final LatLng p4 = new LatLng(37.4229, -122.084);

    public static final LatLng DEFAULT_LAT_LNG = p1; // A default location
    public static final int DEFAULT_ZOOM = 18;

    // Colors
    public static final int COLOR_BLACK_ARGB = 0xff000000;
    public static final int COLOR_WHITE_ARGB = 0xffffffff;
    public static final int COLOR_GREEN_ARGB = 0xff388E3C;
    public static final int COLOR_PURPLE_ARGB = 0xff81C784;
    public static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    public static final int COLOR_BLUE_ARGB = 0xffF9A825;

    // Sidebar
    public static final String note = "note";
    public static final String camera = "camera";
    public static final String star = "star";
    public static final String marker = "marker";

    // Firebase
    public final static String USERS_COLLECTION_NAME = "Users";


}
