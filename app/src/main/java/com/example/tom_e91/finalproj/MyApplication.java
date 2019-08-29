package com.example.tom_e91.finalproj;

import android.app.Application;

import com.example.tom_e91.finalproj.models.Repository;
import com.example.tom_e91.finalproj.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MyApplication extends Application {
    private final static String LOG_TAG = "nadir" + MyApplication.class.getSimpleName();
    Repository repository;

    // -------------------------- LifeCycle -------------------------- //

    @Override public void onCreate() {
        super.onCreate();
        FirebaseFirestore remoteDB = FirebaseFirestore.getInstance();
        remoteDB.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build());
        repository = Repository.getInstance(remoteDB, getApplicationContext());
    }

    // -------------------------- Repository -------------------------- //

    public Repository getRepository() {
        return repository;
    }

    // -------------------------- User -------------------------- //

    public void setUser(User user) {
        repository.setCurrentUser(user);
    }

    public User getUser() {return repository.getCurrentUser();}
}
