package com.vanderhighway.trbac.core.modifier;

import com.brein.time.timeintervals.intervals.IntegerInterval;

import com.vanderhighway.trbac.core.validator.PolicyValidator;
import com.vanderhighway.trbac.model.trbac.model.*;
import com.vanderhighway.trbac.patterns.TimeRangeP;

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

    public PolicyAutomaticModifier(AdvancedViatraQueryEngine engine, PolicyModifier policyModifier) {
        this.engine = engine;
        this.policyModifier = policyModifier;
    }

    public void initialize() {
        this.logger.info("Preparing transformation rules.");
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

    private EventDrivenTransformationRule<TimeRangeP.Match, TimeRangeP.Matcher> ProcessRanges() {
        EventDrivenTransformationRule<TimeRangeP.Match, TimeRangeP.Matcher> dayrangerule =
                this._eventDrivenTransformationRuleFactory.createRule(TimeRangeP.instance()).action(
                        CRUDActivationStateEnum.CREATED, (TimeRangeP.Match it) -> {
                            try {
                                System.out.println("TRIGGERED EventDrivenTransformationRule TimeRangeP CREATED:" + it.prettyPrint());
                                DayScheduleTimeRange range = this.policyModifier.addDayScheduleTimeRange(it.getDaySchedule(),
                                        it.getTimeRange().getName(), new IntegerInterval(it.getStarttime(), it.getEndtime()));
                                this.policyModifier.manipulation.addTo(it.getTimeRange(), ePackage.getTimeRange_DayScheduleTimeRanges(), range);
                            } catch (ModelManipulationException e) {
                                e.printStackTrace();
                            }
                        }).action(
                        CRUDActivationStateEnum.UPDATED, (TimeRangeP.Match it) -> {
                            try {
                                System.out.println("TRIGGERED EventDrivenTransformationRule TimeRangeP UPDATED:" + it.prettyPrint());
                                this.policyModifier.updateDayScheduleTimeRange(it.getTimeRange().getDayScheduleTimeRanges().get(0), //In this example, it's always just one!
                                        new IntegerInterval(it.getStarttime(), it.getEndtime()));
                            } catch (ModelManipulationException e) {
                                e.printStackTrace();
                            }
                        }).action(
                        CRUDActivationStateEnum.DELETED, (TimeRangeP.Match it) -> {
                            System.out.println("TRIGGERED EventDrivenTransformationRule TimeRangeP DELETED:" + it.prettyPrint());
                            try {
                                DayScheduleTimeRange dayScheduleTimeRange = it.getTimeRange().getDayScheduleTimeRanges().get(0); //In this example, it's always just one!
                                this.policyModifier.removeDayScheduleTimeRange(dayScheduleTimeRange);
                            } catch (ModelManipulationException e) {
                                e.printStackTrace();
                            }
                        }
                ).addLifeCycle(Lifecycles.getDefault(true, true))
                        .name("process-timeranges").build();
        return dayrangerule;
    }

    public EventDrivenTransformation getTransformation() {
        return transformation;
    }

}
