package com.example.tom_e91.finalproj.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.models.MyMarker;
import com.example.tom_e91.finalproj.util.Constants;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final static String LOG_TAG = "nadir" + CustomInfoWindowAdapter.class.getSimpleName();

    private Context mContext;

    public CustomInfoWindowAdapter(Context context){
        mContext = context;
    }


    /**
     * @param marker:  The marker whose Info Window we want to set
     * @return View: Based on marker type (tag), return appropriate View for marker's Info Window
     */
    @Override public View getInfoWindow(Marker marker) {
        View layout;
        MyMarker myMarker = (MyMarker) marker.getTag();
        if (myMarker == null) {
            Log.d(LOG_TAG, "getInfoWindow(), myMarker is null");
            return null;
        }


        switch (myMarker.tag) {

            case Constants.note:
                layout = LayoutInflater.from(mContext).inflate(R.layout.costum_info_window_note, null);

                // Set Title
                String markerTitle = marker.getTitle();
                TextView tvTitle = (TextView) layout.findViewById(R.id.title);
                if (!markerTitle.equals("")) {
                    tvTitle.setText(markerTitle);
                }

                // Set Snippet
                String noteContent = myMarker.getNoteContent();
                TextView tvSnippet = (TextView) layout.findViewById(R.id.snippet);
                if (!noteContent.equals("")) {
                    tvSnippet.setText(noteContent);
                }

                return layout;

            case Constants.camera:
                layout = LayoutInflater.from(mContext).inflate(R.layout.custom_info_window_camera, null);

                // Set Image
                ImageView imageView = layout.findViewById(R.id.info_window_image);

                // Set bitmap image
                Bitmap imageBitmap = myMarker.getBitmap();
                imageView.setImageBitmap(imageBitmap);

                return layout;

            case Constants.marker:
                return null;

            default:
                return null;
        }
    }

    @Override public View getInfoContents(Marker marker) {
        return null;
    }

    private Bitmap getBitMapFromImagePath(String imagePath) {
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imagePath), 200, 200);
    }
}
