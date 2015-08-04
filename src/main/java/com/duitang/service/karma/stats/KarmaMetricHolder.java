package com.duitang.service.karma.stats;

import java.util.List;
import java.util.Map;

public class KarmaMetricHolder {
    private static MetricReporterDaemon daemon = new MetricReporterDaemon();
    private static MetricHolder metricHolder = new MetricHolder();

    public static List<Map> getLatestMetric() {
        return metricHolder.holding;
    }

    public static void enable() {
        daemon.start();
    }

    public static void enableKafkaReporter() {
        daemon.addReporter(new KafkaReporter());
    }

    public static void enableHolderReporter() {
        daemon.addReporter(metricHolder.reporter());
    }

    public static void enableDWMetricReporter() {
        addCustomReporter(new DWMetricReporter());
    }

    public static void setReportInterval(int second) {
        daemon.reportInterval(second);
    }

    public static void addReporter(Reporter r) {
        daemon.addReporter(r);
    }

    public static void addCustomReporter(CustomDataReporter r) {
        daemon.addReporter(r);
    }
}
