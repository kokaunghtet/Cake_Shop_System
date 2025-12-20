package com.cakeshopsystem.utils;

public class EnumHelper {
    public static String stringToEnum(String value) {
        return value.replace(' ', '_');
    }

    public static String enumToString(String value) {
        return value.charAt(0) + value.substring(1).replace('_', ' ');
    }
}
