import sys
import httplib
import array
import time

import avro.ipc as ipc
import avro.protocol as protocol

PROTOCOL = protocol.parse(open("../resources/main/demo.avpr").read())

server_addr = ('localhost', 9091)

class UsageError(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return repr(self.value)

if __name__ == '__main__':
    # client code - attach to the server and send a message
    client = ipc.HTTPTransceiver(server_addr[0], server_addr[1])
    req = ipc.Requestor(PROTOCOL, client)
    
    params = {"key": "helloworld", "value": "1", "ttl": 1}
    print "memory_setString Result: " , req.request('memory_setString', params)

    params = {"key": "helloworld"}
    print "memory_getString Result: " , req.request('memory_getString', params)

    params = {"key": "helloworld", "ttl": 100}
    print "cat_mgetstring Result: " , req.request('trace_msg', params)

    lz = []
    ts = time.time() * 1000
    params = {"key": "helloworld"}
    for i in xrange(100):
       ss = req.request('memory_getString', params)
#        print ss
       lz.append(len(ss if ss else ''))
    ts = time.time() * 1000 - ts
    print set(lz)
    print "time:", ts, "ms"
    # cleanup
    client.close()
