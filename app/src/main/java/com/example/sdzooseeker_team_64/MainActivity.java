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
import android.widget.PopupWindow;
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
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Serializable {

    //new:
    ArrayList<String> exhibitList = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    ListView newlist;
    //end

    //button function start
    Button planButton;
    TextView countView;
    Button startButton;
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

    //Tech's for serialize
    ArrayList<String> sorted_IDs;

    // files
    private String edgeFile = "sample_edge_info.json";
    private String graphFile = "sample_zoo_graph.json";
    private String nodeFile = "sample_vertex_info.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //marin
        saveClass();
        int currentSize;
        currentSize = MyPrefs.getTheLength(App.getContext(), "exhibitListSize");
        loadList(currentSize);
        //cao
        exhibitNames = loadMapFromAssets(this, "sample_vertex_info.json");

        listView = findViewById(R.id.list);

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, exhibitNames);
        listView.setAdapter(arrayAdapter);

        //button function start
        planButton = findViewById(R.id.plan_btn);
        planButton.setOnClickListener(this::onPlanClicked);
        countView = findViewById(R.id.exhibit_count);
        countView.setText(Integer.toString(currentSize));

        startButton = findViewById(R.id.start_btn);
//        startButton.setAlpha(0);
        //button function end

        //Plan Button function
        idAndNameMap = loadExhibitFromAssets(this, "sample_vertex_info.json");
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
                saveList(exhibitList);
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
        Map<String, ZooData.VertexInfo> vInfo = ZooData.loadVertexInfoJSON(this, "sample_vertex_info.json");
        Map<String, ZooData.EdgeInfo> eInfo = ZooData.loadEdgeInfoJSON(this,"sample_edge_info.json");
        for(String exhibit : unsortedList) {
            System.out.println(exhibit);
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
        //serializeSortedId(intent);
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
            String kind = Objects.requireNonNull(vInfo.get(key)).kind;

            //Tech: remove entrance from list
            if(kind.compareTo("exhibit") == 0 )
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

        System.out.println("list:");
        System.out.println(exhibitList);

        for(String str : exhibitList) {
            idList.add(idAndNameMap.get(str));
        }
        System.out.println(idList);
        //After sorted
        Map<String, ZooData.VertexInfo> vInfo = ZooData.loadVertexInfoJSON(this, nodeFile);

        sortedList = sortByDistance(idList);
        String output = "";
        for(String str : sortedList.keySet()) {
            output += vInfo.get(str).name;
            output += ": ";
            output += sortedList.get(str);
            output += "m\n";
        }

        //end
        //new5/24
        serializeSortedId();
        //end/5/24
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        displayPlan = popupView.findViewById(R.id.plan_text);
        displayPlan.setText(output);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        if(sortedList.size() > 0) {
            startButton.setAlpha(1);
        }
    }

    private void serializeSortedId(){
        sorted_IDs = new ArrayList<String>();
        for(String id: sortedList.keySet()){
            sorted_IDs.add(id);
        }
        for(int i = 0; i < sorted_IDs.size(); i++) {
            MyPrefs.saveString(App.getContext(), "serial", sorted_IDs.get(i), i);
        }
        MyPrefs.saveLength(App.getContext(), "serial_size", sorted_IDs.size());
    }
    //marin
    private void saveClass() {
        MyPrefs.setLastActivity(App.getContext(), "lastActivity", this.getClass().getName());
    }
    public void loadList(int length) {
        for(int i = 0; i < length; i++) {
            exhibitList.add(MyPrefs.getTheString(App.getContext(), "exhibitList"+i));
        }
    }
    public void saveList(ArrayList<String> temp) {
        for(int i = 0; i < temp.size(); i++) {
            MyPrefs.saveString(App.getContext(), "exhibitList", temp.get(i), i);
        }
        MyPrefs.saveLength(App.getContext(), "exhibitListSize",temp.size());
    }
    //cao
}