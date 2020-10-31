package analyzer;

import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanderhighway.trbac.core.CoreUtils;
import com.vanderhighway.trbac.core.modifier.PolicyAutomaticModifier;
import com.vanderhighway.trbac.core.modifier.PolicyModifier;
import com.vanderhighway.trbac.model.trbac.model.*;
import com.vanderhighway.trbac.patterns.PolicyConstraints;
import com.vanderhighway.trbac.patterns.PolicyRelations;
import com.vanderhighway.trbac.patterns.PolicySmells;
import evaluator.CompoundMeasurement;
import evaluator.Measurement;
import evaluator.TestCaseResult;
import generators.datatypes.*;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Level;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngineOptions;
import org.eclipse.viatra.query.runtime.emf.EMFScope;
import org.eclipse.viatra.query.runtime.localsearch.matcher.integration.LocalSearchEMFBackendFactory;
import org.eclipse.viatra.query.runtime.localsearch.matcher.integration.LocalSearchHints;
import org.eclipse.viatra.query.runtime.matchers.backend.QueryEvaluationHint;
import org.eclipse.viatra.query.runtime.rete.matcher.DRedReteBackendFactory;
import org.eclipse.viatra.query.runtime.util.ViatraQueryLoggingUtil;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;

import java.io.*;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Analyzer {

    public static List<String> allWeekDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    public static List<String> allMonthDays;
    public static List<String> allWeekMonthDays;
    public static List<String> allYearDays;

    public static void main(String[] args) throws ParseException, ModelManipulationException, InterruptedException, IOException {

        // output all unexpected errors to the log file!
        System.setErr(new PrintStream(new FileOutputStream("errors.log", true), true));

        TRBACPackage.eINSTANCE.getName();
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("trbac", new XMIResourceFactoryImpl());
        Resource.Factory.Registry.INSTANCE.getContentTypeToFactoryMap().put("*", new XMIResourceFactoryImpl());

        ObjectMapper mapper = new ObjectMapper();
        File testSuiteCollectionFile = new File(args[0]);
        TestSuiteCollection collection = mapper.readValue(testSuiteCollectionFile, new TypeReference<TestSuiteCollection>() {});

        Map<String, List<TestCaseResult>> results = new HashMap<>();
        for(TestSuite suite: collection.getSuites()) {
            results.put(suite.getName(), new LinkedList<>());
            File resultDirectory = new File(args[1] + suite.getOutputDirectory());
            File[] resultFiles = resultDirectory.listFiles();
            List<File> orderedResultFiles = Arrays.stream(resultFiles).sorted().collect(Collectors.toList());
            for(File resultFile: orderedResultFiles) {
                TestCaseResult result = mapper.readValue(resultFile, new TypeReference<TestCaseResult>() {});
                results.get(suite.getName()).add(result);
            }
        }

        Map<String, List<Double>> flattenedResults = new HashMap<>();
        for(String suite: results.keySet()) {
            flattenedResults.put(suite, new LinkedList<>());
            for(TestCaseResult result: results.get(suite)) {
                double totalTime = 0;
                for (CompoundMeasurement cm : result.getMeasurements()) {
                    for(Measurement m: cm.getMeasurements()) {
                        totalTime += m.getTime();
                    }
                }
                flattenedResults.get(suite).add(totalTime);
            }
        }

        Map<String, DescriptiveStatistics> descriptiveStatisticsMap = new HashMap<>();
        for (String suite: results.keySet()) {
            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
            for (double value: flattenedResults.get(suite)) {
                descriptiveStatistics.addValue(value);
            }
            descriptiveStatisticsMap.put(suite, descriptiveStatistics);
        }

        Map<String, Map<String, Double>> statistics = new HashMap<>();
        for (String suite: results.keySet()) {
            HashMap sc = new HashMap<>();
            DescriptiveStatistics descriptiveStatistics = descriptiveStatisticsMap.get(suite);

            double Q1 = descriptiveStatistics.getPercentile(25);
            double median = descriptiveStatistics.getPercentile(50);
            double Q3 = descriptiveStatistics.getPercentile(75);
            double IQR = Q3 - Q1;
            double highRange = Q3 + 3 * IQR;
            double lowRange = Q1 - 3 * IQR;

            sc.put("Q1", Q1);
            sc.put("Q3", Q3);
            sc.put("IQR", IQR);
            sc.put("highRange", highRange);
            sc.put("lowRange", lowRange);

        }

        System.out.println("results!");
    }

    private static Measurement performAction(Action action, PolicyModifier modifier) throws Exception {
        long timeElapsed;
        long memoryUsed;
        long start;
        long finish;
        switch (action.getType()) {
            case ADD_USER: {
                String name = action.getParameters().get(0);
                start = System.nanoTime();
                modifier.addUser(name);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case ADD_ROLE: {
                String name = action.getParameters().get(0);
                start = System.nanoTime();
                modifier.addRole(name);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case ADD_DEMARCATION: {
                String name = action.getParameters().get(0);
                start = System.nanoTime();
                modifier.addUser(name);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case ADD_PERMISSION: {
                String name = action.getParameters().get(0);
                start = System.nanoTime();
                modifier.addPermission(name);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case ADD_TEMPORALCONTEXT: {
                String name = action.getParameters().get(0);
                start = System.nanoTime();
                modifier.addTemporalContext(name);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case ADD_TEMPORALCONTEXTINSTANCE: {
                TemporalContext context = (TemporalContext) modifier.getResource().getEObject(action.getParameters().get(0));
                DaySchedule daySchedule = (DaySchedule) modifier.getResource().getEObject(action.getParameters().get(1));
                int lowerBound = Integer.parseInt(action.getParameters().get(2));
                int upperBound = Integer.parseInt(action.getParameters().get(3));
                start = System.nanoTime();
                modifier.addTemporalContextInstance(context, daySchedule, new IntegerInterval(lowerBound, upperBound));
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case ADD_TEMPORALGRANTRULE: {
                TemporalContext context = (TemporalContext) modifier.getResource().getEObject(action.getParameters().get(1));
                String name = action.getParameters().get(0);
                Role role = (Role) modifier.getResource().getEObject(action.getParameters().get(2));
                Demarcation demarcation = (Demarcation) modifier.getResource().getEObject(action.getParameters().get(3));
                boolean enable = Boolean.parseBoolean(action.getParameters().get(4));
                int priority = Integer.parseInt(action.getParameters().get(5));
                start = System.nanoTime();
                modifier.addTemporalGrantRule(context, name, role, demarcation, enable, priority);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case ADD_TEMPORALAUTHENTICATIONRULE: {
                TemporalContext context = (TemporalContext) modifier.getResource().getEObject(action.getParameters().get(1));
                String name = action.getParameters().get(0);
                SecurityZone zone = (SecurityZone) modifier.getResource().getEObject(action.getParameters().get(2));
                int status = Integer.parseInt(action.getParameters().get(3));
                int priority = Integer.parseInt(action.getParameters().get(4));
                start = System.nanoTime();
                modifier.addTemporalAuthenticationRule(context, name, zone, status, priority);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case ADD_SECURITYZONE: {
                String name = action.getParameters().get(0);
                boolean isPublic = Boolean.parseBoolean(action.getParameters().get(1));
                start = System.nanoTime();
                modifier.addSecurityZone(name, isPublic);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case ADD_UNIDIRECTIONAL_REACHABILITY: {
                SecurityZone fromZone = (SecurityZone) modifier.getResource().getEObject(action.getParameters().get(0));
                SecurityZone toZone = (SecurityZone) modifier.getResource().getEObject(action.getParameters().get(1));
                start = System.nanoTime();
                modifier.setReachability(fromZone, toZone);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case REMOVE_USER: {
                User user = (User) modifier.getResource().getEObject(action.getParameters().get(0));
                start = System.nanoTime();
                modifier.removeUser(user);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case REMOVE_ROLE: {
                Role role = (Role) modifier.getResource().getEObject(action.getParameters().get(0));
                start = System.nanoTime();
                modifier.removeRole(role);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case REMOVE_DEMARCATION: {
                Demarcation demarcation = (Demarcation) modifier.getResource().getEObject(action.getParameters().get(0));
                start = System.nanoTime();
                modifier.removeDemarcation(demarcation);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case REMOVE_PERMISSION: {
                Permission permission = (Permission) modifier.getResource().getEObject(action.getParameters().get(0));
                start = System.nanoTime();
                modifier.removePermission(permission);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case REMOVE_TEMPORALCONTEXT: {
                TemporalContext context = (TemporalContext) modifier.getResource().getEObject(action.getParameters().get(0));
                start = System.nanoTime();
                modifier.removeTemporalContext(context);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case REMOVE_TEMPORALCONTEXTINSTANCE:
                TimeRange timeRange = (TimeRange) modifier.getResource().getEObject(action.getParameters().get(0));
                start = System.nanoTime();
                modifier.removeTemporalContextInstance(timeRange);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            case REMOVE_TEMPORALGRANTRULE: {
                TemporalGrantRule rule = (TemporalGrantRule) modifier.getResource().getEObject(action.getParameters().get(0));
                start = System.nanoTime();
                modifier.removeTemporalGrantRule(rule);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case REMOVE_TEMPORALAUTHENTICATIONRULE: {
                TemporalAuthenticationRule rule = (TemporalAuthenticationRule) modifier.getResource().getEObject(action.getParameters().get(0));
                start = System.nanoTime();
                modifier.removeTemporalAuthenticationRule(rule);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case REMOVE_SECURITYZONE: {
                SecurityZone zone = (SecurityZone) modifier.getResource().getEObject(action.getParameters().get(0));
                start = System.nanoTime();
                modifier.removeSecurityZone(zone);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case REMOVE_UNIDIRECTIONAL_REACHABILITY: {
                SecurityZone fromZone = (SecurityZone) modifier.getResource().getEObject(action.getParameters().get(0));
                SecurityZone toZone = (SecurityZone) modifier.getResource().getEObject(action.getParameters().get(1));
                start = System.nanoTime();
                modifier.removeReachability(fromZone, toZone);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case ASSIGN_ROLE_TO_USER: {
                User user = (User) modifier.getResource().getEObject(action.getParameters().get(0));
                Role role = (Role) modifier.getResource().getEObject(action.getParameters().get(1));
                start = System.nanoTime();
                modifier.assignRoleToUser(user, role);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case ASSIGN_PERMISSION_TO_DEMARCATION: {
                Demarcation demarcation = (Demarcation) modifier.getResource().getEObject(action.getParameters().get(0));
                Permission permission = (Permission) modifier.getResource().getEObject(action.getParameters().get(1));
                start = System.nanoTime();
                modifier.assignPermissionToDemarcation(demarcation, permission);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case ASSIGN_OBJECT_TO_PERMISSION: {
                Permission permission = (Permission) modifier.getResource().getEObject(action.getParameters().get(0));
                XObject object = (XObject) modifier.getResource().getEObject(action.getParameters().get(1));
                start = System.nanoTime();
                modifier.assignObjectToPermission(permission, object);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case DEASSIGN_ROLE_FROM_USER: {
                User user = (User) modifier.getResource().getEObject(action.getParameters().get(0));
                Role role = (Role) modifier.getResource().getEObject(action.getParameters().get(1));
                start = System.nanoTime();
                modifier.assignRoleToUser(user, role);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case DEASSIGN_PERMISSION_FROM_DEMARCATION: {
                Demarcation demarcation = (Demarcation) modifier.getResource().getEObject(action.getParameters().get(0));
                Permission permission = (Permission) modifier.getResource().getEObject(action.getParameters().get(1));
                start = System.nanoTime();
                modifier.assignPermissionToDemarcation(demarcation, permission);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            case DEASSIGN_OBJECT_FROM_PERMISSION: {
                Permission permission = (Permission) modifier.getResource().getEObject(action.getParameters().get(0));
                XObject object = (XObject) modifier.getResource().getEObject(action.getParameters().get(1));
                start = System.nanoTime();
                modifier.deassignObjectFromPermission(permission, object);
                finish = System.nanoTime();
                timeElapsed = finish - start;
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown action type: " + action.getType());
        }

        Runtime runtime = Runtime.getRuntime();
        //runtime.gc();
        long memory = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
        return new Measurement(timeElapsed / 1_000_000.0, memory);
    }

    public static <T> T getRandom(List<T> list, Random seed) {
        int objectIndex = seed.nextInt(list.size());
        return list.get(objectIndex);
    }

    //TODO: place this somewhere as a core utility!
    public static String addDayToDateString(String dateString) throws ParseException {
        Calendar cal = Calendar.getInstance();
        DateFormat format = new SimpleDateFormat("d_MMMM_yyyy", Locale.ENGLISH);
        cal.setTime(format.parse(dateString));
        int day = cal.get(Calendar.DAY_OF_WEEK);
        String newDateString = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday").get(day - 1);
        newDateString = newDateString + "_" + dateString;
        return newDateString;
    }
}
