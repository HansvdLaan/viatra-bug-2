
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class VerySimplePolicyGenerator {

	@Extension
	private static TRBACPackage ePackage = TRBACPackage.eINSTANCE;

	public static void main(String[] args) throws IOException, InvocationTargetException, ModelManipulationException, ModelManipulationException, ParseException {

		String fileSeparator = System.getProperty("file.separator");

		System.out.println("Trebla Policy Generator Called!");
		System.out.print("Initialize model scope and preparing engine... ");

		// Initializing the EMF package
		TRBACPackage.eINSTANCE.getName();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("trbac", new XMIResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getContentTypeToFactoryMap().put("*", new XMIResourceFactoryImpl());

		ResourceSet set = new ResourceSetImpl();

		String relativePath = "."+fileSeparator+"simple_company.trbac";
		File file = new File(relativePath);
		if(file.createNewFile()){
			System.out.println(relativePath+" File Created in Project root directory");
		}
		else System.out.println("File "+relativePath+" already exists in the project root directory");
		URI uri = URI.createFileURI("./simple_company.trbac");
		Resource resource = set.createResource(uri);

		resource.getContents().add(EcoreUtil.create(ePackage.getSecurityPolicy()));
		SecurityPolicy securityPolicy = ((SecurityPolicy) resource.getContents().get(0));
		securityPolicy.setName("DummySecurityPolicy");

		AuthorizationPolicy authorizationPolicy = (AuthorizationPolicy) EcoreUtil.create(ePackage.getAuthorizationPolicy());
		resource.getContents().add(authorizationPolicy);
		authorizationPolicy.setName("DummyAuthorizationPolicy");
		securityPolicy.setAuthorizationPolicy(authorizationPolicy);

		Schedule schedule = (Schedule) EcoreUtil.create(ePackage.getSchedule());
		schedule.setName("DummySchedule");
		authorizationPolicy.setSchedule(schedule);

		final AdvancedViatraQueryEngine engine = AdvancedViatraQueryEngine.createUnmanagedEngine(new EMFScope(set));
		PolicyModifier modifier = new PolicyModifier(engine, (SecurityPolicy) resource.getContents().get(0), resource);

		TemporalContext always = modifier.addTemporalContext("Always");
		TemporalContext workingHours = modifier.addTemporalContext("WorkingHours");
		TemporalContext lunchBreaks = modifier.addTemporalContext("LunchBreaks");
		TemporalContext holidays = modifier.addTemporalContext("Holidays");

		Map<String, DayOfWeekSchedule> dayOfWeekScheduleMap = new HashMap<>();
		Map<String, Map<Integer, DayOfMonthSchedule>> dayOfMonthScheduleMap = new HashMap();

		List<String> allDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
		List<String> weekDays = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
		List<String> weekEnd = Arrays.asList("Saturday", "Sunday");

		for (String day: weekDays) {
			DayOfWeekSchedule dayOfWeekSchedule = modifier.addDayOfWeekSchedule(day);
			TimeRange alwaysRange = modifier.addTemporalContextInstance(always, dayOfWeekSchedule, "Always_" + day, new IntegerInterval(0,1439));
			TimeRange workingHoursRange = modifier.addTemporalContextInstance(workingHours, dayOfWeekSchedule, "WorkingHours_" + day, new IntegerInterval(480,1019));
			TimeRange lunchBreakRange = modifier.addTemporalContextInstance(lunchBreaks, dayOfWeekSchedule, "LunchBreak_" + day, new IntegerInterval(720,779));

			DayScheduleTimeRange alwaysScheduleRange = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "" +
					"Always_" + day + "_DSTR", new IntegerInterval(0,1439));
			modifier.getManipulation().addTo(alwaysRange, ePackage.getTimeRange_DayScheduleTimeRanges(), alwaysScheduleRange);

			dayOfWeekScheduleMap.put(day, dayOfWeekSchedule);
		}

		for (String day: weekEnd) {
			DayOfWeekSchedule dayOfWeekSchedule = modifier.addDayOfWeekSchedule(day);
			TimeRange alwaysRange = modifier.addTemporalContextInstance(always, dayOfWeekSchedule, "Always_" + day, new IntegerInterval(0,1439));

			DayScheduleTimeRange alwaysScheduleRange = modifier.addDayScheduleTimeRange(dayOfWeekSchedule, "" +
					"Always_" + day + "_DSTR", new IntegerInterval(0,1439));
			modifier.getManipulation().addTo(alwaysRange, ePackage.getTimeRange_DayScheduleTimeRanges(), alwaysScheduleRange);

			dayOfWeekScheduleMap.put(day, dayOfWeekSchedule);
		}


		List<String> months = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");
		List<Integer> monthDays = Arrays.asList(31,29,31,30,31,30,31,31,30,31,30,31);
		for (int monthIndex = 0; monthIndex < months.size(); monthIndex++) {
			dayOfMonthScheduleMap.put(months.get(monthIndex), new HashMap<>());
			for (int dayIndex = 0; dayIndex < monthDays.get(monthIndex); dayIndex++) {

				String monthDay = (dayIndex+1) + "_" + months.get(monthIndex);
				DayOfMonthSchedule dayOfMonthSchedule = modifier.addDayOfMonthSchedule(monthDay);
				TimeRange alwaysRange = modifier.addTemporalContextInstance(always, dayOfMonthSchedule, "Always_" + monthDay,
						new IntegerInterval(0, 1439));

				DayScheduleTimeRange alwaysScheduleRange = modifier.addDayScheduleTimeRange(dayOfMonthSchedule, "" +
						"Always_" + monthDay + "_DSTR", new IntegerInterval(0,1439));
				modifier.getManipulation().addTo(alwaysRange, ePackage.getTimeRange_DayScheduleTimeRanges(), alwaysScheduleRange);

				if((monthIndex == 11 && dayIndex == 24) ) { // 25 december
					TimeRange holidayRange = modifier.addTemporalContextInstance(holidays, dayOfMonthSchedule, "Holiday" + monthDay, new IntegerInterval(480,1019));
				}
				else {
				}
				dayOfMonthScheduleMap.get(months.get(monthIndex)).put(dayIndex, dayOfMonthSchedule);
			}
		}

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = formatter.parse("2020-01-01");
		Date endDate = formatter.parse("2030-01-01");

		LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
			DayOfWeekSchedule weekSchedule = dayOfWeekScheduleMap.get(allDays.get(date.getDayOfWeek().getValue()-1));
			DayOfMonthSchedule monthSchedule = dayOfMonthScheduleMap.get(months.get(date.getMonthValue() - 1)).get(date.getDayOfMonth() - 1);

			String name = weekSchedule.getName() + "_" + monthSchedule.getName() + "_" + date.getYear();
			DayOfYearSchedule yearSchedule = modifier.addDayOfYearSchedule(weekSchedule, monthSchedule, name);

			TimeRange alwaysRange = modifier.addTemporalContextInstance(always, yearSchedule, "Always_" + name,
					new IntegerInterval(0, 1439));

			DayScheduleTimeRange alwaysScheduleRange = modifier.addDayScheduleTimeRange(yearSchedule, "" +
					"Always_" + name + "_DSTR", new IntegerInterval(0,1439));
			modifier.getManipulation().addTo(alwaysRange, ePackage.getTimeRange_DayScheduleTimeRanges(), alwaysScheduleRange);
		}

		// Add Users
		User user1 = modifier.addUser("User1");
		User user2 = modifier.addUser("User2");
		User user3 = modifier.addUser("User3");
		User user4 = modifier.addUser("User4");

		// Add Roles
		Role roleEmployee = modifier.addRole("Employee");
		Role roleManager = modifier.addRole("Manager");

		// Add Demarcations
		Demarcation demOffice = modifier.addDemarcation("Office");
		Demarcation demCanteen = modifier.addDemarcation("Canteen");

		// Add Permissions
		Permission permOpenOffice = modifier.addPermission("OpenOffice");
		Permission permBreakRoom = modifier.addPermission("BreakRoom");
		Permission permKitchen = modifier.addPermission("Kitchen");

		// Add Relations
		// Add User-Role relations
		modifier.assignRoleToUser(user1, roleEmployee);
		modifier.assignRoleToUser(user2, roleEmployee);
		modifier.assignRoleToUser(user2, roleManager);

		// Add Role-Demarcation relation
		modifier.assignDemarcationToRole(roleEmployee, demOffice);
		modifier.assignDemarcationToRole(roleManager, demCanteen);

		modifier.assignPermissionToDemarcation(demOffice, permOpenOffice);
		modifier.assignPermissionToDemarcation(demOffice, permBreakRoom);
		modifier.assignPermissionToDemarcation(demCanteen, permBreakRoom);
		modifier.assignPermissionToDemarcation(demCanteen, permKitchen);

		modifier.addTemporalGrantRule(workingHours, "R1", roleEmployee, demOffice, true, 3);;
		modifier.addTemporalGrantRule(lunchBreaks, "R2", roleEmployee, demCanteen, true, 3);
		modifier.addTemporalGrantRule(holidays, "R3", roleEmployee, demOffice, false, 4);
		modifier.addTemporalGrantRule(holidays, "R4", roleEmployee, demCanteen, false, 4);

		modifier.addTemporalGrantRule(workingHours, "R5", roleManager, demOffice, true, 3);;
		modifier.addTemporalGrantRule(lunchBreaks, "R6", roleManager, demCanteen, true, 3);
		modifier.addTemporalGrantRule(holidays, "R7", roleEmployee, demOffice, false, 4);

		// Add Trebla Building
		Building building = modifier.addBuilding("TreblaHQ");
		SecurityZone szLobby = modifier.addSecurityZone(building, "ZoneLobby", true);
		SecurityZone szOpenOffice = modifier.addSecurityZone(building, "ZoneOpenOffice", false);
		SecurityZone szBreakRoom = modifier.addSecurityZone(building, "ZoneBreakRoom", false);
		SecurityZone szKitchen = modifier.addSecurityZone(building, "ZoneKitchen", false);

		modifier.assignObjectToPermission(permOpenOffice, szOpenOffice);
		modifier.assignObjectToPermission(permBreakRoom, szBreakRoom);
		modifier.assignObjectToPermission(permKitchen, szKitchen);

		modifier.setBidirectionalReachability(szLobby, szOpenOffice);
		modifier.setBidirectionalReachability(szOpenOffice, szBreakRoom);
		modifier.setBidirectionalReachability(szBreakRoom, szKitchen);

		resource.save(Collections.emptyMap());

		modifier.dispose();

		System.out.println("Done!");
	}
}
