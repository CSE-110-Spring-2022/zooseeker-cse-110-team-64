package com.example.sdzooseeker_team_64;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZooPlan {
    public List<ZooExhibit> exhibits;
    private int currentStartExhibitIndex;
    private int currentEndExhibitIndex;

    public ZooPlan() {
        exhibits = new ArrayList<ZooExhibit>();

        // Initialize instance variables
        currentStartExhibitIndex = 0;
        currentEndExhibitIndex = 1;
    }

    public ZooPlan(List<ZooExhibit> exhibits) {
        this.exhibits = exhibits;
        sortExhibits();

        // Initialize instance variables
        currentStartExhibitIndex = 0;
        currentEndExhibitIndex = 1;
    }

    public ZooPlan(ZooExhibit[] exhibits) {
        this.exhibits = Arrays.asList(exhibits);
        sortExhibits();

        // Initialize instance variables
        currentStartExhibitIndex = 0;
        currentEndExhibitIndex = 1;
    }

    public boolean addExhibit(ZooExhibit exhibit) {
        boolean isSuccessful = exhibits.add(exhibit);
        // sort all exhibits
        sortExhibits();
        return isSuccessful;
    }

    public boolean removeExhibit(ZooExhibit exhibit) {
        return exhibits.remove(exhibit); // no need to sort
    }

    public void sortExhibits() {

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
