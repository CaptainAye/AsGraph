package org.asgraph;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Tomek on 2017-01-14.
 */
public class RegexCheck {

    private static final String DIGIT_REGEX = "[0-9]+";
    private static final String IP_REGEX = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static final String WHITESPACE_REGEX = "\\s*";
    private static final String MS_PATTERN = "ms";
    private static final String PRIVATE_IP_REGEX = "^(?:10|127|172\\.(?:1[6-9]|2[0-9]|3[01])|192\\.168)\\..*";


    public static boolean checkMS(String toCheck){
        return check(MS_PATTERN,toCheck);
    }

    public static boolean checkPrivateIp(String toCheck){
        return check(PRIVATE_IP_REGEX, toCheck);
    }

    public static boolean checkInteger(String toCheck){
        return check(DIGIT_REGEX,toCheck);
    }

    public static boolean checkIp(String toCheck){
        return check(IP_REGEX,toCheck);

    }

    public static boolean checkWhitespace(String toCheck){
        return check(WHITESPACE_REGEX,toCheck);

    }



    private static boolean check(String regex,String toCheck){
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(toCheck).matches();
    }
}
