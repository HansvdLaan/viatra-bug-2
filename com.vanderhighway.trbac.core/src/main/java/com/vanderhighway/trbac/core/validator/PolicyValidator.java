package com.vanderhighway.trbac.core.validator;

import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.vanderhighway.trbac.patterns.*;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.xtext.xbase.lib.Extension;

import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class PolicyValidator {


    @Extension
    private Logger logger = Logger.getLogger(PolicyValidator.class);

    protected AdvancedViatraQueryEngine engine;

    private IntervalTree tree;
    Map<IntegerInterval, EObject> intervalmap;

    public PolicyValidator(final AdvancedViatraQueryEngine engine) {
        this.engine = engine;
    }

    //TODO: move this to somewhere else!
    public void addChangeListeners(AdvancedViatraQueryEngine engine, boolean fireNow) {
        //fireNow = true ; //parameter means all current matches are sent to the listener

        // Add some other Listeners
        //engine.addMatchUpdateListener(UserShouldHaveARole.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getUserShouldHaveARoleUpdateListener(), true);
        //engine.addMatchUpdateListener(RoleShouldHaveADemarcation.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getRoleShouldHaveADemarcationUpdateListener(), true);
        //engine.addMatchUpdateListener(DemarcationShouldHaveAPermission.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getDemarcationShouldHaveAPermissionUpdateListener(), true);

        //engine.addMatchUpdateListener(OnlyOneDirector.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getOnlyOneDirectorUpdateListeer(), true);
        //engine.addMatchUpdateListener(OnlyOneRnDManager.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getOnlyOneRnDManagerUpdateListener(), true);
        //engine.addMatchUpdateListener(OnlyOneOperationsManager.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getOnlyOneOperationsManagerUpdateListener(), true);

        //engine.addMatchUpdateListener(SoDEmployeeAndContractor.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getSoDEmployeeAndContractorUpdateListener(), true);
        //engine.addMatchUpdateListener(SoDEmployeeAndVisitor.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getSoDEmployeeAndVisitorUpdateListener(), true);

        //engine.addMatchUpdateListener(PrerequisiteEverybodyHasAccessToLobby.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getPrerequisiteEveryHasAccessToLobbyUpdateListener(), true);
        //engine.addMatchUpdateListener(PrerequisiteVaultImpliesOpenOffice.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getPrerequisiteVaultImpliesOpenOfficeUpdateListener(), true);

        // Add Role Name Listener
        //engine.addMatchUpdateListener(RoleName.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getRoleNameMatchUpdateListener(), true);

        //engine.addMatchUpdateListener(MissingInheritedDemarcation.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getMissingInheritedDemarcationUpdateListener(), true);
        //engine.addMatchUpdateListener(InheritedDemarcation.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getInheritedDemarcationUpdateListener(), true);

        // Add Access Relation Listener
        //engine.addMatchUpdateListener(AllJuniors.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getAllJuniorsUpdateListener(), true);
        //engine.addMatchUpdateListener(AccessRelation.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getAccessRelationUpdateListener(), true);
        //engine.addMatchUpdateListener(AccessRelation2.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getAccessRelation2UpdateListener(), true);
//        engine.addMatchUpdateListener(DayRange.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getDayRangeUpdateListener(), true);
//        engine.addMatchUpdateListener(NonOverlappingDayRange.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getNonOverlappingDayRangeUpdateListener(), true);
        //engine.addMatchUpdateListener(RangeP.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getRangeUpdateListener(), true);
        //engine.addMatchUpdateListener(ScheduleRangeP.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getScheduleRangeUpdateListener(), true);
        //engine.addMatchUpdateListener(TimeRange.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getTimeRangeUpdateListener(), true);
        //engine.addMatchUpdateListener(SetTestQuery.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getSetTestQueryUpdateListener(), true);
        //engine.addMatchUpdateListener(TimeRangeGroupsDistinct.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getTimeRangeGroupDistinctUpdateListener(), true);
        //engine.addMatchUpdateListener(DistinctGroups.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getDistinctGroupsUpdateListener(), true);

        //engine.addMatchUpdateListener(AllJuniors.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getAllJuniorsUpdateListener(), true);
        //engine.addMatchUpdateListener(TimeRangeGroupCollection.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getTimeRangeGroupCombinationsUpdateListener(), true);
        //engine.addMatchUpdateListener(TimeRangeGroupCollectionHasGroup.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getTimeRangeGroupCollectionHasGroupUpdateListener(), true);
        //engine.addMatchUpdateListener(TimeRangeGroupCollectionEnabled.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getTimeRangeGroupCollectionEnabledUpdateListener(), true);

        //engine.addMatchUpdateListener(EnabledPriority.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getEnabledPriorityUpdateListener(), true);
        //engine.addMatchUpdateListener(DisabledPriority.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getDisabledPriorityUpdateListener(), true);
        //engine.addMatchUpdateListener(TimeRangeGroupCollectionEnabled.Matcher.on(engine), ListenerFactory.getTimeRangeGroupCollectionEnabledUpdateListener(), true);
        engine.addMatchUpdateListener(AccessRelation.Matcher.on(engine), ListenerFactory.getAccessRelationUpdateListener(), fireNow);
        //engine.addMatchUpdateListener(AllJuniors.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getAllJuniorsUpdateListener(), true);

        engine.addMatchUpdateListener(Reachable.Matcher.on(engine), ListenerFactory.getReachableUpdateListener(), fireNow);
        engine.addMatchUpdateListener(ReachableAccess.Matcher.on(engine), ListenerFactory.getReachableAccessUpdateListener(), fireNow);
        engine.addMatchUpdateListener(UnreachableAccess.Matcher.on(engine), ListenerFactory.getUnreachableAccessUpdateListener(), fireNow);

        engine.addMatchUpdateListener(Scenarios.Matcher.on(engine), com.vanderhighway.trbac.core.validator.ListenerFactory.getScenarioUpdateListener(), fireNow);
    }

}
