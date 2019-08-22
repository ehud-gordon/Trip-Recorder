package com.example.tom_e91.finalproj;

import android.app.Application;

import com.example.tom_e91.finalproj.models.User;

public class MyApplication extends Application {
    private User user = null;

    @Override public void onCreate() {
        super.onCreate();
    }

    public User getUser() {return user;}

    public void setUser(User user) {this.user = user;}
}
