package com.example.tom_e91.finalproj.models;

import android.support.annotation.NonNull;

public class User {
    private String email;

    // ----------------------------- Constructors ---------------------- //
    User() {}

    public User(String email) {
        this.email = email;
    }

    // --------------------------- Getters & Setters ------------------------- //

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // --------------------------- Util ------------------------- //

    private static String getUsernameFromEmail(@NonNull final String email) {
        return email.substring(0, email.indexOf("@"));
    }
}
