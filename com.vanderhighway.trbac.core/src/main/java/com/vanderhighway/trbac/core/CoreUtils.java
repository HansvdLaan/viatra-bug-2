package com.vanderhighway.trbac.core;

import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.vanderhighway.trbac.model.trbac.model.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
import org.eclipse.xtext.xbase.lib.Extension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class CoreUtils {

    @Extension
    private static TRBACPackage ePackage = TRBACPackage.eINSTANCE;

    // Map used to give a unique ID to instances;
    private HashMap<String, Integer> instanceIDCounter;
    
    public CoreUtils() {
        this.instanceIDCounter = new HashMap<>();
    }
    
    public void addMissingDaySchedules(Resource resource, SecurityPolicy policy, String startDateString, String endDateString) throws ModelManipulationException, ParseException {

        Map<String, DayOfWeekSchedule> dayOfWeekScheduleMap = new HashMap<>();
        Map<String, Map<Integer, DayOfMonthSchedule>> dayOfMonthScheduleMap = new HashMap();
        Schedule schedule = policy.getAuthorizationPolicy().getSchedule();
        TemporalContext alwaysTC = (TemporalContext) resource.getEObject("Always");

        List<String> allDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        for (String day: allDays) {
            DayOfWeekSchedule ws;
            if(resource.getEObject(day) == null) {
                ws = addDayOfWeekScheduleCore(schedule, day);
            } else {
                ws = (DayOfWeekSchedule) resource.getEObject(day);
            }
            dayOfWeekScheduleMap.put(day, ws);
            addTemporalContextInstanceCore(schedule, alwaysTC, ws, new IntegerInterval(0, 1439));
            addDayScheduleTimeRangeCore(ws, new IntegerInterval(0, 1439));
        }

        List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
                "September", "October", "November", "December");
        List<Integer> monthDays = Arrays.asList(31,29,31,30,31,30,31,31,30,31,30,31);
        for (int monthIndex = 0; monthIndex < months.size(); monthIndex++) {
            dayOfMonthScheduleMap.put(months.get(monthIndex), new HashMap<>());
            for (int dayIndex = 0; dayIndex < monthDays.get(monthIndex); dayIndex++) {
                DayOfMonthSchedule ms;
                String name = (dayIndex+1) + "_" + months.get(monthIndex);
                if(resource.getEObject(name) == null) {
                    ms = addDayOfMonthScheduleCore(schedule, name);
                } else {
                    ms = (DayOfMonthSchedule) resource.getEObject(name);
                }
                dayOfMonthScheduleMap.get(months.get(monthIndex)).put(dayIndex, ms);
                addTemporalContextInstanceCore(schedule, alwaysTC, ms, new IntegerInterval(0, 1439));
                addDayScheduleTimeRangeCore(ms, new IntegerInterval(0, 1439));
            }
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = formatter.parse(startDateString);
        Date endDate = formatter.parse(endDateString);
        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
            DayOfWeekSchedule weekSchedule = dayOfWeekScheduleMap.get(allDays.get(date.getDayOfWeek().getValue()-1));
            DayOfMonthSchedule monthSchedule = dayOfMonthScheduleMap.get(months.get(date.getMonthValue() - 1)).get(date.getDayOfMonth() - 1);

            DayOfYearSchedule ys;
            String name = weekSchedule.getName() + "_" + monthSchedule.getName() + "_" + date.getYear();
            if(resource.getEObject(name) == null) {
                ys = addDayOfYearScheduleCore(schedule, weekSchedule, monthSchedule, name);
            } else {
                ys = (DayOfYearSchedule) resource.getEObject(name);
            }
            addTemporalContextInstanceCore(schedule, alwaysTC, ys, new IntegerInterval(0, 1439));
            addDayScheduleTimeRangeCore(ys, new IntegerInterval(0, 1439));
        }
    }

    public DayOfWeekSchedule addDayOfWeekScheduleCore(Schedule schedule, String name) throws ModelManipulationException {
        DayOfWeekSchedule dw = (DayOfWeekSchedule) EcoreUtil.create(ePackage.getDayOfWeekSchedule());
        dw.setName(name);
        schedule.getDaySchedules().add(dw);
        return dw;
    }

    public DayOfMonthSchedule addDayOfMonthScheduleCore(Schedule schedule, String name) throws ModelManipulationException {
        DayOfMonthSchedule dm = (DayOfMonthSchedule) EcoreUtil.create(ePackage.getDayOfMonthSchedule());
        dm.setName(name);
        schedule.getDaySchedules().add(dm);
        return dm;
    }
    public DayOfYearSchedule addDayOfYearScheduleCore(Schedule schedule, DayOfWeekSchedule weekSchedule, DayOfMonthSchedule monthSchedule, String name) throws ModelManipulationException {
        DayOfYearSchedule dy = (DayOfYearSchedule) EcoreUtil.create(ePackage.getDayOfYearSchedule());
        dy.setName(name);
        dy.setDayOfWeekSchedule(weekSchedule);
        dy.setDayOfMonthSchedule(monthSchedule);
        schedule.getDaySchedules().add(dy);
        return dy;
    }

    public TimeRange addTemporalContextInstanceCore(Schedule schedule, TemporalContext context, DaySchedule daySchedule, IntegerInterval interval) throws ModelManipulationException {
        TimeRange instance = (TimeRange) EcoreUtil.create(ePackage.getTimeRange());
        instance.setName(getUniqueID(getUniqueID(context.getName() + "-" + daySchedule.getName())));
        instance.setDaySchedule(daySchedule);
        instance.setStart(interval.getStart());
        instance.setEnd(interval.getEnd());
        context.getInstances().add(instance);
        return instance;
    }

    public DayScheduleTimeRange addDayScheduleTimeRangeCore(DaySchedule daySchedule, IntegerInterval interval) throws ModelManipulationException {
        DayScheduleTimeRange instance = (DayScheduleTimeRange) EcoreUtil.create(ePackage.getDayScheduleTimeRange());
        instance.setName(getUniqueID(daySchedule.getName()));
        instance.setStart(interval.getStart());
        instance.setEnd(interval.getEnd());
        daySchedule.getInstances().add(instance);
        return instance;
    }

    public String getUniqueID(String key) {
        this.instanceIDCounter.putIfAbsent(key, 0);
        this.instanceIDCounter.put(key, this.instanceIDCounter.get(key) + 1);
        return key + "-" + this.instanceIDCounter.get(key);
    }

    public HashMap<String, Integer> getInstanceIDCounter() {
        return instanceIDCounter;
    }

    public void setInstanceIDCounter(HashMap<String, Integer> instanceIDCounter) {
        this.instanceIDCounter = instanceIDCounter;
    }
}
