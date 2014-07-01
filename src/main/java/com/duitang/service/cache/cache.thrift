
namespace java com.duitang.service.cache
service Cache{
 	bool setstring(1: string key, 2: string value, 3: i32 ttl),
	bool add(1: string key, 2: string value, 3: i32 ttl),
	i64 incr(1: string key, 2: i64 delta),
	string getstring(1: string key),
	bool delstring(1: string key),
	map<string, string> getmultistring(1: list<string> key)
}
