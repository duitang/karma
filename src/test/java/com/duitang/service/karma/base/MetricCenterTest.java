package com.duitang.service.karma.base;

import org.HdrHistogram.Histogram;
import org.LatencyUtils.LatencyStats;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MetricCenterTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// @Test
	public void test() throws Exception {
		LatencyStats stat = new LatencyStats();
		Histogram hist = stat.getIntervalHistogram();

		// Get the histograms (without allocating new ones):
		// stat.getIntervalHistogramInto(hist);

		for (int jjj = 0; jjj < 2; jjj++) {

			// stat.forceIntervalSample();

			for (int i = 0; i < 500; i++) {
				long tt = (long) (Math.random() * 100000000);
				stat.recordLatency(tt);
				Thread.sleep(10);
			}

			stat.forceIntervalSample();
			stat.getIntervalHistogramInto(hist);

			// hist.outputPercentileDistribution(System.out, 1000000D);

			System.out.println(hist.getStartTimeStamp());
			System.out.println(hist.getEndTimeStamp());
			System.out.println(hist.getTotalCount());
			System.out.println(hist.getMean());
			System.out.println(hist.getMaxValue());
			System.out.println(hist.getMinValue());
			System.out.println(hist.getStdDeviation());
			System.out.println(hist.getValueAtPercentile(10));

		}

	}

	@Test
	public void test2() {
//		MetricCenter.initMetric(DemoService.class, clientid);
		System.out.println(MetricCenter.genClientIdFromCode());
	}

}
