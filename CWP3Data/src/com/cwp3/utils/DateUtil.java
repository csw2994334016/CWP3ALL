package com.cwp3.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Created by csw on 2017/12/13.
 * Description:
 */
public class DateUtil {

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static long getSecondTime(Date date) {
        return date.getTime() / 1000;
    }

    public static long getSecondTimeByFormatStr(String formatStr) {
        return getSecondTime(Objects.requireNonNull(getDateByFormatStr(formatStr)));
    }

    public static Date getDateByFormatStr(String formatStr) {
        try {
            return sdf.parse(formatStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
