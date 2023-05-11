package com.updeat.models;

import java.util.List;
import java.util.Map;

public class Eatery {
    String name, timerange;
    Map<String, List<String>> Menu;
    Double latitude, longitude;
    Integer budget;

    Eatery(){}

    public Eatery(String name, String timerange, Map<String, List<String>> menu, Double latitude, Double longitude, Integer budget) {
        this.name = name;
        this.timerange = timerange;
        Menu = menu;
        this.latitude = latitude;
        this.longitude = longitude;
        this.budget = budget;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getBudget() { return budget; }

    public void setBudget(Integer budget) { this.budget = budget; }

}
