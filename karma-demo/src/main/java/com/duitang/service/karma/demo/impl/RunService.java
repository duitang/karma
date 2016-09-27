/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.demo.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.LoggerFactory;

import com.duitang.service.karma.client.KarmaClient;
import com.duitang.service.karma.handler.ReflectRPCHandler;
import com.duitang.service.karma.router.JavaRouter;
import com.duitang.service.karma.server.ServiceConfig;
import com.duitang.service.karma.server.TCPServer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public class RunService {

	static final public Map<Class, Integer> ports;
	static {
		ports = new HashMap<>();
		int base = 11220;
		ports.put(A.class, base++);
		ports.put(B.class, base++);
		ports.put(C.class, base++);
		ports.put(D.class, base++);
		ports.put(E.class, base++);
		ports.put(F.class, base++);
		ports.put(G.class, base++);
	}

	/**
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 * @throws InstantiationException
	 */
	public static void main(String[] args) throws Throwable {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		String name = args[0];
		Class svc = Class.forName("com.duitang.service.karma.demo.impl." + name);
		Object obj = svc.newInstance();
		ServiceConfig conf = new ServiceConfig();
		conf.addService(svc, obj);
		TCPServer tcps = new TCPServer();

		ReflectRPCHandler rpc = new ReflectRPCHandler();
		rpc.setConf(conf);
		rpc.init();

		JavaRouter rt = new JavaRouter();
		rt.setHandler(rpc);

		tcps.setRouter(rt);
		tcps.setPort(ports.get(svc));
		tcps.start();

		System.out.println("started for: " + args[0]);
		new CountDownLatch(0).await();
	}

	static void initService(Class svc, Object obj) throws Throwable {
		Field[] fields = svc.getClass().getDeclaredFields();
		for (Field f : fields) {
			Class t = f.getType();
			int port = ports.get(t);
			KarmaClient cli = KarmaClient.createKarmaClient(t, Arrays.asList("localhost:" + port), "dev1");
			f.setAccessible(true);
			f.set(obj, cli.getService());
		}
	}

}
