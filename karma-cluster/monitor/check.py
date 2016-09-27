#encoding=utf8

from string import Template

block_0 = '''
package com.sun.btrace.samples;

import static com.sun.btrace.BTraceUtils.*;

import com.sun.btrace.AnyType;
import com.sun.btrace.aggregation.Aggregation;
import com.sun.btrace.aggregation.AggregationFunction;
import com.sun.btrace.aggregation.AggregationKey;
import com.sun.btrace.annotations.*;

@BTrace
public class KarmaMonitor {

    private static Aggregation hist = Aggregations.newAggregation(AggregationFunction.QUANTIZE);
    private static Aggregation avg = Aggregations.newAggregation(AggregationFunction.AVERAGE);
    private static Aggregation max = Aggregations.newAggregation(AggregationFunction.MAXIMUM);
    private static Aggregation count = Aggregations.newAggregation(AggregationFunction.COUNT);
    private static Aggregation arg = Aggregations.newAggregation(AggregationFunction.QUANTIZE);

'''

block_1 = '''
    @OnMethod(clazz = "$clazz", method = "$method")
    public static void $cm1(AnyType[] args) {
        AggregationKey key = Aggregations.newAggregationKey("$cm");
        if (args.length > 0) {
            Aggregations.addToAggregation(arg, key, strlen(str(args[0])));
        }
    }

    @OnMethod(clazz = "$clazz", method = "$method", location = @Location(Kind.RETURN))
    public static void $cm(@Duration long durationL) {
        AggregationKey key = Aggregations.newAggregationKey("$cm");
        long duration = (long) durationL;
        Aggregations.addToAggregation(hist, key, duration);
        Aggregations.addToAggregation(avg, key, duration);
        Aggregations.addToAggregation(max, key, duration);
        Aggregations.addToAggregation(count, key, duration);
    }

'''

block_2 = '''
    @OnTimer(5000)
    public static void onEvent() {
        Aggregations.truncateAggregation(hist, 10);
        Aggregations.printAggregation("Count", count);
        Aggregations.printAggregation("Max", max);
        Aggregations.printAggregation("Average", avg);
        Aggregations.printAggregation("Histogram", hist);
        Aggregations.printAggregation("Arguments", arg);
        println("---------------------------------------------");
    }

}
'''

def gen(monitor_items):
    sss = []
    sss.append(block_0)
    for clz, m in monitor_items:
        t = Template(block_1)
        cm = '%s_%s' % (clz, m)
        cm = cm.replace("/", "")
        cm = cm.replace("\\", "")
        cm = cm.replace("+", "")
        cm = cm.replace(".", "")
        cm = cm.replace("*", "")
        cm1 = cm + "_1"
        sss.append(t.substitute(clazz=clz.replace('\\', '\\\\'), method=m.replace('\\', '\\\\'), cm=cm, cm1=cm1))
    sss.append(block_2)
    return "\n".join(sss)

if __name__ == '__main__':
    print gen([('+com.duitang.service.l2.L2Service', 'cat_getstring'),
               ('+com.duitang.service.l2.L2Service', 'cat_setstring'),
               ('+com.duitang.service.l2.L2Service', 'cat_addstring'),
               ('+com.duitang.service.l2.L2Service', 'cat_incr'),
               ('+com.duitang.service.l2.L2Service', 'cat_delstring'),
               ('+com.duitang.service.l2.L2Service', 'cat_mgetstring')])
    