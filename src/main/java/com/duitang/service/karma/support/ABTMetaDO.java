package com.duitang.service.karma.support;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * 
 * @author kevx
 * @since 1:55:54 PM May 12, 2015
 */
public class ABTMetaDO {

    public final static String abt_meta = "abt_meta";
    public final static String abt_whitelist = "abt_whitelist";
    
    public final static ObjectMapper mapper = new ObjectMapper();
    
    private String appName;
    private boolean activated;
    private long startTime;
    private long endTime;
    private List<ABT> abts;
    
    public static class ABT {
        public String abtId;
        public int weights;
        public ABT(String abtId, int wt) {
            this.abtId = abtId;
            this.weights = wt;
        }
        public ABT() {
        }
        public void setAbtId(String abtId) {
            this.abtId = abtId;
        }
        public void setWeights(int weights) {
            this.weights = weights;
        }
    }
    
    public static enum Cond {
        LOGINED,
        SUBSCRIBED
        ;
    }

    public static ABTMetaDO createFromJson(String json) {
        try {
            return mapper.readValue(json, ABTMetaDO.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static Map<Cond, Boolean> defaultCondMap() {
        Map<Cond, Boolean> m = Maps.newHashMap();
        for (Cond c : Cond.values()) {
            m.put(c, false);
        }
        return m;
    }
    
    public ABT fromCondMap(Map<ABTMetaDO.Cond, Boolean> map) {
        int x = 0x00;
        if (map.entrySet().size() == Cond.values().length) {
            Cond[] conds = Cond.values();
            for (int i = 0; i < conds.length; i++) {
                boolean b = map.get(conds[i]);
                if (b) x = x | 1 << i;
            }
            return abts.get(x);
        }
        return null;
    }
    
    public boolean checkValidation() {
        long curr = System.currentTimeMillis();
        boolean isInTesting = curr > startTime && curr < endTime;
        boolean abtsOk = abts != null && abts.size() == (1 << Cond.values().length);
        return abtsOk && isInTesting && activated;
    }
    
    public int leftSeconds() {
        long t = endTime - System.currentTimeMillis();
        if (t <= 1000) return 0;
        return Double.valueOf(t / 1000.0).intValue();
    }
    
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public List<ABT> getAbts() {
        return abts;
    }

    public void setAbts(List<ABT> abts) {
        this.abts = abts;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
 
    public static void main(String[] argv) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 7);
        ABTMetaDO a = new ABTMetaDO();
        a.setAppName("vienna");
        a.setActivated(true);
        a.setStartTime(new Date().getTime());
        a.setEndTime(cal.getTimeInMillis());
        a.setAbts(Lists.newArrayList(
            new ABT("hot-waterfall-a", 50),
            new ABT("hot-waterfall-a", 50),
            new ABT("hot-waterfall-a", 50),
            new ABT("hot-waterfall-a", 50)
        ));
        System.out.println(a.checkValidation());
        System.out.println(a.leftSeconds());
        System.out.println(mapper.writeValueAsString(a));
    }
}
