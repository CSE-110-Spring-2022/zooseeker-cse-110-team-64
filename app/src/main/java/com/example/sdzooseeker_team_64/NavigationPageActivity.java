package com.example.sdzooseeker_team_64;

import static com.example.sdzooseeker_team_64.ZooPlan.ZOOPLANKEY;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Map;
import java.util.Objects;

public class NavigationPageActivity extends AppCompatActivity {

    private int startExhibitIndex;
    private int endExhibitIndex;
    private int status;

    private ArrayList<String> exhibitsList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private ListView listView;
    private TextView startExhibitTextView;
    private TextView endExhibitTextView;
    private Button prevButton;
    private Button nextButton;
    private Button skipButton;
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
        setTitle("Direction");

        //marin
        saveClass();
        startExhibitIndex = MyPrefs.getTheLength(App.getContext(), "startIndex");
        status = MyPrefs.getStatus(App.getContext(), "status");
        if(status == 1) {
            endExhibitIndex = startExhibitIndex + 1;
        }
        else {
            endExhibitIndex = startExhibitIndex - 1;
        }

        // Prepare for UI
        listView = findViewById(R.id.direction_listView);
        startExhibitTextView = findViewById(R.id.startExhibitTextView);
        endExhibitTextView = findViewById(R.id.endExhibitTextView);

        //convert exhibitList string to ID
        /*
        for(int i = 0; i < MyPrefs.getTheLength(App.getContext(), "serial_size"); i++) {
            exhibitsList.add(MyPrefs.getTheString(App.getContext(), "serial"+i));
        }*/
        ZooPlan zooPlan = (ZooPlan) getIntent().getSerializableExtra(ZOOPLANKEY);
        Log.i("ZOOPLAN", String.format("ZooPlan: %s", zooPlan));

        if(zooPlan != null) {
            for (var e : zooPlan.exhibits) {
                exhibitsList.add(e.getID());
            }
        }

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

