/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
package com.duitang.service.karma.trace;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.duitang.service.karma.support.IPUtils;
import com.duitang.service.karma.trace.zipkin.ZipkinReporterImpl;

import zipkin.reporter.Sender;
import zipkin.reporter.kafka08.KafkaSender;
import zipkin.reporter.libthrift.LibthriftSender;
import zipkin.reporter.libthrift.LibthriftSender.Builder;
import zipkin.reporter.okhttp3.OkHttpSender;

/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
public class ReporterSender {

	final static public BlockingQueue<List<TraceCell>> items = new LinkedBlockingQueue<>();
	static protected Thread reporterDaemon = new Thread() {

		@Override
		public void run() {
			while (true) {
				try {
					List<TraceCell> item = items.take();
					for (TracerReporter s : senders) {
						if (s != null) {
							s.report(item);
						}
					}
				} catch (InterruptedException e) {
					//
				}
			}
		}

	};

	static public boolean useConsole = false;
	static public ConsoleReporter console = new ConsoleReporter();
	static protected Set<TracerReporter> senders;

	static {
		senders = new HashSet<TracerReporter>();
		reporterDaemon.setDaemon(true);
		reporterDaemon.start();
	}

	static public TracerReporter addZipkinSender(String url) throws URISyntaxException {
		Sender sender = null;
		if (url == null || url.toLowerCase().equals("console")) {
			return console;
		}
		if (url.startsWith("kafka://")) {
			sender = KafkaSender.create(url.substring("kafka://".length()));
		}
		if (url.startsWith("thrift://")) {
			String u = url.substring("thrift://".length());
			Builder bd = LibthriftSender.builder().host(IPUtils.getHost(u));
			Integer port = IPUtils.getPort(u);
			if (port != null) {
				bd.port(port);
			}
			sender = bd.build();
		}
		if (url.startsWith("http://")) {
			String path = "/api/v1/spans";
			sender = OkHttpSender.builder().compressionEnabled(true).endpoint(url + path).build();
		}
		if (sender == null) {
			String only = "( kafka:// | thrift:// | console )";
			throw new RuntimeException("not valid: " + url + " ; please using prefix => " + only);
		}

		ZipkinReporterImpl ret = new ZipkinReporterImpl(sender);
		senders.add(ret);
		return ret;
	}

	public static void commitReports(List<TraceCell> item) {
		ReporterSender.items.add(item);
	}

}
