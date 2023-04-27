package com.updeat.models;

import java.util.List;
import java.util.Map;

public class Eatery {
    String name, timerange;
    Map<String, List<String>> Menu;

    Eatery(){}

    public Eatery(String name, String timerange, Map<String, List<String>> menu) {
        this.name = name;
        this.timerange = timerange;
        Menu = menu;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimerange() {
        return timerange;
    }

    public void setTimerange(String timerange) {
        this.timerange = timerange;
    }

    public Map<String, List<String>> getMenu() {
        return Menu;
    }

    public void setMenu(Map<String, List<String>> menu) {
        Menu = menu;
    }

}
