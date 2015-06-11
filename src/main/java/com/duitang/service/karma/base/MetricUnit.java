package com.duitang.service.karma.base;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.HdrHistogram.Histogram;
import org.LatencyUtils.LatencyStats;

public class MetricUnit {

    private ByteBuffer targetBuffer;

    public String name;
    public String group;
    protected LatencyStats stats;
    protected Histogram histo;

    public MetricUnit(String name) {
        this(name, null);
    }

    public MetricUnit(String name, String group) {
        this.name = name;
        this.group = group;

        stats = new LatencyStats();
        histo = stats.getIntervalHistogram();
    }

    synchronized public Map<String, Object> sample() {
        Map<String, Object> ret = new HashMap<>();
        stats.getIntervalHistogramInto(histo);
        ret.put("timestamp", System.currentTimeMillis());
        ret.put("location", MetricCenter.getHostname());
        ret.put("name", name);
        if(group != null) {
            ret.put("group", group);
        }

        ret.put("from", histo.getStartTimeStamp());
        ret.put("to", histo.getEndTimeStamp());

        ret.put("total", histo.getTotalCount());
        ret.put("mean", histo.getMean());
        ret.put("max", histo.getMaxValue());
        ret.put("min", histo.getMinValue());
        ret.put("stddev", histo.getStdDeviation());

        long gap = histo.getEndTimeStamp() - histo.getStartTimeStamp();
        if (gap != 0) {
            ret.put("qps", histo.getTotalCount() / (gap / 1000D));
        }

        ret.put("p50", histo.getValueAtPercentile(50D));
        ret.put("p75", histo.getValueAtPercentile(75D));
        ret.put("p87", histo.getValueAtPercentile(87D));
        ret.put("p93", histo.getValueAtPercentile(93D));
        ret.put("p96", histo.getValueAtPercentile(96D));
        ret.put("p98", histo.getValueAtPercentile(98D));
        ret.put("p99", histo.getValueAtPercentile(99D));
        ret.put("p999", histo.getValueAtPercentile(99.9D));
        ret.put("p9999", histo.getValueAtPercentile(99.99D));

        ret.put("histogram_b64", encodeCompressedArray(histo));
        return ret;
    }

    protected String encodeCompressedArray(final Histogram histogram) {
        if(targetBuffer == null || targetBuffer.capacity() < histogram.getNeededByteBufferCapacity()) {
            targetBuffer = ByteBuffer.allocate(histogram.getNeededByteBufferCapacity());
        }
        targetBuffer.clear();
        int compressedLength = histogram.encodeIntoCompressedByteBuffer(targetBuffer);
        byte[] compressedArray = Arrays.copyOf(targetBuffer.array(), compressedLength);
        return DatatypeConverter.printBase64Binary(compressedArray);
    }

    public void record(long latency) {
        stats.recordLatency(latency);
    }
}
