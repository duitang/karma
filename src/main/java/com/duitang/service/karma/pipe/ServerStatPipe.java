package com.duitang.service.karma.pipe;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 
 * @author kevx
 * @since 11:30:30 AM Apr 7, 2015
 */
public class ServerStatPipe extends CloudPipeBase {
    
    public void stat(String name, String node, String category, Object value) {
        Map<String, Object> m = Maps.newHashMap();
        m.put("name", name);
        m.put("node", node);
        m.put("category", category);
        if (value != null) {
            if (value instanceof Number) {
                m.put("value", ((Number)value));
            } else {
                m.put("value", value.toString());
            }
        } else {
            m.put("value", "");
        }
        pumpMap(m);
    }
    
    @Override
    protected String getBiz() {
        return "server_stat";
    }

}
