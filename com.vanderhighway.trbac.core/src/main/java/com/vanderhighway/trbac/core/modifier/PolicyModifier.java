package com.vanderhighway.trbac.core.modifier;

import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.google.common.base.Objects;
import com.vanderhighway.trbac.model.trbac.model.*;
import com.vanderhighway.trbac.model.trbac.model.Object;
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
import java.util.concurrent.Callable;

public class PolicyModifier {

	/**
	 * Transformation-related extensions
	 */

	@Extension
	public IModelManipulations manipulation;

	@Extension
	private BatchTransformationRuleFactory batchFactory = new BatchTransformationRuleFactory();

	@Extension
	private BatchTransformation transformation;

	@Extension
	private TRBACPackage ePackage = TRBACPackage.eINSTANCE;

	private AdvancedViatraQueryEngine engine;

	private SecurityPolicy securityPolicy;
	private Resource resource;

	public PolicyModifier(final AdvancedViatraQueryEngine engine, SecurityPolicy securityPolicy, Resource resource) {
		this.engine = engine;
		this.manipulation = new SimpleModelManipulations(this.engine);
		this.securityPolicy = securityPolicy;
		this.resource = resource;
		//this.transformation = BatchTransformation.forEngine(this.engine).build();
	}

	// ---------- Add / Remove Authorization Model Entities ----------

	public User addUser(String name) throws ModelManipulationException {
		User user = (User) manipulation.createChild(securityPolicy.getAuthorizationPolicy(),
				ePackage.getAuthorizationPolicy_Users(), ePackage.getUser());
		manipulation.set(user, ePackage.getUser_Name(), name);
		return user;
	}

	public void removeUser(User user) throws ModelManipulationException {
		manipulation.remove(user);
	}

	public Role addRole(String name) throws ModelManipulationException {
		Role role = (Role) manipulation.createChild(securityPolicy.getAuthorizationPolicy(),
				ePackage.getAuthorizationPolicy_Roles(), ePackage.getRole());
		manipulation.set(role, ePackage.getRole_Name(), name);
		return role;
	}

	public void removeRole(Role role) throws ModelManipulationException {
		manipulation.remove(role);
	}

	public Demarcation addDemarcation(String name) throws ModelManipulationException {
		Demarcation demarcation = (Demarcation) manipulation.createChild(securityPolicy.getAuthorizationPolicy(),
				ePackage.getAuthorizationPolicy_Demarcations(), ePackage.getDemarcation());
		manipulation.set(demarcation, ePackage.getDemarcation_Name(), name);
		return demarcation;
	}

	public void removeDemarcation(Demarcation demarcation) throws ModelManipulationException {
		manipulation.remove(demarcation);
	}

	public Permission addPermission(String name) throws ModelManipulationException {
		Permission permission = (Permission) manipulation.createChild(securityPolicy.getAuthorizationPolicy(),
				ePackage.getAuthorizationPolicy_Permissions(), ePackage.getPermission());
		manipulation.set(permission, ePackage.getPermission_Name(), name);
		return permission;
	}

	public void removePermission(Permission permission) throws ModelManipulationException {
		manipulation.remove(permission);
	}

	public TemporalContext addTemporalContext(String name) throws ModelManipulationException {
		Schedule schedule = securityPolicy.getAuthorizationPolicy().getSchedule();
		TemporalContext rangeGroup = (TemporalContext) this.manipulation.createChild(schedule, ePackage.getSchedule_TemporalContexts(), ePackage.getTemporalContext());
		this.manipulation.set(rangeGroup, ePackage.getTemporalContext_Name(), name);
		return rangeGroup;
	}

	public void removeTemporalContext(TemporalContext group) throws ModelManipulationException {
		manipulation.remove(group);
	}

