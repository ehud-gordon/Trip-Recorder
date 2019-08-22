package com.example.tom_e91.finalproj.models;

import android.graphics.Bitmap;

public class MarkerData {
    public String tag;
    private Bitmap bitmap;

    public MarkerData(String tag) {
        this.tag = tag;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

}
