package com.vanderhighway.trbac.core.modifier;

import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.google.common.base.Objects;
import com.vanderhighway.trbac.model.trbac.model.*;
import com.vanderhighway.trbac.model.trbac.model.TRBACPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.IModelManipulations;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.SimpleModelManipulations;
import org.eclipse.viatra.transformation.runtime.emf.rules.batch.BatchTransformationRule;
import org.eclipse.viatra.transformation.runtime.emf.rules.batch.BatchTransformationRuleFactory;
import org.eclipse.viatra.transformation.runtime.emf.transformation.batch.BatchTransformation;
import org.eclipse.xtext.xbase.lib.Extension;

import java.lang.reflect.InvocationTargetException;
import java.time.Period;
import java.util.concurrent.Callable;

public class PolicyModifier {

	/**
	 * Transformation-related extensions
	 */

	@Extension
	public IModelManipulations manipulation;

	//@Extension
	//public BatchTransformationRuleFactory batchFactory = new BatchTransformationRuleFactory();

	//@Extension
	//public BatchTransformation transformation;

	@Extension
	private TRBACPackage ePackage = TRBACPackage.eINSTANCE;

	private AdvancedViatraQueryEngine engine;

	private Policy policy;
	private Resource resource;

	public PolicyModifier(final AdvancedViatraQueryEngine engine, Policy policy, Resource resource) {
		this.engine = engine;
		this.manipulation = new SimpleModelManipulations(this.engine);
		this.policy = policy;
		this.resource = resource;
		//this.transformation = BatchTransformation.forEngine(this.engine).build();
	}

	// ---------- Add / Remove Authorization Model Entities ----------

	public User addUser(String name) throws ModelManipulationException {
		User user = (User) manipulation.createChild(policy, ePackage.getPolicy_Users(), ePackage.getUser());
		manipulation.set(user, ePackage.getUser_Name(), name);
		return user;
	}

	public void removeUser(User user) throws ModelManipulationException {
		manipulation.remove(user);
	}

	public Role addRole(String name) throws ModelManipulationException {
		Role role = (Role) manipulation.createChild(policy, ePackage.getPolicy_Roles(), ePackage.getRole());
		manipulation.set(role, ePackage.getRole_Name(), name);
		return role;
	}

	public void removeRole(Role role) throws ModelManipulationException {
		manipulation.remove(role);
	}

	public Demarcation addDemarcation(String name) throws ModelManipulationException {
		Demarcation demarcation = (Demarcation) manipulation.createChild(policy, ePackage.getPolicy_Demarcations(), ePackage.getDemarcation());
		manipulation.set(demarcation, ePackage.getDemarcation_Name(), name);
		return demarcation;
	}

	public void removeDemarcation(Demarcation demarcation) throws ModelManipulationException {
		manipulation.remove(demarcation);
	}

	public Permission addPermission(String name) throws ModelManipulationException {
		Permission permission = (Permission) manipulation.createChild(policy, ePackage.getPolicy_Permissions(), ePackage.getPermission());
		manipulation.set(permission, ePackage.getPermission_Name(), name);
		return permission;
	}

	public void removePermission(Permission permission) throws ModelManipulationException {
		manipulation.remove(permission);
	}

	public TimeRangeGroup addTimeRangeGroup(String name) throws ModelManipulationException {
		Schedule schedule = policy.getSchedule();
		TimeRangeGroup rangeGroup = (TimeRangeGroup) this.manipulation.createChild(schedule, ePackage.getSchedule_TimeRangeGroups(), ePackage.getTimeRangeGroup());
		this.manipulation.set(rangeGroup, ePackage.getTimeRangeGroup_Name(), name);
		return rangeGroup;
	}

	public void removeTimeRangeGroup(TimeRangeGroup group) throws ModelManipulationException {
		manipulation.remove(group);
	}

