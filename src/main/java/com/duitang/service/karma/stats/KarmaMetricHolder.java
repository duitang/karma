package com.duitang.service.karma.stats;

public class KarmaMetricHolder {
    private static MetricReporterDaemon daemon = new MetricReporterDaemon();
    private static MetricHolder metricHolder = new MetricHolder();

    public static MetricHolder getMetricHolder() {
        return metricHolder;
    }

    public static MetricReporterDaemon getReporterDaemon() {
        return daemon;
    }
}
