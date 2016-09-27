package com.duitang.service.karma.trace;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
public class NoopTraceVisitor implements TraceVisitor {

	static ObjectMapper mapper = new ObjectMapper();
	Logger logger = LoggerFactory.getLogger(NoopTraceVisitor.class);

	@Override
	public void visit(TraceCell tc) {
		// NOOP
		if (logger.isDebugEnabled()) {
			try {
				logger.debug(mapper.writeValueAsString(tc));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void visits(List<TraceCell> tcs) {
		// NOOP
		if (logger.isDebugEnabled()) {
			for (TraceCell tc : tcs) {
				try {
					logger.debug(mapper.writeValueAsString(tc));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
