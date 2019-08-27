package com.example.tom_e91.finalproj.models;

import android.graphics.Bitmap;
import android.location.Location;
import android.support.annotation.NonNull;

import com.example.tom_e91.finalproj.util.util_func;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class MarkerTag {
    public String tag;
    public Location location;

    public LatLng position;
    public Date date;

    // In case this is a camera-marker
    public Bitmap bitmap;
    public String imagePath;

    // In case this is a note-marker
    public String noteContent;

    // ------------------------------- Constructors ------------------------------- //

    public MarkerTag() {}

    public MarkerTag(@NonNull String tag, Location location) {
        this.tag = tag;
        this.location = location;
        position = util_func.locationToLatLng(location);
        date = new Date(location.getTime());
    }

    // ------------------------------- Getters & Setters ------------------------------- //

    public String getTag() {
        return tag;
    }

    public Location getLocation() {
        return location;
    }

    public LatLng getPosition() {
        return position;
    }

    public Date getDate() {
        return date;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getImagePath() {
        return imagePath;
    }

    public MarkerTag setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    public MarkerTag setImagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public MarkerTag setNoteContent(String noteContent) {
        this.noteContent = noteContent;
        return this;
    }
}
