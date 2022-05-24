package com.example.sdzooseeker_team_64;

import java.util.List;

public class ZooPlan {
    public List<ZooExhibit> exhibits;
    private int currentStartExhibitIndex;
    private int currentEndExhibitIndex;

    public ZooPlan(List<ZooExhibit> exhibits) {

    }

    public ZooPlan(ZooExhibit[] exhibits) {

    }

    public boolean goToNextExhibit() {

        return true;
    }

    public boolean goToPrevExhibit() {

        return true;
    }

    public boolean currentEndIsLastExhibit() {
        return (currentEndExhibitIndex == exhibits.size()-1);
    }

    public boolean currentStartIsFirstExhibit() {
        return (currentStartExhibitIndex == 0);
    }

}
