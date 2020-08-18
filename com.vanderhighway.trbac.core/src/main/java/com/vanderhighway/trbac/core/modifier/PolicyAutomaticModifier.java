package com.vanderhighway.trbac.core.modifier;

import com.brein.time.timeintervals.collections.ListIntervalCollection;
import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder;
import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.google.common.base.Objects;

import com.vanderhighway.trbac.core.validator.PolicyValidator;
import com.vanderhighway.trbac.model.trbac.model.*;
import com.vanderhighway.trbac.patterns.RangeP;

import org.apache.log4j.Logger;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.viatra.transformation.evm.specific.Lifecycles;
import org.eclipse.viatra.transformation.evm.specific.crud.CRUDActivationStateEnum;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.IModelManipulations;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.SimpleModelManipulations;
import org.eclipse.viatra.transformation.runtime.emf.rules.eventdriven.EventDrivenTransformationRule;
import org.eclipse.viatra.transformation.runtime.emf.rules.eventdriven.EventDrivenTransformationRuleFactory;
import org.eclipse.viatra.transformation.runtime.emf.transformation.eventdriven.EventDrivenTransformation;
import org.eclipse.xtext.xbase.lib.Extension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class PolicyAutomaticModifier {

    @Extension
    private Logger logger = Logger.getLogger(PolicyValidator.class);

    @Extension
    private IModelManipulations manipulation;

    @Extension
    private TRBACPackage ePackage = TRBACPackage.eINSTANCE;

    @Extension //Transformation-related extensions
    private EventDrivenTransformation transformation;

    @Extension //Transformation rule-related extensions
    private EventDrivenTransformationRuleFactory _eventDrivenTransformationRuleFactory = new EventDrivenTransformationRuleFactory();

    private AdvancedViatraQueryEngine engine;
    private PolicyModifier policyModifier;
    Map<String,IntervalTree> trees;

    public PolicyAutomaticModifier(AdvancedViatraQueryEngine engine, PolicyModifier policyModifier, SecurityPolicy policy) {
        this.engine = engine;
        this.policyModifier = policyModifier;
        this.trees = new HashMap<>();

        if(policy.getAuthorizationPolicy().getSchedule() != null) {
            for (DaySchedule daySchedule : policy.getAuthorizationPolicy().getSchedule().getDaySchedules()) {
                IntervalTree tree = IntervalTreeBuilder.newBuilder()
                        .usePredefinedType(IntervalTreeBuilder.IntervalType.LONG)
                        .collectIntervals(interval -> new ListIntervalCollection())
                        .build();
                tree.add(new IntegerInterval(0, 1439));
                this.trees.put(daySchedule.getName(), tree);
            }
        }
    }

    public void initialize() {
        this.logger.info("Preparing transformation rules.");
        //this.policyModifier.execute(com.vanderhighway.trbac.core.modifier.IntervalUtil.addAllAlwaysRanges(policyModifier, this.tree));
        this.transformation = createTransformation();
        this.logger.info("Prepared transformation rules");
    }

    public void execute() {
        this.logger.debug("Executing transformations");
        this.transformation.getExecutionSchema().startUnscheduledExecution();
    }

    private EventDrivenTransformation createTransformation() {
        EventDrivenTransformation transformation = null;
        this.manipulation = new SimpleModelManipulations(this.engine);
        transformation = EventDrivenTransformation.forEngine(this.engine)
                .addRule(this.ProcessRanges())
                .build();
        return transformation;
    }

    public void dispose() {
        if (!Objects.equal(this.transformation, null)) {
            this.transformation.dispose();
        }
        this.transformation = null;
        return;
    }

//    public class DoAddScheduleRange implements Callable<Void> {
//        private final com.vanderhighway.trbac.core.modifier.PolicyModifier modifier;
//        private final Range.Match it;
//
//        public DoAddScheduleRange(com.vanderhighway.trbac.core.modifier.PolicyModifier modifier, Range.Match it) {
//            this.modifier = modifier;
//            this.it = it;
//        }
//
//        public Void call() throws Exception {
//            String key = String.valueOf("copy-" + it.getRange().toString());
//            this.modifier.execute(this.modifier.addScheduleRange(it.getRange().getDayschedule(),
//                    key, it.getStarttime(), it.getEndtime()));
//            return null;
//        }
//    }

    public class DoProcessAddRange implements Callable<Void> {
        private final PolicyModifier modifier;
        private final RangeP.Match it;

        public DoProcessAddRange(PolicyModifier modifier, RangeP.Match it) {
            this.modifier = modifier;
            this.it = it;
        }

        public Void call() throws Exception {
            IntervalTree tree = trees.get(it.getInstance().getDaySchedule().getName());
            IntervalUtil.processAddRange(modifier, tree, it);
            return null;
        }
    }

    public class DoProcessRemoveRange implements Callable<Void> {
        private final PolicyModifier modifier;
        private final RangeP.Match it;

        public DoProcessRemoveRange(PolicyModifier modifier, RangeP.Match it) {
            this.modifier = modifier;
            this.it = it;
        }

        public Void call() throws Exception {
            IntervalTree tree = trees.get(it.getInstance().getDaySchedule().getName());
            IntervalUtil.processRemoveRange(modifier, tree, it);
            return null;
        }
    }

    public static class DoDummy implements Callable<Void> {

        public DoDummy() {
        }

        public Void call() throws Exception {
            //System.out.println("The dummy speaks!");
            return null;
        }
    }



    private EventDrivenTransformationRule<RangeP.Match, RangeP.Matcher> ProcessRanges() {
        final Consumer<RangeP.Match> function = (RangeP.Match it) -> {
            System.out.println("hey!");
        };
        EventDrivenTransformationRule<RangeP.Match, RangeP.Matcher> dayrangerule =
                this._eventDrivenTransformationRuleFactory.createRule(RangeP.instance()).action(
                        CRUDActivationStateEnum.CREATED, (RangeP.Match it) -> {
                            //System.out.println("DayRangeMatch CREATED:" + it.toString());
                            try {
                                IntervalTree tree = trees.get(it.getInstance().getDaySchedule().getName());

                                //Check if the match hasn't been processed before, e.g. in
                                // the case of the always day schedule time ranges.
                                if(it.getInstance().getDayScheduleTimeRanges().size() == 0) {
                                    IntervalUtil.processAddRange(this.policyModifier, tree, it);
                                }
                            } catch (ModelManipulationException e) {
                                e.printStackTrace();
                            }
                        }).action(
                        CRUDActivationStateEnum.UPDATED, (RangeP.Match it) -> {
                            //System.out.println("DayRangeMatch UPDATED:" + it.toString());
                        }).action(
                        CRUDActivationStateEnum.DELETED, (RangeP.Match it) -> {
                            try {
                                IntervalTree tree = trees.get(it.getInstance().getDaySchedule().getName());
                                IntervalUtil.processRemoveRange(this.policyModifier, tree, it);
                            } catch (ModelManipulationException e) {
                                e.printStackTrace();
                            }
                        }
                            ).addLifeCycle(Lifecycles.getDefault(false, true))
                        .name("process-day-ranges").build();
        return dayrangerule;
    }

//    private EventDrivenTransformationRule<RoleName.Match, RoleName.Matcher> DummyProcess() {
//        final Consumer<RoleName.Match> function = (RoleName.Match it) -> {
//            System.out.println("hey!");
//        };
//        EventDrivenTransformationRule<RoleName.Match, RoleName.Matcher> test =
//                this._eventDrivenTransformationRuleFactory.createRule(RoleName.instance()).action(
//                        CRUDActivationStateEnum.CREATED, (RoleName.Match it) -> {
//                            try {
//                                engine.delayUpdatePropagation(
//                                        new DoDummy()
//                                );
//                            } catch (InvocationTargetException e) {
//                                e.printStackTrace();
//                            }
//                        }).action(
//                        CRUDActivationStateEnum.UPDATED, (RoleName.Match it) -> {
//                            System.out.println("RoleName UPDATED:" + it.toString());}).action(
//                        CRUDActivationStateEnum.DELETED, (RoleName.Match it) -> {
//                            System.out.println("RoleName DELETED:" + it.toString());}
//                ).addLifeCycle(Lifecycles.getDefault(true, true))
//                        .name("process-day-ranges").build();
//        return test;
//    }

//    private EventDrivenTransformationRule<DayRange.Match, DayRange.Matcher> ProcessDayRanges() {
//        final Consumer<DayRange.Match> function = (DayRange.Match it) -> {
//            System.out.println("hey!");
//            final Consumer<Policy.Match> function2 = (Policy.Match it2) -> {
//                try {
                    //EObject entity = this.manipulation.createChild(it2.getPolicy(), ePackage.getPolicy_Roles(), ePackage.getRole());
                    //this.manipulation.set(entity, ePackage.getRole_Name(), it.getRange().getName() + " copy");
        //}

//        final EventDrivenTransformationRule<DayRange.Match, DayRange.Matcher> exampleRule =
//                this._eventDrivenTransformationRuleFactory.createRule(DayRange.instance()).action(
//                        CRUDActivationStateEnum.CREATED,  (DayRange.Match it) -> {
//                        }).action(
//                        CRUDActivationStateEnum.UPDATED, (DayRange.Match it) -> {
//                        }).action(
//                        CRUDActivationStateEnum.DELETED, (DayRange.Match it) -> {
//                        }).addLifeCycle(Lifecycles.getDefault(true, true)).build();
//        return exampleRule;
//    }


    public EventDrivenTransformation getTransformation() {
        return transformation;
    }
}
