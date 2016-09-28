package com.duitang.service.karma.trace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BaseVisitor implements TraceVisitor {

	protected volatile Map<String, TracerReporter> reporters = new HashMap<String, TracerReporter>();
	protected ConsoleReporter console = new ConsoleReporter();
	
	
	@Override
	public void visit(TraceCell tc) {
		visits(Arrays.asList(tc));
	}

	@Override
	public void visits(List<TraceCell> tcs) {
		Map<String, List<TraceCell>> grp = new HashMap<String, List<TraceCell>>();
		for (TraceCell tc : tcs) {
			if (!grp.containsKey(tc.group)) {
				grp.put(tc.group, new LinkedList<TraceCell>());
			}
			grp.get(tc.group).add(tc);
		}
		if (reporters.isEmpty()) { // default console using zipkin json
			console.commit(tcs);
		} else {
			for (Entry<String, List<TraceCell>> en : grp.entrySet()) {
				if (reporters.containsKey(en.getKey())) {
					reporters.get(en.getKey()).commit(en.getValue());
				} else {
					TracerReporter rpt = reporters.get(null); // special
					if (rpt != null) {
						rpt.commit(en.getValue());
					}
				}
			}
		}
	}

	synchronized public void addReporter(String grp, TracerReporter report) {
		HashMap<String, TracerReporter> m = new HashMap<String, TracerReporter>(reporters);
		m.put(grp, report);
		reporters = m;
	}

	synchronized public void removeReporter(String grp) {
		HashMap<String, TracerReporter> m = new HashMap<String, TracerReporter>(reporters);
		m.remove(grp);
		reporters = m;
	}

}
