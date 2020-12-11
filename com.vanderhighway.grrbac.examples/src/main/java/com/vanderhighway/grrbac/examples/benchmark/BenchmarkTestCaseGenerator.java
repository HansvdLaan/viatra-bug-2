package com.vanderhighway.grrbac.examples.benchmark;

import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanderhighway.grrbac.core.modifier.GeneratorUtil;
import com.vanderhighway.grrbac.core.modifier.PolicyModifier;
import com.vanderhighway.grrbac.examples.util.*;
import com.vanderhighway.grrbac.model.grrbac.model.*;
import com.vanderhighway.grrbac.patterns.AuthenticationStatus;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.viatra.query.runtime.emf.EMFScope;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BenchmarkTestCaseGenerator {

    @Extension
    private static GRRBACPackage ePackage = GRRBACPackage.eINSTANCE;

    private static List<String> allDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private static List<String> weekDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
    private static List<String> weekEnd = Arrays.asList("Saturday", "Sunday");
    private static List<String> MonToThu = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday");

    private static int MAX_USER_COUNT = 8000;

    public static void main(String[] args) throws IOException, InvocationTargetException, ModelManipulationException, ModelManipulationException, ParseException {

        // Set up template access control policy
        Resource resource = GeneratorUtil.createAndSaveGRRBACModel("benchmark");
        SiteAccessControlSystem system = GeneratorUtil.buildBasicSecurityPolicy(ePackage, resource, "TestSecurityPolicy",
                "TestAuthorizationPolicy", "TestAuthenticationPolicy",
                "TestTopology", "TestSchedule");

        final AdvancedViatraQueryEngine engine = AdvancedViatraQueryEngine.createUnmanagedEngine(new EMFScope(resource));
        PolicyModifier modifier = new PolicyModifier(engine, (SiteAccessControlSystem) resource.getContents().get(0), resource);

        // Sets to keep track of used Names
        Set<String> userNames = new LinkedHashSet<>();
        Set<String> roleNames = new LinkedHashSet<>();
        Set<String> demarcationNames = new LinkedHashSet<>();
        Set<String> permissionNames = new LinkedHashSet<>();
        Set<String> unkownTransitions = new LinkedHashSet<>();
        Set<String> temporalContextNames = new LinkedHashSet<>();
        Map<String, Set<Permission>> transitionToSecurityZones = new LinkedHashMap<>();
        Map<String, Set<String>> URRel = new LinkedHashMap<>();
        Map<String, Set<String>> DPRel = new LinkedHashMap<>();
        Map<String, Map<String, Set<String>>> RTDRel = new LinkedHashMap<>();
        List<Integer> sortedBounds = new LinkedList<>();

        //Should only exist one per user and one per (user,role)!
        Map<String, String> proxyRoles = new LinkedHashMap<>(); // User -> Role
        Map<String, Set<String>> proxyDemarcations = new LinkedHashMap<>(); // Role -> Demarcation
        int proxyDemarcationCounter = 0;
        int proxyRoleCounter = 0;

        // Load Files
        String authorizationsFilePath = System.getProperty("user.home") + "\\Documents\\TestData\\Case1\\authorisation.csv";
        File authorizationsFile = new File(authorizationsFilePath);
        String stairsFilePath = System.getProperty("user.home") + "\\Documents\\TestData\\Case1\\stairs.csv";
        File stairsFile = new File(stairsFilePath);
        File[] caseFileNames = new File(System.getProperty("user.home") + "\\Documents\\TestData\\Case1\\").listFiles(); //all file names of the case
        List<File> roomFiles = Arrays.stream(caseFileNames).filter(c -> c.getName().contains("rooms")).collect(Collectors.toList());
        List<File> topologyFiles = Arrays.stream(caseFileNames).filter(c -> c.getName().contains("topology")).collect(Collectors.toList());
        List<File> scheduleFiles = Arrays.stream(caseFileNames).filter(c -> c.getName().contains("schedules")).collect(Collectors.toList());

        // Export Files
        File topologyExportFile = new File("./topology_performance_case.graphml");

        // Add always temporal context and weekday dayschedules
        TemporalContext always = modifier.addTemporalContext("Always");
        temporalContextNames.add("Always");
        for (String day : allDays) {
            modifier.addValidDayOfWeek(day);
        }

        Map<String, Map<String, Set<String>>> temporalGrantRules = new LinkedHashMap<>();
        int temporalGrantRuleCounter = 0;

        LocalDateTime startTime = LocalDateTime.now();

        // Load Rooms
        System.out.println("Loading Rooms (from: " + roomFiles.toString() + ")");
        ObjectMapper mapper = new ObjectMapper();
        Set<Room> rooms = new LinkedHashSet<>();
        int floorID = 0; //Since rooms on different floors may have the same ID.
        for (File roomsFile : roomFiles) {
            List<Room> parsedRooms = mapper.readValue(roomsFile, new TypeReference<List<Room>>() {
            });
            int finalFloorID = floorID;
            parsedRooms.forEach(r -> r.setFloorID(finalFloorID));
            parsedRooms.forEach(r -> {
                if (r.getName().contains("room")) {
                    r.setName("f" + r.floorID + "_" + r.getName());
                }
            });
            floorID++;
            rooms.addAll(parsedRooms);
        }

        // Load Transitions
        System.out.println("Loading Transitions (from: " + topologyFiles.toString() + ")");
        floorID = 0;
        Set<Transition> transitions = new LinkedHashSet<>();
        for (File topologyFile : topologyFiles) {
            List<Transition> parsedTransitions = mapper.readValue(topologyFile, new TypeReference<List<Transition>>() {
            });
            int finalFloorID = floorID;
            parsedTransitions.forEach(t -> t.setFloorID(finalFloorID));
            parsedTransitions.forEach(t -> {
                if (t.getName().contains("Transition")) {
                    t.setName("f" + t.floorID + "_" + t.getName());
                }
            });
            transitions.addAll(parsedTransitions);
            floorID++;
        }

        // Load Schedules
        System.out.println("Loading Schedules (from: " + scheduleFiles.toString() + ")");
        floorID = 0;
        Set<ScheduleRoutine> schedules = new LinkedHashSet<>();
        for (File scheduleFile : scheduleFiles) {
            List<ScheduleRoutine> parsedSchedules = mapper.readValue(scheduleFile, new TypeReference<List<ScheduleRoutine>>() {});
            int finalFloorID1 = floorID;
            parsedSchedules.forEach(s -> s.setFloorID(finalFloorID1));
            schedules.addAll(parsedSchedules);
            floorID++;
        }

        // Transform Schedule Routines into Temporal Contexts
        Set<ScheduleRoutineTemporalContext> scheduleRoutineTemporalContexts = new LinkedHashSet<>();
        Map<Integer, Map<Integer, Set<ScheduleRoutineTemporalContext>>> transitionSideToTemporalContext = new LinkedHashMap<>();
        Map<ScheduleRoutineTemporalContext, TemporalContext> scheduleRoutineTemporalContextTemporalContextMap = new LinkedHashMap<>();
        for (ScheduleRoutine routine : schedules) {
            Set<ScheduleRoutineTemporalContext> contextSet = ScheduleRoutineTemporalContext.extractFromScheduleRoutine(routine);
            scheduleRoutineTemporalContexts.addAll(contextSet);
            transitionSideToTemporalContext.putIfAbsent(routine.getFloorID(), new LinkedHashMap<>());
            transitionSideToTemporalContext.get(routine.getFloorID()).put(routine.id, contextSet);
        }

        Map<String, Integer> strcCounterMap = new LinkedHashMap<>();
        for (ScheduleRoutineTemporalContext strc : scheduleRoutineTemporalContexts) {
            strcCounterMap.putIfAbsent(strc.getStatus().toString(), 0);
            TemporalContext context = BenchmarkTestCaseGenerator.processScheduleRoutineTemporalContext("ScheduleRoutine_"
                            + strc.getStatus().toString() + "_" + strcCounterMap.get(strc.getStatus().toString()),
                    strc, modifier);
            scheduleRoutineTemporalContextTemporalContextMap.put(strc, context);
            strcCounterMap.put(strc.getStatus().toString(), strcCounterMap.get(strc.getStatus().toString()) + 1);
        }

        // Transform Rooms into Security Zones and add a Temporal Authentication Rule which states it is always unlocked
        System.out.println("Transforming Rooms into Security Zones & Add Temporal Authentication Rules");
        Map<Integer, Map<Integer, String>> roomIDToLabelMap = new LinkedHashMap<>(); //first argument is the floor number, second one the roomID.
        for (Room room : rooms) {
            roomIDToLabelMap.putIfAbsent(room.floorID, new LinkedHashMap<>());
            roomIDToLabelMap.get(room.floorID).put(room.id, room.getName());
            if (resource.getEObject(room.getName()) == null) { //The room "Outside" is present in both room lists
                SecurityZone zone = modifier.addSecurityZone(room.getName(), room.isOutside);
                modifier.addTemporalAuthenticationRule(always, "TAR_" + zone.getName(), zone,
                        AuthenticationStatus.UNLOCKED.getStatusCode(), 3);
            }
        }

        // Transforming Transitions into Security Zones and add corresponding Temporal Authentication Rules
        System.out.println("Transforming Transitions into Security Zones & Add Temporal Authentication Rules");
        for (Transition transition : transitions) {
            transition.name = transition.name.replace("&", " and ").replace(" ", "_"); //Entrance
            SecurityZone z1 = (SecurityZone) modifier.getResource().getEObject(
                    roomIDToLabelMap.get(transition.floorID).get(transition.sides[0].roomOnThisSide));
            SecurityZone z2 = (SecurityZone) modifier.getResource().getEObject(
                    roomIDToLabelMap.get(transition.floorID).get(transition.sides[1].roomOnThisSide));

            // if the door is always open, just connect the two security zones directly
            if (!transition.sides[0].annotations.isAccessControlled && !transition.sides[0].annotations.isLocked) {
                modifier.setReachability(z1, z2);
            } else if (!transition.sides[0].annotations.isLocked) {
                SecurityZone securityZoneDoor1To2 = modifier.addSecurityZone(transition.name + "_"
                        + z1.getName() + "_To_" + z2.getName(), false);
                modifier.setReachability(z1, securityZoneDoor1To2);
                modifier.setReachability(securityZoneDoor1To2, z2);

                Set<ScheduleRoutineTemporalContext> strcSet = transitionSideToTemporalContext.get(transition.floorID).getOrDefault(transition.sides[0].id, new LinkedHashSet<>());
                for (ScheduleRoutineTemporalContext strc : strcSet) {
                    TemporalContext context = scheduleRoutineTemporalContextTemporalContextMap.get(strc);
                    modifier.addTemporalAuthenticationRule(
                            context,
                            "TAR_" + securityZoneDoor1To2.getName() + "_" + strc.getStatus().toString(),
                            securityZoneDoor1To2,
                            strc.getStatus().getStatusCode(),
                            3
                    );
                }

                String permissionName = "permission_" + transition.name + "_1_To_2";
                Permission permission = modifier.addPermission(permissionName);
                modifier.assignObjectToPermission(permission, securityZoneDoor1To2);

                transitionToSecurityZones.putIfAbsent(transition.name, new LinkedHashSet<>());
                transitionToSecurityZones.get(transition.name).add(permission);
            }
            if (!transition.sides[1].annotations.isAccessControlled && !transition.sides[1].annotations.isLocked) {
                modifier.setReachability(z2, z1);
            } else if (!transition.sides[1].annotations.isLocked) {
                SecurityZone securityZoneDoor2To1 = modifier.addSecurityZone(transition.name + "_"
                        + z2.getName() + "_To_" + z1.getName(), false);
                modifier.setReachability(z2, securityZoneDoor2To1);
                modifier.setReachability(securityZoneDoor2To1, z1);

                Set<ScheduleRoutineTemporalContext> strcSet = transitionSideToTemporalContext.get(transition.floorID).getOrDefault(
                        transition.sides[1].id, new LinkedHashSet<>());
                for (ScheduleRoutineTemporalContext strc : strcSet) {
                    TemporalContext context = scheduleRoutineTemporalContextTemporalContextMap.get(strc);
                    modifier.addTemporalAuthenticationRule(
                            context,
                            "TAR_" + securityZoneDoor2To1.getName() + "_" + strc.getStatus().toString(),
                            securityZoneDoor2To1,
                            strc.getStatus().getStatusCode(),
                            3
                    );
                }

                String permissionName = "permission_" + transition.name + "_2_To_1";
                Permission permission = (Permission) resource.getEObject(permissionName);
                if (permission == null) {
                    permission = modifier.addPermission(permissionName);
                }
                modifier.assignObjectToPermission(permission, securityZoneDoor2To1);

                transitionToSecurityZones.putIfAbsent(transition.name, new LinkedHashSet<>());
                transitionToSecurityZones.get(transition.name).add(permission);
            }
        }

        // Load stairs/elevators and add corresponding reachability
        System.out.println("Loading Stairs/Elevators (from: " + stairsFile.getName() + ")");
        FileInputStream inputStreamStairs = new FileInputStream(stairsFile);
        Scanner scStairs = new Scanner(inputStreamStairs, "UTF-8");
        scStairs.nextLine(); // Skip header line
        while (scStairs.hasNextLine()) {
            String line = scStairs.nextLine();
            String[] info = line.split(";");
            SecurityZone z1 = (SecurityZone) modifier.getResource().getEObject(info[0]);
            SecurityZone z2 = (SecurityZone) modifier.getResource().getEObject(info[1]);
            modifier.setBidirectionalReachability(z1, z2);
        }

        // Convert authorizations
        long lineCount;
        System.out.print("Converting Authorizations:     ");
        try (Stream<String> stream = Files.lines(Paths.get(authorizationsFilePath))) {
            lineCount = stream.count();
        }
        long lineCountPercent = lineCount / 100;
        try {
            FileInputStream inputStream = new FileInputStream(authorizationsFile);
            Scanner sc = new Scanner(inputStream, "UTF-8");
            sc.nextLine(); // Skip header line
            int scanCount = 1;
            while (sc.hasNextLine()) {

                scanCount++;
                if (0 == (scanCount % (lineCountPercent * 5))) {
                    System.out.print("\u0008\u0008\u0008" + scanCount / lineCountPercent + "%");
                }

                String line = sc.nextLine();
                String[] info = line.split(";");

                // Parse Names
                String userName = info[2]; // Person(nel) no
                String roleName = info[5]; //Template
                String demarcationName = info[6]; //Entrance Group
                String transitionName = info[7].replace("&", " and ").replace(" ", "_"); //Entrance
                String temporalContextName = info[8]; //Schedule

                // Create various entities if they have not been seen before
                // Create user if this is the first time this role was encountered
                if (!userNames.contains(userName) && userNames.size() < MAX_USER_COUNT) {
                    modifier.addUser(userName);
                    userNames.add(userName);
                    URRel.put(userName, new LinkedHashSet<>());
                } else if (!userNames.contains(userName) && userNames.size() >= MAX_USER_COUNT) {
                    continue; // limit the amount of users
                }

                // Create new proxy role if no template was specified
                roleName = roleName.replace("&", " and ").replace(" ", "_").replace("/", "_");
                if (roleName.isEmpty()) {
                    proxyRoles.putIfAbsent(userName, "ProxyRole" + proxyRoles.size());
                    proxyRoleCounter++;
                    roleName = proxyRoles.get(userName);
                }

                // Create role if this is the first time this role was encountered
                if (!roleNames.contains(roleName)) {
                    modifier.addRole(roleName);
                    roleNames.add(roleName);
                }

                URRel.get(userName).add(roleName);

                // Skip cases where no transition is specified
                if (transitionName.isEmpty()) {
                    continue;
                }

                // Skip cases where the entrance does not corresponds to a known transition (e.g. because the probe is incomplete)
                if (!transitionToSecurityZones.containsKey(transitionName)) {
                    unkownTransitions.add(transitionName);
                    continue;
                }

                temporalGrantRules.putIfAbsent(roleName, new LinkedHashMap<>());

                // create new proxy demarcation if no entrance group was specified
                demarcationName = demarcationName.replace("&", " and ").replace(" ", "_");
                if (demarcationName.isEmpty()) {
                    proxyDemarcations.putIfAbsent(roleName, new LinkedHashSet<>());
                    demarcationName = "ProxyDemarcation" + proxyDemarcationCounter++;
                    proxyDemarcations.get(roleName).add(demarcationName);
                }

                // Create demarcation if this is the first time this demarcation was encountered
                if (!demarcationNames.contains(demarcationName)) {
                    modifier.addDemarcation(demarcationName);
                    demarcationNames.add(demarcationName);
                }
                temporalGrantRules.get(roleName).putIfAbsent(demarcationName, new LinkedHashSet<>());
                DPRel.putIfAbsent(demarcationName, new LinkedHashSet<>());

                // Fix some naming issues
                temporalContextName = temporalContextName.replace("&", " and ").replace(" ", "_").replace("/", "_").replace(":", "-");
                if (temporalContextName.equals("Altijd") || temporalContextName.equals("AlwaysValidSchedule") || temporalContextName.equals("Nedap_UK_Always")) {
                    temporalContextName = "Always";
                }
                if (temporalContextName.isEmpty()) {
                    temporalContextName = "Never";
                }

                // Create temporal context if this is the first time this temporal context was encountered
                if (!temporalContextNames.contains(temporalContextName)) {
                    modifier.addTemporalContext(temporalContextName);
                    temporalContextNames.add(temporalContextName);
                }

                RTDRel.putIfAbsent(roleName, new LinkedHashMap<>());
                RTDRel.get(roleName).putIfAbsent(temporalContextName, new LinkedHashSet<>());
                RTDRel.get(roleName).get(temporalContextName).add(demarcationName);

                DPRel.get(demarcationName).addAll(transitionToSecurityZones.get(transitionName).stream().map(x -> x.getName())
                        .sorted().collect(Collectors.toList()));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


//        System.out.println("Created " + system.getAuthorizationPolicy().getUsers().size() + " Users.");
//        System.out.println("Created " + system.getAuthorizationPolicy().getRoles().size() + " Roles (" + proxyRoleCounter + " proxy).");
//        System.out.println("Created " + system.getAuthorizationPolicy().getDemarcations().size() + " Demarcations (" + proxyDemarcationCounter + " proxy).");
//        System.out.println("Created " + system.getAuthorizationPolicy().getPermissions().size() + " Permissions.");
//        System.out.println("Created " + system.getSchedule().getTemporalGrantRules().size() + " TGR.");
//        System.out.println("Created " + system.getAuthenticationPolicy().getTemporalAuthenticationRules().size() + " TAR.");

        // Assign converted role to converted users.
        for(User user: system.getAuthorizationPolicy().getUsers()) {
            for (String roleName : URRel.get(user.getName())) {
                Role role = (Role) modifier.getResource().getEObject(roleName);
                modifier.assignRoleToUser(user, role);
            }
        }

        //Remove certain non-Groenlo roles
        modifier.removeRole((Role) modifier.getResource().getEObject("Nedap_UK_All_Doors_24_7_less_Arlington_Comms"));
        modifier.removeRole((Role) modifier.getResource().getEObject("Nedap_China_Office_Shanghai"));
        modifier.removeRole((Role) modifier.getResource().getEObject("Nedap_HongKong_Office"));
        modifier.removeRole((Role) modifier.getResource().getEObject("Nedap_Poland_Employee"));
        modifier.removeRole((Role) modifier.getResource().getEObject("Nedap_Dubai_Office"));
        modifier.removeRole((Role) modifier.getResource().getEObject("Nedap_Spain_Employee"));
        modifier.removeRole((Role) modifier.getResource().getEObject("Nedap_UK_All_Doors_24_7"));
        modifier.removeRole((Role) modifier.getResource().getEObject("Nedap_Spain_Employee__and__IT_Tech_rooms"));
        modifier.removeRole((Role) modifier.getResource().getEObject("Nedap_Spain_Cleaning"));
        modifier.removeRole((Role) modifier.getResource().getEObject("Nedap_Poland_Maintenance"));

        //Remove rooms/transitions which can only be reached through a door locked with a key.
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("15.1-R2"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f1_Transition_686_15.1-R2_To_15.1-R1"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f1_Transition_686_15.1-R1_To_15.1-R2"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("15.1-R1"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("13-R9"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("13-D2_13-R9_To_13-R4"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("13-R4"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f0_room_1001"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("9.1a"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f0_room_146"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f1_room_615"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f0_room_78"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f0_room_142"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("8-R7"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f1_room_200"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f0_room_6"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f1_room_251"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f1_room_138"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f1_room_252"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f0_room_157"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("8a-R3"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f1_room_256"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("10-R2"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f1_room_140"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f1_room_255"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f1_room_616"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f0_room_75"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("5.1-R5"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f0_room_103"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f0_room_62"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f1_room_206"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("f0_room_7"));
        modifier.removeSecurityZone((SecurityZone) modifier.getResource().getEObject("HVK1"));

        //Assign permissions to demarcations, group proxy roles together and add temporal grant rules
        int oldProxyDemarcationCounter = proxyDemarcationCounter;
        for (String roleName : RTDRel.keySet()) {
            Role role = (Role) modifier.getResource().getEObject(roleName);
            for (String contextName : RTDRel.get(roleName).keySet()) {
                TemporalContext context = (TemporalContext) modifier.getResource().getEObject(contextName);
                Set<String> existingDemarcationNames = new LinkedHashSet<>(RTDRel.get(roleName).get(contextName).stream().filter(d -> !d.contains("Proxy"))
                        .sorted().collect(Collectors.toList()));
                for (String demarcationName : existingDemarcationNames) {
                    Demarcation demarcation = (Demarcation) modifier.getResource().getEObject(demarcationName);
                    for (String permissionName : DPRel.get(demarcation.getName())) {
                        Permission permission = (Permission) modifier.getResource().getEObject(permissionName);
                        if(permission != null) { //since we removed permissions
                            modifier.assignPermissionToDemarcation(demarcation, permission);
                        }
                    }
                    modifier.addTemporalGrantRule(context, "TGR" + temporalGrantRuleCounter++, role, demarcation, true, 3);
                    temporalGrantRules.get(roleName).get(demarcationName).add(contextName);
                }
                Set<String> proxyDemarcationNames = new LinkedHashSet<>(
                        RTDRel.get(roleName).get(contextName).stream().filter(d -> d.contains("Proxy"))
                                .sorted().collect(Collectors.toList()));
                if (proxyDemarcationNames.size() > 0) {
                    Demarcation groupedProxyDemarcation = modifier.addDemarcation("GroupedProxyDemarcation" + proxyDemarcationCounter++);
                    for (String proxyDemarcationName : proxyDemarcationNames) {
                        for(String permissionName: DPRel.get(proxyDemarcationName)) {
                                Permission permission =  (Permission) modifier.getResource().getEObject(permissionName);
                                if(permission != null) { //since we removed permissions
                                    modifier.assignPermissionToDemarcation(groupedProxyDemarcation, permission);
                                }
                        }
                        Demarcation proxyDemarcation = (Demarcation) modifier.getResource().getEObject(proxyDemarcationName);
                        modifier.removeDemarcation(proxyDemarcation);
                    }
                    modifier.addTemporalGrantRule(context, "TGR" + temporalGrantRuleCounter++, role, groupedProxyDemarcation, true, 3);
                }
            }
        }
        proxyDemarcationCounter = (int) system.getAuthorizationPolicy().getDemarcations().stream().filter(r -> r.getName().contains("Proxy")).count();
        System.out.println("\nReduced " + oldProxyDemarcationCounter + " to " + proxyDemarcationCounter + " proxy demarcations through grouping.");

        Map<Set<Permission>, Set<Demarcation>> equivilantDemarcations = new LinkedHashMap<>();
        for (Demarcation demarcation : system.getAuthorizationPolicy().getDemarcations()) {
            if (demarcation.getName().contains("Proxy")) {
                Set<Permission> permissions = new LinkedHashSet<>(demarcation.getDP());
                equivilantDemarcations.putIfAbsent(permissions, new LinkedHashSet<>());
                equivilantDemarcations.get(permissions).add(demarcation);
            }
        }

        int uniqueProxyDemarcationCounter = 0;
        for (Set<Permission> permissions : equivilantDemarcations.keySet()) {
            Demarcation uniqueDemarcation = modifier.addDemarcation("UniqueProxyDemarcation" + uniqueProxyDemarcationCounter);
            for (Permission permission : permissions) {
                modifier.assignPermissionToDemarcation(uniqueDemarcation, permission);
            }
            for (Demarcation demarcation: equivilantDemarcations.get(permissions)) {
                for(TemporalGrantRule temporalGrantRule: demarcation.getConstrainedBy().stream().collect(Collectors.toList())) {
                    modifier.getManipulation().set(temporalGrantRule, ePackage.getTemporalGrantRule_Demarcation(), uniqueDemarcation);
                }
                modifier.removeDemarcation(demarcation);
            }
            uniqueProxyDemarcationCounter++;
        }

        oldProxyDemarcationCounter = proxyDemarcationCounter;
        proxyDemarcationCounter = (int) system.getAuthorizationPolicy().getDemarcations().stream().filter(r -> r.getName().contains("Proxy")).count();
        System.out.println("Reduced " + oldProxyDemarcationCounter + " to " + proxyDemarcationCounter + " proxy demarcations by removing non-unique one's. ");

        //Remove empty demarcations
        int oldDemarcationCount = system.getAuthorizationPolicy().getDemarcations().size();
        List<Demarcation> dummyDemarcations = system.getAuthorizationPolicy().getDemarcations().stream().filter(demarcation -> demarcation.getDP().size() == 0).collect(Collectors.toList());
        for (Demarcation demarcation : dummyDemarcations) {
            modifier.removeDemarcation(demarcation);
        }

        System.out.println("Reduced " + oldDemarcationCount + " to " + system.getAuthorizationPolicy().getDemarcations().size()
                + " demarcations by removing empty demarcations.");

        //Rename UniqueProxyDemarcations to just ProxyDemarcations and restart counting from 0
        List<Demarcation> uniqueProxyDemarcations = system.getAuthorizationPolicy().getDemarcations().stream()
                .filter(r -> r.getName().contains("Proxy")).collect(Collectors.toList());
        proxyDemarcationCounter = 1;
        for(Demarcation demarcation: uniqueProxyDemarcations) {
            demarcation.setName("ProxyDemarcation" + proxyDemarcationCounter++);
        }

        //Group equivalent roles together
        int proxyRoleCount = (int) system.getAuthorizationPolicy().getRoles().stream().filter(r -> r.getName().contains("Proxy")).count();
        Map<Set<Pair<TemporalContext, Demarcation>>, Set<Role>> uniqueProxyRoles = new LinkedHashMap<>();
        for (Role proxyRole : system.getAuthorizationPolicy().getRoles().stream().filter(r -> r.getName().contains("Proxy")).sorted(Comparator.comparing(Role::getName))
                .collect(Collectors.toList())) {
            Set<Pair<TemporalContext, Demarcation>> grantPairs = new LinkedHashSet<Pair<TemporalContext, Demarcation>>();
            for (TemporalGrantRule rule : proxyRole.getConstrainedBy()) {
                grantPairs.add(new Pair<>(rule.getTemporalContext(), rule.getDemarcation()));
            }
            uniqueProxyRoles.putIfAbsent(grantPairs, new LinkedHashSet<>());
            uniqueProxyRoles.get(grantPairs).add(proxyRole);
        }

        // Replace equivalent role by a new "Unique Proxy Role"
        int uniqueProxyRoleCounter = 0;
        for (Set<Pair<TemporalContext, Demarcation>> key : uniqueProxyRoles.keySet()) {
            Role uniqueRole = modifier.addRole("UniqueProxyRole" + uniqueProxyRoleCounter);
            uniqueProxyRoleCounter++;
            for (Role role : uniqueProxyRoles.get(key)) {
                for (User user : new LinkedHashSet<>(role.getRU())) {
                    user.getUR().add(uniqueRole);
                    user.getUR().remove(role);
                }
                modifier.removeRole(role);
            }

            for (Pair<TemporalContext, Demarcation> pair : key) {
                modifier.addTemporalGrantRule(pair.getKey(), "TGR" + temporalGrantRuleCounter++, uniqueRole, pair.getValue(), true, 3);
            }

            // Since the export contained roles without users.
            if (uniqueRole.getRU().isEmpty()) {
                modifier.removeRole(uniqueRole);
                uniqueProxyRoleCounter--;
            }
        }

        int oldProxyRoleCount = proxyRoleCount;
        proxyRoleCount = (int) system.getAuthorizationPolicy().getRoles().stream().filter(r -> r.getName().contains("Proxy")).count();
        System.out.println("Reduced " + oldProxyRoleCount + " to " + proxyRoleCount + " proxy roles by removing non-unique one's. ");

        //Remove empty roles
        oldProxyRoleCount = proxyRoleCount;
        List<Role> emptyRoles = system.getAuthorizationPolicy().getRoles().stream()
                .filter(role -> role.getConstrainedBy().size() == 0)
                .filter(role -> role.getName().contains("Proxy"))
                .collect(Collectors.toList());
        for (Role role : emptyRoles) {
            modifier.removeRole(role);
        }
        proxyRoleCount = (int) system.getAuthorizationPolicy().getRoles().stream().filter(r -> r.getName().contains("Proxy")).count();
        System.out.println("Reduced " + oldProxyRoleCount + " to " + proxyRoleCount
                + " roles by removing empty proxy roles.");

        //Rename UniqueProxyRoles to just ProxyRoles and restart counting from 0
        List<Role> uniqueProxyRoles2 = system.getAuthorizationPolicy().getRoles().stream()
                .filter(r -> r.getName().contains("Proxy")).collect(Collectors.toList());
        proxyRoleCount = 1;
        for(Role role: uniqueProxyRoles2) {
            role.setName("ProxyRole" + proxyRoleCount++);
        }


        // Reduce the set of users to unique users
        // Group equivalent users together
        int initialUserCount = (int) system.getAuthorizationPolicy().getUsers().size();
        Map<Set<Role>, Set<User>> uniqueUsers = new LinkedHashMap<>();
        int uniqueUserCounter = 0;
        for (User user : system.getAuthorizationPolicy().getUsers()) {
            Set<Role> roleSet = new LinkedHashSet<>(user.getUR().stream().sorted(Comparator.comparing(Role::getName)).collect(Collectors.toList()));
            uniqueUsers.putIfAbsent(roleSet, new LinkedHashSet<>());
            uniqueUsers.get(roleSet).add(user);
        }

        // Replace equivalent user by a new "Unique User"
        for (Set<Role> key : uniqueUsers.keySet()) {
            User uniqueUser = modifier.addUser("User" + uniqueUserCounter);
            for (Role role : key) {
                modifier.assignRoleToUser(uniqueUser, role);
            }
            uniqueUserCounter++;
            for (User user : uniqueUsers.get(key)) {
                ArrayDeque<Role> queue = new ArrayDeque<>(user.getUR());
                queue.spliterator().forEachRemaining(role -> {
                    try {
                        modifier.deassignRoleFromUser(user, role);
                    } catch (ModelManipulationException e) {
                        e.printStackTrace();
                    }
                });
                modifier.removeUser(user);
            }
        }

        System.out.println("Reduced " + initialUserCount + " users to " +
                (int) system.getAuthorizationPolicy().getUsers().size() + " unique users.");

        // Manually add temporal contexts
        TemporalContext tc1 = (TemporalContext) modifier.getResource().getEObject("AutoUnlock_01_-_ma_t_m_vr_07-15_-_16-45");
        if (tc1 == null) {
            tc1 = modifier.addTemporalContext("AutoUnlock_01_-_ma_t_m_vr_07-15_-_16-45");
        }
        TemporalContext tc2 = (TemporalContext) modifier.getResource().getEObject("AutoUnlock_04_-__ma_t_m_vr_07-45_-_17-30");
        if (tc2 == null) {
            tc2 = modifier.addTemporalContext("AutoUnlock_04_-__ma_t_m_vr_07-45_-_17-30");
        }
        TemporalContext tc3 = (TemporalContext) modifier.getResource().getEObject("AutoUnlock_07_-_ma_t_m_vr_07-30_-_16-30");
        if (tc3 == null) {
            tc3 = modifier.addTemporalContext("AutoUnlock_07_-_ma_t_m_vr_07-30_-_16-30");
        }
        TemporalContext tc4 = (TemporalContext) modifier.getResource().getEObject("AutoUnlock_08_-_ma_t_m_vr_07-30_-_16-30");
        if (tc4 == null) {
            tc4 = modifier.addTemporalContext("AutoUnlock_08_-_ma_t_m_vr_07-30_-_16-30");
        }
        TemporalContext tc5 = (TemporalContext) modifier.getResource().getEObject("Autounlock_05_-_ma_t_m_vr_07-30_-_17-30");
        if (tc5 == null) {
            tc5 = modifier.addTemporalContext("Autounlock_05_-_ma_t_m_vr_07-30_-_17-30");
        }
        TemporalContext tc6 = (TemporalContext) modifier.getResource().getEObject("ma_t_m_do_16-00_tot_23-00_vrij_14-45_t_m_23-00");
        if (tc6 == null) {
            tc6 = modifier.addTemporalContext("ma_t_m_do_16-00_tot_23-00_vrij_14-45_t_m_23-00");
        }
        TemporalContext tc7 = (TemporalContext) modifier.getResource().getEObject("ma_t_m_do_16-30_tot_20-00_vrij_16-00_t_m_20-00");
        if (tc7 == null) {
            tc7 = modifier.addTemporalContext("ma_t_m_do_16-30_tot_20-00_vrij_16-00_t_m_20-00");
        }
        TemporalContext tc8 = (TemporalContext) modifier.getResource().getEObject("ma_t_m_vr_05-30_-_23-15");
        if (tc8 == null) {
            tc8 = modifier.addTemporalContext("ma_t_m_vr_05-30_-_23-15");
        }
        TemporalContext tc9 = (TemporalContext) modifier.getResource().getEObject("ma_t_m_vr_06-00_-_17-00");
        if (tc9 == null) {
            tc9 = modifier.addTemporalContext("ma_t_m_vr_06-00_-_17-00");
        }
        TemporalContext tc10 = (TemporalContext) modifier.getResource().getEObject("ma_t_m_vr_06-00_-_19-00");
        if (tc10 == null) {
            tc10 = modifier.addTemporalContext("ma_t_m_vr_06-00_-_19-00");
        }
        TemporalContext tc11 = (TemporalContext) modifier.getResource().getEObject("ma_t_m_vr_06-30_-_11-30");
        if (tc11 == null) {
            tc11 = modifier.addTemporalContext("ma_t_m_vr_06-30_-_11-30");
        }
        TemporalContext tc12 = (TemporalContext) modifier.getResource().getEObject("ma_t_m_vr_06-30_-_17-30");
        if (tc12 == null) {
            tc12 = modifier.addTemporalContext("ma_t_m_vr_06-30_-_17-30");
        }
        TemporalContext tc13 = (TemporalContext) modifier.getResource().getEObject("ma_t_m_vr_07-00_-_18-00_za_07-00_-_16-00");
        if (tc13 == null) {
            tc13 = modifier.addTemporalContext("ma_t_m_vr_07-00_-_18-00_za_07-00_-_16-00");
        }
        TemporalContext tc14 = (TemporalContext) modifier.getResource().getEObject("ma_t_m_vr__05-00_-_19-00");
        if (tc14 == null) {
            tc14 = modifier.addTemporalContext("ma_t_m_vr__05-00_-_19-00");
        }
        TemporalContext tc15 = (TemporalContext) modifier.getResource().getEObject("ma_t_m_vr__07-00_-_19-00");
        if (tc15 == null) {
            tc15 = modifier.addTemporalContext("ma_t_m_vr__07-00_-_19-00");
        }

        // Schedules from AEOS
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc1, weekDays, Arrays.asList(new IntegerInterval(435, 1005)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc2, weekDays, Arrays.asList(new IntegerInterval(465, 1050)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc3, weekDays, Arrays.asList(new IntegerInterval(450, 990)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc4, weekDays, Arrays.asList(new IntegerInterval(450, 990)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc5, weekDays, Arrays.asList(new IntegerInterval(450, 1050)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc6, MonToThu, Arrays.asList(new IntegerInterval(960, 1380)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc6, Arrays.asList("Friday"), Arrays.asList(new IntegerInterval(885, 1380)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc7, MonToThu, Arrays.asList(new IntegerInterval(990, 1200)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc7, Arrays.asList("Friday"), Arrays.asList(new IntegerInterval(960, 1200)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc8, weekDays, Arrays.asList(new IntegerInterval(330, 1395)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc9, weekDays, Arrays.asList(new IntegerInterval(360, 1020)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc10, weekDays, Arrays.asList(new IntegerInterval(360, 1140)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc11, weekDays, Arrays.asList(new IntegerInterval(390, 690)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc12, weekDays, Arrays.asList(new IntegerInterval(390, 1050)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc13, weekDays, Arrays.asList(new IntegerInterval(420, 1080)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc13, Arrays.asList("Saturday"), Arrays.asList(new IntegerInterval(420, 960)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc14, weekDays, Arrays.asList(new IntegerInterval(300, 1140)));
        GeneratorUtil.addManyTemporalContextInstances(modifier, tc15, weekDays, Arrays.asList(new IntegerInterval(420, 1140)));

        // Add Constraints
        Role groenlo_Contractor5_19 = (Role) modifier.getResource().getEObject("Groenlo_Contractor_Externe_Medew._05:00_t_m_19:00");
        Role groenlo_Contractor7_19 = (Role) modifier.getResource().getEObject("Groenlo_Contractor_Externe_Medew._07:00_t_m_19:00");
        Role groenlo_Schoonmaak_medewerker_Smart = (Role) modifier.getResource().getEObject("Groenlo_Schoonmaak-medewerker_Smart");
        Role groenlo_Schoonmaak_medewerker = (Role) modifier.getResource().getEObject("Groenlo_Schoonmaak-medewerker");
        Role groenlo_ATD = (Role) modifier.getResource().getEObject("Groenlo_Contractor_Externe_Medew._07:00_t_m_19:00");
        Role groenlo_Medewerker = (Role) modifier.getResource().getEObject("Groenlo_Medewerker");
        Role groenlo_ATD_Monteurspas = (Role) modifier.getResource().getEObject("Groenlo_ATD-Monteurspas");
        Role groenlo_ATD_Asito_Kopie_1 = (Role) modifier.getResource().getEObject("Groenlo_ATD_Asito_Kopie_minus_geb.23a_en_Serverruimten");
        Role groenlo_ATD_Asito_Kopie_2 = (Role) modifier.getResource().getEObject("Groenlo_ATD_Asito_van_06:00_tot_17:30_minus_23a");
        Role groenlo_Pashouder_Receptie = (Role) modifier.getResource().getEObject("Groenlo_Pashouder_Receptie");
        Role groenlo_Telefoniste = (Role) modifier.getResource().getEObject("Groenlo_Telefoniste");
        Role groenlo_Bewaking = (Role) modifier.getResource().getEObject("Groenlo_Bewaking");
        Role groenlo_Schuifpoorten_Pendeldienst = (Role) modifier.getResource().getEObject("Groenlo_Schuifpoorten_Pendeldienst");
        Role groenlo_Tuinman = (Role) modifier.getResource().getEObject("Groenlo_Tuinman");
        Role groenlo_Verbouwing = (Role) modifier.getResource().getEObject("Groenlo_Verbouwing");

        modifier.addSoDURConstraint("Constraint2", groenlo_Contractor5_19, groenlo_Contractor7_19);
        modifier.addSoDURConstraint("Constraint4", groenlo_Schoonmaak_medewerker_Smart, groenlo_Schoonmaak_medewerker);
        modifier.addSoDURConstraint("Constraint8", groenlo_ATD, groenlo_Medewerker);
        modifier.addSoDURConstraint("Constraint9", groenlo_ATD, groenlo_ATD_Monteurspas);
        modifier.addSoDURConstraint("Constraint10", groenlo_ATD, groenlo_ATD_Asito_Kopie_1);
        modifier.addSoDURConstraint("Constraint11", groenlo_ATD, groenlo_ATD_Asito_Kopie_2);
        modifier.addSoDURConstraint("Constraint12", groenlo_ATD_Asito_Kopie_1, groenlo_ATD_Asito_Kopie_2);
        modifier.addPrerequisiteURConstraint("Constraint19", groenlo_Pashouder_Receptie, groenlo_Medewerker);
        modifier.addPrerequisiteURConstraint("Constraint23", groenlo_Telefoniste, groenlo_Medewerker);
        modifier.addSoDURConstraint("Constraint26", groenlo_ATD_Monteurspas, groenlo_Medewerker);
        modifier.addSoDURConstraint("Constraint32", groenlo_Bewaking, groenlo_Medewerker);
        modifier.addSoDURConstraint("Constraint38", groenlo_Schoonmaak_medewerker, groenlo_Medewerker);
        modifier.addSoDURConstraint("Constraint42", groenlo_Schuifpoorten_Pendeldienst, groenlo_Medewerker);
        modifier.addSoDURConstraint("Constraint44", groenlo_Tuinman, groenlo_Medewerker);
        modifier.addSoDURConstraint("Constraint46", groenlo_Verbouwing, groenlo_Medewerker);



        if(true) {
            //Anonimize Everything!
            List<Role> namedRoles = system.getAuthorizationPolicy().getRoles().stream()
                .filter(role -> !role.getName().contains("Proxy")).collect(Collectors.toList());
            int renameCounter = 1;
            for (Role role : namedRoles) {
                role.setName("Role" + renameCounter++);
            }

            List<Demarcation> namedDemarcations = system.getAuthorizationPolicy().getDemarcations().stream()
                    .filter(role -> !role.getName().contains("Proxy")).collect(Collectors.toList());
            renameCounter = 1;
            for (Demarcation demarcation : namedDemarcations) {
                demarcation.setName("Demarcation" + renameCounter++);
            }

            List<Permission> permissions = new LinkedList<>(system.getAuthorizationPolicy().getPermissions());
            renameCounter = 1;
            for (Permission permission : permissions) {
                permission.setName("Permission" + renameCounter++);
            }

            List<SecurityZone> zones = new LinkedList<>(system.getTopology().getSecurityZones());
            renameCounter = 1;
            for (SecurityZone zone : zones) {
                zone.setName("SecurityZone" + renameCounter++);
            }

            List<TemporalGrantRule> tgRules = new LinkedList<>(system.getAuthorizationPolicy().getTemporalGrantRules());
            renameCounter = 1;
            for (TemporalGrantRule rule : tgRules) {
                rule.setName("TGR" + renameCounter++);
            }

            List<TemporalAuthenticationRule> taRules = new LinkedList<>(system.getAuthenticationPolicy().getTemporalAuthenticationRules());
            renameCounter = 1;
            for (TemporalAuthenticationRule rule : taRules) {
                rule.setName("TAR" + renameCounter++);
            }

            List<TemporalContext> contexts = new LinkedList<>(system.getContextContainer().getTemporalContexts());
            Set<Integer> allBounds = new LinkedHashSet<>();
            renameCounter = 1;
            for (TemporalContext context : contexts) {

                //Should not rename always!
                if (context.getName().equals("Always")) {
                    continue;
                }
                context.setName("TC" + renameCounter);

                List<TimeRange> timeRanges = new LinkedList(context.getInstances());
                int renameCounter2 = 1;
                for (TimeRange tr : timeRanges) {
                    tr.setName("TC" + renameCounter + "_" + renameCounter2++);
                    allBounds.add(tr.getStart());
                    allBounds.add(tr.getEnd());
                }
                renameCounter++;
            }

            //Change bounds of temporal contexts
            System.out.println(allBounds);
            sortedBounds = new LinkedList<>(allBounds);
            Map<Integer, Integer> anonimizedBounds = new LinkedHashMap<>();
            Collections.sort(sortedBounds);
            for (int i = 0; i < sortedBounds.size(); i++) {
                int bound = sortedBounds.get(i);

                //preserve the start and end times of 00:00 and 23:59
                if (bound == 0 || bound == 1439) {
                    anonimizedBounds.put(bound, bound);
                } else {
                    int previousbound = sortedBounds.get(i - 1);
                    int previousAnonimizedBound = anonimizedBounds.get(previousbound);
                    if (previousbound == bound - 1) {
                        anonimizedBounds.put(bound, previousAnonimizedBound + 1);
                    } else {
                        anonimizedBounds.put(bound, previousAnonimizedBound + 10);
                    }
                }
            }

            for (TemporalContext context : system.getContextContainer().getTemporalContexts()) {
                if (!context.equals(always)) {
                    for (TimeRange tr : context.getInstances()) {
                        tr.setStart(anonimizedBounds.get(tr.getStart()));
                        tr.setEnd(anonimizedBounds.get(tr.getEnd()));
                    }
                }
            }
        }


        //Duplicate all data except the scenarios!

        int ENTITIES_DUPLICATE_COUNT = 0;

        List<User> originalUsers = new LinkedList<>(system.getAuthorizationPolicy().getUsers());
        List<Role> originalRoles = new LinkedList<>(system.getAuthorizationPolicy().getRoles());
        List<Demarcation> originalDemarcations = new LinkedList<>(system.getAuthorizationPolicy().getDemarcations());
        List<Permission> originalPermissions = new LinkedList<>(system.getAuthorizationPolicy().getPermissions());
        List<TemporalGrantRule> originalTemporalGrantRules = new LinkedList<>(system.getAuthorizationPolicy().getTemporalGrantRules());
        List<SecurityZone> originalSecurityZones = new LinkedList<>(system.getTopology().getSecurityZones());
        List<TemporalAuthenticationRule> originalTemporalAuthenticationRules = new LinkedList<>(system.getAuthenticationPolicy().getTemporalAuthenticationRules());

        for (User user : originalUsers) {
            for (int i = 1; i <= ENTITIES_DUPLICATE_COUNT; i++) {
                modifier.addUser(user.getName() + "_copy" + i);
            }
        }


        for (Role role : originalRoles) {
            for (int i = 1; i <= ENTITIES_DUPLICATE_COUNT; i++) {
                modifier.addRole(role.getName() + "_copy" + i);
            }
        }


        for (Demarcation demarcation : originalDemarcations) {
            for (int i = 1; i <= ENTITIES_DUPLICATE_COUNT; i++) {
                modifier.addDemarcation(demarcation.getName() + "_copy" + i);
            }
        }

        for (Permission permission : originalPermissions) {
            for (int i = 1; i <= ENTITIES_DUPLICATE_COUNT; i++) {
                modifier.addPermission(permission.getName() + "_copy" + i);
            }
        }

        for (User user : originalUsers) {
            for (int i = 1; i <= ENTITIES_DUPLICATE_COUNT; i++) {
                User userCopy = (User) resource.getEObject(user.getName() + "_copy" + i);
                for (Role role : user.getUR()) {
                    Role roleCopy = (Role) resource.getEObject(role.getName() + "_copy" + i);
                    modifier.assignRoleToUser(userCopy, roleCopy);
                }
            }
        }

        for (Demarcation demarcation : originalDemarcations) {
            for (int i = 1; i <= ENTITIES_DUPLICATE_COUNT; i++) {
                Demarcation demarcationCopy = (Demarcation) resource.getEObject(demarcation.getName() + "_copy" + i);
                for (Permission permission : demarcation.getDP()) {
                    Permission permissionCopy = (Permission) resource.getEObject(permission.getName() + "_copy" + i);
                    modifier.assignPermissionToDemarcation(demarcationCopy, permissionCopy);
                }
            }
        }

        for (TemporalGrantRule rule : originalTemporalGrantRules) {
            for (int i = 1; i <= ENTITIES_DUPLICATE_COUNT; i++) {
                Role roleCopy = (Role) resource.getEObject(rule.getRole().getName() + "_copy" + i);
                Demarcation demarcationCopy = (Demarcation) resource.getEObject(rule.getDemarcation().getName() + "_copy" + i);
                modifier.addTemporalGrantRule(rule.getTemporalContext(), rule.getName() + "_copy" + i, roleCopy,
                        demarcationCopy, rule.isIsGrant(), rule.getPriority());
            }
        }

        for (SecurityZone securityZone : originalSecurityZones) {
            for (int i = 1; i <= ENTITIES_DUPLICATE_COUNT; i++) {
                modifier.addSecurityZone(securityZone.getName() + "_copy" + i, securityZone.isPublic());
            }
        }

        for (SecurityZone securityZone : originalSecurityZones) {
            for (SecurityZone reachable : securityZone.getReachable()) {
                for (int i = 1; i <= ENTITIES_DUPLICATE_COUNT; i++) {
                    SecurityZone securityZoneCopy = (SecurityZone) resource.getEObject(securityZone.getName() + "_copy" + i);
                    SecurityZone reachableCopy = (SecurityZone) resource.getEObject(reachable.getName() + "_copy" + i);
                    modifier.setReachability(securityZoneCopy, reachableCopy);
                }
            }
        }

        for (TemporalAuthenticationRule rule : originalTemporalAuthenticationRules) {
            for (int i = 1; i <= ENTITIES_DUPLICATE_COUNT; i++) {
                SecurityZone securityZoneCopy = (SecurityZone) resource.getEObject(rule.getSecurityZone().getName() + "_copy" + i);
                modifier.addTemporalAuthenticationRule(rule.getTemporalContext(), rule.getName() + "_copy" + i, securityZoneCopy,
                        rule.getStatus(), rule.getPriority());
            }
        }

        //Add More Scenarios!
        int SCENARIO_DUPLICATE_COUNT = 0;
        List<String> scenarioDates = Arrays.asList("2_January", "3_January", "4_January", "5_January", "6_January");
        for (int i = 0; i < SCENARIO_DUPLICATE_COUNT; i++) {
            TemporalContext tc = modifier.addTemporalContext("TEST_TC_" + i);
            GeneratorUtil.addManyTemporalContextInstances(modifier, tc, scenarioDates.subList(i, i+1), Arrays.asList(new IntegerInterval(0,
                    sortedBounds.get(sortedBounds.size() - 1))));
        }

        System.out.println("Exporting System Topology (to: " + topologyExportFile.getName() + ")");
        Exporter.exportTopology(topologyExportFile, system);

        // CLIContainer.getInstance().getEngine().getMatcher(Scenarios.instance()).getAllMatches().stream().map(m -> m.getScenario()).map( s -> CLIContainer.getInstance().getEngine().getMatcher(DayOfWeekAndMonthAllCombinedScheduleInstance_To_Scenario.instance()).getAllMatches(null, null, null, s)).collect(Collectors.toList())

        LocalDateTime stopTime = LocalDateTime.now();
        System.out.printf("\u0008 Done! (" + (startTime.until(stopTime, ChronoUnit.SECONDS)) + " sec)\n\n\n");

        System.out.println("---- Statistics: ----");
        System.out.println("users:" + system.getAuthorizationPolicy().getUsers().size());
        System.out.println("roles:" + system.getAuthorizationPolicy().getRoles().size());
        System.out.println("demarcations:" + system.getAuthorizationPolicy().getDemarcations().size());
        System.out.println("permissions:" + system.getAuthorizationPolicy().getPermissions().size());
        System.out.println("zones:" + system.getTopology().getSecurityZones().size());
        System.out.println("temporal contexts:" + system.getContextContainer().getTemporalContexts().size());
        System.out.println("temporal grant rules:" + system.getAuthorizationPolicy().getTemporalGrantRules().size());
        System.out.println("temporal authentication rules:" + system.getAuthenticationPolicy().getTemporalAuthenticationRules().size());
        System.out.println("---------------------");

        System.out.println("Known transitions: \n" + transitionToSecurityZones.keySet().stream().sorted().collect(Collectors.toList()).toString());
        System.out.println("Unkown transitions: \n" + unkownTransitions.stream().sorted().collect(Collectors.toList()).toString());
        resource.save(Collections.emptyMap());

        modifier.dispose();
    }

    public static TemporalContext processScheduleRoutineTemporalContext(String name, ScheduleRoutineTemporalContext srtc,
                                                                        PolicyModifier modifier)
            throws ModelManipulationException, InvocationTargetException {
        TemporalContext context = modifier.addTemporalContext(name);
        for(String day: srtc.getIntervals().keySet()){
            for(IntegerInterval interval: srtc.getIntervals().get(day)) {
                modifier.addTemporalContextInstance(context, (ValidDay) modifier.getResource().getEObject(dayCompletermap.get(day)), interval);
            }
        }
        return context;
    }

    private static final Map<String, String> dayCompletermap;
    static {
        Map<String, String> aMap = new LinkedHashMap<>();
        aMap.put("Mon", "Monday");
        aMap.put("Tue", "Tuesday");
        aMap.put("Wed", "Wednesday");
        aMap.put("Thu", "Thursday");
        aMap.put("Fri", "Friday");
        aMap.put("Sat", "Saturday");
        aMap.put("Sun", "Sunday");
        dayCompletermap = Collections.unmodifiableMap(aMap);
    }

}
