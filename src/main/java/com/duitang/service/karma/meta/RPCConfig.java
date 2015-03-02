package com.duitang.service.karma.meta;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * <pre>
 * every config related things here
 * e.g.
 * serialization mechanism for parameter?
 * </pre>
 * 
 * @author laurence
 * 
 */
public class RPCConfig implements Serializable {

	private static final long serialVersionUID = 1L;
	private Map<String, Serializable> data = Maps.newHashMap();
	
	public boolean isValid() {
	    return data != null;
	}
	
	@Override
	public String toString() {
		return "";
	}

    public Serializable getConf(String key) {
        return data.get(key);
    }

    public void addConf(String key, Serializable data) {
        this.data.put(key, data);
    }

}
