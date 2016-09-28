package com.duitang.service.karma.trace.zipkin;

import com.duitang.service.karma.trace.ReporterFactory;
import com.duitang.service.karma.trace.TraceCell;
import com.duitang.service.karma.trace.TracerLogger;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by water on 9/28/16.
 */
public class UDPGELFLoggerTest {

	@Test
	public void testLog() throws Exception {
		TracerLogger logger = ReporterFactory.createLogger("61.152.115.82", 30011);
		logger.log(new TraceCell(false, "", 1));
	}
}