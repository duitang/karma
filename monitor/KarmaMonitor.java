package com.sun.btrace.samples;

import static com.sun.btrace.BTraceUtils.*;

import com.sun.btrace.AnyType;
import com.sun.btrace.aggregation.Aggregation;
import com.sun.btrace.aggregation.AggregationFunction;
import com.sun.btrace.aggregation.AggregationKey;
import com.sun.btrace.annotations.*;

@BTrace
public class KarmaMonitor {

	private static Aggregation histogram = Aggregations.newAggregation(AggregationFunction.QUANTIZE);

	private static Aggregation average = Aggregations.newAggregation(AggregationFunction.AVERAGE);

	private static Aggregation max = Aggregations.newAggregation(AggregationFunction.MAXIMUM);

	private static Aggregation min = Aggregations.newAggregation(AggregationFunction.MINIMUM);

	private static Aggregation sum = Aggregations.newAggregation(AggregationFunction.SUM);

	private static Aggregation count = Aggregations.newAggregation(AggregationFunction.COUNT);

	private static Aggregation globalCount = Aggregations.newAggregation(AggregationFunction.COUNT);

	@OnMethod(clazz = "+com.duitang.service.l2.L2Service", method = "cat_getstring", location = @Location(Kind.RETURN))
	public static void ret_cat_getstring(@Duration long durationL) {
		AggregationKey key = Aggregations.newAggregationKey("cat_getstring");
		long duration = (long) durationL;
		Aggregations.addToAggregation(histogram, key, duration);
		Aggregations.addToAggregation(average, key, duration);
		Aggregations.addToAggregation(max, key, duration);
		Aggregations.addToAggregation(min, key, duration);
		// Aggregations.addToAggregation(sum, key, duration);
		Aggregations.addToAggregation(count, key, duration);
		// Aggregations.addToAggregation(globalCount, duration);
	}

	@OnMethod(clazz = "/com\\.sun\\.proxy\\..*/", method = "cat_getstring", location = @Location(Kind.RETURN))
	public static void m(@Duration long durationL, @ProbeClassName String probeClass,
	        @ProbeMethodName String probeMethod) {
		AggregationKey key = Aggregations.newAggregationKey("cat_getstring");
		long duration = (long) durationL;
		Aggregations.addToAggregation(histogram, key, duration);
		Aggregations.addToAggregation(average, key, duration);
		Aggregations.addToAggregation(max, key, duration);
		Aggregations.addToAggregation(min, key, duration);
		// Aggregations.addToAggregation(sum, key, duration);
		Aggregations.addToAggregation(count, key, duration);
		// Aggregations.addToAggregation(globalCount, duration);
	}

	@OnTimer(5000)
	public static void onEvent() {
		// Top 10 queries only
		Aggregations.truncateAggregation(histogram, 10);

		println("---------------------------------------------");
		Aggregations.printAggregation("Count", count);
		Aggregations.printAggregation("Min", min);
		Aggregations.printAggregation("Max", max);
		Aggregations.printAggregation("Average", average);
		// Aggregations.printAggregation("Sum", sum);
		Aggregations.printAggregation("Histogram", histogram);
		// Aggregations.printAggregation("Global Count", globalCount);
		println("---------------------------------------------");
	}

}
