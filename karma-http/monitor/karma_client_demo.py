#encoding=utf8
import sys
import json
import urllib
import os

host = "127.0.0.1:8888"
domain = "com.duitang.service.demo.IDemoService"
domain = domain.replace('.', '/')
method = "memory_getString"
key = u"aaaa"
param = [key]
param = urllib.quote(json.dumps(param))
url = "http://%s/%s/%s?q=%s" % (host, domain, method, param)

cmd = "curl \"%s\"" % url
print cmd
os.system(cmd)
