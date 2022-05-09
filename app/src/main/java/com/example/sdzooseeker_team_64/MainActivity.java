package com.example.sdzooseeker_team_64;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    //new:
    ArrayList<String> exhibitList = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    ListView newlist;
    //end

    //button function start
    Button planButton;
    TextView countView;
    //button function end

    //find distance function
    TextView displayPlan;
    Map<String, String> idAndNameMap;
    ArrayList<String> idList = new ArrayList<>();
    Map<String, Double> sortedList;
    //find distance end
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

        //button function start
        planButton = findViewById(R.id.plan_btn);
        planButton.setOnClickListener(this::onPlanClicked);
        countView = findViewById(R.id.exhibit_count);
        countView.setText("0");
        //button function end

        //Plan Button function
        idAndNameMap = loadExhibitFromAssets(this,"sample_node_info.json");
        //plan function end
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
                String number = Integer.toString(exhibitList.size());
                countView.setText(number);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private Map<String, Double> sortByDistance(ArrayList<String> unsortedList) {
        Map<String, Double> beforeSorted = new HashMap<>();
        String start = "entrance_exit_gate";
        Graph<String, IdentifiedWeightedEdge> g = ZooData.loadZooGraphJSON(this,"sample_zoo_graph.json");
        Map<String, ZooData.VertexInfo> vInfo = ZooData.loadVertexInfoJSON(this,"sample_node_info.json");
        Map<String, ZooData.EdgeInfo> eInfo = ZooData.loadEdgeInfoJSON(this,"sample_edge_info.json");
        for(String exhibit : unsortedList) {
            GraphPath<String, IdentifiedWeightedEdge> path = DijkstraShortestPath.findPathBetween(g, start, exhibit);
            double distance = 0;
            for (IdentifiedWeightedEdge e : path.getEdgeList()) {
                distance += g.getEdgeWeight(e);
            }
            beforeSorted.put(exhibit, distance);
        }
        Map<String, Double> after = sortHelper(beforeSorted);
        return after;
    }

    public LinkedHashMap<String, Double> sortHelper(Map<String, Double> unSortedMap){
        LinkedHashMap <String, Double> sortedMap = new LinkedHashMap<String, Double>();
        unSortedMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
        return sortedMap;
    }

    public void onStartDirectionClicked(View view) {
        Intent intent = new Intent(this, NavigationPageActivity.class);
        startActivity(intent);
    }

    class ValueComparator implements Comparator<String> {
        Map<String, Double> base;

        public ValueComparator(Map<String, Double> base) {
            this.base = base;
        }
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
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

    public static Map<String, String> loadExhibitFromAssets(Context context, String fileName) {
        Map<String, ZooData.VertexInfo> vInfo = ZooData.loadVertexInfoJSON(context, fileName);

        Map<String, String> idNamePair = new HashMap<>();
        for(String key : vInfo.keySet()) {
            String name = Objects.requireNonNull(vInfo.get(key)).name;
            String id = Objects.requireNonNull(key);
            idNamePair.put(name, id);
        }

        return idNamePair;
    }
    void onPlanClicked(View view) {
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup, null);


        for(String str : exhibitList) {
            idList.add(idAndNameMap.get(str));
        }
        //After sorted
        sortedList = sortByDistance(idList);
        String output = "";
        for(String str : sortedList.keySet()) {
            output += str;
            output += ": ";
            output += sortedList.get(str);
            output += "\n";
        }
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        displayPlan = popupView.findViewById(R.id.plan_text);
        displayPlan.setText(output);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }
}