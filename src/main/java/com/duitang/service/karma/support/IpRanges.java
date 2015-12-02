package com.duitang.service.karma.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;


/**
 * 
 * @author kevx
 * @since 2:04:27 PM Apr 14, 2015
 */
public class IpRanges {
    
    private static final String[] productionIPRanges = new String[]{
        "^192\\.168\\.172\\.(1[2-9]|[2-9][0-9]|1([0-9][0-9])|2([0-4][0-9]|5[0-4]))$",
        "^10\\.0|1\\.1\\.([1-9][0-9]|1([0-9][0-9])|2([0-4][0-9]|5[0-5]))$",
        "^10\\.0|1\\.2\\.([1-9][0-9]|1([0-9][0-9])|2([0-4][0-9]|5[0-5]))$",
        "^10\\.0|1\\.3\\.([1-9][0-9]|1([0-9][0-9])|2([0-4][0-9]|5[0-5]))$",
    };
    
    public static boolean isProduction(String ip) {
        for (String ipr : productionIPRanges) {
            Pattern r = Pattern.compile(ipr);
            Matcher m = r.matcher(ip);
            if (m.find()) return true;
        }
        return false;
    }
    
    public static void main(String[] args) {
        Validate.isTrue(!isProduction("192.168.172.2"));
        Validate.isTrue(isProduction("192.168.172.12"));
        Validate.isTrue(isProduction("192.168.172.18"));
        Validate.isTrue(isProduction("192.168.172.42"));
        
        Validate.isTrue(isProduction("10.0.1.10"));
        Validate.isTrue(isProduction("10.0.1.11"));
        Validate.isTrue(isProduction("10.0.3.10"));
        
        Validate.isTrue(isProduction("10.1.1.174"));
        Validate.isTrue(isProduction("10.1.2.222"));
        Validate.isTrue(isProduction("10.1.3.10"));
        Validate.isTrue(isProduction("10.1.3.100"));
        Validate.isTrue(isProduction("10.1.3.222"));
        System.out.println("all done!");
    }
}
