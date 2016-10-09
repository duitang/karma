package com.duitang.service.karma.trace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BaseVisitor implements TraceVisitor {

	protected volatile Map<String, Set<TracerReporter>> reporters = new HashMap<String, Set<TracerReporter>>();
	protected ConsoleReporter console = new ConsoleReporter();

	@Override
	public void visit(TraceCell tc) {
		visits(Arrays.asList(tc));
	}

	@Override
	public void visits(List<TraceCell> tcs) {
		Map<String, Set<TracerReporter>> reporters = this.reporters;
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
				Set<TracerReporter> rpts = null;
				if (reporters.containsKey(en.getKey())) {
					rpts = reporters.get(en.getKey());
				} else {
					rpts = reporters.get(null); // special
				}
				for (TracerReporter rpt : rpts) {
					if (rpt != null) {
						rpt.commit(en.getValue());
					}
				}
			}
		}
	}

	synchronized public void addReporter(String grp, TracerReporter report) {
		HashMap<String, Set<TracerReporter>> m = new HashMap<String, Set<TracerReporter>>(reporters);
		if (!m.containsKey(grp)) {
			m.put(grp, new HashSet<TracerReporter>());
			m.get(grp).add(console);
		}
		m.get(grp).add(report);
		reporters = m;
	}

	synchronized public void removeReporter(String grp) {
		HashMap<String, Set<TracerReporter>> m = new HashMap<String, Set<TracerReporter>>(reporters);
		if (grp != null) {
			m.remove(grp);
		}
		reporters = m;
	}

}
