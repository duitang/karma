import sys
import httplib

import avro.ipc as ipc
import avro.protocol as protocol

PROTOCOL = protocol.parse(open("../resources/l2.avpr").read())

server_addr = ('localhost', 9090)

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

    # cleanup
    client.close()
