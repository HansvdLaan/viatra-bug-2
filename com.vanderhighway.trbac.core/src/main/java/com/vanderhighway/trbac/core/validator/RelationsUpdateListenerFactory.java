package com.vanderhighway.trbac.core.validator;

import com.vanderhighway.trbac.model.trbac.model.TemporalContext;
import com.vanderhighway.trbac.patterns.*;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.viatra.query.runtime.api.IMatchUpdateListener;

import java.util.Set;
import java.util.stream.Collectors;

public class RelationsUpdateListenerFactory {

	public static void addALlUpdateListeners(AdvancedViatraQueryEngine engine, boolean fireNow) {
		engine.addMatchUpdateListener(USP.Matcher.on(engine), RelationsUpdateListenerFactory.getAccessRelationUpdateListener(), fireNow);
		engine.addMatchUpdateListener(Scenarios.Matcher.on(engine), RelationsUpdateListenerFactory.getScenarioUpdateListener(), fireNow);
		engine.addMatchUpdateListener(RSD.Matcher.on(engine), RelationsUpdateListenerFactory.getRDSUpdateListener(), fireNow);
	}

	public static IMatchUpdateListener<USP.Match> getAccessRelationUpdateListener() {
		return new IMatchUpdateListener<USP.Match>() {
			@Override
			public void notifyAppearance(USP.Match match) {
				String userName = match.getUser().getName();
				String permissionName = match.getPermission().getName();
				System.out.println("[ADD AccessRelation Match] " + userName + " has permission " + permissionName + " during " + match.getScenario().toString());
			}

			@Override
			public void notifyDisappearance(USP.Match match) {
				String userName = match.getUser().getName();
				String permissionName = match.getPermission().getName();
				System.out.println("[ADD AccessRelation Match] " + userName + " has permission " + permissionName + " during " + match.getScenario().toString());
			}
		};
	}

//	public static IMatchUpdateListener<TimeRangeP.Match> getRangeUpdateListener() {
//		return new IMatchUpdateListener<TimeRangeP.Match>() {
//			@Override
//			public void notifyAppearance(TimeRangeP.Match match) {
//				//System.out.printf("[ADD Range Match] %s %n", match.prettyPrint());
//			}
//
//			@Override
//			public void notifyDisappearance(TimeRangeP.Match match) {
//				//System.out.printf("[REM Range Match] %s %n", match.prettyPrint());
//
//			}
//		};
//	}

	public static IMatchUpdateListener<Scenarios.Match> getScenarioUpdateListener() {
		return new IMatchUpdateListener<Scenarios.Match>() {
			@Override
			public void notifyAppearance(Scenarios.Match match) {
				System.out.println("[ADD Scenarios Match] " + match.getScenario().toString());
			}

			@Override
			public void notifyDisappearance(Scenarios.Match match) {
				System.out.println("[REM Scenarios Match] " + match.getScenario().toString());
			}
		};
	}

	public static IMatchUpdateListener<RSD.Match> getRDSUpdateListener() {
		return new IMatchUpdateListener<RSD.Match>() {
			@Override
			public void notifyAppearance(RSD.Match match) {
				String roleName = match.getRole().getName();
				String demarcationName = match.getDemarcation().getName();
				System.out.println("[ADD RSD Match] " + match.getScenario().toString() + " -> " + roleName + "-" + demarcationName);
			}

			@Override
			public void notifyDisappearance(RSD.Match match) {
				String roleName = match.getRole().getName();
				String demarcationName = match.getDemarcation().getName();
				System.out.println("[REM RSD Match]" + match.getScenario().toString() + " -> " + roleName + "-" + demarcationName);
			}
		};
	}

	public static IMatchUpdateListener<Reachable.Match> getReachableUpdateListener() {
		return new IMatchUpdateListener<Reachable.Match>() {
			@Override
			public void notifyAppearance(Reachable.Match match) {
				System.out.println("[ADD Reachable Match] " + match.getZone().getName());
			}

			@Override
			public void notifyDisappearance(Reachable.Match match) {
				System.out.println("[REM Reachable Match]" + match.getZone().getName());
			}
		};
	}

	public static IMatchUpdateListener<SecurityZoneAccessible.Match> getSecurityZoneAccessibleUpdateListener() {
		return new IMatchUpdateListener<SecurityZoneAccessible.Match>() {
			@Override
			public void notifyAppearance(SecurityZoneAccessible.Match match) {
				System.out.println("[ADD SecurityZoneAccessible Match] " + match.getUser().getName()
						+ " - " + match.getZone().getName() + " during " + match.getScenario().toString());
			}

			@Override
			public void notifyDisappearance(SecurityZoneAccessible.Match match) {
				System.out.println("[REMOVE SecurityZoneAccessible Match] " + match.getUser().getName()
						+ " - " + match.getZone().getName() + " during " + match.getScenario().toString());
			}
		};
	}
}
