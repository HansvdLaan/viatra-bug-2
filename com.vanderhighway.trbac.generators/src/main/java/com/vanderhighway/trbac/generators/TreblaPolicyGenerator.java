//
//package com.vanderhighway.trbac.generators;
//
//import com.brein.time.timeintervals.intervals.IntegerInterval;
//import com.vanderhighway.trbac.core.modifier.PolicyModifier;
//import com.vanderhighway.trbac.model.trbac.model.*;
//import org.eclipse.emf.common.util.URI;
//import org.eclipse.emf.ecore.resource.Resource;
//import org.eclipse.emf.ecore.resource.ResourceSet;
//import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
//import org.eclipse.emf.ecore.util.EcoreUtil;
//import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
//import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
//import org.eclipse.viatra.query.runtime.emf.EMFScope;
//import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
//import org.eclipse.xtext.xbase.lib.Extension;
//
//import java.io.File;
//import java.io.IOException;
//import java.lang.reflect.InvocationTargetException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.util.*;
//
//public class TreblaPolicyGenerator {
//
//	@Extension
//	private static TRBACPackage ePackage = TRBACPackage.eINSTANCE;
//
//	public static void main(String[] args) throws IOException, InvocationTargetException, ModelManipulationException, ModelManipulationException, ParseException {
//
//		String fileSeparator = System.getProperty("file.separator");
//
//		System.out.println("Trebla Policy Generator Called!");
//		System.out.print("Initialize model scope and preparing engine... ");
//
//		// Initializing the EMF package
//		TRBACPackage.eINSTANCE.getName();
//		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("trbac", new XMIResourceFactoryImpl());
//		Resource.Factory.Registry.INSTANCE.getContentTypeToFactoryMap().put("*", new XMIResourceFactoryImpl());
//
//		ResourceSet set = new ResourceSetImpl();
//
//		String relativePath = "."+fileSeparator+"empty_policy_trebla.trbac";
//		File file = new File(relativePath);
//		if(file.createNewFile()){
//			System.out.println(relativePath+" File Created in Project root directory");
//		}
//		else System.out.println("File "+relativePath+" already exists in the project root directory");
//		URI uri = URI.createFileURI("./empty_policy_trebla.trbac");
//		Resource resource = set.createResource(uri);
//
//		resource.getContents().add(EcoreUtil.create(ePackage.getSecurityPolicy()));
//		SecurityPolicy securityPolicy = ((SecurityPolicy) resource.getContents().get(0));
//		securityPolicy.setName("DummySecurityPolicy");
//
//		resource.getContents().add(EcoreUtil.create(ePackage.getAuthorizationPolicy()));
//		AuthorizationPolicy authorizationPolicy = ((AuthorizationPolicy) resource.getContents().get(0));
//		authorizationPolicy.setName("DummyAuthorizationPolicy");
//		securityPolicy.setAuthorizationPolicy(authorizationPolicy);
//
//		Schedule schedule = (Schedule) EcoreUtil.create(ePackage.getSchedule());
//		schedule.setName("DummySchedule");
//		authorizationPolicy.setSchedule(schedule);
//
//		final AdvancedViatraQueryEngine engine = AdvancedViatraQueryEngine.createUnmanagedEngine(new EMFScope(set));
//		PolicyModifier modifier = new PolicyModifier(engine, (SecurityPolicy) resource.getContents().get(0), resource);
//
//		TimeRangeGroup always = modifier.addTimeRangeGroup("Always");
//		TimeRangeGroup guardShifts = modifier.addTimeRangeGroup("GuardShifts");
//		TimeRangeGroup workingHours = modifier.addTimeRangeGroup("WorkingHours");
//		TimeRangeGroup visitingHours = modifier.addTimeRangeGroup("VisitingHours");
//		TimeRangeGroup lunchBreaks = modifier.addTimeRangeGroup("LunchBreaks");
//		TimeRangeGroup overtime = modifier.addTimeRangeGroup("Overtime");
//		TimeRangeGroup cleaningShifts = modifier.addTimeRangeGroup("CleaningShifts");
//
//		Map<String, DayOfWeekSchedule> dayOfWeekScheduleMap = new HashMap<>();
//		Map<String, Map<Integer, DayOfMonthSchedule>> dayOfMonthScheduleMap = new HashMap();
//
//		List<String> allDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
//		List<String> weekDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
//		List<String> weekEnd = Arrays.asList("Saturday", "Sunday");
//
//		for (String day: weekDays) {
//			DayOfWeekSchedule dayOfWeekSchedule = modifier.addDayOfWeekSchedule(day);
//			TimeRange alwaysRange = modifier.addTimeRange(always, dayOfWeekSchedule, "Always_" + day, new IntegerInterval(0,1439));
//			TimeRange guardShift1Range = modifier.addTimeRange(guardShifts, dayOfWeekSchedule, "GuardShift1_" + day, new IntegerInterval(0,449));
//			TimeRange guardShift2Range = modifier.addTimeRange(guardShifts, dayOfWeekSchedule, "GuardShift2_" + day, new IntegerInterval(1170,1439));
//			TimeRange workingHoursRange = modifier.addTimeRange(workingHours, dayOfWeekSchedule, "WorkingHours_" + day, new IntegerInterval(480,1019));
//			TimeRange visitingHoursRange = modifier.addTimeRange(visitingHours, dayOfWeekSchedule, "VisitingHours_" + day, new IntegerInterval(600,959));
//			TimeRange overtimeRange = modifier.addTimeRange(overtime, dayOfWeekSchedule, "Overtime_" + day, new IntegerInterval(1020,1169));
//			TimeRange lunchBreakRange = modifier.addTimeRange(lunchBreaks, dayOfWeekSchedule, "LunchBreak_" + day, new IntegerInterval(720,779));
//
//			DayScheduleTimeRange srAlways = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR_ALWAYS" + day, new IntegerInterval(0, 1439));
////			DayScheduleTimeRange sr1 = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR1_" + day, new IntegerInterval(0,449));
////			DayScheduleTimeRange sr2 = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR2_" + day, new IntegerInterval(450,479));
////			DayScheduleTimeRange sr3 = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR3_" + day, new IntegerInterval(480,599));
////			DayScheduleTimeRange sr4 = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR4_" + day, new IntegerInterval(600,719));
////			DayScheduleTimeRange sr5 = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR5_" + day, new IntegerInterval(720,779));
////			DayScheduleTimeRange sr6 = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR6_" + day, new IntegerInterval(780,959));
////			DayScheduleTimeRange sr7 = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR7_" + day, new IntegerInterval(960,1019));
////			DayScheduleTimeRange sr8 = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR8_" + day, new IntegerInterval(1020,1169));
////			DayScheduleTimeRange sr9 = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR9_" + day, new IntegerInterval(1070,1439));
////
//
//			modifier.getManipulation().addAllTo(alwaysRange, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(srAlways));
//
////			modifier.getManipulation().addAllTo(alwaysRange, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(sr1,sr2,sr3,sr4,
////					sr5,sr6,sr7,sr8,sr9));
////			modifier.getManipulation().addAllTo(guardShift1Range, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(sr1));
////			modifier.getManipulation().addAllTo(guardShift2Range, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(sr9));
////			modifier.getManipulation().addAllTo(workingHoursRange, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(sr3,sr4,sr5,sr6,sr7));
////			modifier.getManipulation().addAllTo(visitingHoursRange, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(sr4,sr5,sr6));
////			modifier.getManipulation().addAllTo(overtimeRange, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(sr8));
////			modifier.getManipulation().addAllTo(lunchBreakRange, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(sr5));
//
//			if(day.equals("Tuesday") || day.equals("Friday")) {
//				TimeRange cleaningShiftRange = modifier.addTimeRange(cleaningShifts, dayOfWeekSchedule, "CleaningShift_" + day, new IntegerInterval(1020,1169));
//				//modifier.getManipulation().addAllTo(cleaningShiftRange, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(sr9));
//			}
//
//			dayOfWeekScheduleMap.put(day, dayOfWeekSchedule);
//		}
//
//		for (String day: weekEnd) {
//			DayOfWeekSchedule dayOfWeekSchedule = modifier.addDayOfWeekSchedule(day);
//			TimeRange alwaysRange = modifier.addTimeRange(always, dayOfWeekSchedule, "Always_" + day, new IntegerInterval(0,1439));
//			TimeRange guardShift1Range = modifier.addTimeRange(guardShifts, dayOfWeekSchedule, "GuardShift1_" + day, new IntegerInterval(0,449));
//			TimeRange guardShift2Range = modifier.addTimeRange(guardShifts, dayOfWeekSchedule, "GuardShift2_" + day, new IntegerInterval(1170,1439));
//
//			DayScheduleTimeRange srAlways = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR_ALWAYS" + day, new IntegerInterval(0, 1439));
//
//			//DayScheduleTimeRange sr1 = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR1_" + day, new IntegerInterval(0,449));
//			//DayScheduleTimeRange sr2 = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR2_" + day, new IntegerInterval(450,1169));
//			//DayScheduleTimeRange sr3 = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "SR7_" + day, new IntegerInterval(1070,1439));
//
//			modifier.getManipulation().addAllTo(alwaysRange, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(srAlways));
//
//			//modifier.getManipulation().addAllTo(alwaysRange, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(sr1,sr2,sr3));
//			//modifier.getManipulation().addAllTo(guardShift1Range, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(sr1));
//			//modifier.getManipulation().addAllTo(guardShift2Range, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(sr3));
//
//			dayOfWeekScheduleMap.put(day, dayOfWeekSchedule);
//		}
//
//		TimeRangeGroup workshops = modifier.addTimeRangeGroup("HolidayWorkshops");
//
//
//		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
//				"September", "October", "November", "December");
//		List<Integer> monthDays = Arrays.asList(31,29,31,30,31,30,31,31,30,31,30,31);
//		for (int monthIndex = 0; monthIndex < months.size(); monthIndex++) {
//			dayOfMonthScheduleMap.put(months.get(monthIndex), new HashMap<>());
//			for (int dayIndex = 0; dayIndex < monthDays.get(monthIndex); dayIndex++) {
//
//				String monthDay = (dayIndex+1) + "_" + months.get(monthIndex);
//				DayOfMonthSchedule dayOfMonthSchedule = modifier.addDayOfMonthSchedule(monthDay);
//				TimeRange alwaysRange = modifier.addTimeRange(always, dayOfMonthSchedule, "Always_" + monthDay,
//						new IntegerInterval(0, 1439));
//
////				if((monthIndex == 11 && dayIndex == 4) || (monthIndex == 11 && dayIndex == 30)) { // 5 december or 31 december
////					TimeRange workshopRange = modifier.addTimeRange(workshops, dayOfMonthSchedule, "Workshop" + monthDay, new IntegerInterval(480,1019));
////					DayScheduleTimeRange sr1 = modifier.addDayScheduleTimeRange(dayOfMonthSchedule, "" +
////							"SR1_" + monthDay, new IntegerInterval(0, 479));
////					DayScheduleTimeRange sr2 = modifier.addDayScheduleTimeRange(dayOfMonthSchedule, "" +
////							"SR2_" + monthDay, new IntegerInterval(480,1019));
////					DayScheduleTimeRange sr3 = modifier.addDayScheduleTimeRange(dayOfMonthSchedule, "" +
////							"SR3_" + monthDay, new IntegerInterval(1020, 1439));
////					modifier.getManipulation().addAllTo(alwaysRange, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(sr1,sr2,sr3));
////					modifier.getManipulation().addAllTo(workshopRange, ePackage.getTimeRange_DayScheduleTimeRanges(), Arrays.asList(sr2));
////				}
////				else {
//					DayScheduleTimeRange alwaysScheduleRange = modifier.addDayScheduleTimeRange(dayOfMonthSchedule, "" +
//							"Always_" + monthDay + "_DSTR", new IntegerInterval(0, 1439));
//					modifier.getManipulation().addTo(alwaysRange, ePackage.getTimeRange_DayScheduleTimeRanges(), alwaysScheduleRange);
////				}
//
//				dayOfMonthScheduleMap.get(months.get(monthIndex)).put(dayIndex, dayOfMonthSchedule);
//			}
//		}
//
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//		Date startDate = formatter.parse("2020-01-01");
//		Date endDate = formatter.parse("2030-01-01");
//
//		LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//		LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//
//		for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
//			// Do your job here with `date`.
//
//			DayOfWeekSchedule weekSchedule = dayOfWeekScheduleMap.get(allDays.get(date.getDayOfWeek().getValue()-1));
//			DayOfMonthSchedule monthSchedule = dayOfMonthScheduleMap.get(months.get(date.getMonthValue() - 1)).get(date.getDayOfMonth() - 1);
//
//			String name = weekSchedule.getName() + "_" + monthSchedule.getName() + "_" + date.getYear();
//			DayOfYearSchedule yearSchedule = modifier.addDayOfYearSchedule(weekSchedule, monthSchedule, name);
//
//			TimeRange alwaysRange = modifier.addTimeRange(always, yearSchedule, "Always_" + name,
//					new IntegerInterval(0, 1439));
//
//			DayScheduleTimeRange alwaysScheduleRange = modifier.addDayScheduleTimeRange(yearSchedule, "" +
//					"Always_" + name + "_DSTR", new IntegerInterval(0,1439));
//
//			modifier.getManipulation().addTo(alwaysRange, ePackage.getTimeRange_DayScheduleTimeRanges(), alwaysScheduleRange);
//		}
//
//		// Add Users
//		User userAmy = modifier.addUser("Amy");
//		User userBart = modifier.addUser("Bart");
//		User userChristine = modifier.addUser("Christine");
//		User userDave = modifier.addUser("Dave");
//		User userEmily = modifier.addUser("Emily");
//		User userFrank = modifier.addUser("Frank");
//		User userGrace = modifier.addUser("Grace");
//		User userHank = modifier.addUser("Hank");
//		User userIsaac = modifier.addUser("Isaac");
//		User userJulia = modifier.addUser("Julia");
//
//		// Add Roles
//		Role roleVisitor = modifier.addRole("Visitor");
//		Role roleEmployee = modifier.addRole("Employee");
//		Role roleContractor = modifier.addRole("Contractor");
//		Role roleGuard = modifier.addRole("Guard");
//		Role roleCleaner = modifier.addRole("Cleaner");
//		Role roleOperations = modifier.addRole("Operations");
//		Role roleOperationsManager = modifier.addRole("OperationsManager");
//		Role roleDesigner = modifier.addRole("Designer");
//		Role roleHeadDesigner = modifier.addRole("HeadDesigner");
//		Role roleDirector = modifier.addRole("Director");
//
//		// Add Demarcations
//		Demarcation demarcationWorking = modifier.addDemarcation("Working");
//		Demarcation demarcationDesigning = modifier.addDemarcation("Designing");
//		Demarcation demarcationGuarding = modifier.addDemarcation("Guarding");
//		Demarcation demarcationCleaning = modifier.addDemarcation("Cleaning");
//		Demarcation demarcationVisiting = modifier.addDemarcation("Visiting");
//		Demarcation demarcationManagingFinances = modifier.addDemarcation("ManagingFinances");
//		Demarcation demarcationManagingPeople = modifier.addDemarcation("ManagingPeople");
//		Demarcation demarcationManagingStock = modifier.addDemarcation("ManagingStock");
//		Demarcation demarcationManaging = modifier.addDemarcation("Managing");
//		Demarcation demarcationTakingABreak = modifier.addDemarcation("TakingABreak");
//
//		// Add Permissions
//		Permission permissionLobby = modifier.addPermission("Lobby");
//		Permission permissionWorkshop = modifier.addPermission("Workshop");
//		Permission permissionOpenOffice = modifier.addPermission("OpenOffice");
//		Permission permissionRestaurant = modifier.addPermission("Restaurant");
//		Permission permissionStorage = modifier.addPermission("Storage");
//		Permission permissionMeetingRoom1 = modifier.addPermission("MeetingRoom1");
//		Permission permissionMeetingRoom2 = modifier.addPermission("MeetingRoom2");
//		Permission permissionOffice1 = modifier.addPermission("Office1");
//		Permission permissionOffice2 = modifier.addPermission("Office2");
//		Permission permissionVault = modifier.addPermission("Vault");
//		Permission permissionToilets = modifier.addPermission("Toilets");
//
//		// Add Role Hierarchies
//		modifier.addRoleInheritance(roleContractor, roleCleaner);
//		modifier.addRoleInheritance(roleContractor, roleGuard);
//		modifier.addRoleInheritance(roleEmployee, roleDesigner);
//		modifier.addRoleInheritance(roleEmployee, roleOperations);
//		modifier.addRoleInheritance(roleDesigner, roleHeadDesigner);
//		modifier.addRoleInheritance(roleOperations, roleOperationsManager);
//		modifier.addRoleInheritance(roleHeadDesigner, roleDirector);
//		modifier.addRoleInheritance(roleOperationsManager, roleDirector);
//
//		// Add Demarcation Hierarchies
//		modifier.addDemarcationInheritance(demarcationWorking, demarcationDesigning);
//		modifier.addDemarcationInheritance(demarcationWorking, demarcationManagingFinances);
//		modifier.addDemarcationInheritance(demarcationWorking, demarcationManagingPeople);
//		modifier.addDemarcationInheritance(demarcationWorking, demarcationManagingStock);
//		modifier.addDemarcationInheritance(demarcationWorking, demarcationGuarding);
//		modifier.addDemarcationInheritance(demarcationWorking, demarcationCleaning);
//
//		// Add Relations
//		// Add User-Role relations
//		modifier.assignRoleToUser(userAmy, roleDirector);
//		modifier.assignRoleToUser(userBart, roleHeadDesigner);
//		modifier.assignRoleToUser(userChristine, roleOperationsManager);
//		modifier.assignRoleToUser(userDave, roleDesigner);
//		modifier.assignRoleToUser(userEmily, roleDesigner);
//		modifier.assignRoleToUser(userFrank, roleOperations);
//		modifier.assignRoleToUser(userGrace, roleGuard);
//		modifier.assignRoleToUser(userHank, roleGuard);
//		modifier.assignRoleToUser(userIsaac, roleCleaner);
//		modifier.assignRoleToUser(userJulia, roleVisitor);
//
//		// Add Role-Demarcation relation
//		modifier.assignDemarcationToRole(roleEmployee, demarcationWorking);
//		modifier.assignDemarcationToRole(roleDesigner, demarcationDesigning);
//		modifier.assignDemarcationToRole(roleHeadDesigner, demarcationManagingPeople);
//		modifier.assignDemarcationToRole(roleHeadDesigner, demarcationManagingStock);
//		modifier.assignDemarcationToRole(roleOperationsManager, demarcationManagingPeople);
//		modifier.assignDemarcationToRole(roleOperationsManager, demarcationManagingStock);
//		modifier.assignDemarcationToRole(roleDirector, demarcationManaging);
//		modifier.assignDemarcationToRole(roleGuard, demarcationGuarding);
//		modifier.assignDemarcationToRole(roleCleaner, demarcationCleaning);
//		modifier.assignDemarcationToRole(roleVisitor, demarcationVisiting);
//		modifier.assignDemarcationToRole(roleVisitor, demarcationTakingABreak);
//
//		// Add Demarcation-Permission relation
//		Arrays.asList(permissionLobby, permissionOpenOffice, permissionRestaurant, permissionMeetingRoom1, permissionMeetingRoom2,
//				permissionToilets).forEach(p -> {
//					try {
//						modifier.assignPermissionToDemarcation(demarcationWorking, p);
//					} catch (ModelManipulationException e) {
//						e.printStackTrace();
//					}
//				}
//		);
//		modifier.assignPermissionToDemarcation(demarcationDesigning, permissionWorkshop);
//		modifier.assignPermissionToDemarcation(demarcationDesigning, permissionStorage);
//		Arrays.asList(permissionLobby,permissionOpenOffice,permissionRestaurant,permissionStorage,permissionMeetingRoom1,
//				permissionMeetingRoom2,permissionOffice1,permissionOffice2,permissionToilets).forEach(p -> {
//					try {
//						modifier.assignPermissionToDemarcation(demarcationManagingPeople, p);
//					} catch (ModelManipulationException e) {
//						e.printStackTrace();
//					}
//				}
//		);
//		modifier.assignPermissionToDemarcation(demarcationManagingFinances, permissionVault);
//		modifier.assignPermissionToDemarcation(demarcationManagingStock, permissionStorage);
//
//		modifier.assignPermissionToDemarcation(demarcationGuarding, permissionOffice1);
//		modifier.assignPermissionToDemarcation(demarcationGuarding, permissionOffice2);
//		modifier.assignPermissionToDemarcation(demarcationGuarding, permissionStorage);
//		modifier.assignPermissionToDemarcation(demarcationGuarding, permissionWorkshop);
//
//		modifier.assignPermissionToDemarcation(demarcationGuarding, permissionOffice1);
//		modifier.assignPermissionToDemarcation(demarcationGuarding, permissionOffice2);
//		modifier.assignPermissionToDemarcation(demarcationGuarding, permissionWorkshop);
//
//		modifier.assignPermissionToDemarcation(demarcationVisiting, permissionToilets);
//		modifier.assignPermissionToDemarcation(demarcationVisiting, permissionLobby);
//		modifier.assignPermissionToDemarcation(demarcationVisiting, permissionWorkshop);
//
//		modifier.assignPermissionToDemarcation(demarcationTakingABreak, permissionRestaurant);
//
//
//		modifier.addTemporalGrantRule(workingHours, "PE1", roleEmployee, demarcationWorking, true, 3);;
//		modifier.addTemporalGrantRule(workingHours, "PE2", roleDesigner, demarcationDesigning, true, 3);
//		modifier.addTemporalGrantRule(workingHours, "PE3", roleHeadDesigner, demarcationManagingPeople, true, 3);
//		modifier.addTemporalGrantRule(workingHours, "PE4", roleHeadDesigner, demarcationManagingStock, true, 3);
//		modifier.addTemporalGrantRule(workingHours, "PE5", roleOperationsManager, demarcationManagingPeople, true, 3);
//		modifier.addTemporalGrantRule(workingHours, "PE6", roleOperationsManager, demarcationManagingStock, true, 3);
//		modifier.addTemporalGrantRule(workingHours, "PE7", roleDirector, demarcationManaging, true, 3);
//		modifier.addTemporalGrantRule(guardShifts, "PE8", roleGuard, demarcationGuarding, true, 3);
//		modifier.addTemporalGrantRule(cleaningShifts, "PE9", roleCleaner, demarcationCleaning, true, 3);
//		modifier.addTemporalGrantRule(visitingHours, "PE10", roleVisitor, demarcationVisiting, true, 3);
//		modifier.addTemporalGrantRule(lunchBreaks, "PE11", roleVisitor, demarcationTakingABreak, true, 3);
//
//		// Add Trebla Building
//		Building building = modifier.addBuilding("TreblaHQ");
//
//
//		resource.save(Collections.emptyMap());
//
//		modifier.dispose();
//
//		System.out.println("Done!");
//	}
//}
