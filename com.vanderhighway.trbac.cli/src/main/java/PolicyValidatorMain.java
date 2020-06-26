/*******************************************************************************
 * Copyright (c) 2010-2015, Zoltan Ujhelyi, Gabor Szarnyas
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

import com.brein.time.timeintervals.intervals.IntegerInterval;
import com.google.common.reflect.TypeToken;
import com.vanderhighway.trbac.core.modifier.PolicyAutomaticModifier;
import com.vanderhighway.trbac.core.modifier.PolicyModifier;
import com.vanderhighway.trbac.core.validator.PolicyValidator;
import com.vanderhighway.trbac.model.trbac.model.*;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.viatra.query.runtime.api.AdvancedViatraQueryEngine;
import org.eclipse.viatra.query.runtime.api.IQuerySpecification;
import org.eclipse.viatra.query.runtime.emf.EMFScope;
import org.eclipse.viatra.transformation.evm.specific.scheduler.UpdateCompleteBasedScheduler;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.IModelManipulations;
import org.eclipse.viatra.transformation.runtime.emf.modelmanipulation.ModelManipulationException;
import org.eclipse.viatra.transformation.runtime.emf.rules.batch.BatchTransformationRule;
import org.eclipse.viatra.transformation.runtime.emf.rules.batch.BatchTransformationRuleFactory;
import org.eclipse.viatra.transformation.runtime.emf.transformation.batch.BatchTransformation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

public class PolicyValidatorMain {
	public static final Type test = new TypeToken<Set<Integer>>(){}.getType();
	public static final String val0 = "A";

	public static void main(String[] args) throws IOException, InvocationTargetException, ModelManipulationException {

		//Debug output
		//BasicConfigurator.configure();

		System.out.println("Policy Validator Started!");
		System.out.print("Initialize model scope and preparing engine... ");

		// Initializing the EMF package
		TRBACPackage.eINSTANCE.getName();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("trbac", new XMIResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getContentTypeToFactoryMap().put("*", new XMIResourceFactoryImpl());

		LocalDateTime now = LocalDateTime.now();

		ResourceSet set = new ResourceSetImpl();
		//URI uri = URI.createFileURI("./dummy_policy.trbac"); //Uncomment if running on Eclipse
		URI uri = URI.createFileURI("dummy_policy.trbac"); //Intellij
		Resource resource = set.getResource(uri, true);

		final AdvancedViatraQueryEngine engine = AdvancedViatraQueryEngine.createUnmanagedEngine(new EMFScope(set));


		PolicyModifier modifier = new PolicyModifier(engine, (Policy) resource.getContents().get(0), resource);
		PolicyValidator validator = new PolicyValidator(engine);
		validator.addChangeListeners(engine);

		PolicyAutomaticModifier automaticModifier = new PolicyAutomaticModifier(engine, modifier);

		automaticModifier.initialize();
		automaticModifier.execute();

		DaySchedule monday = (DaySchedule) resource.getEObject("Monday");
		TimeRangeGroup group = modifier.addTimeRangeGroup("DummyTimeRangeGroup");

		engine.delayUpdatePropagation(() -> modifier.addTimeRange(group, monday, "time_range_1", new IntegerInterval(0,10)));
		engine.delayUpdatePropagation(() -> modifier.addTimeRange(group, monday, "time_range_2", new IntegerInterval(20,30)));
		engine.delayUpdatePropagation(() -> modifier.addTimeRange(group, monday, "time_range_3", new IntegerInterval(40,50)));

		TimeRange timeRange3 = (TimeRange) resource.getEObject("time_range_3");
		engine.delayUpdatePropagation(() -> modifier.updateTimeRange(timeRange3, new IntegerInterval(42,42)));
		engine.delayUpdatePropagation(() -> { modifier.removeTimeRange(timeRange3); return null; });

// 		This also doesn't trigger all event driven transformations!
//		modifier.addTimeRange(group, monday, "time_range_1", new IntegerInterval(0,10));
//		modifier.addTimeRange(group, monday, "time_range_2", new IntegerInterval(20,30))
//		modifier.addTimeRange(group, monday, "time_range_3", new IntegerInterval(30,40));
//
//		modifier.updateTimeRange(timeRange3, new IntegerInterval(42,42));
//		modifier.removeTimeRange(timeRange3);

		//modifier.dispose();

		System.out.println("Done!");
	}
}