	public TimeRange addTimeRange(TimeRangeGroup rangeGroup, DaySchedule daySchedule, String rangeName, IntegerInterval interval) throws ModelManipulationException {
		TimeRange timeRange = (TimeRange) this.manipulation.createChild(rangeGroup,
				ePackage.getTimeRangeGroup_TimeRanges(), ePackage.getTimeRange());
		this.manipulation.set(timeRange, ePackage.getTimeRange_Name(), rangeName);
		this.manipulation.set(timeRange, ePackage.getTimeRange_Start(), interval.getStart());
		this.manipulation.set(timeRange, ePackage.getTimeRange_End(), interval.getEnd());
		this.manipulation.set(timeRange, ePackage.getTimeRange_DaySchedule(), daySchedule);
		return timeRange;
	}

	public TimeRange updateTimeRange(TimeRange timeRange, IntegerInterval interval) throws ModelManipulationException {
		this.manipulation.set(timeRange, ePackage.getTimeRange_Start(), interval.getStart());
		this.manipulation.set(timeRange, ePackage.getTimeRange_End(), interval.getEnd());
		return timeRange;
	}

	public void removeTimeRange(TimeRange timeRange) throws ModelManipulationException {
		manipulation.remove(timeRange);
	}

	public DayScheduleTimeRange addDayScheduleTimeRange(DaySchedule daySchedule, String name, IntegerInterval interval) throws ModelManipulationException {
		DayScheduleTimeRange scheduleTimeRange = (DayScheduleTimeRange) manipulation.createChild(daySchedule,
				ePackage.getDaySchedule_DayScheduleTimeRanges(), ePackage.getDayScheduleTimeRange());
		manipulation.set(scheduleTimeRange, ePackage.getDayScheduleTimeRange_Name(), name);
		manipulation.set(scheduleTimeRange, ePackage.getDayScheduleTimeRange_Start(), interval.getStart());
		manipulation.set(scheduleTimeRange, ePackage.getDayScheduleTimeRange_End(), interval.getEnd());
		return scheduleTimeRange;
	}

	public void updateDayScheduleTimeRange(DayScheduleTimeRange timeRange, IntegerInterval interval) throws ModelManipulationException{
		manipulation.set(timeRange, ePackage.getDayScheduleTimeRange_Start(), interval.getStart());
		manipulation.set(timeRange, ePackage.getDayScheduleTimeRange_Start(), interval.getEnd());
	}

	public void removeDayScheduleTimeRange(DayScheduleTimeRange scheduleTimeRange) throws ModelManipulationException {
		manipulation.remove(scheduleTimeRange);
	}

	public DayOfWeekSchedule addDayOfWeekSchedule(String name) throws ModelManipulationException {
		Schedule schedule = policy.getSchedule();
		DayOfWeekSchedule weekdaySchedule = (DayOfWeekSchedule) this.manipulation.createChild(schedule,
				ePackage.getSchedule_DaySchedules(), ePackage.getDayOfWeekSchedule());
		this.manipulation.set(weekdaySchedule, ePackage.getDaySchedule_Name(), name);
		return weekdaySchedule;
	}

	public void removeDayOfWeekSchedule(DayOfWeekSchedule weekdaySchedule) throws ModelManipulationException {
		manipulation.remove(weekdaySchedule);
	}

	public DayOfMonthSchedule addDayOfMonthSchedule(String name) throws ModelManipulationException {
		Schedule schedule = policy.getSchedule();
		DayOfMonthSchedule yeardaySchedule = (DayOfMonthSchedule) this.manipulation.createChild(schedule,
				ePackage.getSchedule_DaySchedules(), ePackage.getDayOfMonthSchedule());
		this.manipulation.set(yeardaySchedule, ePackage.getDaySchedule_Name(), name);
		return yeardaySchedule;
	}

	public void removeDayOfMonthSchedule(DayOfMonthSchedule yeardaySchedule) throws ModelManipulationException {
		manipulation.remove(yeardaySchedule);
	}

	public DayOfYearSchedule addDayOfYearSchedule(DayOfWeekSchedule weekSchedule, DayOfMonthSchedule monthSchedule, String name) throws ModelManipulationException {
		Schedule schedule = policy.getSchedule();
		DayOfYearSchedule yearSchedule = (DayOfYearSchedule) manipulation.createChild(schedule,
				ePackage.getSchedule_DaySchedules(), ePackage.getDayOfYearSchedule());
		this.manipulation.set(yearSchedule, ePackage.getDayOfYearSchedule_DayOfWeekSchedule(), weekSchedule);
		this.manipulation.set(yearSchedule, ePackage.getDayOfYearSchedule_DayOfMonthSchedule(), monthSchedule);
		manipulation.set(yearSchedule, ePackage.getDaySchedule_Name(), name);
		return yearSchedule;
	}


