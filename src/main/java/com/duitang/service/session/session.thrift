
namespace java com.duitang.service.session
service Session{
 	map< getSession(String sessionid);

   
    String get(String sessionid);

    String set(String sessionid, String value);

    Long expire(String sessionid, int expiryAge);

    Long delete(String sessionid);
}
