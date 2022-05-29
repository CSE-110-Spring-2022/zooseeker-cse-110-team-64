package com.example.sdzooseeker_team_64;

import static com.example.sdzooseeker_team_64.ZooPlan.ZOOPLANKEY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.List;

public class PlanActivity extends AppCompatActivity {

    public RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        setTitle("Exhibit Planning");

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        PlanExhibitListAdapter adapter = new PlanExhibitListAdapter();
        adapter.setHasStableIds(true);

        recyclerView = findViewById(R.id.exhibit_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // get exhibit lists passed from previous activity
        ZooPlan zooPlan = (ZooPlan) getIntent().getSerializableExtra(ZOOPLANKEY);

        adapter.setExhibitList(zooPlan.exhibits);
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
}