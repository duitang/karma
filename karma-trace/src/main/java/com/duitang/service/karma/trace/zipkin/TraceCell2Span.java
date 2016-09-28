package com.duitang.service.karma.trace.zipkin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import com.duitang.service.karma.boot.KarmaServerConfig;
import com.duitang.service.karma.support.NameUtil;
import com.duitang.service.karma.trace.TraceBlock;
import com.duitang.service.karma.trace.TraceCell;
import com.duitang.service.karma.trace.TraceCellVisitor;

import zipkin.Annotation;
import zipkin.BinaryAnnotation;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.Span.Builder;

/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
public class TraceCell2Span implements TraceCellVisitor<Span> {

	@SuppressWarnings("deprecation")
	public static Endpoint addr = Endpoint.create(NameUtil.getInstanceTag().app, KarmaServerConfig.host,
			KarmaServerConfig.port);

	protected void extra(TraceCell src, Builder dest) {
		// ignore at TraceCell
		if (src instanceof TraceBlock) {
			for (Entry<String, String> en : ((TraceBlock) src).props.entrySet()) {
				dest.addBinaryAnnotation(BinaryAnnotation.create(en.getKey(), en.getValue(), addr));
			}
		}
	}

	@Override
	public Span transform(TraceCell tc) {
		return transform(Arrays.asList(tc)).get(0);
	}

	@Override
	public List<Span> transform(List<TraceCell> src) {
		List<Span> ret = new ArrayList<Span>();
		Builder r = null;
		for (TraceCell tc : src) {
			r = Span.builder().traceId(tc.traceId).name(tc.name).id(tc.spanId).timestamp(tc.timestamp)
					.duration((tc.ts2 - tc.timestamp));

			if (tc.parentId != null) {
				r.parentId(tc.parentId);
			}

			if (tc.isLocal) {
				Annotation an1 = Annotation.create(tc.ts1, "lc", addr);
				r.addAnnotation(an1);
			} else {
				Annotation an1 = Annotation.create(tc.ts1, tc.type[0], addr);
				Annotation an2 = Annotation.create(tc.ts2, tc.type[1], addr);
				r.addAnnotation(an1).addAnnotation(an2);
			}
			if (tc.err != null) {
				BinaryAnnotation err = BinaryAnnotation.create("error", tc.err, addr);
				r.addBinaryAnnotation(err);
			}
			if (tc.pid != null) {
				r.addBinaryAnnotation(BinaryAnnotation.create("pid", String.valueOf(tc.pid), addr));
			}
			if (tc.clazzName != null) {
				r.addBinaryAnnotation(BinaryAnnotation.create("class", tc.clazzName, addr));
			}
			if (tc.group != null) {
				r.addBinaryAnnotation(BinaryAnnotation.create("group", tc.group, addr));
			}
			extra(tc, r);
			ret.add(r.build());
		}
		return ret;
	}

}
