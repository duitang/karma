package com.duitang.service.karma.stats;

import java.util.List;
import java.util.Map;

import com.duitang.service.karma.pipe.CloudPipeBase;

public class KafkaReporter extends CloudPipeBase implements Reporter {
    @Override
    protected String getBiz() {
        return "kafka_metrics";
    }

    @Override
    public void report(List<Map> data) throws Exception {
        pumpString(mapper.writeValueAsString(data));
    }
}
