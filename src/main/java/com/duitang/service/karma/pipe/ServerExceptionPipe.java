package com.duitang.service.karma.pipe;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 
 * @author kevx
 * @since 11:22:33 AM Apr 7, 2015
 */
public class ServerExceptionPipe extends CloudPipeBase {

    public static enum ExceptionSource {
        VIEW,
        DOMAIN,
        RPC,
        DAO,
        UTIL,
        EXT;
    }

    public void exception(ExceptionSource es, Throwable detail) {
        if (es == null || detail == null) return;
        Map<String, Object> m = Maps.newHashMap();
        m.put("source", es.name().toLowerCase());
        m.put("type", detail.getClass().getName());
        m.put("message", detail.getMessage());
        m.put("top_frame", "");
        if (detail.getStackTrace() != null && detail.getStackTrace().length > 0) {
            m.put("top_frame", detail.getStackTrace()[0].getClassName());
        }
        pumpMap(m);
    }
    
    @Override
    protected String getBiz() {
        return "server_exception";
    }

}
