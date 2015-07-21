package com.duitang.service.karma.stats;

import java.util.List;
import java.util.Map;

public class KarmaMetricHolder {
    private static MetricReporterDaemon daemon = new MetricReporterDaemon();
    private static MetricHolder metricHolder = new MetricHolder();

    public static List<Map> getLatestMetric() {
        return metricHolder.holding;
    }

    public static synchronized KarmaMetricHolder enable() {
        daemon.start();
        return new KarmaMetricHolder();
    }

    public static KarmaMetricHolder enableKafkaReporter() {
        daemon.addReporter(new KafkaReporter());
        return new KarmaMetricHolder();
    }

    public static KarmaMetricHolder enableHolderReporter() {
        daemon.addReporter(metricHolder.reporter());
        return new KarmaMetricHolder();
    }

    public static KarmaMetricHolder setReportInterval(int second) {
        daemon.reportInterval(second);
        return new KarmaMetricHolder();
    }

    public static KarmaMetricHolder addReporter(Reporter r) {
        daemon.addReporter(r);
        return new KarmaMetricHolder();
    }
}
