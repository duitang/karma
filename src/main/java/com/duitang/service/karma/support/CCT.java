package com.duitang.service.karma.support;

import java.security.SecureRandom;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * 分布式调用链路跟踪
 * 
 * Call Chain Tracing (CCT)
 * 
 * 使用方式：
 * 
 * 请务必确保*成对*的执行，否则可能会导致内存泄露或逻辑出错
 * 
 * @author kevx
 * @since 4:04:00 PM Feb 26, 2015
 */
public class CCT {

    public static final String RPC_CONF_KEY = "cct";
    
    private static Logger cctlog = Logger.getLogger("cct");
    
    private static ThreadLocal<TraceChainDO> chainHolder = new ThreadLocal<TraceChainDO>();
    
    private static ObjectMapper mapper = new ObjectMapper();
    private static final SecureRandom numberGenerator = new SecureRandom();
    
    private static long baseline = 0L;
    
    public static TraceChainDO get() {
        TraceChainDO tc = chainHolder.get();
        return tc;
    }

    public static void setForcibly(TraceChainDO tc) {
        chainHolder.set(tc);
    }
    
    public static void mergeTraceChain(TraceChainDO remoteTc) {
        TraceChainDO tc = get();
        if (tc != null && remoteTc != null) {
            tc.seqMap().putAll(remoteTc.seqMap());
        }
    }
    
    public static void call(String target, boolean isRoot) {
        TraceChainDO tc = chainHolder.get();
        if (tc == null && isRoot == true) {
            tc = new TraceChainDO(generateToken());
            chainHolder.set(tc);
        }
        
        if (tc != null) {
            tc.push(target, currentTimeMillis(tc));
        }
    }
    
    public static void ret() {
        boolean islast = false;
        try {
            TraceChainDO tc = chainHolder.get();
            if (tc == null) return;
            
            int depth = tc.depth();
            Pair<String, Long> p = tc.pop();
            int parent = tc.getParent();
            long curr = currentTimeMillis(tc);
            long last = p.getRight();
            
            traceLog(
                p.getLeft(), 
                p.getRight(), 
                currentTimeMillis(tc),
                depth,
                parent
            );
            
            if (tc.isEmptyStack()) {
                islast = true;
                if (curr - last < baseline) return;
                for (String s : tc.getBuff()) {
                    cctlog.warn(s);
                }
            }
        } finally {
            if (islast) chainHolder.remove();
        }
    }

    public static void traceLog(String target, long start, long end, int depth, int parent) {
        TraceInfoDO ti = new TraceInfoDO();
        TraceChainDO tt = get();
        ti.setToken(tt.getToken());
        ti.setDepth(depth + tt.getBaseDepth());
        ti.setElapsedTime(end - start);
        ti.setStartTime(start);
        ti.setEndTime(end);
        ti.setSeq(tt.seqMap().get(depth + tt.getBaseDepth()));
        ti.setTarget(target);
        ti.setParent(parent);
        ti.setSelf(Math.abs(target.hashCode()));
        try {
            tt.addTraceInfo(mapper.writeValueAsString(ti));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static long currentTimeMillis(TraceChainDO tc) {
        if (tc != null) return System.currentTimeMillis() + tc.getTimedelta();
        else return System.currentTimeMillis();
    }
    
    private static String generateToken() {
        String t = Long.toString(System.currentTimeMillis(), 36);
        return String.format("%s%04d", t, numberGenerator.nextInt(9999));
    }
    
    //spring专用
    public void setBaseline(long v) {
        CCT.baseline = v;
    }
}
