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
import com.duitang.service.karma.demo.ServiceA;
import com.duitang.service.karma.demo.ServiceB;
import com.duitang.service.karma.demo.ServiceC;
import com.duitang.service.karma.demo.ServiceD;
import com.duitang.service.karma.demo.ServiceE;
import com.duitang.service.karma.demo.ServiceF;
import com.duitang.service.karma.demo.ServiceG;
import com.duitang.service.karma.handler.ReflectRPCHandler;
import com.duitang.service.karma.router.JavaRouter;
import com.duitang.service.karma.server.ServiceConfig;
import com.duitang.service.karma.server.TCPServer;
import com.duitang.service.karma.trace.Finder;

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
		ports.put(ServiceA.class, base++);
		ports.put(ServiceB.class, base++);
		ports.put(ServiceC.class, base++);
		ports.put(ServiceD.class, base++);
		ports.put(ServiceE.class, base++);
		ports.put(ServiceF.class, base++);
		ports.put(ServiceG.class, base++);
	}

	/**
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws Throwable
	 * @throws InstantiationException
	 */
	public static void main(String[] args) throws Throwable {
		Finder.enableConsole(true);
		Finder.enableZipkin(null, "http://192.168.1.180:9411");
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		String name = args[0];
		Class svc = Class.forName("com.duitang.service.karma.demo.impl." + name);
		Class iface = svc.getInterfaces()[0];
		Object obj = svc.newInstance();
		ServiceConfig conf = new ServiceConfig();
		conf.addService(iface, obj);
		initService(iface, obj);
		TCPServer tcps = new TCPServer();

		ReflectRPCHandler rpc = new ReflectRPCHandler();
		rpc.setConf(conf);
		rpc.init();

		JavaRouter rt = new JavaRouter();
		rt.setHandler(rpc);

		tcps.setRouter(rt);
		tcps.setPort(ports.get(iface));
		tcps.start();

		System.out.println("started for: " + args[0]);
		new CountDownLatch(0).await();
	}

	static void initService(Class svc, Object obj) throws Throwable {
		Field[] fields = obj.getClass().getDeclaredFields();
		for (Field f : fields) {
			Class t = f.getType();
			if (!t.getSimpleName().startsWith("Service")) {
				return;
			}
			int port = ports.get(t);
			KarmaClient cli = KarmaClient.createKarmaClient(t, Arrays.asList("localhost:" + port), "dev1");
			f.setAccessible(true);
			f.set(obj, cli.getService());
		}
	}

}
