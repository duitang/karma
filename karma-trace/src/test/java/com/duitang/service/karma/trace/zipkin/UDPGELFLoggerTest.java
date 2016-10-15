package com.duitang.service.karma.trace.zipkin;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Date;

import org.junit.Test;
import org.slf4j.MDC;

import com.duitang.service.karma.trace.AlwaysSampled;
import com.duitang.service.karma.trace.ReporterFactory;
import com.duitang.service.karma.trace.TraceBlock;
import com.duitang.service.karma.trace.TraceContextHolder;
import com.duitang.service.karma.trace.TracerLogger;

/**
 * Created by water on 9/28/16.
 */
public class UDPGELFLoggerTest {

	@Test
	public void testLog() throws Exception {
		TraceContextHolder.setSampler(new AlwaysSampled());

		TracerLogger logger = ReporterFactory.createGELFUDPLogger("61.152.115.82", 30011);
		TraceBlock ts = new TraceBlock();
		ts.tc.host = "cwj_home3";
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

		String jvmName = runtimeBean.getName();
		System.out.println("JVM Name = " + jvmName);
		long pid = Long.valueOf(jvmName.split("@")[0]);
		System.out.println("JVM PID  = " + pid);

		ThreadMXBean bean = ManagementFactory.getThreadMXBean();

		int peakThreadCount = bean.getPeakThreadCount();
		System.out.println("Peak Thread Count = " + peakThreadCount);

		MDC.put("PID", String.valueOf(pid));
		MDC.put("JVM", jvmName);
		MDC.put("Peak Thread", String.valueOf(peakThreadCount));
		System.out.println(MDC.getCopyOfContextMap());
		Thread.sleep(100);
		ts.close();
		logger.log(new Date() + " finished in cwj_home3", ts.tc);

		Thread.sleep(10000);
	}

}