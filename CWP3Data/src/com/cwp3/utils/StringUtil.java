package com.cwp3.utils;

import java.util.*;

/**
 * Created by csw on 2017/9/20.
 * Description:
 */
public class StringUtil {

    public static String getKey(Long id, String str) {
        return id.toString() + "@" + str;
    }

    public static String getKey(String id, String str) {
        return id + "@" + str;
    }

    public static String getKey(String str, Integer id) {
        return str + "@" + id.toString();
    }

    public static String getKey(Integer id, String str) {
        return id.toString() + "@" + str;
    }

    public static String getKey(Integer id1, Integer id2) {
        return id1.toString() + "@" + id2.toString();
    }

    public static String getSecondKey(String key) {
        return key.split("@")[1];
    }

    public static String getFirstKey(String key) {
        return key.split("@")[0];
    }

    public static boolean isNotBlank(String str) {
        return str != null && !"".equals(str.trim());
    }

    public static List<Long> getTwoTimeValues(String timeStr) {
        List<Long> values = new LinkedList<>();
        if (StringUtil.isNotBlank(timeStr)) {
            if (timeStr.contains(",")) {
                values.add(Long.valueOf(timeStr.split(",")[0]));
                values.add(Long.valueOf(timeStr.split(",")[1]));
            } else {
                values.add(Long.valueOf(timeStr));
                values.add(Long.valueOf(timeStr));
            }
        }
        return values;
    }

    public static List<Long> getFourTimeValues(String timeStr) {
        List<Long> values = new LinkedList<>();
        if (StringUtil.isNotBlank(timeStr)) {
            if (timeStr.contains("/")) {
                List<Long> values0 = getTwoTimeValues(timeStr.split("/")[0]);
                List<Long> values1 = getTwoTimeValues(timeStr.split("/")[1]);
                values.addAll(values0);
                values.addAll(values1);
            } else {
                List<Long> values1 = getTwoTimeValues(timeStr);
                values.addAll(values1);
                values.addAll(values1);
            }
        }
        return values;
    }

    public static Set<String> getSetBySplit(String str, String s) {
        Set<String> strSet = new HashSet<>();
        if (isNotBlank(str) && isNotBlank(s)) {
            if (str.contains(s)) {
                String[] ss = str.split(s);
                strSet.addAll(Arrays.asList(ss));
            } else {
                strSet.add(str);
            }
        }
        return strSet;
    }
}
