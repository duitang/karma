package com.duitang.service.karma.support;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 跟踪链
 * @author kevx
 * @since 12/09/2014
 */
public class TraceChainDO implements Serializable {
    
    private static final long serialVersionUID = -4932391903529618278L;
    
    private String token;
    private int baseDepth;
    private int baseParent;
	private Map<Integer, Integer> seqMap = Maps.newHashMap();
	private transient Stack<Long> timeStack;
	private transient Stack<String> targetStack;//saving a lot of traffics
	private transient long timedelta;
	private transient List<String> buff;
	
	@Override
    public TraceChainDO clone() {
	    TraceChainDO tc = new TraceChainDO();
	    tc.token = this.token;
	    tc.seqMap = this.seqMap;
	    tc.baseDepth = this.targetStack.size();
	    tc.baseParent = this.getParent();
	    return tc;
    }

    public TraceChainDO(String t) {
        reset();
        this.token = t;
        this.seqMap.put(0, 0);
    }
	
	public TraceChainDO() {
	    reset();
	}
	
	public void reset() {
	    buff = Lists.newArrayList();
	    timeStack = new Stack<Long>();
	    targetStack = new Stack<String>();
	}
	
	public void push(String target, long t) {
	    timeStack.push(t);
	    targetStack.push(target);
	    int depth = timeStack.size() + baseDepth;
	    if (!seqMap.containsKey(depth)) {
            this.seqMap.put(depth, 0);
            return;
        }
	    int seq = this.seqMap.get(depth);
        this.seqMap.put(depth, seq + 1);
	}
	
	public Pair<String, Long> pop() {
	    Pair<String, Long> ret = Pair.of(targetStack.pop(), timeStack.pop());
	    return ret;
	}
	public Map<Integer, Integer> seqMap() {
		return seqMap;
	}
	
	public int depth() {
	    return targetStack.size();
	}
	
	public boolean isEmptyStack() {
	    return targetStack.isEmpty();
	}
	
	public void addTraceInfo(String e) {
	    this.buff.add(e);
	}
	
    public List<String> getBuff() {
        return buff;
    }

    public long getTimedelta() {
        return timedelta;
    }

    public void setTimedelta(long timedelta) {
        this.timedelta = timedelta;
    }

    public String getToken() {
        return token;
    }

    public int getBaseDepth() {
        return baseDepth;
    }

    public void loadBaseDepth() {
        this.baseDepth = targetStack.size();
    }

    public int getParent() {
        if (targetStack.isEmpty()) return baseParent;
        return Math.abs(targetStack.peek().hashCode());
    }
}