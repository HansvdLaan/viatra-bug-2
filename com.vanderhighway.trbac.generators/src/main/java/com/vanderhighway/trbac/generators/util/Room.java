package com.vanderhighway.trbac.generators.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Room {
    private String id;
    private String label;
    private boolean isPublic;

    public Room() {

    }

    public void setId(String id) {
        this.id = id;
        if(id.contains(".")) {
            this.isPublic = true;
        }
    }

    public String getId() {
        return this.id;
    }

    public void setLabel(String label) {
        label = label.replace("&", " and ").replace(" ", "_");
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public boolean isPublic() {
        return this.isPublic;
    }
}
