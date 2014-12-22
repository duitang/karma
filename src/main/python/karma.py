# -*- coding: utf-8 -*-
import socket
import urllib
import inspect
import time
from random import shuffle, choice
from datetime import datetime
import struct
import random
from json import  JSONEncoder
import re
import urllib2
import json

import logging
logger = logging.getLogger("karma")
slow_logger = logging.getLogger("karma_slow")

            
class DuitangRemoteProxy:

    def __init__(self, config):
        self.server = {}
        for sname, sval in config.items():
            for sss in sval['references']:
                sss['domain'] = sss['domain'].replace('.', '/')
                sss['locations'] = sval["locations"]
                sss['hostz'] = len(sval["locations"])
                self.server[sss["id"]] = sss
                logger.info("init %s locations = %s,timeout = %s" % (sss['id'], ";".join(sss['locations']), sss.get('timeout', 500)))
            
    def getService(self, serviceName):
        return ServiceProxy(self, serviceName) 

    def invoke(self, serviceName, method, query):
        the_service = self.server.get(serviceName)
        if not the_service:
            raise Exception("karma client Exception", "cant't find service,serviceName=%s" % serviceName)
            
        iid = random.randint(0, the_service['hostz']) % the_service['hostz']
        qry = urllib.quote(query)
        ur = "http://%s/%s/%s?q=%s" % (
                                       the_service['locations'][iid],
                                       the_service['domain'],
                                       method,
                                       qry)
        resp = urllib2.urlopen(ur, timeout=the_service.get('timeout', 500))        
        if resp.code == 200:
            data = resp.read()        
            if not data:
                return None
            ret = json.loads(data)
            return ret['r']
        else:
            raise Exception('karma invoke ERROR: url =%s \r\n parameter=%s' % (ur, query))
        
class ServiceProxy:    

    def __init__(self, remoteProxy, serviceName):
        self.remoteProxy = remoteProxy
        self.serviceName = serviceName

    def __getattr__(self, name):
        return _Method(self.__invoke, name)  
    
    def __invoke(self, method, params):  

        if method in ['__repr__', '__unicode__', '__str__', 'repr']: 
            return method

        if method.startswith('__'):
            return None

        query = []
        for param in params:
            if param is None:
                param = param
            elif    isinstance(param, (unicode, str, int, float)):
                param = param
            elif isinstance(param, datetime):
                param = time.mktime(param.timetuple())
            else:
                param = json.dumps(param)
            query.append({'v': param})
        param = json.dumps(query)
        return self.remoteProxy.invoke(self.serviceName, method, param)
      
class _Method:  
    def __init__(self, invoker, method):  
        self._invoker = invoker  
        self._method = method    
    def __call__(self, *args):  
        return self._invoker(self._method, args)

# version 2.0 support dboss java client
def create_duitang_remote_proxy(config):
    return  DuitangRemoteProxy(config)


if __name__ == "__main__":
    KARMA = {
        "demo": {
              "locations":["localhost:9998"],
              "references":[
                {
                    "id":"demoservice",
                    "domain":"com.duitang.service.demo.DemoService",
                    "timeout": 500,
                    "version":"1.0",
                }
              ]
        }
    }
    from karma import create_duitang_remote_proxy
    proxy = create_duitang_remote_proxy(KARMA)
    demo = proxy.getService('demoservice')
    ret = demo.memory_getString("aaa")
    print ret
    print type(ret)
