package com.example.sdzooseeker_team_64;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NavigationPageActivity extends AppCompatActivity {
    private int currentExhibitIndex = 0;
    private ArrayList<String> exhibitsList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private ListView listView;
    private Button prevButton;
    private Button nextButton;

    private String edgeFile = "sample_edge_info.json";
    private String graphFile = "sample_zoo_graph.json";
    private String nodeFile = "sample_node_info.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_page);

        currentExhibitIndex = 0;
        exhibitsList.add("entrance_exit_gate");
        listView = findViewById(R.id.direction_listView);

        //Todo convert exhibitList string to ID
        Intent i = getIntent();
        exhibitsList.addAll((ArrayList<String>) i.getSerializableExtra("Sorted IDs"));

        if (exhibitsList.size() >= 2) {
            ArrayList<String> paths = getExhibitPaths(exhibitsList.get(0),exhibitsList.get(1), edgeFile, graphFile);

            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, paths);
            listView.setAdapter(adapter);
            currentExhibitIndex++;
        }

        // Prepare for buttons
        prevButton = findViewById(R.id.previous_btn);
        nextButton = findViewById(R.id.next_btn);
        updateButtonStates();
    }

    public void onPreviousBtnClicked(View view) {
        // Get the direction text for previous exhibit

        if (isAtFirstExhibit()) {
            return;
        }

        ArrayList<String> paths = getExhibitPaths(exhibitsList.get(currentExhibitIndex),exhibitsList.get(currentExhibitIndex-1), edgeFile, graphFile);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, paths);
        listView.setAdapter(adapter);

        currentExhibitIndex--;
        updateButtonStates();
    }

    public void onNextBtnClicked(View view) {
        if (isAtLastExhibit()) {
            // The button should finish and dismiss the direction avtivity.
            finish();
        } else {
            ArrayList<String> paths = getExhibitPaths(exhibitsList.get(currentExhibitIndex),exhibitsList.get(currentExhibitIndex + 1), edgeFile, graphFile);

            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, paths);
            listView.setAdapter(adapter);
        }
        currentExhibitIndex++;
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
        return currentExhibitIndex == 0;
    }

    private boolean isAtLastExhibit() {
        return currentExhibitIndex >= exhibitsList.size() - 1;
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

    public void onSettingsClicked(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}