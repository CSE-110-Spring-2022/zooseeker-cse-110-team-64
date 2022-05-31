package com.example.sdzooseeker_team_64;

import static com.example.sdzooseeker_team_64.ZooPlan.ZOOPLANKEY;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlanActivity extends AppCompatActivity {

    public RecyclerView recyclerView;
  
    List<ZooGraph.Exhibit> exhibitList;
    ZooGraph zooGraph;
  
    private ZooPlan zooPlan;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        saveClass();
        int currentSize;
        currentSize = MyPrefs.getTheLength(App.getContext(), "exhibitListSize");
        exhibitList = new ArrayList<>();
        zooGraph = new ZooGraph(this);
        loadList(currentSize);
        setTitle("Exhibit Planning");
        // Setup Back Button on Navigation Bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        PlanExhibitListAdapter adapter = new PlanExhibitListAdapter();
        adapter.setHasStableIds(true);

        recyclerView = findViewById(R.id.exhibit_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // get exhibit lists passed from previous activity
        zooPlan = new ZooPlan(zooGraph, exhibitList);
        adapter.setExhibitList(zooPlan.exhibits);
    }

    public void onStartDirectionClicked(View view) {
        Intent intent = new Intent(this, NavigationPageActivity.class);
        intent.putExtra(ZOOPLANKEY, zooPlan);
        startActivity(intent);
    }

    // this event will enable the back
    // function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void saveClass() {
        MyPrefs.setLastActivity(App.getContext(), "lastActivity", this.getClass().getName());
    }
    public void loadList(int length) {
        for(int i = 0; i < length; i++) {
            exhibitList.add(MyPrefs.getTheExhibit(App.getContext(), "exhibitList"+i, zooGraph));
        }
    }
}