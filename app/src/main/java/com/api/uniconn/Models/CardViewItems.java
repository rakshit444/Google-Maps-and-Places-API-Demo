package com.api.uniconn.Models;

/**
 * Created by Rakshit on 7/16/2016.
 */

public class CardViewItems {
    private String name;
    private String type;
    private String distance;
    private String location;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDistance() {
        return distance;
    }

    public String getLocation() {
        return location;
    }

    public CardViewItems(String name, String type, String distance, String location) {
        this.name = name;
        this.type = type;
        this.distance = distance;
        this.location = location;
    }
}
