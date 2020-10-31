package com.vanderhighway.trbac.generators.util;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Transition {
    public String id;
    public String name;
    public Side[] sides;
    public int floorID;

    public void setFloorID(int floorID) {
        this.floorID = floorID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