	public void removeDayOfYearSchedule(DayOfYearSchedule schedule) throws ModelManipulationException {
		manipulation.remove(schedule);
	}

	public PeriodicEvent addPeriodicEvent(TimeRangeGroup time, String name, Role role, Demarcation demarcation, boolean enable, int priority) throws ModelManipulationException {
		Schedule schedule = policy.getSchedule();
		PeriodicEvent event = (PeriodicEvent) manipulation.createChild(schedule, ePackage.getSchedule_Periodicevent(), ePackage.getPeriodicEvent());
		manipulation.addTo(event, ePackage.getPeriodicEvent_TimeRangeGroups(), time);
		manipulation.set(event, ePackage.getPeriodicEvent_Name(), name);
		manipulation.set(event, ePackage.getPeriodicEvent_Role(), role);
		manipulation.set(event, ePackage.getPeriodicEvent_Demarcation(), demarcation);
		manipulation.set(event, ePackage.getPeriodicEvent_Enable(), enable);
		manipulation.set(event, ePackage.getPeriodicEvent_Priority(), priority);
		return event;
	}

	public void removePeriodicEvent(PeriodicEvent event) throws ModelManipulationException {
		manipulation.remove(event);
	}

	// -----------------------------------------------


	// ---------- Add / Remove Authorization Model Relations ----------

	public void assignRoleToUser(User user, Role role) throws ModelManipulationException {
		manipulation.addTo(user, ePackage.getUser_UR(), role);
	}

	public void deassignRoleFromUser(User user, Role role) throws ModelManipulationException {
		manipulation.remove(user, ePackage.getUser_UR(), role);
	}

	public void assignDemarcationToRole(Role role, Demarcation demarcation) throws ModelManipulationException {
		manipulation.addTo(role, ePackage.getRole_RD(), demarcation);
	}

	public void deassignDemarcationFromRole(Role role, Demarcation demarcation) throws ModelManipulationException {
		manipulation.remove(role, ePackage.getRole_RD(), demarcation);
	}

	public void assignPermissionToDemarcation(Demarcation demarcation, Permission permission) throws ModelManipulationException {
		manipulation.addTo(demarcation, ePackage.getDemarcation_DP(), permission);
	}

	public void deassignPermissionFromDemarcation(Demarcation demarcation, Permission permission) throws ModelManipulationException {
		manipulation.remove(demarcation, ePackage.getDemarcation_DP(), permission);
	}

	public void addRoleInheritance(Role juniorRole, Role seniorRole) throws ModelManipulationException {
		manipulation.addTo(juniorRole, ePackage.getRole_Seniors(), seniorRole);
	}

	public void removeRoleInheritance(Role juniorRole, Role seniorRole) throws ModelManipulationException {
		manipulation.remove(juniorRole, ePackage.getRole_Seniors(), seniorRole);
	}

	public void addDemarcationInheritance(Demarcation subdemarcation, Demarcation supdemarcation) throws ModelManipulationException {
		manipulation.addTo(subdemarcation, ePackage.getDemarcation_Superdemarcations(), supdemarcation);
	}

	public void removeDemarcationInheritance(Demarcation subdemarcation, Demarcation supdemarcation) throws ModelManipulationException {
		manipulation.remove(subdemarcation, ePackage.getDemarcation_Superdemarcations(), supdemarcation);
	}

	// -------------------------------------------

	public IModelManipulations getManipulation() {
		return manipulation;
	}

	public void setManipulation(IModelManipulations manipulation) {
		this.manipulation = manipulation;
	}

//	public void dispose() {
//		if (!Objects.equal(this.transformation, null)) {
//			this.transformation.dispose();
//		}
//		this.transformation = null;
//		return;
//	}


}
