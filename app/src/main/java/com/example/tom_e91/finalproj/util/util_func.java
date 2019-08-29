package com.example.tom_e91.finalproj.util;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.EditText;

import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.models.MyLocation;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class util_func {
    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";


    public static boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // ------------------------------- Time ------------------------------- //

    public static String millisToDateTimeString(final long timeInMillis) {
        LocalDateTime localDateTime = new LocalDateTime(timeInMillis);
        return localDateTime.toString("yyyy-MM-dd HH:mm:ss a", Locale.getDefault());
    }

    public static String millisToHourMinString(final long timeInMillis) {
        LocalTime localTime = new LocalTime(timeInMillis);
        return localTime.toString("HH:mm a");
    }

    public static String getFormattedDurationString(final long startTime, final long endTime) {
        Duration duration = new Duration(startTime, endTime);
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendHours()
                .appendSuffix(" hour", " hours")
                .appendSeparator(", ")
                .appendMinutes()
                .appendSuffix(" minute", " minutes")
                .appendSeparator(", and ")
                .appendSeconds()
                .appendSuffix(" second", " seconds")
                .toFormatter();
        String durationString = formatter.print(duration.toPeriod());
        return durationString;
    }

    // ------------------------------- Location ------------------------------- //

    public static LatLng locationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static boolean isNewLocation(MyLocation currPos, MyLocation newPos) {
        if (currPos == null) {
            return true;
        }
        double tol = 0.0001;
        return (Math.abs(currPos.getLatitude() - newPos.getLatitude()) > tol || Math.abs(currPos.getLongitude() - newPos.getLongitude()) > tol);
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