            detailedPath = getDetailedPath(startId, endId,  nodeFile, edgeFile, graphFile);
            briefPath = getBriefPath(startId, endId,  nodeFile, edgeFile, graphFile);

            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, briefPath);
            listView.setAdapter(adapter);
            saveStartIndex(startExhibitIndex);
            if(status == 1) {
                startExhibitIndex++;
            }
            else {
                startExhibitIndex--;
            }

        }

        // Prepare for buttons
        prevButton = findViewById(R.id.previous_btn);
        nextButton = findViewById(R.id.next_btn);
        skipButton = findViewById(R.id.skip_btn);
        skipButton.setOnClickListener(this::onSkipBtnClicked);
        directionSwitch = findViewById(R.id.direction_switch);

        updateButtonStates();


        //Using alert example. You xn use this showAlert function in this way
        Utility.popAlert(this, "Off-track. Replan?");

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
        saveStartIndex(startExhibitIndex);
        startExhibitIndex--;
        saveStatus(0);
        updateButtonStates();
    }

    public void onNextBtnClicked(View view) {
        if (isAtLastExhibit()) {
            // The button should finish and dismiss the direction avtivity.
            //marin
            startExhibitIndex = 0;
            saveStartIndex(startExhibitIndex);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            //cao
            //finish();
        } else {
            // Set up from/to UI
            endExhibitIndex = startExhibitIndex + 1;


            String startId = exhibitsList.get(startExhibitIndex);
            String endId = exhibitsList.get(endExhibitIndex);

            Map<String, ZooData.VertexInfo> vInfo = ZooData.loadVertexInfoJSON(this, nodeFile);
            startExhibitTextView.setText(vInfo.get(startId).name);
            endExhibitTextView.setText(vInfo.get(endId).name);

            detailedPath = getDetailedPath(startId, endId,  nodeFile, edgeFile, graphFile);
            briefPath = getBriefPath(startId, endId,  nodeFile, edgeFile, graphFile);

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
        saveStartIndex(startExhibitIndex);
        startExhibitIndex++;
        saveStatus(1);
        updateButtonStates();
    }
    public void onSkipBtnClicked(View view) {
        //check if the current page is the last page
        if (isAtLastExhibit()){

            endExhibitIndex = startExhibitIndex - 1;
            startExhibitTextView.setText(exhibitsList.get(startExhibitIndex));
            endExhibitTextView.setText(exhibitsList.get(endExhibitIndex));

            detailedPath = getDetailedPath(exhibitsList.get(startExhibitIndex), exhibitsList.get(endExhibitIndex),  nodeFile, edgeFile, graphFile);
            briefPath = getBriefPath(exhibitsList.get(startExhibitIndex), exhibitsList.get(endExhibitIndex),  nodeFile, edgeFile, graphFile);

            if(!directionSwitch.isChecked()){
                adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, briefPath);
            }

            else{
                adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, detailedPath);
            }

            listView.setAdapter(adapter);
            String skipTarget = exhibitsList.get(startExhibitIndex);
            exhibitsList.remove(skipTarget);
            startExhibitIndex--;
        }else {
            String skipTarget = exhibitsList.get(startExhibitIndex);
            exhibitsList.remove(skipTarget);
            startExhibitIndex--;
            endExhibitIndex = startExhibitIndex + 1;
            startExhibitTextView.setText(exhibitsList.get(startExhibitIndex));
            endExhibitTextView.setText(exhibitsList.get(endExhibitIndex));

            detailedPath = getDetailedPath(exhibitsList.get(startExhibitIndex), exhibitsList.get(endExhibitIndex),  nodeFile, edgeFile, graphFile);
            briefPath = getBriefPath(exhibitsList.get(startExhibitIndex), exhibitsList.get(endExhibitIndex),  nodeFile, edgeFile, graphFile);

            if(!directionSwitch.isChecked()){
                adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, briefPath);
            }

            else{
                adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, detailedPath);
            }

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

    // dir == true : right, dir == false : left
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

        String pastStreet = "";
        String startVertex = vInfo.get(path.getStartVertex()).name;

        for (IdentifiedWeightedEdge e: path.getEdgeList()){
            String k2, p2;
                k2 = Objects.requireNonNull(vInfo.get(g.getEdgeTarget(e).toString())).kind;
                p2 = Objects.requireNonNull(vInfo.get(g.getEdgeTarget(e).toString())).name;

            if(startVertex.compareTo(p2) == 0){
                k2 = Objects.requireNonNull(vInfo.get(g.getEdgeSource(e).toString())).kind;
                p2 = Objects.requireNonNull(vInfo.get(g.getEdgeSource(e).toString())).name;
            }
            startVertex = p2;

            String currStreet = eInfo.get(e.getId()).street;
            double eWeight = g.getEdgeWeight(e);
            String detailedDirection = "";

            if (pastStreet.compareTo(currStreet) == 0){
                detailedDirection = "Continue on " + currStreet + " "+
                        eWeight + " ft towards ";
            }
            else{
                pastStreet = currStreet;
                detailedDirection = "Proceed on " + currStreet + " "+
                        eWeight + " ft towards ";
            }

            // target location
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
                detailedDirection += p2;
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

        double totalWeight = 0;
        String briefDirection = "";
        String pastStreet = "";
        String endLocation = vInfo.get(goal).name;

        String startVertex = vInfo.get(path.getStartVertex()).name;

        for (IdentifiedWeightedEdge e: path.getEdgeList()) {
            String k1, k2, p1, p2;
                k1 = Objects.requireNonNull(vInfo.get(g.getEdgeSource(e).toString())).kind;
                k2 = Objects.requireNonNull(vInfo.get(g.getEdgeTarget(e).toString())).kind;
                p1 = Objects.requireNonNull(vInfo.get(g.getEdgeSource(e).toString())).name;
                p2 = Objects.requireNonNull(vInfo.get(g.getEdgeTarget(e).toString())).name;
            if(startVertex.compareTo(p1) != 0){
                String temp;
                temp = p1;
                p1 = p2;
                p2 = temp;
                temp = k1;
                k1 = k2;
                k2 = temp;
            }
            startVertex = p2;

            double eWeight = g.getEdgeWeight(e);

            totalWeight += eWeight;
            String currStreet = eInfo.get(e.getId()).street;

            // Starting location
            if (pastStreet.compareTo("") == 0) {
                pastStreet = currStreet;
                briefDirection = "Proceed on " + currStreet + " ";
            }



            // look ahead
            // return end path if curr path != prev path
            // start new path
            if (pastStreet.compareTo(currStreet) != 0) {
                totalWeight -= eWeight;
                briefDirection += totalWeight + " ft towards ";

                if (k1.compareTo("exhibit") == 0) {
                    briefDirection += p1 + " exhibit";
                }

                else if (k1.compareTo("intersection") == 0) {
                    if (p1.contains(" / ")) {
                        String[] tokens = p1.split(" / ");
                        briefDirection +=
                                " corner of " + tokens[0] + " and " + tokens[1];
                    } else {
                        briefDirection += p1;
                    }
                }
                else if (k1.compareTo("gate") == 0) {
                    briefDirection += p1;
                }

                briefPath.add(briefDirection);

                // start new Path
                totalWeight = eWeight;
                briefDirection = "Proceed on " + currStreet + " ";
                pastStreet = currStreet;
            }
            // last location
            if (endLocation.compareTo(p2) == 0) {
                briefDirection += totalWeight + " ft towards ";
                if (k2.compareTo("exhibit") == 0) {
                    briefDirection += p2 + " exhibit";
                    totalWeight = 0;
                }
                else if (k2.compareTo("intersection") == 0) {
                    if (p2.contains(" / ")) {
                        String[] tokens = p2.split(" / ");
                        briefDirection +=
                                " corner of " + tokens[0] + " and " + tokens[1];
                    }
                    else {
                        briefDirection += p2;
                    }
                }
                else if (k2.compareTo("gate") == 0) {
                    briefDirection += p2;
                }

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

    //marin
    private void saveClass() {
        MyPrefs.setLastActivity(App.getContext(), "lastActivity", this.getClass().getName());
    }
    private void saveStartIndex(int i) {
        MyPrefs.saveLength(App.getContext(), "startIndex", i);
    }
    private void saveStatus(int i) {
        MyPrefs.saveLength(App.getContext(), "status", i);
    }




}