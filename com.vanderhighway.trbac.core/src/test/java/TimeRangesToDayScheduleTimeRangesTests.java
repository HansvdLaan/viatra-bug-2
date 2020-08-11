//import com.brein.time.timeintervals.intervals.IntegerInterval;
//import com.vanderhighway.trbac.core.modifier.PolicyAutomaticModifier;
//import com.vanderhighway.trbac.core.modifier.PolicyModifier;
//import com.vanderhighway.trbac.model.trbac.model.*;
//import org.eclipse.emf.common.util.URI;
//import org.eclipse.emf.ecore.resource.Resource;
//import org.eclipse.emf.ecore.resource.ResourceSet;
//import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
//import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
//import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
//import org.eclipse.viatra.query.runtime.emf.EMFScope;
//import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
//import org.eclipse.xtext.xbase.lib.Extension;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.lang.reflect.InvocationTargetException;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//public class TimeRangesToDayScheduleTimeRangesTests {
//
//    ResourceSet set;
//    ResourceSet resultSet;
//
//    static URI uriEmptyMonday;
//    static URI uriEmptySchedules;
//
//    static URI uriBasicRanges;
//
//    static URI uriResultBasicAddRanges;
//    static URI uriResultBasicRemoveRanges;
//
//    @Extension
//    private TRBACPackage ePackage = TRBACPackage.eINSTANCE;
//
//    @BeforeAll
//    static void setup() {
//        TRBACPackage.eINSTANCE.getName();
//        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("trbac", new XMIResourceFactoryImpl());
//        Resource.Factory.Registry.INSTANCE.getContentTypeToFactoryMap().put("*", new XMIResourceFactoryImpl());
//
//        uriEmptyMonday = URI.createFileURI("./src/test/resources/models/shared/empty.trbac");
//        uriEmptySchedules = URI.createFileURI("./src/test/resources/models/shared/empty_policy_all_schedules.trbac");
//        uriBasicRanges = URI.createFileURI("./src/test/resources/models/time_ranges_to_day_schedule_time_ranges/basic_ranges.trbac");
//        uriResultBasicAddRanges = URI.createFileURI("./src/test/resources/models/time_ranges_to_day_schedule_time_ranges/basic_add_ranges_result.trbac");
//        uriResultBasicRemoveRanges = URI.createFileURI("./src/test/resources/models/time_ranges_to_day_schedule_time_ranges/basic_remove_ranges_result.trbac");
//    }
//
//    @BeforeEach
//    void init() {
//        set = new ResourceSetImpl();
//        resultSet = new ResourceSetImpl();
//    }
//
//    @Test
//    public void emptyPolicy() {
//        Resource resource = set.getResource(uriEmptySchedules, true);
//        AdvancedViatraQueryEngine engine = AdvancedViatraQueryEngine.createUnmanagedEngine(new EMFScope(set));
//
//        PolicyModifier modifier = new PolicyModifier(engine, (Policy) resource.getContents().get(0), resource);
//        PolicyAutomaticModifier automaticModifier = new PolicyAutomaticModifier(engine, modifier, (Policy) resource.getContents().get(0));
//
//        automaticModifier.initialize();
//        automaticModifier.execute();
//    }
//
//    @Test
//    public void basicAddRanges() throws InvocationTargetException, ModelManipulationException {
//
//        Resource resource = set.getResource(uriEmptyMonday, true);
//        Resource resultResource = resultSet.getResource(uriResultBasicAddRanges, true);
//        AdvancedViatraQueryEngine engine = AdvancedViatraQueryEngine.createUnmanagedEngine(new EMFScope(set));
//
//
//        PolicyModifier modifier = new PolicyModifier(engine, (Policy) resource.getContents().get(0), resource);
//        PolicyAutomaticModifier automaticModifier = new PolicyAutomaticModifier(engine, modifier, (Policy) resource.getContents().get(0));
//
//        automaticModifier.initialize();
//        automaticModifier.execute();
//
//        modifier.addRole("dummy"); //?
//
//        DayOfWeekSchedule monday = (DayOfWeekSchedule) resource.getEObject("Monday");
//        TimeRangeGroup group = modifier.addTimeRangeGroup("TestRangeGroup") ;
//
//        engine.delayUpdatePropagation( () -> { return modifier.addTimeRange(group, monday, "TR1", new IntegerInterval(10,20)); } );
//        engine.delayUpdatePropagation( () -> { return modifier.addTimeRange(group, monday, "TR2", new IntegerInterval(20,30)); } );
//        engine.delayUpdatePropagation( () -> { return modifier.addTimeRange(group, monday, "TR3", new IntegerInterval(30,40)); } );
//        engine.delayUpdatePropagation( () -> { return modifier.addTimeRange(group, monday, "TR4", new IntegerInterval(35,35)); } );
//
//        engine.getCurrentMatchers();
//        modifier.addRole("dummy2"); //?
//
//        DaySchedule expectedDaySchedule = ((Schedule) resultResource.getContents().get(0).eGet(ePackage.getPolicy_Schedule()))
//                .getDaySchedules().get(0);
//        DaySchedule actualDaySchedule = ((Schedule) resource.getContents().get(0).eGet(ePackage.getPolicy_Schedule()))
//                .getDaySchedules().get(0);
//        assertDayScheduleEquals(expectedDaySchedule, actualDaySchedule);
//    }
//
//    @Test
//    public void basicRemoveRanges() throws InvocationTargetException, ModelManipulationException {
//        Resource resource = set.getResource(uriEmptyMonday, true);
//        Resource resultResource = resultSet.getResource(uriEmptyMonday, true);
//
//        AdvancedViatraQueryEngine engine = AdvancedViatraQueryEngine.createUnmanagedEngine(new EMFScope(set));
//
//        PolicyModifier modifier = new PolicyModifier(engine, (Policy) resource.getContents().get(0), resource);
//        PolicyAutomaticModifier automaticModifier = new PolicyAutomaticModifier(engine, modifier, (Policy) resource.getContents().get(0));
//
//        automaticModifier.initialize();
//        automaticModifier.execute();
//
//
//        TimeRangeGroup group = modifier.addTimeRangeGroup("TestRangeGroup");
//        DayOfWeekSchedule monday = (DayOfWeekSchedule) resource.getEObject("Monday");
//        TimeRange testRange1 = engine.delayUpdatePropagation( () -> { return modifier.addTimeRange(group, monday, "TR1", new IntegerInterval(10,20)); } );
//        TimeRange testRange2 = engine.delayUpdatePropagation( () -> { return modifier.addTimeRange(group, monday, "TR2", new IntegerInterval(20,30)); } );
//        TimeRange testRange3 = engine.delayUpdatePropagation( () -> { return modifier.addTimeRange(group, monday, "TR3", new IntegerInterval(30,40)); } );
//        TimeRange testRange4 = engine.delayUpdatePropagation( () -> { return modifier.addTimeRange(group, monday, "TR4", new IntegerInterval(35,35)); } );
//
////        modifier.addRole("sometestrole");
////
//        engine.delayUpdatePropagation( () -> { modifier.removeTimeRange(testRange1); return 0; });
//        engine.delayUpdatePropagation( () -> { modifier.removeTimeRange(testRange3); return 0; });
//        engine.delayUpdatePropagation( () -> { modifier.removeTimeRange(testRange2); return 0; });
//        engine.delayUpdatePropagation( () -> { modifier.removeTimeRange(testRange4); return 0; });
////
//       modifier.addRole("dummy3");
//
//        DaySchedule expectedDaySchedule = ((Schedule) resultResource.getContents().get(0).eGet(ePackage.getPolicy_Schedule()))
//                .getDaySchedules().get(0);
//        DaySchedule actualDaySchedule = ((Schedule) resource.getContents().get(0).eGet(ePackage.getPolicy_Schedule()))
//                .getDaySchedules().get(0);
//        assertDayScheduleEquals(expectedDaySchedule, actualDaySchedule);
//    }
//
//    @Test
//    public void basicRemoveRanges2() throws InvocationTargetException, ModelManipulationException {
//        Resource resource = set.getResource(uriEmptyMonday, true);
//        Resource resultResource = resultSet.getResource(uriEmptyMonday, true);
//
//        AdvancedViatraQueryEngine engine = AdvancedViatraQueryEngine.createUnmanagedEngine(new EMFScope(set));
//
//        PolicyModifier modifier = new PolicyModifier(engine, (Policy) resource.getContents().get(0), resource);
//        PolicyAutomaticModifier automaticModifier = new PolicyAutomaticModifier(engine, modifier, (Policy) resource.getContents().get(0));
//
//        automaticModifier.initialize();
//        automaticModifier.execute();
//
//        //modifier.executeCompound(modifier.addRole("dummy")); //?
//
//        TimeRangeGroup group = modifier.addTimeRangeGroup("TestRangeGroup") ;
//        DayOfWeekSchedule monday = (DayOfWeekSchedule) resource.getEObject("Monday");
//        TimeRange testRange1 = engine.delayUpdatePropagation( () -> { return modifier.addTimeRange(group, monday, "TR1", new IntegerInterval(10,20)); } );
//        TimeRange testRange2 = engine.delayUpdatePropagation( () -> { return modifier.addTimeRange(group, monday, "TR2", new IntegerInterval(20,30)); } );
//        TimeRange testRange3 = engine.delayUpdatePropagation( () -> { return modifier.addTimeRange(group, monday, "TR3", new IntegerInterval(30,40)); } );
//        TimeRange testRange4 = engine.delayUpdatePropagation( () -> { return modifier.addTimeRange(group, monday, "TR4", new IntegerInterval(35,35)); } );
//
//        modifier.removeTimeRange(testRange1);
//        modifier.removeTimeRange(testRange2);
//        modifier.removeTimeRange(testRange4);
//        modifier.removeTimeRange(testRange3);
//
//        //modifier.executeCompound(modifier.addRole("dummy2")); //?
//
//        DaySchedule expectedDaySchedule = ((Schedule) resultResource.getContents().get(0).eGet(ePackage.getPolicy_Schedule()))
//                .getDaySchedules().get(0);
//        DaySchedule actualDaySchedule = ((Schedule) resource.getContents().get(0).eGet(ePackage.getPolicy_Schedule()))
//                .getDaySchedules().get(0);
//        assertDayScheduleEquals(expectedDaySchedule, actualDaySchedule);
//    }
//
//    public static void assertDayScheduleEquals(DaySchedule expected, DaySchedule actual) {
//        Map<String, IntegerInterval> expectedRanges = expected.getTimeRanges().stream().collect(
//                Collectors.toMap(TimeRange::getName,
//                        range -> new IntegerInterval(range.getStart(), range.getEnd())));
//        Map<String, IntegerInterval> actualRanges = actual.getTimeRanges().stream().collect(
//                Collectors.toMap(TimeRange::getName,
//                        range -> new IntegerInterval(range.getStart(), range.getEnd())));
//
//        for (String expectedName : expectedRanges.keySet()) {
//            assertTrue(actualRanges.containsKey(expectedName), "actual DaySchedule " + expected.getName() +
//                    " has no range associated with it named " + expectedName);
//            assertEquals(expectedRanges.get(expectedName).getStart(), actualRanges.get(expectedName).getStart(), "expected and actual start time of range " + expectedName + " do not match.");
//            assertEquals(expectedRanges.get(expectedName).getEnd(), actualRanges.get(expectedName).getEnd(), "expected and actual emd time of range " + expectedName + " do not match.");
//        }
//
//        for (String actualName : actualRanges.keySet()) {
//            assertTrue(expectedRanges.containsKey(actualName), "expected DaySchedule " + actual.getName() +
//                    " has no range associated with it named " + actualName);
//            assertEquals(expectedRanges.get(actualName).getStart(), actualRanges.get(actualName).getStart(), "expected and actual start time of range " + actualName + " do not match.");
//            assertEquals(expectedRanges.get(actualName).getEnd(), actualRanges.get(actualName).getEnd(), "expected and actual emd time of range " + actualName + " do not match.");
//        }
//
//        Map<IntegerInterval, Set<String>> expectedScheduleRanges = expected.getDayScheduleTimeRanges().stream()
//                .collect(Collectors.toMap(
//                        sr -> new IntegerInterval(sr.getStart(), sr.getEnd()),
//                        sr -> sr.getTimeRanges().stream().map(TimeRange::getName).collect(Collectors.toSet())
//                ));
//        Map<IntegerInterval, Set<String>> actualScheduleRanges = actual.getDayScheduleTimeRanges().stream()
//                .collect(Collectors.toMap(
//                        sr -> new IntegerInterval(sr.getStart(), sr.getEnd()),
//                        sr -> sr.getTimeRanges().stream().map(TimeRange::getName).collect(Collectors.toSet())
//                ));
//        assertEquals(expectedScheduleRanges.keySet(), actualScheduleRanges.keySet(), "expected and actual schedule ranges intervals of DaySchedule " + actual.getName() +
//                " do not match");
//        assertEquals(expectedScheduleRanges.keySet(), actualScheduleRanges.keySet(), "expected and actual schedule ranges intervals of DaySchedule " + actual.getName() +
//                " do not match");
//
//        expectedScheduleRanges.keySet().forEach(sr ->
//                assertEquals(expectedScheduleRanges.get(sr), actualScheduleRanges.get(sr),
//                                "expected and actual references do not match."
//                )
//        );
//    }
//}
