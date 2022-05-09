package com.example.sdzooseeker_team_64;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //new:
    ArrayList<String> exhibitList = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    ListView newlist;
    //end
    ListView listView;
    String[] exhibitNames;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exhibitNames = loadMapFromAssets(this, "sample_node_info.json");

        listView = findViewById(R.id.list);

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, exhibitNames);
        listView.setAdapter(arrayAdapter);

        // new:
        newlist = findViewById(R.id.new_list);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                exhibitList);
        newlist.setAdapter(adapter);

        // end:
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String)adapterView.getItemAtPosition(i);
                if(exhibitList.contains(item) == true) {
                    return;
                }
                exhibitList.add(item);
                adapter.notifyDataSetChanged();
            }
        });
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

                arrayAdapter.getFilter().filter(newText);

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public static String[] loadMapFromAssets(Context context, String fileName) {
        Map<String, ZooData.VertexInfo> vInfo = ZooData.loadVertexInfoJSON(context, fileName);

        ArrayList<String> exhibitNames = new ArrayList<>();
        for(String key : vInfo.keySet()) {
            String name = Objects.requireNonNull(vInfo.get(key)).name;
            exhibitNames.add(name);
        }
        String[] names = new String[exhibitNames.size()];
        names = exhibitNames.toArray(names);

        return names;
    }

    public void onPlanBtnClicked(View view) {
        Intent intent = new Intent(MainActivity.this, NavigationPageActivity.class);
        startActivity(intent);
    }
}