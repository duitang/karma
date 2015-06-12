package com.duitang.service.karma.client;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClusterZKRouter implements IOBalance {

	final static Logger err = LoggerFactory.getLogger(ClusterZKRouter.class);
	final static float lower = 0.7f;
	final static float upper = 1f + (1f - lower);
	final static String KARMA_CLUSTER_LOAD_NAME = "/trivial/rpc_lb/cobweb";
	final static ObjectMapper mapper = new ObjectMapper();

	final static Object tinyLock = new Object();
	static CuratorFramework zkUtil = null;
	static PathChildrenCache cfgCache = null;
	static String zkUrl = null;
	static Thread helper = new Thread(new HelperDaemon());

	static {
		helper.setDaemon(true);
		helper.start();
	}

	final static AtomicBoolean needReset = new AtomicBoolean(false);
	final static ConcurrentHashMap<String, ClusterZKRouter> groupRouter = new ConcurrentHashMap<String, ClusterZKRouter>();
	final static ConcurrentHashMap<String, Map<String, Integer>> defaultGroupLoads = new ConcurrentHashMap<String, Map<String, Integer>>();
	final static Semaphore helpLock = new Semaphore(0);

	protected String groupName;
	protected NavigableMap<Float, String> iidMap;
	protected Random rnd;

	static class HelperDaemon implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					helpLock.acquire();
				} catch (InterruptedException e) {
				}
				synchronized (tinyLock) {
					if (zkUtil == null) {
						RetryPolicy retryPolicy = new ExponentialBackoffRetry(500, 3, 2000);
						zkUtil = CuratorFrameworkFactory.newClient(zkUrl, retryPolicy);
						EnsurePath ep = new EnsurePath(KARMA_CLUSTER_LOAD_NAME);
						zkUtil.start();
						try {
							// zkUtil.checkExists().forPath(KARMA_CLUSTER_LOAD_NAME);
							ep.ensure(zkUtil.getZookeeperClient());
						} catch (Exception e1) {
						}
						cfgCache = new PathChildrenCache(zkUtil, KARMA_CLUSTER_LOAD_NAME, true);
						cfgCache.getListenable().addListener(new LBReceiver());
						try {
							cfgCache.start();
						} catch (Exception e) {
							err.error("add load listener error: ", e);
						}
						tryTouch();
					}
				}
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
				}
			}
		}

	}

	static public void enableZK(String url) {
		if (zkUtil == null) {
			zkUrl = url;
			helpLock.release();
		}
	}

	static public void disableZK(String url) {
		synchronized (tinyLock) {
			if (cfgCache != null) {
				try {
					cfgCache.close();
					cfgCache = null;
				} catch (IOException e) {
				}
			}
			if (zkUtil != null) {
				zkUtil.close();
				zkUtil = null;
			}
		}
	}

	static void tryTouch() {
		try {
			List<String> lst = ZKPaths.getSortedChildren(zkUtil.getZookeeperClient().getZooKeeper(), KARMA_CLUSTER_LOAD_NAME);
			for (String p : lst) {
				try {
					String fp = KARMA_CLUSTER_LOAD_NAME + "/" + p;
					String group = ZKPaths.getNodeFromPath(fp);
					HashMap load = mapper.readValue(zkUtil.getData().forPath(fp), HashMap.class);
					if (!load.isEmpty()) {
						defaultGroupLoads.put(group, load);
					}
				} catch (Exception e) {
					// ignore
					err.error("initialization group loads: ", e);
				}
			}
		} catch (Exception e) {
			err.error("try fetch from ZK for initialization: ", e);
		}
	}

	static Map<String, Integer> toLoadList(byte[] src) {
		try {
			HashMap lst = mapper.readValue(src, HashMap.class);
			Set<Entry> es = lst.entrySet();
			Map<String, Integer> ret = new HashMap<String, Integer>();
			for (Entry en : es) {
				ret.put(en.getKey().toString(), Integer.valueOf(en.getValue().toString()));
			}
			return ret;
		} catch (Exception e) {
			err.error("deserialization load list json: ", e);
			return Collections.EMPTY_MAP;
		}
	}

	static class LBReceiver implements PathChildrenCacheListener {

		@Override
		public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
			ChildData data = event.getData();
			String group = StringUtils.substringAfterLast(data.getPath(), "/");
			Map<String, Integer> loads = toLoadList(data.getData());

			if (loads.isEmpty()) {
				return;
			}

			ClusterZKRouter router = groupRouter.get(group);
			if (router != null) {
				router.updateLoad(loads);
			}

			defaultGroupLoads.put(group, loads);
		}

	}

	private ClusterZKRouter(String group, Map<String, Integer> load) {
		this.groupName = group;
		updateLoad(load);
		this.rnd = new Random();
	}

	static public Map<String, Integer> fairLoad(List<String> url) {
		Map<String, Integer> ret = new HashMap<String, Integer>();
		for (String s : url) {
			ret.put(s, 1);
		}
		return ret;
	}

	static public boolean setReset() {
	    return needReset.compareAndSet(false, true);
	}
	
	/**
	 * 在重新创建ClientFactory的时候调用
	 */
	static public void reset(String group, Map<String, Integer> load) {
	    if (needReset.get()) {
	        groupRouter.put(group, new ClusterZKRouter(group, load));
	        new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    needReset.compareAndSet(true, false); 
                }
            }, 6000);
	    }
	}
	
	static public ClusterZKRouter createRouter(String group, Map<String, Integer> load) {
		ClusterZKRouter ret = groupRouter.get(group);
		if (ret == null) {
			// try using load found from ZK
			//这里会有问题，所以注释掉
//			if (defaultGroupLoads.containsKey(group)) {
//				load = defaultGroupLoads.get(group);
//			}
			ret = new ClusterZKRouter(group, load);
			groupRouter.putIfAbsent(group, ret);
			ret = groupRouter.get(group);
		}
		return ret;
	}

	/**
	 * <pre>
	 * 
	 * A     B          C          D                      E    
	 * -------------------------------------------------------------->
	 * 0    0.1        0.3        0.5                    0.8
	 * </pre>
	 */
	@Override
	public String next(String token) {
		return iidMap.floorEntry(rnd.nextFloat()).getValue();
	}

	@Override
	public void updateLoad(Map<String, Integer> load) {
		iidMap = createLoadMap(load);
	}

	protected NavigableMap<Float, String> createLoadMap(Map<String, Integer> load) {
		Float total = 0f;
		for (Integer ii : load.values()) {
			total += (1f / ii);
		}
		Float delta0 = 1f / load.keySet().size() * lower;
		Float delta1 = 1f / load.keySet().size() * upper;
		NavigableMap<Float, String> ret = new TreeMap<Float, String>();
		Float v = 0f;
		Float vv = 0f;
		Float vvv = 0f;
		for (Entry<String, Integer> en : load.entrySet()) {
			ret.put(v, en.getKey());
			vv = (1f / en.getValue()) / total + vvv;
			if (vv > delta1) {
				vvv = vv - delta1;
				vv = delta1;
			} else if (vv < delta0) {
				vvv = vv - delta0;
				vv = delta0;
			}
			v += vv;
		}
		return ret;
	}

    @Override
    public void fail(String token) {
    }

}
