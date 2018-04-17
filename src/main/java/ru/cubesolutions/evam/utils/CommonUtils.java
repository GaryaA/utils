package ru.cubesolutions.evam.utils;

import java.util.Collection;

/**
 * Created by Garya on 16.02.2018.
 */
public class CommonUtils {

    private CommonUtils() {
    }

    public static boolean isNull(Object o) {
        return o == null;
    }

    public static boolean isNotNull(Object o) {
        return o != null;
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNullOrEmpty(Collection<?> s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> s) {
        return s != null && !s.isEmpty();
    }

    public static boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }

}
