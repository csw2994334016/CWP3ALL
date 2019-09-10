package com.cwp3.utils;

/**
 * Created by csw on 2017/11/15.
 * Description:
 */
public class CompareUtil {

    public static boolean inEqualScope(Double value, Double from, Double to) {
        return value.compareTo(from) >= 0 && value.compareTo(to) <= 0;
    }

    public static boolean inEqualScope(int value, int from, int to) {
        return from < to ? value >= from && value <= to : value >= to && value <= from;
    }
}
