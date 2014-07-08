import sys
import httplib
import array
import time

import avro.ipc as ipc
import avro.protocol as protocol

PROTOCOL = protocol.parse(open("../resources/l2.avpr").read())

server_addr = ('localhost', 7777)

class UsageError(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return repr(self.value)

if __name__ == '__main__':
    # client code - attach to the server and send a message
    client = ipc.HTTPTransceiver(server_addr[0], server_addr[1])
    requestor = ipc.Requestor(PROTOCOL, client)
    
    params = {"key": "helloworld", "value": "1", "ttl": 1}
    print "cat_setstring Result: " , requestor.request('cat_setstring', params)

    params = {"key": "helloworld"}
    print "cat_getstring Result: " , requestor.request('cat_getstring', params)

    params = {"key": u"aa", "value": u"bb", "ttl": 10000}
    print "cat_setstring Result: " , requestor.request('cat_setstring', params)
    params = {"key": u"cc", "value": u"dd", "ttl": 10000}
    print "cat_setstring Result: " , requestor.request('cat_setstring', params)

    params = {"keys": "\n".join(["aa","cc"])}
    print "cat_mgetstring Result: " , requestor.request('cat_mgetstring', params)

    lz = []
    ts = time.time() * 1000
    params = {"key": "v4:napi:billion-32-0:edd7630ed6332b9a603ed580fe71412e"}
    for i in xrange(100):
       ss = requestor.request('cat_getstring', params)
       print ss
       lz.append(len(ss if ss else ''))
    ts = time.time() * 1000 - ts
    print set(lz)
    print "time:", ts, "ms"
    # cleanup
    client.close()
