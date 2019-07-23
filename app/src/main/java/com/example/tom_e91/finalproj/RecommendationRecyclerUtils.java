package com.example.tom_e91.finalproj;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RecommendationRecyclerUtils
{
    static class RecommendHolder extends RecyclerView.ViewHolder
    {
        public final TextView textViewer;

        public RecommendHolder(@NonNull View itemView)
        {
            super(itemView);
            textViewer = itemView.findViewById(R.id.textView);
        }
    }

    static class MessageCallback
            extends DiffUtil.ItemCallback<Recommend>
    {
        @Override
        public boolean areItemsTheSame(@NonNull Recommend recommend1, @NonNull Recommend recommend2) {
            return recommend1.getTitle().equals(recommend2.getTitle());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Recommend message1, @NonNull Recommend message2) {
            return message1.equals(message2);
        }
    } // class MessageCallback


    interface RecommendClickCallback
    {
        void onRecommendClick(Recommend recommend);
        //void onRecommendLongClick(Recommend rec);
    }



    static class RecommendAdapter extends ListAdapter<Recommend, RecommendHolder> {

        public RecommendAdapter() { super(new MessageCallback()); }

        public RecommendClickCallback callback;

        @NonNull @Override
        public RecommendHolder onCreateViewHolder(@NonNull ViewGroup recommend, int itemType) {
            final Context context = recommend.getContext();
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_one_recommend, recommend, false);
            final RecommendHolder holder = new RecommendHolder(itemView);

            // onClick using anonymous class
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Recommend recommend = getItem(holder.getAdapterPosition());
                    if (callback != null)
                        callback.onRecommendClick(recommend);
                }
            });

//            // onLongClick using anonymous class - want to delete the pressed message
//            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    final Recommend recommend = getItem(holder.getAdapterPosition());
//                    if (callback != null) {
//                        callback.onRecommendLongClick(recommend);
//                    }
//                    return true;
//                }});



            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecommendHolder recommendHolder, int position) {
            Recommend recommend = getItem(position);
            recommendHolder.textViewer.setText(recommend.getTitle());
        }
    }


}
