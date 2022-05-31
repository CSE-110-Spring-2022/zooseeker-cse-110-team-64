package com.example.sdzooseeker_team_64;

import static com.example.sdzooseeker_team_64.ZooPlan.ZOOPLANKEY;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NavigationPageActivity extends AppCompatActivity {

    // Data

    ZooGraph zooGraph;
    ZooPlan zooPlan;
    ArrayList<String> path;
    ArrayAdapter<String> adapter;

    // View references
    private ListView directionListView;
    private TextView startExhibitTextView;
    private TextView endExhibitTextView;
    private Button prevButton;
    private Button nextButton;
    private Button skipButton;
    private Switch directionSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_page);
        setTitle("Direction");

        // Initialize data
        // Initialize data and Restore activity status
        int currentSize = MyPrefs.getTheLength(App.getContext(), "exhibitListSize");
        zooGraph = new ZooGraph(this);
        zooPlan = new ZooPlan(zooGraph, loadList(currentSize));
        path = new ArrayList<>();

        // Setup view references
        directionSwitch = findViewById(R.id.direction_switch);
        directionListView = findViewById(R.id.direction_listView);
        startExhibitTextView = findViewById(R.id.startExhibitTextView);
        endExhibitTextView = findViewById(R.id.endExhibitTextView);
        prevButton = findViewById(R.id.previous_btn);
        nextButton = findViewById(R.id.next_btn);
        skipButton = findViewById(R.id.skip_btn);
        skipButton.setOnClickListener(this::onSkipBtnClicked);

        // load sharedpreference for directionSwitch

        // get directiontexts
        path = showDetailedDirection() ?
                zooPlan.getCurrentBriefPath() : zooPlan.getCurrentBriefPath();

        // show direction text in list
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, path);
        directionListView.setAdapter(adapter);

        updateViews();
        saveClass();
    }

    private boolean showDetailedDirection() {
        return directionSwitch.isChecked();
    }

    private void updateViews() {
        // Update buttons
        updateButtonStates();
        // Update start/end text views
        startExhibitTextView.setText(zooPlan.getCurrentStartExhibit().name);
        endExhibitTextView.setText(zooPlan.getCurrentEndExhibit().name);
        // Update direction list
        path.clear();
        path.addAll(showDetailedDirection() ?
                zooPlan.getCurrentDetailedPath() : zooPlan.getCurrentBriefPath());
        adapter.notifyDataSetChanged();
    }

    private void updateButtonStates() {
        nextButton.setText(zooPlan.canGoNext() ? "NEXT" : "FINISH");
        prevButton.setAlpha(zooPlan.canGoPrev() ? 1 : 0);
    }

    public void onPreviousBtnClicked(View view) {
        // Get the direction text for previous exhibit
        zooPlan.goToPrevExhibit();
        updateViews();
    }

    public void onNextBtnClicked(View view) {
        // check if plan is at exit gate, if so, dismiss this activity
        if(zooPlan.canGoNext()) {
            zooPlan.goToNextExhibit();
        } else {
            this.finish();
        }
        updateViews();
    }

    public void onSkipBtnClicked(View view) {
        zooPlan.skipThisExhibit();
        updateViews();
    }

    public void showDetailedDirectionOnToggled(View view) {
        // update sharedpreference

        // update direction list
        updateViews();
    }

    // Retain data
    private void saveClass() {
        MyPrefs.setLastActivity(App.getContext(), "lastActivity", this.getClass().getName());
    }

    public List<ZooGraph.Exhibit> loadList(int length) {
        List<ZooGraph.Exhibit> exhibitList = new ArrayList<>();
        for(int i = 0; i < length; i++) {
            exhibitList.add(MyPrefs.getTheExhibit(App.getContext(), "exhibitList"+i, zooGraph));
        }
        return exhibitList;
    }

}