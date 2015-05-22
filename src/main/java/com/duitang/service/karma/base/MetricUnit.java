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

    public String clientId;
    public String name;
    public String group;
    public String server;
    public String slug;
    protected LatencyStats stats;
    protected Histogram histo;

    public MetricUnit(String clientId, String name, String group) {
        this.clientId = clientId;
        this.name = name;
        this.group = group;
        this.server = clientId.split("@")[1];
        this.slug = clientId.split("@")[0] + "." + name.split(":")[1];

        stats = new LatencyStats();
        histo = stats.getIntervalHistogram();
    }

    synchronized public Map<String, Object> sample() {
        Map<String, Object> ret = new HashMap<>();
        stats.getIntervalHistogramInto(histo);
        ret.put("timestamp", System.currentTimeMillis());
        ret.put("client_id", clientId);
        ret.put("name", name);
        ret.put("group", group);
        ret.put("server", server);
        ret.put("slug", slug);

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
        ret.put("p996", histo.getValueAtPercentile(99.6D));
        ret.put("p998", histo.getValueAtPercentile(99.8D));
        ret.put("p999", histo.getValueAtPercentile(99.9D));
        ret.put("p9995", histo.getValueAtPercentile(99.95D));
        ret.put("p9997", histo.getValueAtPercentile(99.97D));
        ret.put("p9998", histo.getValueAtPercentile(99.98D));
        ret.put("p9999", histo.getValueAtPercentile(99.99D));
        ret.put("p99999", histo.getValueAtPercentile(99.999D));
        ret.put("p999999", histo.getValueAtPercentile(99.9999D));

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
