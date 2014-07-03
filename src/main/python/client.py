import sys
import httplib

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

    params = {"key": "aa", "value": "bb", "ttl": 10000}
    print "cat_setstring Result: " , requestor.request('cat_setstring', params)
    params = {"key": "cc", "value": "dd", "ttl": 10000}
    print "cat_setstring Result: " , requestor.request('cat_setstring', params)

    from com.duitang.service.l2 import Keys
    params = Keys(["aa", "cc"])
    print "cat_mgetstring Result: " , requestor.request('cat_mgetstring', params)

    # cleanup
    client.close()
