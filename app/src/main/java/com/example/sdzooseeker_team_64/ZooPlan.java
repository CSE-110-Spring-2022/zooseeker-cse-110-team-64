package com.example.sdzooseeker_team_64;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ZooPlan implements Serializable {
    public List<ZooGraph.Exhibit> exhibits;
    public LinkedHashMap<ZooGraph.Exhibit, Double> exhibitsInLinkedHashMap;
    public ZooGraph zooGraph;
    private int currentStartExhibitIndex;
    private int currentEndExhibitIndex;

    public static String ZOOPLANKEY = "ZOO_PLAN";

    private ZooGraph.Exhibit getEntranceExitGate() { return zooGraph.getExhibitWithId("entrance_exit_gate"); }

    @Override
    public String toString() {
        return "ZooPlan{" +
                "exhibits=" + exhibits +
                '}';
    }

    public ZooPlan(ZooGraph zooGraph, List<ZooGraph.Exhibit> exhibits) {
        this.zooGraph = zooGraph;
        this.exhibits = exhibits;

        sortExhibits();

        addGateToStartAndBack();

        // Initialize instance variables
        currentStartExhibitIndex = 0;
        currentEndExhibitIndex = 1;
    }

    public ZooPlan(ZooGraph zooGraph, ZooGraph.Exhibit[] exhibits) {
        List<ZooGraph.Exhibit> exhibitsAsList = Arrays.asList(exhibits);
        new ZooPlan(zooGraph, exhibitsAsList);
    }

    public void addGateToStartAndBack() {
        // add entrance_exit_gate to start and back
        exhibits.add(0, getEntranceExitGate());
        exhibits.add(exhibits.size(), getEntranceExitGate());
    }

    public boolean addExhibit(ZooGraph.Exhibit exhibit) {
        boolean isSuccessful = exhibits.add(exhibit);
        // sort all exhibits
        sortExhibits();
        return isSuccessful;
    }

    public boolean removeExhibit(ZooGraph.Exhibit exhibit) {
        return exhibits.remove(exhibit); // no need to sort
    }

    public void skipExhibit(ZooGraph.Exhibit exhibit) {
        // remove one exhibit and re-plan the ones after it
    }

    public void sortExhibits() {
        Map<ZooGraph.Exhibit, Double> unsorted = new HashMap<>();
        String start = "entrance_exit_gate";
        for(ZooGraph.Exhibit exhibit : exhibits) {
            // find complete shortest path from gate to exhibit
            System.out.println(exhibit.id);
            // check if exhibit belongs to a group
            GraphPath<String, IdentifiedWeightedEdge> path;
            if (exhibit.groupId != null) {
                path = DijkstraShortestPath.findPathBetween(zooGraph.graph, start, exhibit.groupId);
            } else {
                path = DijkstraShortestPath.findPathBetween(zooGraph.graph, start, exhibit.id);
            }

            // calculate total distance from gate to exhibit
            double distance = 0;
            for (IdentifiedWeightedEdge e : path.getEdgeList()) {
                distance += zooGraph.graph.getEdgeWeight(e);
            }
            unsorted.put(exhibit, distance);
        }

        // update exhibits to sorted order
        ArrayList<ZooGraph.Exhibit> sortedExhibits = new ArrayList<>();
        LinkedHashMap<ZooGraph.Exhibit, Double> sortedExhibitsInLinkedHashMap = new LinkedHashMap<>();
        unsorted.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered( x -> {
                    sortedExhibits.add(x.getKey());
                    sortedExhibitsInLinkedHashMap.put(x.getKey(), x.getValue());
                });
        exhibits = sortedExhibits;
        exhibitsInLinkedHashMap = sortedExhibitsInLinkedHashMap;
    }

    public boolean goToNextExhibit() {
        // Check if it is possible to go to next exhibit
        if(canGoNext()) {
            // if user is going forward in plan, increment start/end exhibit indices,
            // otherwise, reverse start/end exhibit indices
            if(goingForward()) {
                currentStartExhibitIndex++;
                currentEndExhibitIndex++;
            } else {
                int tmp = currentStartExhibitIndex;
                currentStartExhibitIndex = currentEndExhibitIndex;
                currentEndExhibitIndex = currentStartExhibitIndex;
            }
            return true;
        } else {
           return false;
        }
    }

    public boolean goToPrevExhibit() {
        // Check if it is possible to go to previous exhibit
        if(canGoPrev()) {
            // if user is going forward in plan, reverse start/end exhibit indices,
            // otherwise, decrement start/end exhibit indices
            if(goingForward()) {
                int tmp = currentStartExhibitIndex;
                currentStartExhibitIndex = currentEndExhibitIndex;
                currentEndExhibitIndex = currentStartExhibitIndex;
            } else {
                currentStartExhibitIndex--;
                currentEndExhibitIndex--;
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean goingForward() {
        return (currentEndExhibitIndex > currentStartExhibitIndex);
    }

    public boolean canGoNext() {
        // For it to be possible to go next, the user either is going backward on the plan,
        // or going forward and the current destination is not the last one.
        return goingForward() | (currentEndExhibitIndex != exhibits.size()-1);
    }

    public boolean canGoPrev() {
        // For it to be possible to go prev, the user either is going forward on the plan,
        // or going backward and the current destination is not the first exhibit.
        return (!goingForward()) | (currentEndExhibitIndex != 0);
    }

}
