package com.example.sdzooseeker_team_64;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NavigationPageActivity extends AppCompatActivity {

    private int startExhibitIndex = 0;
    private int endExhibitIndex = 1;
    private ArrayList<String> exhibitsList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private ListView listView;
    private TextView startExhibitTextView;
    private TextView endExhibitTextView;
    private Button prevButton;
    private Button nextButton;
    private Button skipButton;
    private Switch directionSwitch;

    private String edgeFile = "sample_edge_info.json";
    private String graphFile = "sample_zoo_graph.json";
    private String nodeFile = "sample_vertex_info.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_page);

        exhibitsList.add("entrance_exit_gate");

        // Prepare for UI
        listView = findViewById(R.id.direction_listView);
        startExhibitTextView = findViewById(R.id.startExhibitTextView);
        endExhibitTextView = findViewById(R.id.endExhibitTextView);

        //Todo convert exhibitList string to ID
        Intent i = getIntent();
        exhibitsList.addAll((ArrayList<String>) i.getSerializableExtra("Sorted IDs"));

        if (exhibitsList.size() >= 2) {
            // Set up from/to UI
            startExhibitTextView.setText(exhibitsList.get(startExhibitIndex));
            endExhibitTextView.setText(exhibitsList.get(endExhibitIndex));
            ArrayList<String> paths = getExhibitPaths(exhibitsList.get(startExhibitIndex),exhibitsList.get(endExhibitIndex), edgeFile, graphFile);

            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, paths);
            listView.setAdapter(adapter);
            startExhibitIndex++;
        }

        // Prepare for buttons
        prevButton = findViewById(R.id.previous_btn);
        nextButton = findViewById(R.id.next_btn);
        skipButton = findViewById(R.id.skip_btn);
        skipButton.setOnClickListener(this::onSkipBtnClicked);
        directionSwitch = findViewById(R.id.direction_switch);

        updateButtonStates();
    }

    public void onPreviousBtnClicked(View view) {
        // Get the direction text for previous exhibit


        if (isAtFirstExhibit()) {
            return;
        }

        // Set up from/to UI
        endExhibitIndex = startExhibitIndex - 1;
        startExhibitTextView.setText(exhibitsList.get(startExhibitIndex));
        endExhibitTextView.setText(exhibitsList.get(endExhibitIndex));

        ArrayList<String> paths = getExhibitPaths(exhibitsList.get(startExhibitIndex),exhibitsList.get(endExhibitIndex), edgeFile, graphFile);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, paths);
        listView.setAdapter(adapter);

        startExhibitIndex--;
        updateButtonStates();
    }

    public void onNextBtnClicked(View view) {
        if (isAtLastExhibit()) {
            // The button should finish and dismiss the direction avtivity.
            finish();
        } else {
            // Set up from/to UI
            endExhibitIndex = startExhibitIndex + 1;
            startExhibitTextView.setText(exhibitsList.get(startExhibitIndex));
            endExhibitTextView.setText(exhibitsList.get(endExhibitIndex));

            ArrayList<String> paths = getExhibitPaths(exhibitsList.get(startExhibitIndex),exhibitsList.get(endExhibitIndex), edgeFile, graphFile);

            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, paths);
            listView.setAdapter(adapter);
        }
        startExhibitIndex++;
        updateButtonStates();
    }
    public void onSkipBtnClicked(View view) {
        String skipTarget = exhibitsList.get(startExhibitIndex);
        //exhibitsList = new ArrayList<>();

        startExhibitIndex = 0;
        endExhibitIndex =1;
        setContentView(R.layout.activity_navigation_page);

        //Intent j = getIntent();
        //exhibitsList.addAll((ArrayList<String>) j.getSerializableExtra("Sorted IDs"));
        exhibitsList.remove(skipTarget);


        listView = findViewById(R.id.direction_listView);
        startExhibitTextView = findViewById(R.id.startExhibitTextView);
        endExhibitTextView = findViewById(R.id.endExhibitTextView);
        if (exhibitsList.size() >= 2) {
            // Set up from/to UI
            startExhibitTextView.setText(exhibitsList.get(startExhibitIndex));
            endExhibitTextView.setText(exhibitsList.get(endExhibitIndex));
            ArrayList<String> paths = getExhibitPaths(exhibitsList.get(startExhibitIndex),exhibitsList.get(endExhibitIndex), edgeFile, graphFile);

            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, paths);
            listView.setAdapter(adapter);
            startExhibitIndex++;
        }


        updateButtonStates();

    }

    // Helper functions
    private void updateButtonStates() {
        // Initially the previous button shouldn't appear as there's no previous exhibit
        if (isAtFirstExhibit()) {
            // hide previous button
            prevButton.setAlpha(0);
        } else {
            prevButton.setAlpha(1);
        }

        // At the last exhibit, change the next button to finish as there's no next exhibit
        if (isAtLastExhibit()) {
            // change next button to finish
            nextButton.setText("FINISH");
        } else {
            nextButton.setText("NEXT");
        }
    }

    private boolean isAtFirstExhibit() {
        return startExhibitIndex == 0;
    }

    private boolean isAtLastExhibit() {
        return startExhibitIndex >= exhibitsList.size() - 1;
    }

    private ArrayList<String> getExhibitPaths(String startId, String endId, String edgeFile, String graphFile) {
        String start = startId;
        String goal = endId;

        ArrayList<String> exhibitPaths = new ArrayList<>();

        // 1. Load the graph...
        Graph<String, IdentifiedWeightedEdge> g = ZooData.loadZooGraphJSON(this, graphFile);
        GraphPath<String, IdentifiedWeightedEdge> path = DijkstraShortestPath.findPathBetween(g, start, goal);

        // 2. Load the information about our nodes and edges...
        Map<String, ZooData.EdgeInfo> eInfo = ZooData.loadEdgeInfoJSON(this, edgeFile);

        System.out.printf("The shortest path from '%s' to '%s' is:\n", start, goal);

        for (IdentifiedWeightedEdge e : path.getEdgeList()) {
            exhibitPaths.add(eInfo.get(e.getId()).street);
        }
        return exhibitPaths;
    }

    private Map<String, String> namesToId(String fileName) {

        Map<String, String> converted = new HashMap<>();

        Map<String, ZooData.VertexInfo> vInfo = ZooData.loadVertexInfoJSON(this, fileName);

        for(String key : vInfo.keySet()) {
            String name = Objects.requireNonNull(vInfo.get(key)).name;
            converted.put(name, key);
        }
        return converted;
    }

    public void directionOnClicked(View view) {
        ArrayList<String> detailedPath = new ArrayList<>();

        // TODO test
        detailedPath = getDetailedPath("entrance_exit_gate", "hippo", nodeFile, edgeFile, graphFile);

        if(!directionSwitch.isChecked()){
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, detailedPath);
        }

        else{
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, detailedPath);
        }

        listView.setAdapter(adapter);
    }

    public ArrayList<String> getDetailedPath(String startId, String endId, String vertexFile, String edgeFile, String graphFile){
        ArrayList<String> detailedPath = new ArrayList<>();

        String start = startId;
        String goal = endId;

        // 1. Load the graph...
        Graph<String, IdentifiedWeightedEdge> g = ZooData.loadZooGraphJSON(this, graphFile);
        GraphPath<String, IdentifiedWeightedEdge> path = DijkstraShortestPath.findPathBetween(g, start, goal);

        // 2. Load the information about our nodes and edges...
        Map<String, ZooData.VertexInfo> vInfo = ZooData.loadVertexInfoJSON(this, vertexFile);
        Map<String, ZooData.EdgeInfo> eInfo = ZooData.loadEdgeInfoJSON(this, edgeFile);

        /*
        for (IdentifiedWeightedEdge e : path.getEdgeList()) {
            detailedPath.add("  %d. Walk %.0f meters along %s from '%s' to '%s'.\n",
                    g.getEdgeWeight(e),
                    eInfo.get(e.getId()).street,
                    vInfo.get(g.getEdgeSource(e).toString()).name,
                    vInfo.get(g.getEdgeTarget(e).toString()).name);
        }*/

        for (IdentifiedWeightedEdge e: path.getEdgeList()){
            String p1 = vInfo.get(g.getEdgeSource(e).toString()).name;
            String p2 = vInfo.get(g.getEdgeTarget(e).toString()).name;
            double eWeight = g.getEdgeWeight(e);

            detailedPath.add(
                    "Proceed on " +
                    p1 + " " + eWeight +
                    " ft towards " + p2
            );
        }
        return detailedPath;
    }

}