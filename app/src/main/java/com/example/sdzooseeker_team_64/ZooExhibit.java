package com.example.sdzooseeker_team_64;

import java.util.List;

public class ZooExhibit {
    public String id;
    public String kind;
    public String name;
    public List<String> tags;

    public ZooExhibit(String id, String kind, String name, List<String> tags) {
        this.id = id;
        this.kind = kind;
        this.name = name;
        this.tags = tags;
    }


}
