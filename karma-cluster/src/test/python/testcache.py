# encoding=utf8

import os.path
import sys
sys.path.append(os.path.realpath("../../main/python"))

from thrift.transport import TTransport
from thrift.transport import TSocket
from thrift.protocol import TBinaryProtocol
from cache import Cache
from cache.ttypes import *

host = "localhost"
port = 9090
key = "aaaa"
value = "bbbb"
ttl = 1111


socket = TSocket.TSocket(host, port)
transport = TTransport.TBufferedTransport(socket)
protocol = TBinaryProtocol.TBinaryProtocol(transport)
client = Cache.Client(protocol)
transport.open()
client.setString(key, value, ttl)
v = client.getString(key)
print value == v

client.setBytes(key, value, ttl)
v = client.getBytes(key)
print value == v

transport.close()

