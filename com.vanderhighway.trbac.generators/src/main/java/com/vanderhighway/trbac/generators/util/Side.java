package com.vanderhighway.trbac.generators.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Side {
    public Annotations annotations;
    public int id;
    public int roomOnThisSide;
    public int roomOnOtherSide;
}
