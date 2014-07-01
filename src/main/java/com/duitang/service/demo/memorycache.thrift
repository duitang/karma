
namespace java com.duitang.service.demo
service MemoryCache{ 
 string getString(1: string key),
 void setString(1: string key, 2: string value, 3: i32 ttl),
 void setBytes(1: string key, 2: binary value, 3: i32 ttl),
 binary getBytes(1: string key)
}
