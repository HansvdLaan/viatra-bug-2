package com.vanderhighway.trbac.core.modifier;

import com.brein.time.timeintervals.intervals.IntegerInterval;
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
import java.lang.reflect.InvocationTargetException;
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

    /**
     * Create a TRBAC model and save it.
     * @param fileName file name of the model
     * @return the created TRBAC model.
     */
    public static Resource createAndSaveTRBACModel(String fileName) throws IOException {

        String fileSeparator = System.getProperty("file.separator");

        // Initializing the EMF package
        ePackage.eINSTANCE.getName();
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

    public static SiteAccessControlSystem buildBasicSecurityPolicy(TRBACPackage ePackage, Resource resource, String securityPolicyName, String authorizationPolicyName,
                                                                   String ScheduleName, String startDate, String endDate) {

        resource.getContents().add(EcoreUtil.create(ePackage.getSiteAccessControlSystem()));
        SiteAccessControlSystem system = ((SiteAccessControlSystem) resource.getContents().get(0));
        system.setName("DummySecurityPolicy");

        AuthorizationPolicy authorizationPolicy = (AuthorizationPolicy) EcoreUtil.create(ePackage.getAuthorizationPolicy());
        resource.getContents().add(authorizationPolicy);
        authorizationPolicy.setName("DummyAuthorizationPolicy");
        system.setAuthorizationPolicy(authorizationPolicy);

        AuthenticationPolicy authenticationPolicy = (AuthenticationPolicy) EcoreUtil.create(ePackage.getAuthenticationPolicy());
        resource.getContents().add(authenticationPolicy);
        authorizationPolicy.setName("DummyAuthenticationPolicy");
        system.setAuthenticationPolicy(authenticationPolicy);

        Schedule schedule = (Schedule) EcoreUtil.create(ePackage.getSchedule());
        schedule.setName("DummySchedule");
        schedule.setStartDate("2020-01-01");
        schedule.setEndDate("2030-01-01");
        system.setSchedule(schedule);

        return system;
    }

    public static DayOfWeekSchedule getOrCreateDayOfWeekSchedule(PolicyModifier modifier, String scheduleName) throws ModelManipulationException, InvocationTargetException {
        DayOfWeekSchedule daySchedule = (DayOfWeekSchedule) modifier.getResource().getEObject(scheduleName);
        if(daySchedule == null) {
            if (allDays.contains(scheduleName)) {
                daySchedule = modifier.addDayOfWeekSchedule(scheduleName);
            }
        }
        return daySchedule;
    }

    public static DayOfMonthSchedule getOrCreateDayOfMonthSchedule(PolicyModifier modifier, String scheduleName) throws ModelManipulationException, InvocationTargetException {
        DayOfMonthSchedule daySchedule = (DayOfMonthSchedule) modifier.getResource().getEObject(scheduleName);
        if(daySchedule == null) {
            String[] parts = scheduleName.split("_");
            if (allMonths.contains(parts[1])) {
                daySchedule = modifier.addDayOfMonthSchedule(scheduleName);
            }
        }
        return daySchedule;
    }

    public static DayOfWeekMonthSchedule getOrCreateDayOfWeekMonthSchedule(PolicyModifier modifier, String scheduleName) throws ModelManipulationException, InvocationTargetException {
        DayOfWeekMonthSchedule daySchedule = (DayOfWeekMonthSchedule) modifier.getResource().getEObject(scheduleName);
        if(daySchedule == null) {
            String[] parts = scheduleName.split("_");
            DayOfWeekSchedule ws = getOrCreateDayOfWeekSchedule(modifier, parts[0]);
            DayOfMonthSchedule ms = getOrCreateDayOfMonthSchedule(modifier, parts[1] + "_" + parts[2]);
            daySchedule = modifier.addDayOfWeekMonthSchedule(ws, ms, scheduleName);
        }
        return daySchedule;
    }

    public static DayOfYearSchedule getOrCreateDayOfYearSchedule(PolicyModifier modifier, String scheduleName) throws ModelManipulationException, InvocationTargetException {
        DayOfYearSchedule daySchedule = (DayOfYearSchedule) modifier.getResource().getEObject(scheduleName);
        if(daySchedule == null) {
            String[] parts = scheduleName.split("_");
            DayOfWeekMonthSchedule wms = getOrCreateDayOfWeekMonthSchedule(modifier, parts[0] + "_" + parts[1] + "_" + parts[2]);
            daySchedule = modifier.addDayOfYearSchedule(wms, scheduleName);
        }
        return daySchedule;
    }

    public static void addManyTemporalContextInstances(PolicyModifier modifier, TemporalContext context, List<String> Schedules, List<IntegerInterval> intervals) throws ModelManipulationException, InvocationTargetException {
        for(String schedule: Schedules) {
            DaySchedule daySchedule = (DaySchedule) modifier.getResource().getEObject(schedule);

            if(daySchedule == null) {
                if(allDays.contains(schedule)) {
                    daySchedule = getOrCreateDayOfWeekSchedule(modifier, schedule);
                }
                else if(schedule.split("_").length == 2) {
                    daySchedule = getOrCreateDayOfMonthSchedule(modifier, schedule);
                } else if(schedule.split("_").length == 3) {
                    daySchedule = getOrCreateDayOfWeekMonthSchedule(modifier, schedule);
                } else if(schedule.split("_").length == 4) {
                    daySchedule = getOrCreateDayOfYearSchedule(modifier, schedule);
                } else {
                    throw new IllegalArgumentException("Can not parse schedule \"" + schedule + "\"" );
                }
            }
            for (IntegerInterval interval: intervals) {
                modifier.addTemporalContextInstance(context, daySchedule, interval);
            }
        }
    }
}
