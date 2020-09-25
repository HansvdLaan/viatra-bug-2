package com.vanderhighway.trbac.aggregators;

import java.util.*;
import java.util.stream.Collectors;

import com.vanderhighway.trbac.model.trbac.model.TemporalContext;

public class Scenario extends HashSet<TemporalContext> {

    public <D> Scenario(Collection<D> collect) {
    	super();
        this.addAll(collect.stream().map(x -> ((TemporalContext) x)).collect(Collectors.toSet()));
    }
    
    @Override
    public String toString() {
    	  List<String> contextList = this.stream().map(x -> x.getName()).collect(Collectors.toList());
          Collections.sort(contextList);
          String prettyString = contextList.toString();
          return prettyString;
    }
}
