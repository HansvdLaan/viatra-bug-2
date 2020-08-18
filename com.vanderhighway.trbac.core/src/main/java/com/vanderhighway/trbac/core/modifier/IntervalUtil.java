package com.vanderhighway.trbac.core.modifier;

import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.vanderhighway.trbac.aggregators.Scenario;
import com.vanderhighway.trbac.core.modifier.PolicyModifier;
import com.vanderhighway.trbac.model.trbac.model.*;
import com.vanderhighway.trbac.patterns.RangeP;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
import org.eclipse.viatra.transformation.runtime.emf.rules.batch.BatchTransformationRule;
import org.eclipse.xtext.xbase.lib.Extension;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class IntervalUtil {

    @Extension
    private static TRBACPackage ePackage = TRBACPackage.eINSTANCE;



    // ---------- Process the adding/removing of ranges ----------
    
    public static void processAddRange(PolicyModifier modifier, IntervalTree tree, RangeP.Match match) throws ModelManipulationException {
        //System.out.println("Process AddRange called with:" + match.toString());
            TimeRange timeRange = match.getInstance();
            IntegerInterval rangeInterval = new IntegerInterval(timeRange.getStart(), timeRange.getEnd());
            List<IntegerInterval> overlaps = tree.overlap(rangeInterval).stream().map(x -> (IntegerInterval) x).collect(toList());
            DaySchedule daySchedule = timeRange.getDaySchedule();

            //Two possible cases, either one overlap or two or more overlaps!
            if (overlaps.size() == 1) {
                IntegerInterval overlap = overlaps.get(0);
                // interval and overlap are equal
                if (timeRange.getStart() == overlap.getStart() && timeRange.getEnd() == overlap.getEnd()) {
                    addRangeReference(modifier, daySchedule, overlap, match.getInstance());
                }
                //no overlapping bounds and interval proper subset of overlap
                else if (timeRange.getStart() > overlap.getStart() && timeRange.getEnd() < overlap.getEnd()) {
                    IntegerInterval resizedOverlap = new IntegerInterval(overlap.getStart(), timeRange.getStart() - 1);
                    resizeDayScheduleTimeRange(modifier, tree, daySchedule, overlap, resizedOverlap);

                    IntegerInterval resizedOverlap2 = new IntegerInterval(timeRange.getEnd() + 1, overlap.getEnd());
                    addDayScheduleTimeRange(modifier, tree, daySchedule , modifier.getUniqueID(daySchedule.getName()), resizedOverlap2);
                    copyAllRangeReferences(modifier, daySchedule, resizedOverlap, resizedOverlap2);

                    updateNextDayScheduleTimeRange(modifier, daySchedule, resizedOverlap, resizedOverlap2);

                    IntegerInterval newDayScheduleTimeRangeInterval = new IntegerInterval(timeRange.getStart(), timeRange.getEnd());
                    addDayScheduleTimeRange(modifier, tree, daySchedule, modifier.getUniqueID(daySchedule.getName()), newDayScheduleTimeRangeInterval);
                    copyAllRangeReferences(modifier, daySchedule, resizedOverlap, newDayScheduleTimeRangeInterval);
                    addRangeReference(modifier, daySchedule, newDayScheduleTimeRangeInterval, match.getInstance());

                    updateNextDayScheduleTimeRange(modifier, daySchedule, resizedOverlap, newDayScheduleTimeRangeInterval);
                }
                // non-overlapping end-time and interval proper subset of overlap
                else if (timeRange.getEnd() < overlap.getEnd()) {
                    IntegerInterval resizedOverlap = new IntegerInterval(timeRange.getEnd() + 1, overlap.getEnd());
                    resizeDayScheduleTimeRange(modifier, tree, daySchedule, overlap, resizedOverlap);

                    IntegerInterval newDayScheduleTimeRangeInterval = new IntegerInterval(timeRange.getStart(), timeRange.getEnd());
                    addDayScheduleTimeRange(modifier, tree, daySchedule, modifier.getUniqueID(daySchedule.getName()), newDayScheduleTimeRangeInterval);
                    copyAllRangeReferences(modifier, daySchedule, resizedOverlap, newDayScheduleTimeRangeInterval);
                    addRangeReference(modifier, daySchedule, newDayScheduleTimeRangeInterval, match.getInstance());

                    updatePreviousDayScheduleTimeRange(modifier, daySchedule, resizedOverlap, newDayScheduleTimeRangeInterval);
                }
                // non-overlapping start-time and interval proper subset of overlap
                else if (timeRange.getStart() > overlap.getStart()) {
                    IntegerInterval resizedOverlap = new IntegerInterval(overlap.getStart(), timeRange.getStart() - 1);
                    resizeDayScheduleTimeRange(modifier, tree, daySchedule, overlap, resizedOverlap);

                    IntegerInterval newDayScheduleTimeRangeInterval = new IntegerInterval(timeRange.getStart(), timeRange.getEnd());
                    addDayScheduleTimeRange(modifier, tree, daySchedule, modifier.getUniqueID(daySchedule.getName()), newDayScheduleTimeRangeInterval);
                    copyAllRangeReferences(modifier, daySchedule, resizedOverlap, newDayScheduleTimeRangeInterval);
                    addRangeReference(modifier, daySchedule, newDayScheduleTimeRangeInterval, match.getInstance());

                    updateNextDayScheduleTimeRange(modifier, daySchedule, resizedOverlap, newDayScheduleTimeRangeInterval);
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                for (IntegerInterval overlap : overlaps) {
                    if(overlap.getStart() < timeRange.getStart()) {
                        IntegerInterval resizedOverlap = new IntegerInterval(overlap.getStart(), timeRange.getStart() - 1);
                        resizeDayScheduleTimeRange(modifier, tree, daySchedule, overlap, resizedOverlap);

                        IntegerInterval newDayScheduleTimeRangeInterval = new IntegerInterval(timeRange.getStart(), overlap.getEnd());
                        addDayScheduleTimeRange(modifier, tree, daySchedule, modifier.getUniqueID(daySchedule.getName()), newDayScheduleTimeRangeInterval);
                        copyAllRangeReferences(modifier, daySchedule, resizedOverlap, newDayScheduleTimeRangeInterval);
                        addRangeReference(modifier, daySchedule, newDayScheduleTimeRangeInterval, match.getInstance());

                        updateNextDayScheduleTimeRange(modifier, daySchedule, resizedOverlap, newDayScheduleTimeRangeInterval);
                    }
                    else if(overlap.getEnd() > timeRange.getEnd()) {
                        IntegerInterval resizedOverlap = new IntegerInterval(timeRange.getEnd() + 1, overlap.getEnd());
                        resizeDayScheduleTimeRange(modifier, tree, daySchedule, overlap, resizedOverlap);

                        IntegerInterval newDayScheduleTimeRangeInterval = new IntegerInterval(overlap.getStart(), timeRange.getEnd());
                        addDayScheduleTimeRange(modifier, tree, daySchedule, modifier.getUniqueID(daySchedule.getName()), newDayScheduleTimeRangeInterval);
                        copyAllRangeReferences(modifier, daySchedule, resizedOverlap, newDayScheduleTimeRangeInterval);
                        addRangeReference(modifier, daySchedule, newDayScheduleTimeRangeInterval, match.getInstance());

                        updatePreviousDayScheduleTimeRange(modifier, daySchedule, resizedOverlap, newDayScheduleTimeRangeInterval);
                    }
                    else {
                        addRangeReference(modifier, daySchedule, overlap, match.getInstance());
                    }
                }
            }
    }

    public static void processRemoveRange(PolicyModifier modifier, IntervalTree tree,
                                                          RangeP.Match match) throws ModelManipulationException{
        System.out.println("Process RemoveRange called with:" + match.toString());
        EList<DayScheduleTimeRange> DayScheduleTimeRanges = match.getInstance().getDayScheduleTimeRanges();
        ECollections.sort(DayScheduleTimeRanges, new DayScheduleTimeRangeComperator()); //Note: regular Collections.sort can not be used! see: https://www.eclipse.org/forums/index.php?t=msg&th=127964/

        DaySchedule daySchedule = match.getDaySchedule();

        DayScheduleTimeRange first = DayScheduleTimeRanges.get(0);
        DayScheduleTimeRange last = DayScheduleTimeRanges.get(DayScheduleTimeRanges.size() - 1);
        modifier.getManipulation().remove(first, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), match.getInstance());
        if(first.getPrevious() != null && containSameRanges(first.getTemporalContextTimeRanges(), first.getPrevious().getTemporalContextTimeRanges())) {
            mergeDayScheduleTimeRanges(modifier, tree, daySchedule, first.getPrevious(), first);
        }


        modifier.getManipulation().remove(last, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), match.getInstance());
        if(last.getNext() != null && containSameRanges(last.getTemporalContextTimeRanges(), last.getNext().getTemporalContextTimeRanges())) {
            mergeDayScheduleTimeRanges(modifier, tree, daySchedule, last, last.getNext());
        }

        List<DayScheduleTimeRange> list = new ArrayList<>(match.getInstance().getDayScheduleTimeRanges());
		for(DayScheduleTimeRange sr: list) {
			modifier.getManipulation().remove(sr, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), match.getInstance());
		}
		modifier.getManipulation().remove(daySchedule, ePackage.getDaySchedule_TimeRanges(), match.getInstance());
    }

    // ----------------------------------------------------------



    // ---------- Reference Management ----------

    private static void addRangeReference(PolicyModifier modifier, DaySchedule daySchedule,
                                                             IntegerInterval DayScheduleTimeRange, TimeRange timeRange) throws ModelManipulationException {
        DayScheduleTimeRange sr = findDayScheduleTimeRangeByDayScheduleAndInterval(daySchedule, DayScheduleTimeRange);
        modifier.manipulation.addTo(sr, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), timeRange);
    }

    private static void copyAllRangeReferences(PolicyModifier modifier, DaySchedule daySchedule, IntegerInterval from, IntegerInterval to) throws ModelManipulationException {
        DayScheduleTimeRange DayScheduleTimeRangeFrom = findDayScheduleTimeRangeByDayScheduleAndInterval(daySchedule, from);
        DayScheduleTimeRange DayScheduleTimeRangeTo = findDayScheduleTimeRangeByDayScheduleAndInterval(daySchedule, to);
        for (TimeRange timeRange : DayScheduleTimeRangeFrom.getTemporalContextTimeRanges()) {
            modifier.manipulation.addTo(DayScheduleTimeRangeTo, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges(), timeRange);
        }
    }

    // ------------------------------------------



    // ---------- Schedule Range Methods ----------

    public static DayScheduleTimeRange addDayScheduleTimeRange(PolicyModifier modifier, IntervalTree tree, DaySchedule daySchedule, String name, IntegerInterval interval) throws ModelManipulationException {
        //System.out.println("add " + interval.toString() );
        DayScheduleTimeRange sr = (com.vanderhighway.trbac.model.trbac.model.DayScheduleTimeRange) modifier.getManipulation().createChild(daySchedule, ePackage.getDaySchedule_Instances(), ePackage.getDayScheduleTimeRange());
        modifier.getManipulation().set(sr, ePackage.getTimeRange_Name(), name);
        modifier.getManipulation().set(sr, ePackage.getTimeRange_Start(), interval.getStart());
        modifier.getManipulation().set(sr, ePackage.getTimeRange_End(), interval.getEnd());
        tree.add(interval);
        return sr;
    }

    private static void resizeDayScheduleTimeRange(PolicyModifier modifier, IntervalTree tree, DaySchedule daySchedule, IntegerInterval originalInterval, IntegerInterval newInterval) throws ModelManipulationException {
        //System.out.println("resize " + originalInterval.toString() + " to " + newInterval  );
        DayScheduleTimeRange sr =  findDayScheduleTimeRangeByDayScheduleAndInterval(daySchedule, originalInterval);
        modifier.getManipulation().set(sr, ePackage.getTimeRange_Start(), newInterval.getStart());
        modifier.getManipulation().set(sr, ePackage.getTimeRange_End(), newInterval.getEnd());
        tree.remove(originalInterval);
        tree.add(newInterval);
    }

    private static void mergeDayScheduleTimeRanges(PolicyModifier modifier, IntervalTree tree, DaySchedule daySchedule, DayScheduleTimeRange left, DayScheduleTimeRange right) throws ModelManipulationException {
        modifier.getManipulation().set(right, ePackage.getDayScheduleTimeRange_Previous(), left.getPrevious());
        removeDayScheduleTimeRange(modifier, tree, daySchedule, new IntegerInterval(left.getStart(), left.getEnd()));
        resizeDayScheduleTimeRange(modifier, tree, daySchedule, new IntegerInterval(right.getStart(), right.getEnd()),
                new IntegerInterval(left.getStart(), right.getEnd()));
    }

    private static void removeDayScheduleTimeRange(PolicyModifier modifier, IntervalTree tree, DaySchedule daySchedule, IntegerInterval interval) throws ModelManipulationException {
        DayScheduleTimeRange sr = findDayScheduleTimeRangeByDayScheduleAndInterval(daySchedule, interval);
        modifier.getManipulation().remove(daySchedule, ePackage.getDaySchedule_Instances(), sr);
        modifier.getManipulation().remove(sr, ePackage.getDayScheduleTimeRange_TemporalContextTimeRanges());
        modifier.getManipulation().remove(sr);
        tree.remove(interval);
    }
    
    private static void updateNextDayScheduleTimeRange(PolicyModifier modifier, DaySchedule daySchedule, IntegerInterval fromInterval, IntegerInterval toInterval) throws ModelManipulationException {
        DayScheduleTimeRange fromDayScheduleTimeRange = findDayScheduleTimeRangeByDayScheduleAndInterval(daySchedule, fromInterval);
        DayScheduleTimeRange toDayScheduleTimeRange = findDayScheduleTimeRangeByDayScheduleAndInterval(daySchedule, toInterval);
        if(fromDayScheduleTimeRange.getNext() == null) {
            //System.out.println("next from: " + fromDayScheduleTimeRange.toString() + " to: " + toDayScheduleTimeRange.toString() );
            modifier.getManipulation().set(fromDayScheduleTimeRange, ePackage.getDayScheduleTimeRange_Next(), toDayScheduleTimeRange);
        } else {
            DayScheduleTimeRange originalToDayScheduleTimeRange = fromDayScheduleTimeRange.getNext();
            //System.out.println("next from: " + fromDayScheduleTimeRange.toString() + " to: " + toDayScheduleTimeRange );
            modifier.getManipulation().set(fromDayScheduleTimeRange, ePackage.getDayScheduleTimeRange_Next(), toDayScheduleTimeRange);
            //System.out.println("next from: " + fromDayScheduleTimeRange.toString() + " to: " + originalToDayScheduleTimeRange.toString() );
            modifier.getManipulation().set(toDayScheduleTimeRange, ePackage.getDayScheduleTimeRange_Next(), originalToDayScheduleTimeRange);
        }
    }

    private static void updatePreviousDayScheduleTimeRange(PolicyModifier modifier, DaySchedule daySchedule, IntegerInterval toInterval, IntegerInterval fromInterval) throws ModelManipulationException {
        DayScheduleTimeRange toDayScheduleTimeRange = findDayScheduleTimeRangeByDayScheduleAndInterval(daySchedule, toInterval);
        DayScheduleTimeRange fromDayScheduleTimeRange = findDayScheduleTimeRangeByDayScheduleAndInterval(daySchedule, fromInterval);

        if(toDayScheduleTimeRange.getPrevious() == null) {
            //System.out.println("next from: " + fromDayScheduleTimeRange.toString() + " to: " + toDayScheduleTimeRange.toString() );
            modifier.getManipulation().set(fromDayScheduleTimeRange, ePackage.getDayScheduleTimeRange_Next(), toDayScheduleTimeRange);
        } else {
            DayScheduleTimeRange originalFromDayScheduleTimeRange = toDayScheduleTimeRange.getPrevious();

            //System.out.println("next from: " + fromDayScheduleTimeRange.toString() + " to: " + toDayScheduleTimeRange );
            modifier.getManipulation().set(fromDayScheduleTimeRange, ePackage.getDayScheduleTimeRange_Next(), toDayScheduleTimeRange);
            //System.out.println("next from: " + originalFromDayScheduleTimeRange.toString() + " to: " + fromDayScheduleTimeRange.toString() );
            modifier.getManipulation().set(originalFromDayScheduleTimeRange, ePackage.getDayScheduleTimeRange_Next(), fromDayScheduleTimeRange);
        }
    }

    private static BatchTransformationRule removeNextDayScheduleTimeRange(PolicyModifier modifier, DaySchedule daySchedule, IntegerInterval fromInterval) {
//        final Consumer<PolicyP.Match> function = (PolicyP.Match it) -> {
//            try {
//                DayScheduleTimeRange fromDayScheduleTimeRange = findDayScheduleTimeRangeByDayScheduleAndInterval(daySchedule, fromInterval);
//                modifier.getManipulation().remove(fromDayScheduleTimeRange, ePackage.getDayScheduleTimeRange_Next());
//            } catch (ModelManipulationException e) {
//                e.printStackTrace();
//            }
//        };
//        final BatchTransformationRule<PolicyP.Match, PolicyP.Matcher> addDayScheduleTimeRangeRule =
//                modifier.getBatchFactory().createRule(PolicyP.instance()).action(function)
//                        .name("remove-next-DayScheduleTimeRange").build(); //TODO: finish name!
//        return addDayScheduleTimeRangeRule;
        return null;
    }

    // --------------------------------------------



    // ---------- Find Ranges / Schedule Ranges ----------

    public static DayScheduleTimeRange findDayScheduleTimeRangeByDayScheduleAndInterval(DaySchedule daySchedule, IntegerInterval interval) {
        for(DayScheduleTimeRange sr: daySchedule.getInstances()) {
            if(interval.getStart() == sr.getStart() && interval.getEnd() == sr.getEnd()) {
                return sr;
            }
        }
        return null;
    }

//    public static TimeRange findRangeByRangeGroupAndDayScheduleAndName(Scenario group, DaySchedule daySchedule, String name) {
//        for(TimeRange timeRange : group()) {
//            if(name.equals(timeRange.getName()) && daySchedule.getTimeRanges().contains(timeRange)) {
//                return timeRange;
//            }
//        }
//        return null;
//    }

    // ---------------------------------------------------

    public static boolean containSameRanges(EList<TimeRange> list1, EList<TimeRange> list2) {
        RangeComperator comperator = new RangeComperator();
        ECollections.sort(list1, comperator);
        ECollections.sort(list2, comperator);
        return list1.equals(list2);
    }

    public static class DayScheduleTimeRangeComperator implements Comparator<DayScheduleTimeRange> {
        @Override
        public int compare(DayScheduleTimeRange o1, DayScheduleTimeRange o2) {
            return Integer.compare(o1.getStart(),o2.getStart());
        }
    }

    public static class RangeComperator implements Comparator<TimeRange> {
        @Override
        public int compare(TimeRange o1, TimeRange o2) {
            return Integer.compare(o1.getStart(),o2.getStart());
        }
    }

}
