package com.duitang.service.karma.stats;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.base.MetricCenter;

public class MetricReporterDaemon {
    private final static Logger logger = LoggerFactory.getLogger(MetricReporterDaemon.class);
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private int interval = 5; // sec
    private volatile ImmutableList<Reporter> reporters = ImmutableList.of();
    private volatile ImmutableList<CustomDataReporter> cdReporters = ImmutableList.of();

    private int state = INIT;
    private static final int INIT = 0;
    private static final int STARTED = 1;
    private static final int STOPPED = 2;

    // package private
    MetricReporterDaemon() {
    }

    public synchronized MetricReporterDaemon addReporter(Reporter reporter) {
        reporters = ImmutableList.<Reporter>builder().addAll(reporters).add(reporter).build();
        return this;
    }

    public synchronized MetricReporterDaemon addReporter(CustomDataReporter reporter) {
        cdReporters = ImmutableList.<CustomDataReporter>builder().addAll(cdReporters).add(reporter).build();
        return this;
    }

    public MetricReporterDaemon reportInterval(int second) {
        this.interval = second;
        return this;
    }

    public synchronized void start() {
        if (state == INIT) {
            executor.prestartAllCoreThreads();
            executor.scheduleAtFixedRate(REPORT, interval, interval, TimeUnit.SECONDS);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    stop();
                }
            }));
            state = STARTED;
        }
    }

    public synchronized void stop() {
        if (state == STARTED) {
            executor.shutdown();
            state = STOPPED;
        }
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

            for (CustomDataReporter r : cdReporters) {
                try {
                    r.report();
                } catch (Exception e) {
                    logger.error("report_error", e);
                }
            }
        }
    };

}
