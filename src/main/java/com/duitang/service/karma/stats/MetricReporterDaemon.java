package com.duitang.service.karma.stats;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.base.MetricCenter;

public class MetricReporterDaemon {
    private final static Logger logger = LoggerFactory.getLogger(MetricReporterDaemon.class);
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private int interval = 5; // sec
    private List<Reporter> reporters = new ArrayList<>();

    public MetricReporterDaemon() {
    }

    public synchronized MetricReporterDaemon addReporter(Reporter reporter) {
        reporters.add(reporter);
        return this;
    }

    public MetricReporterDaemon interval(int second) {
        this.interval = second;
        return this;
    }

    public synchronized void start() {
        executor.prestartAllCoreThreads();
        executor.scheduleAtFixedRate(REPORT, interval, interval, TimeUnit.SECONDS);
    }

    public synchronized void stop() {
        executor.shutdown();
    }

    private Runnable REPORT = new Runnable() {
        @Override
        public void run() {
            List<Map> sample = MetricCenter.sample();
            for (Reporter reporter : reporters) {
                try {
                    reporter.report(sample);
                } catch (Exception e) {
                    logger.error("report_error", e);
                }
            }
        }
    };
}
