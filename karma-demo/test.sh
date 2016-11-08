#
curl 'http://172.16.253.128:8080/show'
# curl 'http://172.16.253.128:8080/update?aa=\{"id":9,"mean":100,"u":1,"ok":0.99,"begin":true,"end":false,"lost":false\}'


curl 'http://172.16.253.128:8080/update?aa=\{"id":3,"mean":100,"u":1,"ok":0.0001,"begin":true,"end":false,"lost":false\}'
curl 'http://172.16.253.128:8080/update?aa=\{"id":2,"mean":100,"u":1,"ok":0.25,"begin":true,"end":false,"lost":false\}'
curl 'http://172.16.253.128:8080/update?aa=\{"id":1,"mean":100,"u":1,"ok":0.5,"begin":true,"end":false,"lost":false\}'
curl 'http://172.16.253.128:8080/update?aa=\{"id":0,"mean":100,"u":1,"ok":0.99999,"begin":true,"end":false,"lost":false\}'

