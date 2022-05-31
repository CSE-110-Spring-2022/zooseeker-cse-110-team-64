package com.example.sdzooseeker_team_64;

import android.util.Pair;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class ZooPlan implements Serializable {
    public List<ZooGraph.Exhibit> exhibits;
    private LinkedHashMap<ZooGraph.Exhibit, Double> exhibitDistanceMap; // Don't use it other than in plan activity
    public ZooGraph zooGraph;
    private int currentStartExhibitIndex;
    private int currentEndExhibitIndex;

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

        sortAllExhibits();

        addGateToStartAndBack();

        // Initialize instance variables
        currentStartExhibitIndex = 0;
        currentEndExhibitIndex = 1;
    }

    public ZooPlan(ZooGraph zooGraph, ZooGraph.Exhibit[] exhibits) {
        List<ZooGraph.Exhibit> exhibitsAsList = Arrays.asList(exhibits);
        new ZooPlan(zooGraph, exhibitsAsList);
    }

    public ZooGraph.Exhibit getCurrentStartExhibit() {
        return exhibits.get(currentStartExhibitIndex);
    }

    public ZooGraph.Exhibit getCurrentEndExhibit() {
        return exhibits.get(currentEndExhibitIndex);
    }

    public void addGateToStartAndBack() {
        // add entrance_exit_gate to start and back
        exhibits.add(0, getEntranceExitGate());
        exhibits.add(exhibits.size(), getEntranceExitGate());
    }

    public boolean addExhibit(ZooGraph.Exhibit exhibit) {
        boolean isSuccessful = exhibits.add(exhibit);
        // sort all exhibits
        sortAllExhibits();
        return isSuccessful;
    }

    public void skipThisExhibit(double userLat, double userLng) {
        // remove the currentEndExhibit and re-plan the ones after it
        exhibits.remove(currentEndExhibitIndex);
        // If going forward on plan, don't change the star/end index because exhibits shift forward after removal
        // If going backward, decrement both start/end index so that start exhibit is the same, end exhibit goes backward by one
        if(!goingForward()) {
            currentStartExhibitIndex--;
            currentEndExhibitIndex--;
        }

        // Replan the following exhibits according to user location
        // Don't sort first and last one as they are the entry/exit gate
        replanExhibitsWithUserLocation(userLat, userLng, currentEndExhibitIndex, exhibits.size()-2);
    }

    private GraphPath<String, IdentifiedWeightedEdge> findPathBetween(ZooGraph.Exhibit start, ZooGraph.Exhibit end) {
        // check which id to use for both exhibits
        boolean useGroupIdForStart = (start.groupId != null);
        boolean useGroupIdForEnd = (end.groupId != null);

        GraphPath<String, IdentifiedWeightedEdge> path;

        path = DijkstraShortestPath.
                findPathBetween(zooGraph.graph,
                                useGroupIdForStart ? start.groupId : start.id,
                                useGroupIdForEnd ? end.groupId : end.id);
        return path;
    }

    // Both fromIndex and toIndex are inclusive!
    public void replanExhibitsWithUserLocation(double userLat, double userLng, int fromIndex, int toIndex) {
        if(fromIndex == toIndex || fromIndex == 0) { return; }

        // get all location for all exhibits within range
        // Notice exhibits with group_id don't have lat/lng
        Map<Double, ZooGraph.Exhibit> exhibitsToSort = new HashMap<>(); // key value is lat/lng diff
        for(int i = fromIndex; i <= toIndex; i++) {
            // Get direct distance in lat/lng
            ZooGraph.Exhibit exhibit = exhibits.get(i);
            // get lat/lng of exhibit
            boolean useGroupLocation = (exhibit != null);
            double exhibitLat = useGroupLocation ?
                    zooGraph.getExhibitWithId(exhibit.groupId).lat : exhibit.lat;
            double exhibitLng = useGroupLocation ?
                    zooGraph.getExhibitWithId(exhibit.groupId).lng : exhibit.lng;
            // calculate location diff
            double latDiff = userLat - exhibitLat;
            double lngDiff = userLng - exhibitLng;
            double locationDiff = Math.sqrt(Math.pow(latDiff, 2) + Math.pow(lngDiff, 2));

            exhibitsToSort.put(locationDiff, exhibit);
        }

        // sort exhibits
        List<ZooGraph.Exhibit> sortedExhibits = new ArrayList<>();
        exhibitsToSort.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(x -> sortedExhibits.add(x.getValue()));

        // apply exhibits order
        // remove unsorted exhibits
        int numOfExhibitsToRemove = toIndex - fromIndex + 1;
        for(int i = 0; i < numOfExhibitsToRemove; i++) {
            exhibits.remove(fromIndex);
        }
        // add sorted exhibits back
        for(int i = fromIndex; i <= toIndex; i++) {
            exhibits.add(sortedExhibits.get(i - fromIndex));
        }
    }

    private void sortAllExhibits() {
        Map<ZooGraph.Exhibit, Double> unsorted = new HashMap<>();
        ZooGraph.Exhibit gate = zooGraph.getExhibitWithId("entrance_exit_gate");
        for(ZooGraph.Exhibit exhibit : exhibits) {
            // find complete shortest path from gate to exhibit
            System.out.println(exhibit.id);
            // check if exhibit belongs to a group
            GraphPath<String, IdentifiedWeightedEdge> path;
            path = findPathBetween(gate, exhibit);

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
        exhibitDistanceMap = sortedExhibitsInLinkedHashMap;
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
                currentEndExhibitIndex = tmp;
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
                currentEndExhibitIndex = tmp;
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
        return (!goingForward()) | (currentEndExhibitIndex != exhibits.size()-1);
    }

    public boolean canGoPrev() {
        // For it to be possible to go prev, the user either is going forward on the plan,
        // or going backward and the current destination is not the first exhibit.
        return goingForward() | (currentEndExhibitIndex != 0);
    }

    public boolean canSkip() {
        return exhibits.size() > 3 &&
                !exhibits.get(currentEndExhibitIndex).equals(getEntranceExitGate());
    }

    public ArrayList<String> getCurrentDetailedPath() {
        ArrayList<String> path = new ArrayList<>();

        GraphPath<String, IdentifiedWeightedEdge> graphPath =
                findPathBetween(exhibits.get(currentStartExhibitIndex),
                                exhibits.get(currentEndExhibitIndex));

        Graph<String, IdentifiedWeightedEdge> g = zooGraph.graph;

        String pastStreet = "";
        String startVertex = zooGraph.getExhibitWithId(graphPath.getStartVertex()).name;

        for (IdentifiedWeightedEdge e: graphPath.getEdgeList()){
            String k2, p2;
            k2 = zooGraph.getExhibitWithId(g.getEdgeTarget(e)).kind;
            p2 = zooGraph.getExhibitWithId(g.getEdgeTarget(e)).name;

            if(startVertex.compareTo(p2) == 0){
                k2 = zooGraph.getExhibitWithId(g.getEdgeSource(e)).kind;
                p2 = zooGraph.getExhibitWithId(g.getEdgeSource(e)).name;
            }

            startVertex = p2;

            String currStreet = zooGraph.getTrailWithId(e.getId()).street;
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

            path.add(detailedDirection);
        }

        return path;
    }

    public ArrayList<String> getCurrentBriefPath() {
        ArrayList<String> path = new ArrayList<>();

        GraphPath<String, IdentifiedWeightedEdge> graphPath =
                findPathBetween(exhibits.get(currentStartExhibitIndex),
                        exhibits.get(currentEndExhibitIndex));

        Graph<String, IdentifiedWeightedEdge> g = zooGraph.graph;

        double totalWeight = 0;
        String briefDirection = "";
        String pastStreet = "";
        String endLocation = exhibits.get(currentEndExhibitIndex).name;
        String startVertex = zooGraph.getExhibitWithId(graphPath.getStartVertex()).name;

        for (IdentifiedWeightedEdge e: graphPath.getEdgeList()) {
            String k1, k2, p1, p2;
            k1 = zooGraph.getExhibitWithId(g.getEdgeSource(e)).kind;
            k2 = zooGraph.getExhibitWithId(g.getEdgeTarget(e)).kind;
            p1 = zooGraph.getExhibitWithId(g.getEdgeSource(e)).name;
            p2 = zooGraph.getExhibitWithId(g.getEdgeTarget(e)).name;
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
            String currStreet = zooGraph.getTrailWithId(e.getId()).street;

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

                path.add(briefDirection);

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

                path.add(briefDirection);
            }
        }

        return path;
    }

    public LinkedHashMap<ZooGraph.Exhibit, Double> getDistanceMapForPlanSummary() {
        exhibitDistanceMap.remove(getEntranceExitGate());
        return exhibitDistanceMap;
    }

}
