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
import java.util.Arrays;
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
    private Switch directionSwitch;

    // detailed and brief path
    ArrayList<String> detailedPath;
    ArrayList<String> briefPath;

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

        //convert exhibitList string to ID
        Intent i = getIntent();
        exhibitsList.addAll((ArrayList<String>) i.getSerializableExtra("Sorted IDs"));

        // setup detailed and brief path
        detailedPath = new ArrayList<>();
        briefPath = new ArrayList<>();

        if (exhibitsList.size() >= 2) {
            // Set up from/to UI
            String startId = exhibitsList.get(startExhibitIndex);
            String endId = exhibitsList.get(endExhibitIndex);

            Map<String, ZooData.VertexInfo> vInfo = ZooData.loadVertexInfoJSON(this, nodeFile);

            startExhibitTextView.setText(vInfo.get(startId).name);
            endExhibitTextView.setText(vInfo.get(endId).name);

            detailedPath = getDetailedPath(startId, endId, nodeFile, edgeFile, graphFile);
            briefPath = getBriefPath(startId, endId, nodeFile, edgeFile, graphFile);

            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, briefPath);
            listView.setAdapter(adapter);
            startExhibitIndex++;
        }

        // Prepare for buttons
        prevButton = findViewById(R.id.previous_btn);
        nextButton = findViewById(R.id.next_btn);
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

        String startId = exhibitsList.get(startExhibitIndex);
        String endId = exhibitsList.get(endExhibitIndex);

        Map<String, ZooData.VertexInfo> vInfo = ZooData.loadVertexInfoJSON(this, nodeFile);
        startExhibitTextView.setText(vInfo.get(startId).name);
        endExhibitTextView.setText(vInfo.get(endId).name);

        detailedPath = getDetailedPath(startId, endId, nodeFile, edgeFile, graphFile);
        briefPath = getBriefPath(startId, endId, nodeFile, edgeFile, graphFile);

        if(!directionSwitch.isChecked()){
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, briefPath);
        }

        else{
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, detailedPath);
        }

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

            String startId = exhibitsList.get(startExhibitIndex);
            String endId = exhibitsList.get(endExhibitIndex);

            Map<String, ZooData.VertexInfo> vInfo = ZooData.loadVertexInfoJSON(this, nodeFile);
            startExhibitTextView.setText(vInfo.get(startId).name);
            endExhibitTextView.setText(vInfo.get(endId).name);

            detailedPath = getDetailedPath(startId, endId, nodeFile, edgeFile, graphFile);
            briefPath = getBriefPath(startId, endId, nodeFile, edgeFile, graphFile);

            if(!directionSwitch.isChecked()){
                adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, briefPath);
            }

            else{
                adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, detailedPath);
            }

            listView.setAdapter(adapter);
        }
        startExhibitIndex++;
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
        if(!directionSwitch.isChecked()){
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, briefPath);
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

        for (IdentifiedWeightedEdge e: path.getEdgeList()){
            String k1 = Objects.requireNonNull(vInfo.get(g.getEdgeSource(e).toString())).kind;
            String k2 = Objects.requireNonNull(vInfo.get(g.getEdgeTarget(e).toString())).kind;
            String p1 = Objects.requireNonNull(vInfo.get(g.getEdgeSource(e).toString())).name;
            String p2 = Objects.requireNonNull(vInfo.get(g.getEdgeTarget(e).toString())).name;

            double eWeight = g.getEdgeWeight(e);
            String detailedDirection = "";

            if(k1.compareTo("exhibit") == 0) {
                detailedDirection =
                        "Proceed on " +
                        p1 + " exhibit " + eWeight +
                        " ft towards ";
            }

            else if(k1.compareTo("intersection") == 0){
                if (p1.contains(" / ")) {
                    String[] tokens = p1.split(" / ");
                    detailedDirection =
                            "Proceed on corner of " +
                                tokens[0] + " and " + tokens[1] +
                                " " +eWeight +
                                " ft towards ";
                }
                else {
                    detailedDirection =
                            "Proceed on " +
                                    p1 + " " + eWeight +
                                    " ft towards ";
                }
            }

            else if(k1.compareTo("gate") == 0){
                detailedDirection =
                        "Proceed from " +
                        p1 + " " + eWeight +
                        " ft towards ";
            }


            if(k2.compareTo("exhibit") == 0){
                detailedDirection +=
                        p2 + " exhibit";
            }

            else if(k2.compareTo("intersection") == 0){
                if (p2.contains(" / ")) {
                    String[] tokens = p2.split(" / ");
                    detailedDirection +=
                            " corner of " + tokens[0] + " and " + tokens[1];
                }
                else {
                    detailedDirection += p2;
                }
            }

            else if(k2.compareTo("gate") == 0){
                detailedDirection +=
                        p2;
            }

            detailedPath.add(detailedDirection);
        }
        return detailedPath;
    }

    public ArrayList<String> getBriefPath(String startId, String endId, String vertexFile, String edgeFile, String graphFile){
        ArrayList<String> briefPath = new ArrayList<>();

        String start = startId;
        String goal = endId;

        // 1. Load the graph...
        Graph<String, IdentifiedWeightedEdge> g = ZooData.loadZooGraphJSON(this, graphFile);
        GraphPath<String, IdentifiedWeightedEdge> path = DijkstraShortestPath.findPathBetween(g, start, goal);

        // 2. Load the information about our nodes and edges...
        Map<String, ZooData.VertexInfo> vInfo = ZooData.loadVertexInfoJSON(this, vertexFile);
        Map<String, ZooData.EdgeInfo> eInfo = ZooData.loadEdgeInfoJSON(this, edgeFile);

        ArrayList<String> pastWay = new ArrayList<>(Arrays.asList("", ""));
        double totalWeight = 0;
        String briefDirection = "";

        for (IdentifiedWeightedEdge e: path.getEdgeList()){
            String k1 = Objects.requireNonNull(vInfo.get(g.getEdgeSource(e).toString())).kind;
            String k2 = Objects.requireNonNull(vInfo.get(g.getEdgeTarget(e).toString())).kind;
            String p1 = Objects.requireNonNull(vInfo.get(g.getEdgeSource(e).toString())).name;
            String p2 = Objects.requireNonNull(vInfo.get(g.getEdgeTarget(e).toString())).name;

            double eWeight = g.getEdgeWeight(e);
            totalWeight += eWeight;

            // part 1 (p1 and k1)
            if(k1.compareTo("exhibit") == 0) {
                briefDirection =
                        "Proceed on " + p1 + " exhibit ";

                pastWay.set(0, "");
                pastWay.set(1, "");
            }

            else if(k1.compareTo("intersection") == 0){
                if(p1.contains(" / ")){
                    String[] tokens = p1.split(" / ");
                    if(pastWay.contains("")){
                        briefDirection = "Proceed on corner of " + tokens[0] +
                                " and " + tokens[1] + " ";
                        pastWay.set(0, tokens[0]);
                        pastWay.set(1, tokens[1]);
                    }

                    else if(pastWay.contains(tokens[0]) || pastWay.contains(tokens[1])){
                        pastWay.set(0, tokens[0]);
                        pastWay.set(1, tokens[1]);
                    }

                    // look ahead (new proceed -> inter !-> inter)
                    else if(!(pastWay.contains(tokens[0]) || pastWay.contains(tokens[1]))){
                        totalWeight -= eWeight;
                        briefDirection += totalWeight + " ft towards Corner of " + tokens[0] +
                                " and " + tokens[1];
                        briefPath.add(briefDirection);

                        totalWeight = eWeight;
                        briefDirection = briefDirection = "Proceed on corner of " + tokens[0] +
                                " and " + tokens[1] + " ";
                        pastWay.set(0, tokens[0]);
                        pastWay.set(1, tokens[1]);
                    }
                }
                else{
                    briefDirection = "Proceed on " + p1 + " ";
                }
            }

            else if(k1.compareTo("gate") == 0){
                briefDirection =
                        "Proceed on " + p1 + " ";

                pastWay.set(0, "");
                pastWay.set(1, "");
            }

            // part 2 (p2 and k2)
            if(k2.compareTo("exhibit") == 0){
                briefDirection += totalWeight + " ft towards " + p2 + " exhibit";
                totalWeight = 0;
                briefPath.add(briefDirection);
                pastWay.set(0, "");
                pastWay.set(1, "");
            }

            else if(k2.compareTo("intersection") == 0){
                if(p2.contains(" / ")){
                    String[] tokens = p2.split(" / ");
                    // last node
                    if(p2.compareTo(Objects.requireNonNull(vInfo.get(goal).name)) == 0){
                        briefDirection += totalWeight + " ft towards Corner of " + tokens[0] +
                                " and " + tokens[1];
                    }

                    // first time seeing ("" , "")
                    else if(pastWay.contains("")){
                        pastWay.set(0, tokens[0]);
                        pastWay.set(1, tokens[1]);
                    }

                    // not equal to previous (inter !-> inter)
                    else if(!(pastWay.contains(tokens[0]) || pastWay.contains(tokens[1]))){
                        briefDirection += totalWeight + " ft towards Corner of " + tokens[0] +
                                " and " + tokens[1];
                        totalWeight = 0;
                        briefPath.add(briefDirection);
                        pastWay.set(0, "");
                        pastWay.set(1, "");
                    }

                    else{
                        pastWay.set(0, tokens[0]);
                        pastWay.set(1, tokens[1]);
                    }
                }

                else{
                    briefDirection += totalWeight + " ft towards " + p2;
                    totalWeight = 0;
                    briefPath.add(briefDirection);
                    pastWay.set(0, "");
                    pastWay.set(1, "");
                }
            }

            else if(k2.compareTo("gate") == 0){
                briefDirection += totalWeight + " ft towards " + p2;
                totalWeight = 0;
                briefPath.add(briefDirection);
            }
        }
        return briefPath;
    }


    /* past method
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
    }*/
}