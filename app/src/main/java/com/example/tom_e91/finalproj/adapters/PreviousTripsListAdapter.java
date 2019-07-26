package com.example.tom_e91.finalproj.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tom_e91.finalproj.R;
import com.example.tom_e91.finalproj.data.Trip;

import java.util.LinkedList;

public class PreviousTripsListAdapter extends RecyclerView.Adapter<PreviousTripsListAdapter.TripViewHolder> {
    final LayoutInflater inflater;
    final LinkedList<Trip> tripList;

    public PreviousTripsListAdapter(Context context, LinkedList<Trip> tripList) {
        inflater = LayoutInflater.from(context);
        this.tripList = tripList;
    }

    @NonNull @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate an item view.
        View mItemView = inflater.inflate(R.layout.triplist_item, parent, false);
        return new TripViewHolder(mItemView, this);
    }

    @Override public void onBindViewHolder(@NonNull PreviousTripsListAdapter.TripViewHolder holder, int position) {
        Trip currentTrip = tripList.get(position);
        String currentTitle = currentTrip.getTrip_title();
        holder.tripItemView.setText(currentTitle);
    }

    @Override public int getItemCount() {
        return tripList.size();
    }

    // -------------------------------- RecommendViewHolder Start -------------------------------//
    class TripViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView tripItemView;
        final PreviousTripsListAdapter adapter;

        public TripViewHolder(View itemView, PreviousTripsListAdapter adapter) {
            super(itemView);
            tripItemView = itemView.findViewById(R.id.trip);
            this.adapter = adapter;
            tripItemView.setOnClickListener(this);
        }

        @Override public void onClick(View v) {
            // Get position of the trip that was clicked
            int position = getLayoutPosition();
            // Use that to access clicked trip in tripList
            Trip clickedTrip = tripList.get(position);
            // TODO go to clickedTrip Summary
        }
    }
    // --------------------------------- RecommendViewHolder End ------------------------------//
}
