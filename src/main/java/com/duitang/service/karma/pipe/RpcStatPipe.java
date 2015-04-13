package com.duitang.service.karma.pipe;

import java.net.InetAddress;

/**
 * 
 * @author kevx
 * @since 11:00:11 AM Apr 13, 2015
 */
public class RpcStatPipe extends ServerStatPipe {

    private static final RpcStatPipe pipe = new RpcStatPipe();
    
    private static final String NAME = "rpc";
    
    private String node = "127.0.0.1";
    
    public static final String CAT_THREAD_EXCEEDED = "thread_exceeded_coresize";
    public static final String CAT_HIGH_LATENCY = "high_latency";
    
    public RpcStatPipe() {
        try {
            InetAddress ia = InetAddress.getLocalHost();
            String ip = ia.getHostAddress();
            node = ip;
        } catch (Exception e) {
        }
    }
    
    public static void stat(String cat, Number value) {
        pipe.stat(NAME, pipe.node, cat, value);
    }
}
