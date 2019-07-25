package com.example.tom_e91.finalproj;

public class User {
    private String email, user_id, username;

    // ----------------------------- Constructors ---------------------- //
    User() {}
    User(String email, String user_id, String username) {
        this.email = email;
        this.user_id = user_id;
        this.username = username;
    }

    // --------------------------- Getters & Setters -------------------------//

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
