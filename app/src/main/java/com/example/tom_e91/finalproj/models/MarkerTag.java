package com.example.tom_e91.finalproj.models;

import android.graphics.Bitmap;

public class MarkerTag {
    public String tag;
    private Bitmap bitmap;
    private String imagePath;

    public MarkerTag(String tag) {
        this.tag = tag;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public MarkerTag setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    public String getImagePath() {
        return imagePath;
    }

    public MarkerTag setImagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }



}
