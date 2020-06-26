package com.vanderhighway.trbac.core.validator;

import com.vanderhighway.trbac.model.trbac.model.TimeRange;
import com.vanderhighway.trbac.model.trbac.model.TimeRangeGroup;
import com.vanderhighway.trbac.patterns.*;
import com.vanderhighway.trbac.patterns.TimeRangeP;
import org.eclipse.viatra.query.runtime.api.IMatchUpdateListener;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ListenerFactory {


	public static IMatchUpdateListener<TimeRangeP.Match> getTimeRangeUpdateListener() {
		return new IMatchUpdateListener<TimeRangeP.Match>() {
			@Override
			public void notifyAppearance(TimeRangeP.Match match) {
				System.out.printf("[ADD TimeRangeP Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(TimeRangeP.Match match) {
				System.out.printf("[REM TimeRangeP Match] %s %n", match.prettyPrint());

			}
		};
	}
	
	public static IMatchUpdateListener<DayScheduleTimeRangeP.Match> getDayScheduleTimeRangeUpdateListener() {
		return new IMatchUpdateListener<DayScheduleTimeRangeP.Match>() {
			@Override
			public void notifyAppearance(DayScheduleTimeRangeP.Match match) {
				System.out.printf("[ADD DayScheduleTimeRangeP Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(DayScheduleTimeRangeP.Match match) {
				System.out.printf("[REM DayScheduleTimeRangeP Match] %s %n", match.prettyPrint());

			}
		};
	}
}