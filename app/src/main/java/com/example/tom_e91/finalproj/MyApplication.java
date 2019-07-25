package com.example.tom_e91.finalproj;

import android.app.Application;

public class MyApplication extends Application {
    private User user = null;

    public User getUser() {return user;}

    public void setUser(User user) {this.user = user;}
}
