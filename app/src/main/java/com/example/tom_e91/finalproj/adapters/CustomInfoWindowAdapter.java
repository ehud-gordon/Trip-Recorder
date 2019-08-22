package com.example.tom_e91.finalproj.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.models.MarkerTag;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context mContext;

    public CustomInfoWindowAdapter(Context context){
        mContext = context;
    }


    @Override
    public View getInfoWindow(Marker marker) {
        MarkerTag markerTag = (MarkerTag) marker.getTag();
        String tag = markerTag.tag;

        if (tag.equals(mContext.getString(R.string.tag_note))) {
            View layout = LayoutInflater.from(mContext).inflate(R.layout.costum_info_window_note, null);
            String title = marker.getTitle();
            TextView tvTitle = (TextView) layout.findViewById(R.id.title);

            if (!title.equals("")) {
                tvTitle.setText(title);
            }

            String snippet = marker.getTitle();
            TextView tvSnippet = (TextView) layout.findViewById(R.id.snippet);

            if (!snippet.equals("")) {
                tvSnippet.setText(snippet);
            }
            return layout;
        }

        else if (tag.equals(mContext.getString(R.string.tag_camera))) {
            View layout = LayoutInflater.from(mContext).inflate(R.layout.custom_info_window_camera, null);
            ImageView imageView = layout.findViewById(R.id.info_window_image);

            // Set bitmap image
            Bitmap imageBitmap = markerTag.getBitmap();
            imageView.setImageBitmap(imageBitmap);
            return layout;
        }

        else
            return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    private Bitmap getBitMapFromImagePath(String imagePath) {
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imagePath), 200, 200);
    }
}
