package com.example.sdzooseeker_team_64;

import static com.example.sdzooseeker_team_64.ZooPlan.ZOOPLANKEY;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Serializable {

    // Data
    ZooGraph zooGraph;
    ZooPlan zooPlan;
    List<ZooGraph.Exhibit> exhibitList;
    ArrayAdapter<ZooGraph.Exhibit> searchListAdapter;
    ArrayAdapter<ZooGraph.Exhibit> selectedListAdapter;

    // View Components
    ListView searchListView;
    ListView selectedListView;
    Button planButton;
    TextView countView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize zoo graph, extract data from assets
        zooGraph = new ZooGraph(this);
        exhibitList = new ArrayList<>();

        //marin
        saveClass();
        loadList(exhibitList.size());

        // Setup View Component References
        searchListView = findViewById(R.id.search_list);
        selectedListView = findViewById(R.id.selected_list);
        planButton = findViewById(R.id.plan_btn);
        countView = findViewById(R.id.exhibit_count);

        // Setup View Components
        planButton.setOnClickListener(this::onPlanClicked);
        countView.setText(Integer.toString(exhibitList.size()));
        setupSearchListView();
        setupSelectedListView();
    }

    public void setupSearchListView() {
        // Setup view data source
        searchListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, zooGraph.getAllStrictExhibits());
        searchListView.setAdapter(searchListAdapter);

        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ZooGraph.Exhibit item = (ZooGraph.Exhibit) adapterView.getItemAtPosition(i);
                if(exhibitList.contains(item) == true) {
                    return;
                } else {
                    exhibitList.add(item);
                    saveList(exhibitList);
                    String number = Integer.toString(exhibitList.size());
                    countView.setText(number);
                    selectedListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void setupSelectedListView() {
        selectedListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, exhibitList);
        selectedListView.setAdapter(selectedListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Animal Name");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // filter text when search box content changes
            @Override
            public boolean onQueryTextChange(String newText) {
                searchListAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    void onPlanClicked(View view) {
        zooPlan = new ZooPlan(zooGraph, exhibitList);
        Intent intent = new Intent(this, PlanActivity.class);
        intent.putExtra(ZOOPLANKEY, zooPlan);
        startActivity(intent);
    }

    private void saveClass() {
        MyPrefs.setLastActivity(App.getContext(), "lastActivity", this.getClass().getName());
    }
    public void loadList(int length) {
//        for(int i = 0; i < length; i++) {
//            exhibitList.add(MyPrefs.getTheString(App.getContext(), "exhibitList"+i));
//        }
    }
    public void saveList(List<ZooGraph.Exhibit> temp) {
//        for(int i = 0; i < temp.size(); i++) {
//            MyPrefs.saveString(App.getContext(), "exhibitList", temp.get(i), i);
//        }
//        MyPrefs.saveLength(App.getContext(), "exhibitListSize",temp.size());
    }
}