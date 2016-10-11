/**
 * @author laurence
 * @since 2016年10月11日
 *
 */
package com.duitang.service.karma.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author laurence
 * @since 2016年10月11日
 *
 */
public class DeamonJobs {

	static protected ExecutorService jobs;
	static protected ConcurrentHashMap<String, Runnable> cached;

	static class DaemonFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			Thread ret = new Thread(r);
			ret.setDaemon(true);
			ret.setName(pickName(r));
			return ret;
		}

	}

	static {
		jobs = Executors.newCachedThreadPool(new DaemonFactory());
		cached = new ConcurrentHashMap<String, Runnable>();
	}

	static String pickName(Runnable r) {
		return "Karma-Job-" + r.getClass().getName();
	}

	static public void runJob(Runnable r) {
		if (r == null) {
			return;
		}
		String name = pickName(r);
		cached.put(name, r);
		jobs.submit(r);
	}

	static public void shutdown() {
		jobs.shutdown();
	}

	static public void reset() {
		for (Runnable r : cached.values()) {
			jobs.submit(r);
		}
	}

}