	public TimeRange addTemporalContextInstance(TemporalContext rangeGroup, DaySchedule daySchedule, String rangeName, IntegerInterval interval) throws ModelManipulationException {
		//System.out.println("TimeRange " + rangeName + " created!");
		TimeRange timeRange = (TimeRange) this.manipulation.createChild(rangeGroup,
				ePackage.getTemporalContext_Instances(), ePackage.getTimeRange());
		this.manipulation.set(timeRange, ePackage.getTimeRange_Name(), rangeName);
		this.manipulation.set(timeRange, ePackage.getTimeRange_Start(), interval.getStart());
		this.manipulation.set(timeRange, ePackage.getTimeRange_End(), interval.getEnd());
		this.manipulation.set(timeRange, ePackage.getTimeRange_DaySchedule(), daySchedule);
		return timeRange;
	}

	public void removeTemporalContextInstance(TimeRange timeRange) throws ModelManipulationException {
		manipulation.remove(timeRange);
	}

	public DayScheduleTimeRange addDayScheduleTimeRange(DaySchedule daySchedule, String name, IntegerInterval interval) throws ModelManipulationException {
		DayScheduleTimeRange scheduleTimeRange = (DayScheduleTimeRange) manipulation.createChild(daySchedule,
				ePackage.getDaySchedule_Instances(), ePackage.getDayScheduleTimeRange());
		manipulation.set(scheduleTimeRange, ePackage.getTimeRange_Name(), name);
		manipulation.set(scheduleTimeRange, ePackage.getTimeRange_Start(), interval.getStart());
		manipulation.set(scheduleTimeRange, ePackage.getTimeRange_End(), interval.getEnd());
		return scheduleTimeRange;
	}

	public void removeDayScheduleTimeRange(DayScheduleTimeRange scheduleTimeRange) throws ModelManipulationException {
		manipulation.remove(scheduleTimeRange);
	}

	public DayOfWeekSchedule addDayOfWeekSchedule(String name) throws ModelManipulationException {
		Schedule schedule = securityPolicy.getAuthorizationPolicy().getSchedule();
		DayOfWeekSchedule weekdaySchedule = (DayOfWeekSchedule) this.manipulation.createChild(schedule,
				ePackage.getSchedule_DaySchedules(), ePackage.getDayOfWeekSchedule());
		this.manipulation.set(weekdaySchedule, ePackage.getDaySchedule_Name(), name);
		return weekdaySchedule;
	}

	public void removeDayOfWeekSchedule(DayOfWeekSchedule weekdaySchedule) throws ModelManipulationException {
		manipulation.remove(weekdaySchedule);
	}

	public DayOfMonthSchedule addDayOfMonthSchedule(String name) throws ModelManipulationException {
		Schedule schedule = securityPolicy.getAuthorizationPolicy().getSchedule();
		DayOfMonthSchedule yeardaySchedule = (DayOfMonthSchedule) this.manipulation.createChild(schedule,
				ePackage.getSchedule_DaySchedules(), ePackage.getDayOfMonthSchedule());
		this.manipulation.set(yeardaySchedule, ePackage.getDaySchedule_Name(), name);
		return yeardaySchedule;
	}

	public void removeDayOfMonthSchedule(DayOfMonthSchedule yeardaySchedule) throws ModelManipulationException {
		manipulation.remove(yeardaySchedule);
	}

