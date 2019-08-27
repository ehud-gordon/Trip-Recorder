package com.example.tom_e91.finalproj;

import android.app.Application;

import com.example.tom_e91.finalproj.models.Repository;
import com.example.tom_e91.finalproj.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyApplication extends Application {
    private final static String LOG_TAG = "nadir" + MyApplication.class.getSimpleName();
    private User user = null;
    FirebaseFirestore remoteDB;
    Repository repository;

    // -------------------------- LifeCycle -------------------------- //

    @Override public void onCreate() {
        super.onCreate();
        remoteDB = FirebaseFirestore.getInstance();
        repository = Repository.getInstance(remoteDB);
    }



    // -------------------------- Repository -------------------------- //

    public Repository getRepository() {
        return repository;
    }

    public FirebaseFirestore getRemoteDB() {
        return remoteDB;
    }

    // -------------------------- User -------------------------- //

    public void setUser(User user) {
        this.user = user;
        repository.setCurUser(user);
    }

    public User getUser() {return user;}
}
