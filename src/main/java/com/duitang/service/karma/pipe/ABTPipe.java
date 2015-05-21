package com.duitang.service.karma.pipe;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 
 * @author kevx
 * @since 12:28:08 PM May 18, 2015
 */
public class ABTPipe extends CloudPipeBase {

    public void pump(
         String abtId, 
         String sessionid,
         String uri,
         long gmtVisit
         ) {
        Map<String, Object> m = Maps.newHashMap();
        m.put("abt_id", abtId);
        m.put("uri", uri);
        m.put("sessionid", sessionid);
        m.put("gmt_visit", gmtVisit);
        pumpMap(m);
    }
    
    @Override
    protected String getBiz() {
        return "abt";
    }

}
