package com.example.tom_e91.finalproj.models;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public class MyMarker {
    public String tag;
    public MyLocation location;

    // In case this is a camera-marker
    public Bitmap bitmap;
    public String imagePath;

    // In case this is a note-marker
    public String noteContent;

    // ------------------------------- Constructors ------------------------------- //

    public MyMarker() {}

    public MyMarker(@NonNull String tag, MyLocation location) {
        this.tag = tag;
        this.location = location;
    }

    // ------------------------------- Getters & Setters ------------------------------- //


    public String getTag() {
        return tag;
    }

    public MyMarker setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public MyLocation getLocation() {
        return location;
    }

    public MyMarker setLocation(MyLocation location) {
        this.location = location;
        return this;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public MyMarker setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    public String getImagePath() {
        return imagePath;
    }

    public MyMarker setImagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public MyMarker setNoteContent(String noteContent) {
        this.noteContent = noteContent;
        return this;
    }
}
