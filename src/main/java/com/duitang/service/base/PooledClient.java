package com.duitang.service.base;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;

public class PooledClient<T> {

	final static int DEFAULT_CAPACITY = 100;
	final static int DEFAULT_IDLE = Double.valueOf(DEFAULT_CAPACITY * 0.7).intValue();
	final static int MIN_IDLE = Double.valueOf(DEFAULT_CAPACITY * 0.1).intValue();

	final static Logger err = Logger.getLogger("error");
	protected GenericObjectPool<T> pool;
	protected WrapperServiceFactory fac;
	protected int capacity = DEFAULT_CAPACITY;
	protected int idle = DEFAULT_IDLE;
	protected Meter qps;
	protected Histogram dur;
	protected ConcurrentHashMap<T, Long> workTs;

	class WrapperServiceFactory extends BasePooledObjectFactory<T> {

		protected ServiceFactory<T> fac;

		WrapperServiceFactory(ServiceFactory<T> fac) {
			this.fac = fac;
		}

		@Override
		public T create() throws Exception {
			return fac.create();
		}

		@Override
		public PooledObject<T> wrap(T obj) {
			return new DefaultPooledObject<T>(obj);
		}

		@Override
		public void destroyObject(PooledObject<T> p) throws Exception {
			T cli = p.getObject();
			super.destroyObject(p);
			fac.release(cli);
		}

		public String getServiceName() {
			return fac.getServiceName();
		}

		public Class getServiceType() {
			return fac.getServiceType();
		}

	}

	public PooledClient(ServiceFactory fac) {
		this(fac, DEFAULT_CAPACITY, DEFAULT_IDLE);
	}

	public PooledClient(ServiceFactory fac, int capacity, int idle) {
		this.fac = new WrapperServiceFactory(fac);
		this.capacity = capacity;
		this.idle = idle;
		init();
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getIdle() {
		return idle;
	}

	public void setIdle(int idle) {
		this.idle = idle;
	}

	@SuppressWarnings("static-access")
	protected void init() {
		GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
		cfg.setMaxTotal(capacity);
		cfg.setMaxIdle(idle);
		cfg.setMinIdle(MIN_IDLE);
		pool = new GenericObjectPool<T>(fac, cfg);
		AbandonedConfig abandoncfg = new AbandonedConfig();
		abandoncfg.setRemoveAbandonedTimeout(5 * 60); // 5 minute
		// remove abandoned background
		abandoncfg.setRemoveAbandonedOnMaintenance(true);
		pool.setAbandonedConfig(abandoncfg);
		qps = MetricCenter.metrics.meter(MetricCenter.metrics.name(fac.getServiceName(), "qps"));
		dur = MetricCenter.metrics.histogram(fac.getServiceName() + ":" + "response_time");
		workTs = new ConcurrentHashMap<T, Long>();
		MetricCenter.initMetric(fac.getServiceType());
	}

	public void close() {
		pool.close();
	}

	public T getClient() {
		// try 1 time and 30000ms
		return getClient(1000, 1);
	}

	public T getClient(long wtime, int count) {
		try {
			T ret = null;
			for (int i = 0; i < count; i++) {
				ret = pool.borrowObject(wtime);
				if (ret != null) {
					workTs.put(ret, System.currentTimeMillis());
					break;
				}
			}
			qps.mark();
			return ret;
		} catch (Exception e) {
			err.error("get client", e);
			return null;
		}
	}

	public void retClient(T cli) {
		Long ts = null;
		if (cli != null) {
			ts = workTs.remove(cli);
			pool.returnObject(cli);
		}
		if (ts != null) {
			ts = System.currentTimeMillis() - ts;
			dur.update(ts);
		}
	}

	public void releaseClient(T cli) {
		try {
			if (cli != null) {
				Long ts = null;
				ts = workTs.remove(cli);
				pool.invalidateObject(cli);
				if (ts != null) {
					ts = System.currentTimeMillis() - ts;
					dur.update(ts);
				}
			}
		} catch (Exception e) {
			err.error("release client", e);
		}
	}

}
