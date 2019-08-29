package com.example.tom_e91.finalproj.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tom_e91.finalproj.MyApplication;
import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.models.Repository;
import com.example.tom_e91.finalproj.models.Trip;
import com.example.tom_e91.finalproj.ui.SummaryActivity;
import com.example.tom_e91.finalproj.util.util_func;

import java.util.List;

public class PreviousTripsListAdapter extends RecyclerView.Adapter<PreviousTripsListAdapter.TripViewHolder> {
    final LayoutInflater inflater;
    final List<Trip> tripList;
    private Context mContext;
    private Repository repository;

    public PreviousTripsListAdapter(Context context, List<Trip> tripList) {
        mContext = context;
        inflater = LayoutInflater.from(context);
        this.tripList = tripList;
        repository = ((MyApplication)(context.getApplicationContext())).getRepository();
    }

    @NonNull @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate an item view.
        View mItemView = inflater.inflate(R.layout.triplist_item, parent, false);
        return new TripViewHolder(mItemView, this);
    }

    @Override public void onBindViewHolder(@NonNull PreviousTripsListAdapter.TripViewHolder holder, int position) {
        Trip currentTrip = tripList.get(position);
        holder.tripNameTV.setText(currentTrip.tripName);
        String tripDate = util_func.millisToDateTimeString(currentTrip.startTime);
        holder.tripDateTV.setText(tripDate);
        if (currentTrip.firstPhotoPath != null) {
            Glide.with(mContext).load(currentTrip.firstPhotoPath).into(holder.tripImageView);
        }
    }

    @Override public int getItemCount() {
        return tripList.size();
    }

    // -------------------------------- RecommendViewHolder Start -------------------------------//
    class TripViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView tripNameTV;
        public final ImageView tripImageView;
        public final TextView tripDateTV;
        final PreviousTripsListAdapter adapter;

        public TripViewHolder(View itemView, PreviousTripsListAdapter adapter) {
            super(itemView);
            tripNameTV = itemView.findViewById(R.id.rv_trip_name_tv);
            tripImageView = itemView.findViewById(R.id.rv_trip_image);
            tripDateTV = itemView.findViewById(R.id.rv_trip_date_tv);
            this.adapter = adapter;
            itemView.findViewById(R.id.rv_card_view_item).setOnClickListener(this);
        }

        @Override public void onClick(View v) {
            // Get position of the trip that was clicked
            int position = getLayoutPosition();
            // Use that to access clicked trip in tripList
            Trip clickedTrip = tripList.get(position);
            // Set This trip in repository
            repository.setCurrentTrip(clickedTrip);
            // Go to Summary
            mContext.startActivity(new Intent(mContext, SummaryActivity.class));
        }
    }
    // --------------------------------- RecommendViewHolder End ------------------------------//
}
