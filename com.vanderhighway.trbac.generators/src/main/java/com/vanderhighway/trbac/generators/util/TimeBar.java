package com.vanderhighway.trbac.generators.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeBar {

    public double start;
    public double width;
    public String type;
}
