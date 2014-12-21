# encoding=utf8

import json
import urllib
import urllib2
import time

def test1():
    domain = "com.duitang.service.demo.DemoService"
    method = "memory_getString"
    val = "aaa"
    param = urllib.quote(json.dumps([{"v": val}]))
    ur = "http://localhost:9998/%s/%s?q=%s" % (
                                               domain.replace(".", "/"),
                                               method,
                                               param,
                                               )
    
    ts = time.time() * 1000
    resp = urllib2.urlopen(ur)
    ret = resp.read()
    ts = time.time() * 1000 - ts
    print ret
    print "size=[", len(ret), "], time elapsed: ", ts, "ms"
    return len(ret)

def test(loop, sz):
    domain = "com.duitang.service.demo.DemoService"
    method = "memory_getString"
    val = "aaa"
    param = urllib.quote(json.dumps([{"v": val}]))
    ur = "http://localhost:9998/%s/%s?q=%s" % (
                                               domain.replace(".", "/"),
                                               method,
                                               param,
                                               )
    
    ts = time.time() * 1000
    for i in xrange(loop):
        resp = urllib2.urlopen(ur)
        ret = resp.read()
        if len(ret) != sz:
            print "Error Size Fetched! %d" % len(sz)
    ts = time.time() * 1000 - ts
    print "repeat", loop, ", per-size=[", len(ret), "], time elapsed: ", ts, "ms"

sz = test1()
test(1000, sz)