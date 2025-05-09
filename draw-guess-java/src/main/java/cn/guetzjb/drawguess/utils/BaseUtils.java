package cn.guetzjb.drawguess.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BaseUtils {

    public String generateRandomAvatar(String username) {
        return "https://robohash.org/" + username;
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

}
