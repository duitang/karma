package com.duitang.service.karma.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.base.MetricCenter;

public class MetricReporterDaemon {
    private final static Logger logger = LoggerFactory.getLogger(MetricReporterDaemon.class);
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private int interval = 5; // sec
    private List<Reporter> reporters = new ArrayList<>();

    private int state = INIT;
    private static final int INIT = 0;
    private static final int STARTED = 1;
    private static final int STOPPED = 2;

    // package private
    MetricReporterDaemon() {
    }

    synchronized MetricReporterDaemon addReporter(Reporter reporter) {
        reporters.add(reporter);
        return this;
    }

    MetricReporterDaemon reportInterval(int second) {
        this.interval = second;
        return this;
    }

    synchronized void start() {
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

    synchronized void stop() {
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
        }
    };

}
