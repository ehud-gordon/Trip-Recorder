package com.example.tom_e91.finalproj;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class RecommendationsActivity extends AppCompatActivity implements RecommendationRecyclerUtils.RecommendClickCallback
{
    // Define a LOG TAG
    private static final String LOG_TAG = RecommendationsActivity.class.getSimpleName();

    // relevant views
    private RecommendationRecyclerUtils.RecommendAdapter adapt;
    private ArrayList<Recommend> recommends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        // Adds recommendation for testing
        for (int i = 0; i < 10 ; i++)
        {
            recommends.add(new Recommend("a"));
        }

        // RecyclerView definition
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapt = new RecommendationRecyclerUtils.RecommendAdapter();
        recyclerView.setAdapter(adapt);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapt.callback = this;
        adapt.submitList(recommends);

    }


    @Override
    public void onRecommendClick(Recommend recommend) { }

}
