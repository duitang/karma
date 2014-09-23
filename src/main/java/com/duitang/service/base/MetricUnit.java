package com.duitang.service.base;

import java.util.HashMap;
import java.util.Map;

import org.HdrHistogram.Histogram;
import org.LatencyUtils.LatencyStats;

public class MetricUnit {

	public String clientId;
	public String name;
	public String group;
	protected LatencyStats stats;
	protected Histogram histo;

	public MetricUnit(String clientId, String name, String group) {
		this.clientId = clientId;
		this.name = name;
		this.group = group;
		stats = new LatencyStats();
		histo = stats.getIntervalHistogram();
	}

	synchronized public Map sample() {
		Map ret = new HashMap();
		stats.forceIntervalSample();
		stats.getIntervalHistogramInto(histo);
		ret.put("from", histo.getStartTimeStamp());
		ret.put("to", histo.getEndTimeStamp());
		ret.put("total", histo.getTotalCount());
		ret.put("mean", histo.getMean());
		ret.put("max", histo.getMaxValue());
		ret.put("min", histo.getMinValue());
		ret.put("stddev", histo.getStdDeviation());
		try {
			ret.put("p50", histo.getValueAtPercentile(50));
			ret.put("p60", histo.getValueAtPercentile(60));
			ret.put("p70", histo.getValueAtPercentile(70));
			ret.put("p80", histo.getValueAtPercentile(80));
			ret.put("p90", histo.getValueAtPercentile(90));
			ret.put("p95", histo.getValueAtPercentile(95));
			ret.put("p96", histo.getValueAtPercentile(96));
			ret.put("p97", histo.getValueAtPercentile(97));
			ret.put("p98", histo.getValueAtPercentile(98));
			ret.put("p99", histo.getValueAtPercentile(99));
		} catch (Exception e) {
		}
		return ret;
	}

	public void metric(long latency) {
		stats.recordLatency(latency);
	}

}
