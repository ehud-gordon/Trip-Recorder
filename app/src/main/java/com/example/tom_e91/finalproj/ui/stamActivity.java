package com.example.tom_e91.finalproj.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.tom_e91.finalproj.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class stamActivity extends AppCompatActivity {
    private final static String LOG_TAG = "nadir" + stamActivity.class.getSimpleName();
    // Camera
    ImageView imageView;
    private static final int INTENT_IMAGE_CAPTURE_CODE = 1000;
    private static final int PERMISSION_STORAGE_CODE = 2;
    public String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stam);
        imageView = findViewById(R.id.stam_image_view);
        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE_CODE);
    }

    private void dispatchTakePictureIntent() {
        Log.d(LOG_TAG, "dispatchTakePictureIntent()");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d(LOG_TAG, "dispatchTakePictureIntent(), IOException" + ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d(LOG_TAG, "dispatchTakePictureIntent() File created successfully");
                Uri photoURI = FileProvider.getUriForFile(this,"com.example.tom_e91.finalproj.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.d(LOG_TAG, "dispatchTakePictureIntent() got photo URI");
                startActivityForResult(takePictureIntent, INTENT_IMAGE_CAPTURE_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // Create an image
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(LOG_TAG, "createImageFile() Finished");
        // path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }



    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, String.format("onActivityResult: reqCode: %d, resCode: %d", requestCode, resultCode));
        if (requestCode == INTENT_IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            File imgFile = new  File(currentPhotoPath);
            if(imgFile.exists())            {
                imageView.setImageURI(Uri.fromFile(imgFile));
            }
        }
    }

    public void onStamFinishClick(View view) {
        dispatchTakePictureIntent();
    }
}
