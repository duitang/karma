package com.duitang.service.karma.client;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Weighted Round-Robin (WRR)
 * 
 * @author kevx
 * @since 3:29:03 PM Mar 9, 2015
 */
public class WRRBalancer implements IOBalance {

    private static final double KICKOUT_RATE = 0.09;
    private static final Map<String, WRRBalancer> cache = Maps.newConcurrentMap();
    
    private Map<String, AtomicInteger> counts = Maps.newConcurrentMap();
    private Map<String, AtomicInteger> failureCounts = Maps.newConcurrentMap();
    
    private Map<String, Integer> serverWt = Maps.newConcurrentMap();//server weights
    private String[] seq = null;
    private Lock lock = new ReentrantLock();
    private int i = -1;
    private int cw = 0;
    
    public static WRRBalancer getInstance(String group, List<String> ss) {
        WRRBalancer wr = cache.get(group);
        if (wr == null) {
            synchronized(WRRBalancer.class) {
                wr = cache.get(group);
                if (wr == null) {
                    wr = new WRRBalancer();
                    wr.seq = ss.toArray(new String[0]);//initial sequence
                    for (String s : ss) {
                        wr.serverWt.put(s, 100);
                        wr.counts.put(s, new AtomicInteger(0));
                        wr.failureCounts.put(s, new AtomicInteger(0));
                    }
                    wr.init();
                    cache.put(group, wr);
                }
            }
        }
        return wr;
    }
    
    public void reload(List<String> ss) {
        lock.lock();
        try {
            List<String> toBeRemoved = Lists.newArrayList();
            List<String> toBeAdded = Lists.newArrayList();
            for (String s : ss) {
                if (!serverWt.keySet().contains(s)) {
                    toBeAdded.add(s);
                }
            }
            for (String s : serverWt.keySet()) {
                if (!ss.contains(s)) toBeRemoved.add(s);
            }
            for (String s : toBeRemoved) {
                counts.remove(s);
                failureCounts.remove(s);
                serverWt.remove(s);
            }
            for (String s : toBeAdded) {
                serverWt.put(s, 1);//magic：新加入的节点初始权重低，等其自行缓慢升高
                counts.put(s, new AtomicInteger(0));
                failureCounts.put(s, new AtomicInteger(0));
            }
            seq = serverWt.keySet().toArray(new String[0]);
        } finally {
            lock.unlock();
        }
    }
    
    private void init() {
        new Thread() {
            @Override
            public void run() {
                int last = currTimeSeq();
                while (true) {
                    int curr = currTimeSeq();
                    if (last != curr) {
                        lock.lock();
                        //not the same second
                        for (String server : serverWt.keySet()) {
                            double failrate = 0.0;
                            double total = (double)counts.get(server).intValue();
                            double fail = (double)failureCounts.get(server).intValue();
                            if (total != 0) failrate = fail / total;
                            if (failrate > 0.0) System.out.println(server + "@" + total + '/' + fail);
                            int oldwt = serverWt.get(server);
                            int wt = oldwt - (int)(failrate * 100.0);
                            if (failrate > KICKOUT_RATE) wt = 0;
                            //如果出错率为0则缓慢提升权重
                            if (failrate == 0 && wt <= 99) wt = (int) ((wt + 1) * 1.2);
                            if (wt <= 0) wt = 0;
                            if (wt > 100) wt = 100;
                            serverWt.put(server, wt > 0 ? wt : 0);
                            counts.get(server).set(0);
                            failureCounts.get(server).set(0);
                        }
                        last = curr;
                        lock.unlock();
                    }
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    
    @Override
    public String next(String token) {
        try {
            //patch: 当只有一个节点的时候，直接返回该节点。
            //否则在下面这种极端情况下，该方法会返回null：该节点持续出错不可用且没有更多节点加入
            lock.lock();
            while (true) {
                i = (i + 1) % serverWt.size();
                if (i == 0) {
                    cw = cw - gcd(serverWt.values()); 
                    if (cw <= 0) {
                        cw = Collections.max(serverWt.values());
                        if (cw == 0) {
                            System.out.println(serverWt.size() + serverWt.keySet().toArray(new String[0]).length);
                            return null;
                        }
                    }
                } 
                String s = seq[i];
                if (serverWt.get(s) >= cw) {
                    seq[i] = s;
                    counts.get(s).incrementAndGet();
                    return s;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateLoad(Map<String, Integer> load) {
    }
    
    private int gcd(int a, int b){
        if( a<0 ) a = -a;
        if( b<0 ) b = -b;
        while( b!=0 ){
            a %= b;
            if( a==0 ) return b;
            b %= a;
        }
        return a;
    }
    
    private int gcd(Collection<Integer> coll) {
        int ret = 1;
        for (int i : coll) {
            ret = gcd(ret, i);
        }
        return ret;
    }
    
    private int currTimeSeq() {
        long t = System.currentTimeMillis();
        long x = t - (t / 10000) * 10000;
        return (int) (x / 500.0);
    }
    
    @Override
    public void fail(String s) {
        AtomicInteger ai = failureCounts.get(s);
        ai.incrementAndGet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : serverWt.entrySet()) {
            sb.append(e.getKey());
            sb.append(':');
            sb.append(e.getValue());
            sb.append(';');
        }
        return sb.toString();
    }
    
}
