package com.cn.psys.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtils {

    private static final String defaultDateFormatStr = "yyyy-MM-dd";// 系统默认的格式化字符串
    private static final String defaultTimeFormatStr = "yyyy-MM-dd HH:mm:ss";// 系统默认的格式化字符串

    public static String activityDateToString(Date formatStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(defaultTimeFormatStr);
        String timeFormat = sdf.format(formatStr);
        return timeFormat;
    }



}
