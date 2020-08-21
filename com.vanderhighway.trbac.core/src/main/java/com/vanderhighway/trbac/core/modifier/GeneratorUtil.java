package com.vanderhighway.trbac.core.modifier;

import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.vanderhighway.trbac.core.modifier.PolicyModifier;
import com.vanderhighway.trbac.model.trbac.model.*;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
import org.eclipse.xtext.xbase.lib.Extension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class GeneratorUtil {

    @Extension
    private static TRBACPackage ePackage = TRBACPackage.eINSTANCE;

    private static List<String> allMonths = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
            "September", "October", "November", "December");
    private static List<Integer> monthDays = Arrays.asList(31,29,31,30,31,30,31,31,30,31,30,31);
    private static List<String> allDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

    public static Resource generateAndSaveResource(TRBACPackage pckg, String fileName) throws IOException {

        String fileSeparator = System.getProperty("file.separator");

        // Initializing the EMF package
        pckg.eINSTANCE.getName();
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("trbac", new XMIResourceFactoryImpl());
        Resource.Factory.Registry.INSTANCE.getContentTypeToFactoryMap().put("*", new XMIResourceFactoryImpl());

        ResourceSet set = new ResourceSetImpl();

        String relativePath = "." + fileSeparator + fileName + ".trbac";
        File file = new File(relativePath);
        if (file.createNewFile()) {
            System.out.println(relativePath + " File Created in Project root directory");
        } else {
            System.out.println("File " + relativePath + " already exists in the project root directory");
        }
        URI uri = URI.createFileURI(relativePath);
        Resource resource = set.createResource(uri);
        return resource;
    }

    public static SecurityPolicy buildBasicSecurityPolicy(TRBACPackage ePackage, Resource resource, String securityPolicyName, String authorizationPolicyName, String ScheduleName, String startDate, String endDate) {

        resource.getContents().add(EcoreUtil.create(ePackage.getSecurityPolicy()));
        SecurityPolicy securityPolicy = ((SecurityPolicy) resource.getContents().get(0));
        securityPolicy.setName("DummySecurityPolicy");

        AuthorizationPolicy authorizationPolicy = (AuthorizationPolicy) EcoreUtil.create(ePackage.getAuthorizationPolicy());
        resource.getContents().add(authorizationPolicy);
        authorizationPolicy.setName("DummyAuthorizationPolicy");
        securityPolicy.setAuthorizationPolicy(authorizationPolicy);

        Schedule schedule = (Schedule) EcoreUtil.create(ePackage.getSchedule());
        schedule.setName("DummySchedule");
        schedule.setStartDate("2020-01-01");
        schedule.setEndDate("2030-01-01");
        securityPolicy.setSchedule(schedule);

        return securityPolicy;
    }

    public static DayOfWeekSchedule getOrCreateDayOfWeekSchedule(PolicyModifier modifier, String scheduleName) throws ModelManipulationException {
        DayOfWeekSchedule daySchedule = (DayOfWeekSchedule) modifier.getResource().getEObject(scheduleName);
        if(daySchedule == null) {
            if (allDays.contains(scheduleName)) {
                daySchedule = modifier.addDayOfWeekSchedule(scheduleName);
            }
        }
        return daySchedule;
    }

    public static DayOfMonthSchedule getOrCreateDayOfMonthSchedule(PolicyModifier modifier, String scheduleName) throws ModelManipulationException {
        DayOfMonthSchedule daySchedule = (DayOfMonthSchedule) modifier.getResource().getEObject(scheduleName);
        if(daySchedule == null) {
            String[] parts = scheduleName.split("_");
            if (allMonths.contains(parts[1])) {
                daySchedule = modifier.addDayOfMonthSchedule(scheduleName);
            }
        }
        return daySchedule;
    }

    public static DayOfYearSchedule getOrCreateDayOfYearSchedule(PolicyModifier modifier, String scheduleName) throws ModelManipulationException {
        DayOfYearSchedule daySchedule = (DayOfYearSchedule) modifier.getResource().getEObject(scheduleName);
        if(daySchedule == null) {
            String[] parts = scheduleName.split("_");
            DayOfWeekSchedule ws = getOrCreateDayOfWeekSchedule(modifier, parts[0]);
            DayOfMonthSchedule ms = getOrCreateDayOfMonthSchedule(modifier, parts[1] + "_" + parts[2]);
            daySchedule = modifier.addDayOfYearSchedule(ws, ms, scheduleName);
        }
        return daySchedule;
    }

    public static void addManyTemporalContextInstances(PolicyModifier modifier, TemporalContext context, List<String> Schedules, List<IntegerInterval> intervals) throws ModelManipulationException {
        for(String schedule: Schedules) {
            DaySchedule daySchedule = (DaySchedule) modifier.getResource().getEObject(schedule);

            if(daySchedule == null) {
                if(allDays.contains(schedule)) {
                    daySchedule = getOrCreateDayOfWeekSchedule(modifier, schedule);
                }
                else if(schedule.split("_").length == 2) {
                    daySchedule = getOrCreateDayOfMonthSchedule(modifier, schedule);
                } else if(schedule.split("_").length == 3) {
                    daySchedule = getOrCreateDayOfYearSchedule(modifier, schedule);
                }
            }
            for (IntegerInterval interval: intervals) {
                modifier.addTemporalContextInstance(context, daySchedule, interval);
            }
        }
    }
}
