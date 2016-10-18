/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.trace.zipkin;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageBuilder;
import org.graylog2.gelfclient.GelfMessageLevel;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;
import org.slf4j.MDC;
import org.slf4j.event.Level;

import com.duitang.service.karma.trace.TraceCell;
import com.duitang.service.karma.trace.TraceCellVisitor;
import com.duitang.service.karma.trace.TraceStone;
import com.duitang.service.karma.trace.TracerLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public class UDPGELFLogger implements TracerLogger {

	static ObjectMapper mapper = new ObjectMapper();
	static TraceCell2Map toMap = new TraceCell2Map();

	protected GelfTransport transport;
	protected GelfConfiguration config;
	protected TraceCellVisitor visitor;
	protected GelfMessageLevel level = GelfMessageLevel.INFO;

	/**
	 *
	 *
	 * @param host
	 *            graylog host. such as 192.168.0.1. support udp default.
	 * @param port
	 *            graylog port.
	 */
	public UDPGELFLogger(String host, int port) {
		this(host, port, new TraceCell2Map());
	}

	public UDPGELFLogger(String host, int port, TraceCellVisitor<Map> visitor) {
		initGELFLogger(host, port);
		this.visitor = visitor;
	}

	private void initGELFLogger(String host, int port) {
		config = new GelfConfiguration(new InetSocketAddress(host, port)).transport(GelfTransports.UDP).queueSize(5120)
				.connectTimeout(5000).reconnectDelay(1000).tcpNoDelay(true).sendBufferSize(327680);

		transport = GelfTransports.create(config);
	}

	public void setLevel(Level lv) {
		if (Level.DEBUG == lv) {
			level = GelfMessageLevel.DEBUG;
		}
		if (Level.INFO == lv) {
			level = GelfMessageLevel.INFO;
		}
		if (Level.WARN == lv) {
			level = GelfMessageLevel.WARNING;
		}
		if (Level.ERROR == lv) {
			level = GelfMessageLevel.ERROR;
		}
	}

	public void log(String msg, TraceCell tc) {
		log(msg, visitor, tc);
	}

	@Override
	public void log(String text, TraceCellVisitor<Map> visitor, TraceCell tc) {
		String source = tc.host;

		Map content = null;
		if (visitor == null) { // 如果不传visitor,即采用默认的方式,直接整个对象转成json.
			content = toMap.transform(tc);
		} else {
			content = visitor.transform(tc);
		}

		if (content == null) {
			return;
		}

		GelfMessageBuilder builder = new GelfMessageBuilder("", source).level(level);

		builder.additionalFields(content);
		if (tc instanceof TraceStone) {
			Map p = new HashMap<String, Object>(((TraceStone) tc).props);
			content.putAll(p);
		}
		content.remove("props");
		Map<String, String> mm = MDC.getMDCAdapter().getCopyOfContextMap();
		if (mm != null){
			content.putAll(mm);			
		}

		GelfMessage msg = builder.message(text).timestamp(System.currentTimeMillis()).additionalFields(content).build();
		// async mode
		transport.trySend(msg);

	}

}

class TraceCell2Map implements TraceCellVisitor<Map> {

	@Override
	public Map transform(TraceCell src) {
		Iterator<Map> it = transform(Arrays.asList(src)).iterator();
		return it.hasNext() ? it.next() : null;
	}

	@Override
	public List<Map> transform(List<TraceCell> src) {
		List<Map> ret = new ArrayList<Map>();
		for (TraceCell c : src) {
			if (!c.sampled) {
				continue;
			}
			ret.add(UDPGELFLogger.mapper.convertValue(c, Map.class));
		}
		return ret;
	}

}
