package com.vanderhighway.trbac.generators;

import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.vanderhighway.trbac.core.modifier.PolicyModifier;
import com.vanderhighway.trbac.model.trbac.model.*;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.viatra.query.runtime.emf.EMFScope;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
import org.eclipse.xtext.xbase.lib.Extension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collections;

public class IntervalTestCaseGenerator {

    @Extension
    private static TRBACPackage ePackage = TRBACPackage.eINSTANCE;

    public static void main(String[] args) throws IOException, InvocationTargetException, ModelManipulationException, ModelManipulationException, ParseException {

        System.out.println("Policy Interval Test Case Generator Called!");
        System.out.print("Initialize model scope and preparing engine... ");

        // Initializing the EMF package
        TRBACPackage.eINSTANCE.getName();
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("trbac", new XMIResourceFactoryImpl());
        Resource.Factory.Registry.INSTANCE.getContentTypeToFactoryMap().put("*", new XMIResourceFactoryImpl());

        generateBasicAddRangeInput();
        generateBasicAddRangeResult();

        System.out.println("Done!");
    }

    public static void generateBasicAddRangeInput() throws IOException, ModelManipulationException {
        String fileSeparator = System.getProperty("file.separator");
        ResourceSet set = new ResourceSetImpl();

        String relativePath = "." + fileSeparator + "basic_add_range.trbac";
        File file = new File(relativePath);
        if (file.createNewFile()) {
            System.out.println(relativePath + " File Created in Project root directory");
        } else System.out.println("File " + relativePath + " already exists in the project root directory");
        URI uri = URI.createFileURI("./basic_add_range.trbac");
        Resource resource = set.createResource(uri);

        resource.getContents().add(EcoreUtil.create(ePackage.getSecurityPolicy()));
        SecurityPolicy securityPolicy = ((SecurityPolicy) resource.getContents().get(0));
        securityPolicy.setName("DummySecurityPolicy");

        resource.getContents().add(EcoreUtil.create(ePackage.getAuthorizationPolicy()));
        AuthorizationPolicy authorizationPolicy = ((AuthorizationPolicy) resource.getContents().get(0));
        authorizationPolicy.setName("DummyAuthorizationPolicy");
        securityPolicy.setAuthorizationPolicy(authorizationPolicy);

        Schedule schedule = (Schedule) EcoreUtil.create(ePackage.getSchedule());
        schedule.setName("DummySchedule");
        authorizationPolicy.setSchedule(schedule);

        final AdvancedViatraQueryEngine engine = AdvancedViatraQueryEngine.createUnmanagedEngine(new EMFScope(set));
        PolicyModifier modifier = new PolicyModifier(engine, (SecurityPolicy) resource.getContents().get(0), resource);

        DayOfWeekSchedule monday = modifier.addDayOfWeekSchedule("Monday");

        TemporalContext always = modifier.addTemporalContext("Always");
        TimeRange alwaysRange = modifier.addTemporalContextInstance(always, monday, "Always-Monday", new IntegerInterval(0,1439));

        TemporalContext testRanges = modifier.addTemporalContext("TestRanges");
        TimeRange tr1 = modifier.addTemporalContextInstance(testRanges, monday, "TR1", new IntegerInterval(10,20));
        TimeRange tr2 = modifier.addTemporalContextInstance(testRanges, monday, "TR2", new IntegerInterval(20,30));
        TimeRange tr3 = modifier.addTemporalContextInstance(testRanges, monday, "TR3", new IntegerInterval(30,40));
        TimeRange tr4 = modifier.addTemporalContextInstance(testRanges, monday, "TR4", new IntegerInterval(35,35));

        resource.save(Collections.emptyMap());

        modifier.dispose();
    }

