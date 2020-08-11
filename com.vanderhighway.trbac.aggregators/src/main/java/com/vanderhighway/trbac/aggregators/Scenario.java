package com.vanderhighway.trbac.aggregators;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.vanderhighway.trbac.model.trbac.model.TemporalContext;

public class Scenario extends HashSet<TemporalContext> {

    public <D> Scenario(Set<D> collect) {
    	super();
        this.addAll(collect.stream().map(x -> ((TemporalContext) x)).collect(Collectors.toSet()));
    }
}