	public DayOfYearSchedule addDayOfYearSchedule(DayOfWeekSchedule weekSchedule, DayOfMonthSchedule monthSchedule, String name) throws ModelManipulationException {
		Schedule schedule = securityPolicy.getAuthorizationPolicy().getSchedule();
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

	public TemporalGrantRule addTemporalGrantRule(TemporalContext time, String name, Role role, Demarcation demarcation, boolean enable, int priority) throws ModelManipulationException {
		Schedule schedule = securityPolicy.getAuthorizationPolicy().getSchedule();
		TemporalGrantRule rule = (TemporalGrantRule) manipulation.createChild(schedule, ePackage.getSchedule_TemporalGrantRules(), ePackage.getTemporalGrantRule());
		manipulation.set(rule, ePackage.getTemporalGrantRule_TemporalContext(), time);
		manipulation.set(rule, ePackage.getTemporalGrantRule_Name(), name);
		manipulation.set(rule, ePackage.getTemporalGrantRule_Role(), role);
		manipulation.set(rule, ePackage.getTemporalGrantRule_Demarcation(), demarcation);
		manipulation.set(rule, ePackage.getTemporalGrantRule_Enable(), enable);
		manipulation.set(rule, ePackage.getTemporalGrantRule_Priority(), priority);
		return rule;
	}

	public void removeTemporalGrantRule(TemporalGrantRule rule) throws ModelManipulationException {
		manipulation.remove(rule);
	}

	public Building addBuilding(String name) throws ModelManipulationException {
		Building building = (Building) manipulation.createChild(securityPolicy, ePackage.getSecurityPolicy_Buildings(), ePackage.getBuilding());
		manipulation.set(building, ePackage.getBuilding_Name(), name);
		return building;
	}

	public void removeBuilding(Building building) throws ModelManipulationException {
		manipulation.remove(building);
	}

	public SecurityZone addSecurityZone(Building building, String name, boolean publicZone) throws ModelManipulationException {
		SecurityZone zone = (SecurityZone) manipulation.createChild(building, ePackage.getBuilding_Securityzones(),
				ePackage.getSecurityZone());
		manipulation.set(zone, ePackage.getObject_Name(), name);
		manipulation.set(zone, ePackage.getSecurityZone_Public(), publicZone);
		return zone;
	}

	public void removeSecurityZone(SecurityZone securityZone) throws ModelManipulationException {
		manipulation.remove(securityZone);
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

	public void assignObjectToPermission(Permission permission, Object object) throws ModelManipulationException {
		manipulation.set(permission, ePackage.getPermission_PO(), object);
	}

	public void deassignObjectFromPermission(Permission permission, Object object) throws ModelManipulationException {
		manipulation.remove(permission, ePackage.getPermission_PO(), object);
	}

	public void setReachability(SecurityZone from, SecurityZone to) throws ModelManipulationException {
		manipulation.addTo(from, ePackage.getSecurityZone_Reachable(), to);
	}

	public void setBidirectionalReachability(SecurityZone zone1, SecurityZone zone2) throws ModelManipulationException {
		manipulation.addTo(zone1, ePackage.getSecurityZone_Reachable(), zone2);
		manipulation.addTo(zone2, ePackage.getSecurityZone_Reachable(), zone1);

	}

	public void removeReachability(SecurityZone from, SecurityZone to) throws ModelManipulationException {
		manipulation.remove(from, ePackage.getSecurityZone_Reachable(), to);
	}

	public void removeBidirectionalReachability(SecurityZone zone1, SecurityZone zone2) throws ModelManipulationException {
		manipulation.remove(zone1, ePackage.getSecurityZone_Reachable(), zone2);
		manipulation.remove(zone2, ePackage.getSecurityZone_Reachable(), zone1);

	}
	// -------------------------------------------


	public void execute(BatchTransformationRule rule) {
		this.transformation.getTransformationStatements().fireOne(rule);
	}

	public void execute(BatchTransformationRule... rules) {
		for (int i = 0; i < rules.length; i++) {
			this.transformation.getTransformationStatements().fireOne(rules[i]);
		}

	}

	public void executeCompound(BatchTransformationRule rule) throws InvocationTargetException {
		Callable<Void> callable = () -> {
			this.transformation.getTransformationStatements().fireOne(rule);
			return null;
		};
		engine.delayUpdatePropagation(callable);
	}

	public void executeCompound(BatchTransformationRule... rules) throws InvocationTargetException {
		for (int i = 0; i < rules.length; i++) {
			final BatchTransformationRule rule = rules[i];
			Callable<Void> callable = () -> {
				this.transformation.getTransformationStatements().fireOne(rule);
				return null;
			};
			engine.delayUpdatePropagation(callable);
		}
	}

	public IModelManipulations getManipulation() {
		return manipulation;
	}

	public void setManipulation(IModelManipulations manipulation) {
		this.manipulation = manipulation;
	}

	public BatchTransformationRuleFactory getBatchFactory() {
		return batchFactory;
	}

	public void setBatchFactory(BatchTransformationRuleFactory batchFactory) {
		this.batchFactory = batchFactory;
	}

	public void dispose() {
		if (!Objects.equal(this.transformation, null)) {
			this.transformation.dispose();
		}
		this.transformation = null;
		return;
	}


}
