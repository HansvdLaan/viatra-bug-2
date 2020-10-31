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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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

	private SiteAccessControlSystem system;
	private Resource resource;

	// Map used to give a unique ID to instances;
	private HashMap<String, Integer> instanceIDCounter;

	public PolicyModifier(final AdvancedViatraQueryEngine engine, SiteAccessControlSystem system, Resource resource) {
		this.engine = engine;
		this.manipulation = new SimpleModelManipulations(this.engine);
		this.system = system;
		this.resource = resource;
		this.instanceIDCounter = new HashMap<>();
		//this.transformation = BatchTransformation.forEngine(this.engine).build();
	}

	// ---------- Add / Remove Authorization Model Entities ----------

	public User addUser(String name) throws ModelManipulationException, InvocationTargetException {
		return this.engine.delayUpdatePropagation(() -> {
			User user = (User) manipulation.createChild(system.getAuthorizationPolicy(),
					ePackage.getAuthorizationPolicy_Users(), ePackage.getUser());
			manipulation.set(user, ePackage.getUser_Name(), name);
			return user;
		});
	}

	public void removeUser(User user) throws ModelManipulationException, InvocationTargetException {
		this.engine.delayUpdatePropagation(() -> {
			manipulation.remove(user);
			return null;
		});
	}

	public Role addRole(String name) throws ModelManipulationException, InvocationTargetException {
		return this.engine.delayUpdatePropagation(() -> {
			Role role = (Role) manipulation.createChild(system.getAuthorizationPolicy(),
					ePackage.getAuthorizationPolicy_Roles(), ePackage.getRole());
			manipulation.set(role, ePackage.getRole_Name(), name);
			return role;
		});
	}

	public void removeRole(Role role) throws ModelManipulationException, InvocationTargetException {
		this.engine.delayUpdatePropagation(() -> {
			for (User user : role.getRU().stream().collect(Collectors.toList())) {
				deassignRoleFromUser(user, role);
			}
			for (TemporalGrantRule temporalGrantRule : role.getConstrainedBy().stream().collect(Collectors.toList())) {
				removeTemporalGrantRule(temporalGrantRule);
			}
			manipulation.remove(role);
			return null;
		});

	}

	public Demarcation addDemarcation(String name) throws ModelManipulationException, InvocationTargetException {
		return this.engine.delayUpdatePropagation(() -> {
			Demarcation demarcation = (Demarcation) manipulation.createChild(system.getAuthorizationPolicy(),
					ePackage.getAuthorizationPolicy_Demarcations(), ePackage.getDemarcation());
			manipulation.set(demarcation, ePackage.getDemarcation_Name(), name);
			return demarcation;
		});
	}

	public void removeDemarcation(Demarcation demarcation) throws ModelManipulationException, InvocationTargetException {
		this.engine.delayUpdatePropagation(() -> {
					for (TemporalGrantRule temporalGrantRule : demarcation.getConstrainedBy().stream().collect(Collectors.toList())) {
						removeTemporalGrantRule(temporalGrantRule);
					}
			manipulation.remove(demarcation);
			return null;
		});

	}

	public Permission addPermission(String name) throws ModelManipulationException, InvocationTargetException {
		return this.engine.delayUpdatePropagation(() -> {
			Permission permission = (Permission) manipulation.createChild(system.getAuthorizationPolicy(),
							ePackage.getAuthorizationPolicy_Permissions(), ePackage.getPermission());
			manipulation.set(permission, ePackage.getPermission_Name(), name);
			return permission;
		});
	}

	public void removePermission(Permission permission) throws ModelManipulationException, InvocationTargetException {
		this.engine.delayUpdatePropagation(() -> {
			manipulation.remove(permission);
			return null;
		});
	}

	public TemporalContext addTemporalContext(String name) throws ModelManipulationException, InvocationTargetException {
		return this.engine.delayUpdatePropagation(() -> {
			Schedule schedule = system.getSchedule();
			TemporalContext context = (TemporalContext) this.manipulation.createChild(schedule, ePackage.getSchedule_TemporalContexts(), ePackage.getTemporalContext());
			this.manipulation.set(context, ePackage.getTemporalContext_Name(), name);
			return context;
		});
	}

	public void removeTemporalContext(TemporalContext group) throws ModelManipulationException, InvocationTargetException {
		this.engine.delayUpdatePropagation(() -> {
			manipulation.remove(group);
			return null;
		});
	}

	public TimeRange addTemporalContextInstance(TemporalContext context, DaySchedule daySchedule, IntegerInterval interval) throws ModelManipulationException, InvocationTargetException {
		return this.engine.delayUpdatePropagation(() -> {
			TimeRange timeRange = (TimeRange) this.manipulation.createChild(context,
					ePackage.getTemporalContext_Instances(), ePackage.getTimeRange());
			this.manipulation.set(timeRange, ePackage.getTimeRange_Name(), getUniqueID(context.getName() + "-" + daySchedule.getName()));
			this.manipulation.set(timeRange, ePackage.getTimeRange_Start(), interval.getStart());
			this.manipulation.set(timeRange, ePackage.getTimeRange_End(), interval.getEnd());
			this.manipulation.set(timeRange, ePackage.getTimeRange_DaySchedule(), daySchedule);
			return timeRange;
		});
	}

	public void removeTemporalContextInstance(TimeRange timeRange) throws ModelManipulationException, InvocationTargetException {
		this.engine.delayUpdatePropagation(() -> {
			manipulation.remove(timeRange);
			return null;
		});
	}

	public DayScheduleTimeRange addDayScheduleTimeRange(DaySchedule daySchedule, IntegerInterval interval) throws ModelManipulationException, InvocationTargetException {
		return this.engine.delayUpdatePropagation(() -> {
			DayScheduleTimeRange scheduleTimeRange = (DayScheduleTimeRange) manipulation.createChild(daySchedule,
					ePackage.getDaySchedule_Instances(), ePackage.getDayScheduleTimeRange());
			manipulation.set(scheduleTimeRange, ePackage.getTimeRange_Name(), getUniqueID(daySchedule.getName()));
			manipulation.set(scheduleTimeRange, ePackage.getTimeRange_Start(), interval.getStart());
			manipulation.set(scheduleTimeRange, ePackage.getTimeRange_End(), interval.getEnd());
			return scheduleTimeRange;
		});
	}

	public void removeDayScheduleTimeRange(DayScheduleTimeRange scheduleTimeRange) throws ModelManipulationException, InvocationTargetException {
		this.engine.delayUpdatePropagation(() -> {
			manipulation.remove(scheduleTimeRange);
			return null;
		});
	}

	public DayOfWeekSchedule addDayOfWeekSchedule(String name) throws ModelManipulationException, InvocationTargetException {
		return this.engine.delayUpdatePropagation(() -> {
			Schedule schedule = system.getSchedule();
			DayOfWeekSchedule weekdaySchedule = (DayOfWeekSchedule) this.manipulation.createChild(schedule,
					ePackage.getSchedule_DaySchedules(), ePackage.getDayOfWeekSchedule());
			this.manipulation.set(weekdaySchedule, ePackage.getDaySchedule_Name(), name);
			return weekdaySchedule;
		});
	}

	public void removeDayOfWeekSchedule(DayOfWeekSchedule weekdaySchedule) throws ModelManipulationException, InvocationTargetException {
		this.engine.delayUpdatePropagation(() -> {
			manipulation.remove(weekdaySchedule);
			return null;
		});
	}

	public DayOfMonthSchedule addDayOfMonthSchedule(String name) throws ModelManipulationException, InvocationTargetException {
		return this.engine.delayUpdatePropagation(() -> {
			Schedule schedule = system.getSchedule();
			DayOfMonthSchedule yeardaySchedule = (DayOfMonthSchedule) this.manipulation.createChild(schedule,
					ePackage.getSchedule_DaySchedules(), ePackage.getDayOfMonthSchedule());
			this.manipulation.set(yeardaySchedule, ePackage.getDaySchedule_Name(), name);
			return yeardaySchedule;
		});
	}

	public void removeDayOfMonthSchedule(DayOfMonthSchedule yeardaySchedule) throws ModelManipulationException, InvocationTargetException {
		this.engine.delayUpdatePropagation(() -> {
			manipulation.remove(yeardaySchedule);
			return null;
		});
	}

	public DayOfWeekMonthSchedule addDayOfWeekMonthSchedule(DayOfWeekSchedule weekSchedule, DayOfMonthSchedule monthSchedule, String name) throws ModelManipulationException, InvocationTargetException {
		return this.engine.delayUpdatePropagation(() -> {
			Schedule schedule = system.getSchedule();
			DayOfWeekMonthSchedule weekMonthSchedule = (DayOfWeekMonthSchedule) manipulation.createChild(schedule,
					ePackage.getSchedule_DaySchedules(), ePackage.getDayOfWeekMonthSchedule());
			this.manipulation.set(weekMonthSchedule, ePackage.getDayOfWeekMonthSchedule_DayOfWeekSchedule(), weekSchedule);
			this.manipulation.set(weekMonthSchedule, ePackage.getDayOfWeekMonthSchedule_DayOfMonthSchedule(), monthSchedule);
			manipulation.set(weekMonthSchedule, ePackage.getDaySchedule_Name(), name);
			return weekMonthSchedule;
		});
	}

	public void removeDayOfWeekMonthSchedule(DayOfWeekMonthSchedule schedule) throws ModelManipulationException, InvocationTargetException {
		this.engine.delayUpdatePropagation(() -> {
			manipulation.remove(schedule);
			return null;
		});
	}

	public DayOfYearSchedule addDayOfYearSchedule(DayOfWeekMonthSchedule weekMonthSchedule, String name) throws ModelManipulationException, InvocationTargetException {
		return this.engine.delayUpdatePropagation(() -> {
			Schedule schedule = system.getSchedule();
			DayOfYearSchedule yearSchedule = (DayOfYearSchedule) manipulation.createChild(schedule,
					ePackage.getSchedule_DaySchedules(), ePackage.getDayOfYearSchedule());
			this.manipulation.set(yearSchedule, ePackage.getDayOfYearSchedule_DayOfWeekMonthSchedule(), weekMonthSchedule);
			manipulation.set(yearSchedule, ePackage.getDaySchedule_Name(), name);
			return yearSchedule;
		});
	}

	public void removeDayOfYearSchedule(DayOfYearSchedule schedule) throws ModelManipulationException, InvocationTargetException {
		this.engine.delayUpdatePropagation(() -> {
			manipulation.remove(schedule);
			return null;
		});
	}

	public TemporalGrantRule addTemporalGrantRule(TemporalContext context, String name, Role role, Demarcation demarcation, boolean enable, int priority) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			Schedule schedule = system.getSchedule();
			TemporalGrantRule rule = (TemporalGrantRule) manipulation.createChild(schedule, ePackage.getSchedule_TemporalGrantRules(), ePackage.getTemporalGrantRule());
			manipulation.set(rule, ePackage.getTemporalGrantRule_TemporalContext(), context);
			manipulation.set(rule, ePackage.getTemporalGrantRule_Name(), name);
			manipulation.set(rule, ePackage.getTemporalGrantRule_Role(), role);
			manipulation.set(rule, ePackage.getTemporalGrantRule_Demarcation(), demarcation);
			manipulation.set(rule, ePackage.getTemporalGrantRule_Enable(), enable);
			manipulation.set(rule, ePackage.getTemporalGrantRule_Priority(), priority);
			return rule;
		});
	}

	public void removeTemporalGrantRule(TemporalGrantRule rule) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.remove(rule);
			return null;
		});
	}

	public TemporalAuthenticationRule addTemporalAuthenticationRule(TemporalContext context, String name, SecurityZone zone, int status, int priority) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			TemporalAuthenticationRule rule = (TemporalAuthenticationRule) manipulation.createChild(this.system.getAuthenticationPolicy(),
					ePackage.getAuthenticationPolicy_TemporalAuthenticationRules(), ePackage.getTemporalAuthenticationRule());
			manipulation.set(rule, ePackage.getTemporalAuthenticationRule_TemporalContext(), context);
			manipulation.set(rule, ePackage.getTemporalAuthenticationRule_Name(), name);
			manipulation.set(rule, ePackage.getTemporalAuthenticationRule_SecurityZone(), zone);
			manipulation.set(rule, ePackage.getTemporalAuthenticationRule_Status(), status);
			manipulation.set(rule, ePackage.getTemporalAuthenticationRule_Priority(), priority);
			return rule;
		});
	}

	public void removeTemporalAuthenticationRule(TemporalAuthenticationRule rule) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.remove(rule);
			return null;
		});
	}

	public SecurityZone addSecurityZone(String name, boolean publicZone) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			SecurityZone zone = (SecurityZone) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_SecurityZones(),
					ePackage.getSecurityZone());
			manipulation.set(zone, ePackage.getXObject_Name(), name);
			manipulation.set(zone, ePackage.getSecurityZone_Public(), publicZone);
			return zone;
		});
	}

	public void removeSecurityZone(SecurityZone securityZone) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.remove(securityZone);
			return null;
		});
	}
	// -----------------------------------------------


	// ---------- Add / Remove Authorization Model Relations ----------

	public void assignRoleToUser(User user, Role role) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.addTo(user, ePackage.getUser_UR(), role);
			return null;
		});
	}

	public void deassignRoleFromUser(User user, Role role) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.remove(user, ePackage.getUser_UR(), role);
			return null;
		});
	}

	public void assignPermissionToDemarcation(Demarcation demarcation, Permission permission) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.addTo(demarcation, ePackage.getDemarcation_DP(), permission);
			return null;
		});
	}

	public void deassignPermissionFromDemarcation(Demarcation demarcation, Permission permission) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.remove(demarcation, ePackage.getDemarcation_DP(), permission);
			return null;
		});
	}

	public void addRoleInheritance(Role juniorRole, Role seniorRole) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.addTo(juniorRole, ePackage.getRole_Seniors(), seniorRole);
			return null;
		});
	}

	public void removeRoleInheritance(Role juniorRole, Role seniorRole) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.remove(juniorRole, ePackage.getRole_Seniors(), seniorRole);
			return null;
		});
	}

	public void addDemarcationInheritance(Demarcation subdemarcation, Demarcation supdemarcation) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.addTo(subdemarcation, ePackage.getDemarcation_Superdemarcations(), supdemarcation);
			return null;
		});
	}

	public void removeDemarcationInheritance(Demarcation subdemarcation, Demarcation supdemarcation) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.remove(subdemarcation, ePackage.getDemarcation_Superdemarcations(), supdemarcation);
			return null;
		});
	}

	public void assignObjectToPermission(Permission permission, XObject object) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.set(permission, ePackage.getPermission_PO(), object);
			return null;
		});
	}

	public void deassignObjectFromPermission(Permission permission, XObject object) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.remove(permission, ePackage.getPermission_PO(), object);
			return null;
		});
	}

	public void setReachability(SecurityZone from, SecurityZone to) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.addTo(from, ePackage.getSecurityZone_Reachable(), to);
			return null;
		});
	}

	public void setBidirectionalReachability(SecurityZone zone1, SecurityZone zone2) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.addTo(zone1, ePackage.getSecurityZone_Reachable(), zone2);
			manipulation.addTo(zone2, ePackage.getSecurityZone_Reachable(), zone1);
			return null;
		});
	}

	public void removeReachability(SecurityZone from, SecurityZone to) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.remove(from, ePackage.getSecurityZone_Reachable(), to);
			return null;
		});
	}

	public void removeBidirectionalReachability(SecurityZone zone1, SecurityZone zone2) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.remove(zone1, ePackage.getSecurityZone_Reachable(), zone2);
			manipulation.remove(zone2, ePackage.getSecurityZone_Reachable(), zone1);
			return null;
		});
	}
	// -------------------------------------------

	// ---------- Add / Remove SoD constraints ----------

	public SoDURConstraint addSoDURConstraint(String name, Role role1, Role role2) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			SoDURConstraint constraint = (SoDURConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getSoDURConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryRoleConstraint_Left(), role1);
			manipulation.set(constraint, ePackage.getBinaryRoleConstraint_Right(), role2);
			return constraint;
		});
	}

	public SoDUDConstraint addSoDUDConstraint(String name, Demarcation demarcation1, Demarcation demarcation2, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			SoDUDConstraint constraint = (SoDUDConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getSoDUDConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryDemarcationConstraint_Left(), demarcation1);
			manipulation.set(constraint, ePackage.getBinaryDemarcationConstraint_Right(), demarcation2);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public SoDUPConstraint addSoDUPConstraint(String name, Permission permission1, Permission permission2, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			SoDUPConstraint constraint = (SoDUPConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getSoDUPConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Left(), permission1);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Right(), permission2);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public SoDRDConstraint addSoDRDConstraint(String name, Demarcation demarcation1, Demarcation demarcation2, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			SoDRDConstraint constraint = (SoDRDConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getSoDRDConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryDemarcationConstraint_Left(), demarcation1);
			manipulation.set(constraint, ePackage.getBinaryDemarcationConstraint_Right(), demarcation2);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public SoDRPConstraint addSoDRPConstraint(String name, Permission permission1, Permission permission2, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			SoDRPConstraint constraint = (SoDRPConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getSoDRPConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Left(), permission1);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Right(), permission2);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public SoDDPConstraint addSoDDPConstraint(String name, Permission permission1, Permission permission2) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			SoDDPConstraint constraint = (SoDDPConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getSoDDPConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Left(), permission1);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Right(), permission2);
			return constraint;
		});
	}

	// ---------- Add / Remove Prerequisite constraints ----------

	public PrerequisiteURConstraint addPrerequisiteURConstraint(String name, Role role1, Role role2) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			PrerequisiteURConstraint constraint = (PrerequisiteURConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getPrerequisiteURConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryRoleConstraint_Left(), role1);
			manipulation.set(constraint, ePackage.getBinaryRoleConstraint_Right(), role2);
			return constraint;
		});
	}

	public PrerequisiteUDConstraint addPrerequisiteUDConstraint(String name, Demarcation demarcation1, Demarcation demarcation2, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			PrerequisiteUDConstraint constraint = (PrerequisiteUDConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getPrerequisiteUDConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryDemarcationConstraint_Left(), demarcation1);
			manipulation.set(constraint, ePackage.getBinaryDemarcationConstraint_Right(), demarcation2);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public PrerequisiteUPConstraint addPrerequisiteUPConstraint(String name, Permission permission1, Permission permission2, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			PrerequisiteUPConstraint constraint = (PrerequisiteUPConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getPrerequisiteUPConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Left(), permission1);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Right(), permission2);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public PrerequisiteRDConstraint addPrerequisiteRDConstraint(String name, Demarcation demarcation1, Demarcation demarcation2, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			PrerequisiteRDConstraint constraint = (PrerequisiteRDConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getPrerequisiteRDConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryDemarcationConstraint_Left(), demarcation1);
			manipulation.set(constraint, ePackage.getBinaryDemarcationConstraint_Right(), demarcation2);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public PrerequisiteRPConstraint addPrerequisiteRPConstraint(String name, Permission permission1, Permission permission2, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			PrerequisiteRPConstraint constraint = (PrerequisiteRPConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getPrerequisiteRPConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Left(), permission1);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Right(), permission2);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public PrerequisiteDPConstraint addPrerequisiteDPConstraint(String name, Permission permission1, Permission permission2) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			PrerequisiteDPConstraint constraint = (PrerequisiteDPConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getPrerequisiteDPConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Left(), permission1);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Right(), permission2);
			return constraint;
		});
	}

	// ---------- Add / Remove BoD constraints ----------

	public BoDURConstraint addBoDURConstraint(String name, Role role1, Role role2) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			BoDURConstraint constraint = (BoDURConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getBoDURConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryRoleConstraint_Left(), role1);
			manipulation.set(constraint, ePackage.getBinaryRoleConstraint_Right(), role2);
			return constraint;
		});
	}

	public BoDUDConstraint addBoDUDConstraint(String name, Demarcation demarcation1, Demarcation demarcation2, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			BoDUDConstraint constraint = (BoDUDConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getBoDUDConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryDemarcationConstraint_Left(), demarcation1);
			manipulation.set(constraint, ePackage.getBinaryDemarcationConstraint_Right(), demarcation2);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public BoDUPConstraint addBoDUPConstraint(String name, Permission permission1, Permission permission2, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			BoDUPConstraint constraint = (BoDUPConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getBoDUPConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Left(), permission1);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Right(), permission2);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public BoDRDConstraint addBoDRDConstraint(String name, Demarcation demarcation1, Demarcation demarcation2, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			BoDRDConstraint constraint = (BoDRDConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getBoDRDConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryDemarcationConstraint_Left(), demarcation1);
			manipulation.set(constraint, ePackage.getBinaryDemarcationConstraint_Right(), demarcation2);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public BoDRPConstraint addBoDRPConstraint(String name, Permission permission1, Permission permission2, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			BoDRPConstraint constraint = (BoDRPConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getBoDRPConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Left(), permission1);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Right(), permission2);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public BoDDPConstraint addBoDDPConstraint(String name, Permission permission1, Permission permission2) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			BoDDPConstraint constraint = (BoDDPConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getBoDDPConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Left(), permission1);
			manipulation.set(constraint, ePackage.getBinaryPermissionConstraint_Right(), permission2);
			return constraint;
		});
	}

	// ---------- Add / Remove Cardinality constraints ----------

	public CardinalityURConstraint addCardinalityURConstraint(String name, Role role, int bound) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			CardinalityURConstraint constraint = (CardinalityURConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getCardinalityURConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getUnaryRoleConstraint_Role(), role);
			manipulation.set(constraint, ePackage.getCardinalityURConstraint_Bound(), bound);
			return constraint;
		});
	}

	public CardinalityUDConstraint addCardinalityUDConstraint(String name, Demarcation demarcation, int bound, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			CardinalityUDConstraint constraint = (CardinalityUDConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getCardinalityUDConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getUnaryDemarcationConstraint_Demarcation(), demarcation);
			manipulation.set(constraint, ePackage.getCardinalityUDConstraint_Bound(), bound);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public CardinalityUPConstraint addCardinalityUPConstraint(String name, Permission permission, int bound, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			CardinalityUPConstraint constraint = (CardinalityUPConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getCardinalityUPConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getUnaryPermissionConstraint_Permission(), permission);
			manipulation.set(constraint, ePackage.getCardinalityUPConstraint_Bound(), bound);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public CardinalityRDConstraint addCardinalityRDConstraint(String name, Demarcation demarcation, int bound, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			CardinalityRDConstraint constraint = (CardinalityRDConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getCardinalityRDConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getUnaryDemarcationConstraint_Demarcation(), demarcation);
			manipulation.set(constraint, ePackage.getCardinalityRDConstraint_Bound(), bound);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public CardinalityRPConstraint addCardinalityRPConstraint(String name, Permission permission, int bound, TemporalContext context) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			CardinalityRPConstraint constraint = (CardinalityRPConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getCardinalityRPConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getUnaryPermissionConstraint_Permission(), permission);
			manipulation.set(constraint, ePackage.getCardinalityRPConstraint_Bound(), bound);
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_TemporalContext(), context);
			return constraint;
		});
	}

	public CardinalityDPConstraint addCardinalityDPConstraint(String name, Permission permission, int bound) throws ModelManipulationException, InvocationTargetException {
		return engine.delayUpdatePropagation( () -> {
			CardinalityDPConstraint constraint = (CardinalityDPConstraint) manipulation.createChild(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(),
					ePackage.getCardinalityDPConstraint());
			manipulation.set(constraint, ePackage.getAuthorizationConstraint_Name(), name);
			manipulation.set(constraint, ePackage.getUnaryPermissionConstraint_Permission(), permission);
			manipulation.set(constraint, ePackage.getCardinalityDPConstraint_Bound(), bound);
			return constraint;
		});
	}

	public void removeAuthorizationConstraint(AuthorizationConstraint constraint) throws ModelManipulationException, InvocationTargetException {
		engine.delayUpdatePropagation( () -> {
			manipulation.remove(system, ePackage.getSiteAccessControlSystem_AuthorizationConstraints(), constraint);
			return null;
		});
	}

	public SiteAccessControlSystem getSystem() {
		return system;
	}

	public Resource getResource() {
		return resource;
	}

	public TRBACPackage getEPackage() {
		return ePackage;
	}

	public AdvancedViatraQueryEngine getEngine() {
		return engine;
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

	public void execute(BatchTransformationRule rule) {
		this.transformation.getTransformationStatements().fireOne(rule);
	}

	public void execute(BatchTransformationRule... rules) {
		for (int i = 0; i < rules.length; i++) {
			this.transformation.getTransformationStatements().fireOne(rules[i]);
		}

	}

//	public void executeCompound(BatchTransformationRule rule) throws InvocationTargetException {
//		Callable<Void> callable = () -> {
//			this.transformation.getTransformationStatements().fireOne(rule);
//			return null;
//		};
//		engine.delayUpdatePropagation(callable);
//	}
//
//	public void executeCompound(BatchTransformationRule... rules) throws InvocationTargetException {
//		for (int i = 0; i < rules.length; i++) {
//			final BatchTransformationRule rule = rules[i];
//			Callable<Void> callable = () -> {
//				this.transformation.getTransformationStatements().fireOne(rule);
//				return null;
//			};
//			engine.delayUpdatePropagation(callable);
//		}
//	}

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
