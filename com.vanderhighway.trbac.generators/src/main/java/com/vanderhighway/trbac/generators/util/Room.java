package com.vanderhighway.trbac.generators.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Room {
    public int id;
    private String name;
    public boolean isOutside;
    public int floorID;


    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name.replace("&", " and ").replace(" ", "_");
    }

    public Room setFloorID(int floorID) {
        this.floorID = floorID;
        return this;
    }
}
