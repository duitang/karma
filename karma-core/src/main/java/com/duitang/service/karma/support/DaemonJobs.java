/**
 * @author laurence
 * @since 2016年10月11日
 *
 */
package com.duitang.service.karma.support;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author laurence
 * @since 2016年10月11日
 *
 */
public class DaemonJobs {

	static protected ExecutorService jobs;
	final static protected ConcurrentLinkedQueue<Runnable> cached;

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
		cached = new ConcurrentLinkedQueue<Runnable>();
	}

	static String pickName(Runnable r) {
		return "Karma-Job-" + r.getClass().getName();
	}

	static public void runJob(Runnable r) {
		if (r == null) {
			return;
		}
		cached.add(r);
		jobs.submit(r);
	}

	static public void shutdown() {
		if (!jobs.isShutdown()) {
			jobs.shutdown();
		}
	}

	static public void reset() {
		jobs = Executors.newCachedThreadPool(new DaemonFactory());
		for (Runnable r : cached) {
			jobs.submit(r);
		}
	}

	private DaemonJobs() {
		// disable
	}

}