    public static void generateBasicAddRangeResult() throws IOException, ModelManipulationException {
        String fileSeparator = System.getProperty("file.separator");
        ResourceSet set = new ResourceSetImpl();

        String relativePath = "." + fileSeparator + "basic_add_range_result.trbac";
        File file = new File(relativePath);
        if (file.createNewFile()) {
            System.out.println(relativePath + " File Created in Project root directory");
        } else System.out.println("File " + relativePath + " already exists in the project root directory");
        URI uri = URI.createFileURI("./basic_add_range_result.trbac");
        Resource resource = set.createResource(uri);

        resource.getContents().add(EcoreUtil.create(ePackage.getSecurityPolicy()));
        SecurityPolicy securityPolicy = ((SecurityPolicy) resource.getContents().get(0));
        securityPolicy.setName("DummySecurityPolicy");

        resource.getContents().add(EcoreUtil.create(ePackage.getAuthorizationPolicy()));
        AuthorizationPolicy authorizationPolicy = ((AuthorizationPolicy) resource.getContents().get(0));
        authorizationPolicy.setName("DummyAuthorizationPolicy");
        securityPolicy.setAuthorizationPolicy(authorizationPolicy);

        Schedule schedule = (Schedule) EcoreUtil.create(ePackage.getSchedule());
        schedule.setName("DummySchedule");
        authorizationPolicy.setSchedule(schedule);

        final AdvancedViatraQueryEngine engine = AdvancedViatraQueryEngine.createUnmanagedEngine(new EMFScope(set));
        PolicyModifier modifier = new PolicyModifier(engine, (SecurityPolicy) resource.getContents().get(0), resource);

        DayOfWeekSchedule monday = modifier.addDayOfWeekSchedule("Monday");

        TemporalContext always = modifier.addTemporalContext("Always");
        TimeRange alwaysRange = modifier.addTemporalContextInstance(always, monday, "Always-Monday", new IntegerInterval(0,1439));

        TemporalContext testRanges = modifier.addTemporalContext("TestRanges");
        TimeRange tr1 = modifier.addTemporalContextInstance(testRanges, monday, "TR1", new IntegerInterval(10,20));
        TimeRange tr2 = modifier.addTemporalContextInstance(testRanges, monday, "TR2", new IntegerInterval(20,30));
        TimeRange tr3 = modifier.addTemporalContextInstance(testRanges, monday, "TR3", new IntegerInterval(30,40));
        TimeRange tr4 = modifier.addTemporalContextInstance(testRanges, monday, "TR4", new IntegerInterval(35,35));

        DayScheduleTimeRange sr1 = modifier.addDayScheduleTimeRange(monday, "SR-1", new IntegerInterval(0,9));
        modifier.getManipulation().addTo(sr1, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), alwaysRange);

        DayScheduleTimeRange sr2 = modifier.addDayScheduleTimeRange(monday, "SR-2", new IntegerInterval(10,19));
        modifier.getManipulation().addTo(sr2, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), alwaysRange);
        modifier.getManipulation().addTo(sr2, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), tr1);

        DayScheduleTimeRange sr3 = modifier.addDayScheduleTimeRange(monday, "SR-3", new IntegerInterval(20,20));
        modifier.getManipulation().addTo(sr3, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), alwaysRange);
        modifier.getManipulation().addTo(sr3, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), tr1);
        modifier.getManipulation().addTo(sr3, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), tr2);

        DayScheduleTimeRange sr4 = modifier.addDayScheduleTimeRange(monday, "SR-4", new IntegerInterval(21,29));
        modifier.getManipulation().addTo(sr4, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), alwaysRange);
        modifier.getManipulation().addTo(sr4, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), tr2);

        DayScheduleTimeRange sr5 = modifier.addDayScheduleTimeRange(monday, "SR-5", new IntegerInterval(30,30));
        modifier.getManipulation().addTo(sr5, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), alwaysRange);
        modifier.getManipulation().addTo(sr5, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), tr2);
        modifier.getManipulation().addTo(sr5, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), tr3);

        DayScheduleTimeRange sr6 = modifier.addDayScheduleTimeRange(monday, "SR-6", new IntegerInterval(31,34));
        modifier.getManipulation().addTo(sr6, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), alwaysRange);
        modifier.getManipulation().addTo(sr6, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), tr3);

        DayScheduleTimeRange sr7 = modifier.addDayScheduleTimeRange(monday, "SR-7", new IntegerInterval(35,35));
        modifier.getManipulation().addTo(sr7, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), alwaysRange);
        modifier.getManipulation().addTo(sr7, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), tr3);
        modifier.getManipulation().addTo(sr7, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), tr4);

        DayScheduleTimeRange sr8 = modifier.addDayScheduleTimeRange(monday, "SR-8", new IntegerInterval(36,40));
        modifier.getManipulation().addTo(sr8, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), alwaysRange);
        modifier.getManipulation().addTo(sr8, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), tr3);

        DayScheduleTimeRange sr9 = modifier.addDayScheduleTimeRange(monday, "SR-9", new IntegerInterval(41,1439));
        modifier.getManipulation().addTo(sr9, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), alwaysRange);

        resource.save(Collections.emptyMap());

        modifier.dispose();


    }
}
