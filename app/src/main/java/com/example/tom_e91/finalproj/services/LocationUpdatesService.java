package com.example.tom_e91.finalproj.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.tom_e91.finalproj.MyApplication;
import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.models.Repository;
import com.example.tom_e91.finalproj.ui.MapsActivity;
import com.example.tom_e91.finalproj.util.util_func;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static android.support.constraint.Constraints.TAG;

public class LocationUpdatesService extends Service {

    // Constants
    private final static String LOG_TAG = "nadir" + LocationUpdatesService.class.getSimpleName();

    // Location
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private Location mLocation;


    private static final String PACKAGE_NAME = "com.example.tom_e91.finalproj.services";

    private static final String CHANNEL_ID = "channel_01"; // The name of the channel for notifications

    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME + ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    /** The identifier for the notification displayed for the foreground service. */
    private static final int NOTIFICATION_ID = 12345678;

    /** Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes place. */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    private Handler mServiceHandler;

    private Repository repository;


    // ------------------------------- LifeCycle ------------------------------- //

    @Override public void onCreate() {
        super.onCreate();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = getLocationRequest();
        mLocationCallback = new LocationCallback() {
            @Override public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        repository = ((MyApplication)getApplicationContext()).getRepository();

        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Service started");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    // ------------------------------- Binder ------------------------------- //
    /** Class used for the client Binder.  Since this service runs in the same process as its clients, we don't need to deal with IPC. */
    public class LocalBinder extends Binder {
        public LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }

    @Nullable @Override public IBinder onBind(Intent intent) {
        // Called when a client (MapsActivity) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service when that happens.
        Log.d(LOG_TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override public void onRebind(Intent intent) {
        // Called when a client (MapsActivity) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground service when that happens.
        Log.d(LOG_TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "Last client unbound from service");

        // Called when the last client (MapsActivity) unbinds from this service.
        // If this method is called due to a configuration change in MainActivity, we do nothing.
        // Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && util_func.requestingLocationUpdates(this)) {
            Log.d(LOG_TAG, "Starting foreground service");

            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    // ------------------------------- Location  ------------------------------- //

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void onNewLocation(Location location) {
        Log.d(LOG_TAG, "Service New location: " + location);
        mLocation = location;
        repository.addLocation(location);

        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        // Update notification content if running as a foreground service.
        if (isServiceRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    /** Makes a request for location updates. Note that in this sample we merely log the SecurityException */
    public void requestLocationUpdates() {
        Log.d(LOG_TAG, "Requesting location updates");
        // TODO changed from github source code
        if (!util_func.requestingLocationUpdates(this)) {
            util_func.setRequestingLocationUpdates(this, true);
            startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
            try {
                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            } catch (SecurityException unlikely) {
                util_func.setRequestingLocationUpdates(this, false);
                Log.d(LOG_TAG, "Lost location permission. Could not request updates. " + unlikely);
            }
        } else {
            Log.d(LOG_TAG, "Already Requested Location updates");
        }
    }

    /** Removes location updates. Note that in this sample we merely log the SecurityException. */
    public void removeLocationUpdates() {
        Log.d(LOG_TAG, "Removing location updates");
        try {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            util_func.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            util_func.setRequestingLocationUpdates(this, true);
            Log.d(LOG_TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }


    private void getLastLocation() {
        try {
            mFusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.d(LOG_TAG, "Lost location permission." + unlikely);
        }
    }

    // ------------------------------- Notification ------------------------------- //

    private Notification getNotification() {
        Intent intent = new Intent(this, LocationUpdatesService.class);

        CharSequence text = util_func.getLocationText(mLocation);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);


        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MapsActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
                        activityPendingIntent)
                .setContentText(text)
                .setContentTitle(util_func.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    // ------------------------------- utilities ------------------------------- //

    /** Returns true if this is a foreground service. */
    public boolean isServiceRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

}
