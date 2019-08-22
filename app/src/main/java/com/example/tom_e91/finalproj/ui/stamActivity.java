package com.example.tom_e91.finalproj.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.tom_e91.finalproj.R;

public class stamActivity extends AppCompatActivity {
    private final static int REQUEST_IMAGE_CAPTURE = 2;
    private final static String LOG_TAG = "nadir" + stamActivity.class.getSimpleName();
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stam);
        imageView = findViewById(R.id.stam_image_view);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, String.format("onActivityResult: reqCode: %d, resCode: %d", requestCode, resultCode));
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }

    public void onStamFinishClick(View view) {
        dispatchTakePictureIntent();
    }
}
