package com.vanderhighway.trbac.core.validator;

import com.vanderhighway.trbac.aggregators.Scenario;
import com.vanderhighway.trbac.model.trbac.model.SoDURConstraint;
import com.vanderhighway.trbac.model.trbac.model.TemporalContext;
import com.vanderhighway.trbac.patterns.*;
import org.eclipse.viatra.query.runtime.api.IMatchUpdateListener;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ListenerFactory {

	public static Set<IMatchUpdateListener> getALlUpdateListeners() {
		Set<IMatchUpdateListener> updateListeners = new HashSet();
		updateListeners.add(getRoleNameMatchUpdateListener());
		updateListeners.add(getUserShouldHaveARoleUpdateListener());
		updateListeners.add(getRoleShouldHaveADemarcationUpdateListener());
		updateListeners.add(getDemarcationShouldHaveAPermissionUpdateListener());
		updateListeners.add(getOnlyOneDirectorUpdateListeer());
		updateListeners.add(getOnlyOneRnDManagerUpdateListener());
		updateListeners.add(getOnlyOneOperationsManagerUpdateListener());
		updateListeners.add(getSoDEmployeeAndContractorUpdateListener());
		updateListeners.add(getSoDEmployeeAndVisitorUpdateListener());
		updateListeners.add(getAccessRelationUpdateListener());
		return updateListeners;
	}

	public static IMatchUpdateListener<RoleName.Match> getRoleNameMatchUpdateListener() {
		return new IMatchUpdateListener<RoleName.Match>() {
			@Override
			public void notifyAppearance(RoleName.Match match) {
				System.out.printf("[ADD RoleName Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(RoleName.Match match) {
				System.out.printf("[REM RoleName Match] %s %n", match.prettyPrint());

			}
		};
	}

	public static IMatchUpdateListener<UserShouldHaveARole.Match> getUserShouldHaveARoleUpdateListener() {
		return new IMatchUpdateListener<UserShouldHaveARole.Match>() {
			@Override
			public void notifyAppearance(UserShouldHaveARole.Match match) {
				System.out.printf("[ADD UserShouldHaveARole Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(UserShouldHaveARole.Match match) {
				System.out.printf("[REM UserShouldHaveARole Match] %s %n", match.prettyPrint());

			}
		};
	}

	public static IMatchUpdateListener<RoleShouldHaveADemarcation.Match> getRoleShouldHaveADemarcationUpdateListener() {
		return new IMatchUpdateListener<RoleShouldHaveADemarcation.Match>() {
			@Override
			public void notifyAppearance(RoleShouldHaveADemarcation.Match match) {
				System.out.printf("[ADD RoleShouldHaveADemarcation Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(RoleShouldHaveADemarcation.Match match) {
				System.out.printf("[REM RoleShouldHaveADemarcation Match] %s %n", match.prettyPrint());

			}
		};
	}

	public static IMatchUpdateListener<DemarcationShouldHaveAPermission.Match> getDemarcationShouldHaveAPermissionUpdateListener() {
		return new IMatchUpdateListener<DemarcationShouldHaveAPermission.Match>() {
			@Override
			public void notifyAppearance(DemarcationShouldHaveAPermission.Match match) {
				System.out.printf("[ADD DemarcationShouldHaveAPermission Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(DemarcationShouldHaveAPermission.Match match) {
				System.out.printf("[REM DemarcationShouldHaveAPermission Match] %s %n", match.prettyPrint());

			}
		};
	}

	public static IMatchUpdateListener<OnlyOneDirector.Match> getOnlyOneDirectorUpdateListeer() {
		return new IMatchUpdateListener<OnlyOneDirector.Match>() {
			@Override
			public void notifyAppearance(OnlyOneDirector.Match match) {
				System.out.printf("[ADD OnlyOneDirector Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(OnlyOneDirector.Match match) {
				System.out.printf("[REM OnlyOneDirector Match] %s %n", match.prettyPrint());

			}
		};
	}

	public static IMatchUpdateListener<OnlyOneRnDManager.Match> getOnlyOneRnDManagerUpdateListener() {
		return new IMatchUpdateListener<OnlyOneRnDManager.Match>() {
			@Override
			public void notifyAppearance(OnlyOneRnDManager.Match match) {
				System.out.printf("[ADD OnlyOneRnDManager Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(OnlyOneRnDManager.Match match) {
				System.out.printf("[REM OnlyOneRnDManager Match] %s %n", match.prettyPrint());

			}
		};
	}

	public static IMatchUpdateListener<OnlyOneOperationsManager.Match> getOnlyOneOperationsManagerUpdateListener() {
		return new IMatchUpdateListener<OnlyOneOperationsManager.Match>() {
			@Override
			public void notifyAppearance(OnlyOneOperationsManager.Match match) {
				System.out.printf("[ADD OnlyOneOperationsManager Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(OnlyOneOperationsManager.Match match) {
				System.out.printf("[REM OnlyOneOperationsManager Match] %s %n", match.prettyPrint());

			}
		};
	}

	public static IMatchUpdateListener<SoDEmployeeAndContractor.Match> getSoDEmployeeAndContractorUpdateListener() {
		return new IMatchUpdateListener<SoDEmployeeAndContractor.Match>() {
			@Override
			public void notifyAppearance(SoDEmployeeAndContractor.Match match) {
				System.out.printf("[ADD SoDEmployeeAndContractor Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(SoDEmployeeAndContractor.Match match) {
				System.out.printf("[REM SoDEmployeeAndContractor Match] %s %n", match.prettyPrint());

			}
		};
	}

	public static IMatchUpdateListener<SoDEmployeeAndVisitor.Match> getSoDEmployeeAndVisitorUpdateListener() {
		return new IMatchUpdateListener<SoDEmployeeAndVisitor.Match>() {
			@Override
			public void notifyAppearance(SoDEmployeeAndVisitor.Match match) {
				System.out.printf("[ADD SoDEmployeeAndVisitor Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(SoDEmployeeAndVisitor.Match match) {
				System.out.printf("[REM SoDEmployeeAndVisitor Match] %s %n", match.prettyPrint());

			}
		};
	}

//	public static IMatchUpdateListener<PrerequisiteEverybodyHasAccessToLobby.Match> getPrerequisiteEveryHasAccessToLobbyUpdateListener() {
//		return new IMatchUpdateListener<PrerequisiteEverybodyHasAccessToLobby.Match>() {
//			@Override
//			public void notifyAppearance(PrerequisiteEverybodyHasAccessToLobby.Match match) {
//				System.out.printf("[ADD PrerequisiteEverybodyHasAccessToLobby Match] %s %n", match.prettyPrint());
//			}
//
//			@Override
//			public void notifyDisappearance(PrerequisiteEverybodyHasAccessToLobby.Match match) {
//				System.out.printf("[REM PrerequisiteEverybodyHasAccessToLobby Match] %s %n", match.prettyPrint());
//
//			}
//		};
//	}
//
//	public static IMatchUpdateListener<PrerequisiteVaultImpliesOpenOffice.Match> getPrerequisiteVaultImpliesOpenOfficeUpdateListener() {
//		return new IMatchUpdateListener<PrerequisiteVaultImpliesOpenOffice.Match>() {
//			@Override
//			public void notifyAppearance(PrerequisiteVaultImpliesOpenOffice.Match match) {
//				System.out.printf("[ADD PrerequisiteVaultImpliesOpenOffice Match] %s %n", match.prettyPrint());
//			}
//
//			@Override
//			public void notifyDisappearance(PrerequisiteVaultImpliesOpenOffice.Match match) {
//				System.out.printf("[REM PrerequisiteVaultImpliesOpenOffice Match] %s %n", match.prettyPrint());
//
//			}
//		};
//	}

	public static IMatchUpdateListener<AccessRelation.Match> getAccessRelationUpdateListener() {
		return new IMatchUpdateListener<AccessRelation.Match>() {
			@Override
			public void notifyAppearance(AccessRelation.Match match) {
				Set<TemporalContext> groups = match.getScenario();
				Set<String> groupNames = groups.stream().map(x -> x.getName()).collect(Collectors.toSet());
				String userName = match.getUser().getName();
				String permissionName = match.getPermission().getName();
				System.out.println("[ADD AccessRelation Match] " + userName + " has permission " + permissionName + " during " + groupNames);
			}

			@Override
			public void notifyDisappearance(AccessRelation.Match match) {
				Set<TemporalContext> groups = match.getScenario();
				Set<String> groupNames = groups.stream().map(x -> x.getName()).collect(Collectors.toSet());
				String userName = match.getUser().getName();
				String permissionName = match.getPermission().getName();
				System.out.println("[ADD AccessRelation Match] " + userName + " has permission " + permissionName + " during " + groupNames);
			}
		};
	}
	
//	public static IMatchUpdateListener<AccessRelationWithHierarchies.Match> getAccessRelationWithHierarchiesUpdateListener() {
//		return new IMatchUpdateListener<AccessRelationWithHierarchies.Match>() {
//			@Override
//			public void notifyAppearance(AccessRelationWithHierarchies.Match match) {
//				System.out.printf("[ADD AccessRelation2 Match] %s %n", match.prettyPrint());
//			}
//
//			@Override
//			public void notifyDisappearance(AccessRelationWithHierarchies.Match match) {
//				System.out.printf("[REM AccessRelation2 Match] %s %n", match.prettyPrint());
//
//			}
//		};
//	}
	
	public static IMatchUpdateListener<AllJuniors.Match> getAllJuniorsUpdateListener() {
		return new IMatchUpdateListener<AllJuniors.Match>() {
			@Override
			public void notifyAppearance(AllJuniors.Match match) {
				System.out.printf("[ADD AllJuniors Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(AllJuniors.Match match) {
				System.out.printf("[REM AllJuniors Match] %s %n", match.prettyPrint());

			}
		};
	}

	public static IMatchUpdateListener<RangeP.Match> getRangeUpdateListener() {
		return new IMatchUpdateListener<RangeP.Match>() {
			@Override
			public void notifyAppearance(RangeP.Match match) {
				//System.out.printf("[ADD Range Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(RangeP.Match match) {
				//System.out.printf("[REM Range Match] %s %n", match.prettyPrint());

			}
		};
	}

	public static IMatchUpdateListener<Scenarios.Match> getScenarioUpdateListener() {
		return new IMatchUpdateListener<Scenarios.Match>() {
			@Override
			public void notifyAppearance(Scenarios.Match match) {
				Set<TemporalContext> contexts =  match.getScenario();
				Set<String> contextNames = contexts.stream().map(x -> x.getName()).collect(Collectors.toSet());
				System.out.printf("[ADD Scenarios Match] %s %n", contextNames);
			}

			@Override
			public void notifyDisappearance(Scenarios.Match match) {
				Set<TemporalContext> contexts = match.getScenario();
				Set<String> contextNames = contexts.stream().map(x -> x.getName()).collect(Collectors.toSet());
				System.out.printf("[REM Scenarios Match] %s %n", contextNames);
			}
		};
	}

//	public static IMatchUpdateListener<TimeRangeGroupCollectionHasGroup.Match> getTimeRangeGroupCollectionHasGroupUpdateListener() {
//		return new IMatchUpdateListener<TimeRangeGroupCollectionHasGroup.Match>() {
//			@Override
//			public void notifyAppearance(TimeRangeGroupCollectionHasGroup.Match match) {
//				Set<TimeRangeGroup> groups = (Set<TimeRangeGroup>) match.getGroups();
//				Set<String> groupNames = groups.stream().map(x -> x.getName()).collect(Collectors.toSet());
//				String groupName = match.getGroup().getName();
//				System.out.println("[ADD TimeRangeGroupCollectionHasGroup Match]" + groupNames + " has " + groupName);
//			}
//
//			@Override
//			public void notifyDisappearance(TimeRangeGroupCollectionHasGroup.Match match) {
//				Set<TimeRangeGroup> groups = (Set<TimeRangeGroup>) match.getGroups();
//				Set<String> groupNames = groups.stream().map(x -> x.getName()).collect(Collectors.toSet());
//				String groupName = match.getGroup().getName();
//				System.out.println("[ADD TimeRangeGroupCollectionHasGroup Match]" + groupNames + " has " + groupName);
//			}
//		};
//	}
//
	public static IMatchUpdateListener<RSD.Match> getRDSUpdateListener() {
		return new IMatchUpdateListener<RSD.Match>() {
			@Override
			public void notifyAppearance(RSD.Match match) {
				Set<TemporalContext> scenario = match.getScenario();
				Set<String> temporalContextNames = scenario.stream().map(x -> x.getName()).collect(Collectors.toSet());
				String roleName = match.getRole().getName();
				String demarcationName = match.getDemarcation().getName();
				System.out.println("[ADD RSD Match] " + temporalContextNames + " -> " + roleName + "-" + demarcationName);
			}

			@Override
			public void notifyDisappearance(RSD.Match match) {
				Set<TemporalContext> scenario = match.getScenario();
				Set<String> temporalContextNames = scenario.stream().map(x -> x.getName()).collect(Collectors.toSet());
				String roleName = match.getRole().getName();
				String demarcationName = match.getDemarcation().getName();
				System.out.println("[REM RSD Match]" + temporalContextNames + " -> " + roleName + "-" + demarcationName);
			}
		};
	}

	public static IMatchUpdateListener<Reachable.Match> getReachableUpdateListener() {
		return new IMatchUpdateListener<Reachable.Match>() {
			@Override
			public void notifyAppearance(Reachable.Match match) {
				System.out.println("[ADD Reachable Match] " + match.getBuilding().getName() + ":" + match.getZone().getName());
			}

			@Override
			public void notifyDisappearance(Reachable.Match match) {
				System.out.println("[REM Reachable Match]" + match.getBuilding().getName() + ":" + match.getZone().getName());
			}
		};
	}

	public static IMatchUpdateListener<ReachableAccess.Match> getReachableAccessUpdateListener() {
		return new IMatchUpdateListener<ReachableAccess.Match>() {
			@Override
			public void notifyAppearance(ReachableAccess.Match match) {
				Set<TemporalContext> scenario = match.getScenario();
				Set<String> temporalContextNames = scenario.stream().map(x -> x.getName()).collect(Collectors.toSet());
				System.out.println("[ADD ReachableAccess Match] " + match.getUser().getName()
						+ " - " + match.getZone().getName() + " during " + temporalContextNames);
			}

			@Override
			public void notifyDisappearance(ReachableAccess.Match match) {
				Set<TemporalContext> scenario = match.getScenario();
				Set<String> temporalContextNames = scenario.stream().map(x -> x.getName()).collect(Collectors.toSet());
				System.out.println("[REMOVE ReachableAccess Match] " + match.getUser().getName()
						+ " - " + match.getZone().getName() + " during " + temporalContextNames);
			}
		};
	}
	
	public static IMatchUpdateListener<UnreachableAccess.Match> getUnreachableAccessUpdateListener() {
		return new IMatchUpdateListener<UnreachableAccess.Match>() {
			@Override
			public void notifyAppearance(UnreachableAccess.Match match) {
				Set<TemporalContext> scenario = match.getScenario();
				Set<String> temporalContextNames = scenario.stream().map(x -> x.getName()).collect(Collectors.toSet());
				System.out.println("[ADD UnreachableAccess Match] " + match.getUser().getName()
						+ " - " + match.getZone().getName() + " during " + temporalContextNames);
			}

			@Override
			public void notifyDisappearance(UnreachableAccess.Match match) {
				Set<TemporalContext> scenario = match.getScenario();
				Set<String> temporalContextNames = scenario.stream().map(x -> x.getName()).collect(Collectors.toSet());
				System.out.println("[REMOVE UnreachableAccess Match] " + match.getUser().getName()
						+ " - " + match.getZone().getName() + " during " + temporalContextNames);
			}
		};
	}
//
//	public static IMatchUpdateListener<EnabledPriority.Match> getEnabledPriorityUpdateListener() {
//		return new IMatchUpdateListener<EnabledPriority.Match>() {
//			@Override
//			public void notifyAppearance(EnabledPriority.Match match) {
//				Set<TimeRangeGroup> groups = (Set<TimeRangeGroup>) match.getGroups();
//				Set<String> groupNames = groups.stream().map(x -> x.getName()).collect(Collectors.toSet());
//				String roleName = match.getRole().getName();
//				String demarcationName = match.getDemarcation().getName();
//				System.out.println("[ADD EnabledPriority Match]" + groupNames + " -> " + roleName + "-" + demarcationName);
//			}
//
//			@Override
//			public void notifyDisappearance(EnabledPriority.Match match) {
//				Set<TimeRangeGroup> groups = (Set<TimeRangeGroup>) match.getGroups();
//				Set<String> groupNames = groups.stream().map(x -> x.getName()).collect(Collectors.toSet());
//				String roleName = match.getRole().getName();
//				String demarcationName = match.getDemarcation().getName();
//				System.out.println("[ADD EnabledPriority Match]" + groupNames + " -> " + roleName + "-" + demarcationName);
//			}
//		};
//	}
//
//	public static IMatchUpdateListener<DisabledPriority.Match> getDisabledPriorityUpdateListener() {
//		return new IMatchUpdateListener<DisabledPriority.Match>() {
//			@Override
//			public void notifyAppearance(DisabledPriority.Match match) {
//				Set<TimeRangeGroup> groups = (Set<TimeRangeGroup>) match.getGroups();
//				Set<String> groupNames = groups.stream().map(x -> x.getName()).collect(Collectors.toSet());
//				String roleName = match.getRole().getName();
//				String demarcationName = match.getDemarcation().getName();
//				System.out.println("[ADD DisabledPriority Match]" + groupNames + " -> " + roleName + "-" + demarcationName);
//			}
//
//			@Override
//			public void notifyDisappearance(DisabledPriority.Match match) {
//				Set<TimeRangeGroup> groups = (Set<TimeRangeGroup>) match.getGroups();
//				Set<String> groupNames = groups.stream().map(x -> x.getName()).collect(Collectors.toSet());
//				String roleName = match.getRole().getName();
//				String demarcationName = match.getDemarcation().getName();
//				System.out.println("[ADD DisabledPriority Match]" + groupNames + " -> " + roleName + "-" + demarcationName);
//			}
//		};
//	}

	public static IMatchUpdateListener<SoDURPattern.Match> getSoDURPatternUpdateListener() {
		return new IMatchUpdateListener<SoDURPattern.Match>() {
			@Override
			public void notifyAppearance(SoDURPattern.Match match) {
				System.out.println("[ADD SoDURPattern Match] user " + match.getUser().getName() + " violates constraint " + match.getConstraint().getName());
			}

			@Override
			public void notifyDisappearance(SoDURPattern.Match match) {
				System.out.println("[REMOVE SoDURPattern Match] user " + match.getUser().getName() + " violates constraint " + match.getConstraint().getName());
			}
		};
	}

	public static IMatchUpdateListener<SoDUDPattern.Match> getSoDUDPatternUpdateListener() {
		return new IMatchUpdateListener<SoDUDPattern.Match>() {
			@Override
			public void notifyAppearance(SoDUDPattern.Match match) {
				System.out.println("[ADD SoDUDPattern Match] user " + match.getUser().getName() + " violates constraint " + match.getConstraint().getName());
			}

			@Override
			public void notifyDisappearance(SoDUDPattern.Match match) {
				System.out.println("[REMOVE SoDUDPattern Match] user " + match.getUser().getName() + " violates constraint " + match.getConstraint().getName());
			}
		};
	}

	public static IMatchUpdateListener<SoDUPPattern.Match> getSoDUPPatternUpdateListener() {
		return new IMatchUpdateListener<SoDUPPattern.Match>() {
			@Override
			public void notifyAppearance(SoDUPPattern.Match match) {
				System.out.println("[ADD SoDUPPattern Match] user " + match.getUser().getName() + " violates constraint " + match.getConstraint().getName());
			}

			@Override
			public void notifyDisappearance(SoDUPPattern.Match match) {
				System.out.println("[REMOVE SoDUPPattern Match] user " + match.getUser().getName() + " violates constraint " + match.getConstraint().getName());
			}
		};
	}

	public static IMatchUpdateListener<SoDRDPattern.Match> getSoDRDPatternUpdateListener() {
		return new IMatchUpdateListener<SoDRDPattern.Match>() {
			@Override
			public void notifyAppearance(SoDRDPattern.Match match) {
				System.out.println("[ADD SoDRDPattern Match] role " + match.getRole().getName() + " violates constraint " + match.getConstraint().getName());
			}

			@Override
			public void notifyDisappearance(SoDRDPattern.Match match) {
				System.out.println("[REMOVE SoDRDPattern Match] role " + match.getRole().getName() + " violates constraint " + match.getConstraint().getName());
			}
		};
	}

	public static IMatchUpdateListener<SoDRPPattern.Match> getSoDRPPatternUpdateListener() {
		return new IMatchUpdateListener<SoDRPPattern.Match>() {
			@Override
			public void notifyAppearance(SoDRPPattern.Match match) {
				System.out.println("[ADD SoDRPPattern Match] role " + match.getRole().getName() + " violates constraint " + match.getConstraint().getName());
			}

			@Override
			public void notifyDisappearance(SoDRPPattern.Match match) {
				System.out.println("[REMOVE SoDRPPattern Match] role " + match.getRole().getName() + " violates constraint " + match.getConstraint().getName());
			}
		};
	}

	public static IMatchUpdateListener<SoDDPPattern.Match> getSoDDPPatternUpdateListener() {
		return new IMatchUpdateListener<SoDDPPattern.Match>() {
			@Override
			public void notifyAppearance(SoDDPPattern.Match match) {
				System.out.println("[ADD SoDDPPattern Match] demarcation " + match.getDemarcation().getName() + " violates constraint " + match.getConstraint().getName());
			}

			@Override
			public void notifyDisappearance(SoDDPPattern.Match match) {
				System.out.println("[REMOVE SoDDPPattern Match] demarcation " + match.getDemarcation().getName() + " violates constraint " + match.getConstraint().getName());
			}
		};
	}

	public static IMatchUpdateListener<PrerequisteURPattern.Match> getPrerequisiteURPatternUpdateListener() {
		return new IMatchUpdateListener<PrerequisteURPattern.Match>() {
			@Override
			public void notifyAppearance(PrerequisteURPattern.Match match) {
				System.out.println("[ADD PrerequisiteURPattern Match] user " + match.getUser().getName() + " violates constraint " + match.getConstraint().getName());
			}

			@Override
			public void notifyDisappearance(PrerequisteURPattern.Match match) {
				System.out.println("[REMOVE PrerequisiteURPattern Match] user " + match.getUser().getName() + " violates constraint " + match.getConstraint().getName());
			}
		};
	}

	public static IMatchUpdateListener<PrerequisiteUDPattern.Match> getPrerequisiteUDPatternUpdateListener() {
		return new IMatchUpdateListener<PrerequisiteUDPattern.Match>() {
			@Override
			public void notifyAppearance(PrerequisiteUDPattern.Match match) {
				System.out.println("[ADD PrerequisiteUDPattern Match] user " + match.getUser().getName() + " violates constraint " + match.getConstraint().getName());
			}

			@Override
			public void notifyDisappearance(PrerequisiteUDPattern.Match match) {
				System.out.println("[REMOVE PrerequisiteUDPattern Match] user " + match.getUser().getName() + " violates constraint " + match.getConstraint().getName());
			}
		};
	}

	public static IMatchUpdateListener<PrerequisiteUPPattern.Match> getPrerequisiteUPPatternUpdateListener() {
		return new IMatchUpdateListener<PrerequisiteUPPattern.Match>() {
			@Override
			public void notifyAppearance(PrerequisiteUPPattern.Match match) {
				System.out.println("[ADD PrerequisiteUPPattern Match] user " + match.getUser().getName() + " violates constraint " + match.getConstraint().getName());
			}

			@Override
			public void notifyDisappearance(PrerequisiteUPPattern.Match match) {
				System.out.println("[REMOVE PrerequisiteUPPattern Match] user " + match.getUser().getName() + " violates constraint " + match.getConstraint().getName());
			}
		};
	}

	public static IMatchUpdateListener<PrerequisiteRDPattern.Match> getPrerequisiteRDPatternUpdateListener() {
		return new IMatchUpdateListener<PrerequisiteRDPattern.Match>() {
			@Override
			public void notifyAppearance(PrerequisiteRDPattern.Match match) {
				System.out.println("[ADD PrerequisiteRDPattern Match] role " + match.getRole().getName() + " violates constraint " + match.getConstraint().getName());
			}

			@Override
			public void notifyDisappearance(PrerequisiteRDPattern.Match match) {
				System.out.println("[REMOVE PrerequisiteRDPattern Match] role " + match.getRole().getName() + " violates constraint " + match.getConstraint().getName());
			}
		};
	}

	public static IMatchUpdateListener<PrerequisiteRPPattern.Match> getPrerequisiteRPPatternUpdateListener() {
		return new IMatchUpdateListener<PrerequisiteRPPattern.Match>() {
			@Override
			public void notifyAppearance(PrerequisiteRPPattern.Match match) {
				System.out.println("[ADD PrerequisiteRPPattern Match] role " + match.getRole().getName() + " violates constraint " + match.getConstraint().getName());
			}

			@Override
			public void notifyDisappearance(PrerequisiteRPPattern.Match match) {
				System.out.println("[REMOVE PrerequisiteRPPattern Match] role " + match.getRole().getName() + " violates constraint " + match.getConstraint().getName());
			}
		};
	}

	public static IMatchUpdateListener<PrerequisiteDPPattern.Match> getPrerequisiteDPPatternUpdateListener() {
		return new IMatchUpdateListener<PrerequisiteDPPattern.Match>() {
			@Override
			public void notifyAppearance(PrerequisiteDPPattern.Match match) {
				System.out.println("[ADD PrerequisiteDPPattern Match] demarcation " + match.getDemarcation().getName() + " violates constraint " + match.getConstraint().getName());
			}

			@Override
			public void notifyDisappearance(PrerequisiteDPPattern.Match match) {
				System.out.println("[REMOVE PrerequisiteDPPattern Match] demarcation " + match.getDemarcation().getName() + " violates constraint " + match.getConstraint().getName());
			}
		};
	}


}
