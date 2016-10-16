/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.demo.http;

import java.util.concurrent.CountDownLatch;

import org.slf4j.LoggerFactory;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.karma.boot.ServerBootstrap;
import com.duitang.service.karma.trace.NoopTraceVisitor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public class HTTPServerTester {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		log();
		MemoryCacheService mms = new MemoryCacheService();
		mms.memory_setString("aaaa", "bbbb", 5000);
		System.out.println("aaaa ---> " + mms.memory_getString("aaaa"));

		ServerBootstrap server = new ServerBootstrap();
		server.addService(IDemoService.class, mms);

		server.startUp(9999);

		com.duitang.service.karma.http.Finder.enableHTTPService(8888);
		com.duitang.service.karma.trace.Finder.enableConsole(true);
		com.duitang.service.karma.trace.Finder.enableZipkin(null, "http://192.168.10.216:9411");
		new CountDownLatch(0).await();
	}

	static void log() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		Logger logger = (Logger) LoggerFactory.getLogger(NoopTraceVisitor.class);
		logger.setLevel(Level.DEBUG);
	}

}
