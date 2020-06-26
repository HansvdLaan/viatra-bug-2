package com.vanderhighway.trbac.core.validator;

import com.vanderhighway.trbac.patterns.*;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.xtext.xbase.lib.Extension;

import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class PolicyValidator {


    @Extension
    private Logger logger = Logger.getLogger(PolicyValidator.class);

    protected AdvancedViatraQueryEngine engine;

    public PolicyValidator(final AdvancedViatraQueryEngine engine) {
        this.engine = engine;
    }


    public void addChangeListeners(AdvancedViatraQueryEngine engine) {
        boolean fireNow = true; // parameter means all current matches are sent to the listener

        engine.addMatchUpdateListener(TimeRangeP.Matcher.on(engine), ListenerFactory.getTimeRangeUpdateListener(), true);
        engine.addMatchUpdateListener(DayScheduleTimeRangeP.Matcher.on(engine), ListenerFactory.getDayScheduleTimeRangeUpdateListener(), true);
    }

}
