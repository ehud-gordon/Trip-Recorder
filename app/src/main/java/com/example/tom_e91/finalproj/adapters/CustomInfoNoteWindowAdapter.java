package com.example.tom_e91.finalproj.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.tom_e91.finalproj.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoNoteWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context mContext;

    public CustomInfoNoteWindowAdapter (Context context){
        mContext = context;
    }


    @Override
    public View getInfoWindow(Marker marker) {
        String markerTag = marker.getTag().toString();
        if (markerTag.equals(mContext.getString(R.string.tag_note))) {
            View window = LayoutInflater.from(mContext).inflate(R.layout.costum_info_window_note, null);
            String title = marker.getTitle();
            TextView tvTitle = (TextView) window.findViewById(R.id.title);

            if (!title.equals("")) {
                tvTitle.setText(title);
            }

            String snippet = marker.getTitle();
            TextView tvSnippet = (TextView) window.findViewById(R.id.snippet);

            if (!snippet.equals("")) {
                tvSnippet.setText(snippet);
            }
            return window;
        }
        else if (markerTag.equals(mContext.getString(R.string.tag_camera))) {
            View window = LayoutInflater.from(mContext).inflate(R.layout.custom_info_window_camera, null);
            return window;
        }

        else {
            return null;
        }


    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
