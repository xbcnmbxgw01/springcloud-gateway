/*
 * Copyright 2017 ~ 2025 the original author or authors. <springcloudgateway@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springcloud.gateway.core.lang;

import static org.springcloud.gateway.core.lang.Assert2.notNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * Date enhanced utility.
 * 
 * @author springcloudgateway@gmail.com
 * @version v1.0.0
 * @since
 */
public abstract class DateUtils2 extends DateUtils {

    public static final String[] parsePatterns = { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss",
            "yyyy.MM.dd HH:mm", "yyyy.MM" };
    public static final String UTC = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String YMD_HMS = "yyyy-MM-dd HH:mm:ss";
    public static final String YMD = "yyyy-MM-dd";
    public static final String YMDHM = "yyyyMMddHHmm";

    /**
     * Gets the natural date list. The policy is 'DAY_OF_MONTH'.
     * 
     * @param startDate
     * @param endDate
     * @return
     * @throws Exception
     */
    public static List<String> getDates(String startDate, String endDate) throws Exception {
        Date d1 = new SimpleDateFormat(YMD).parse(startDate);// ??????????????????
        Date d2 = new SimpleDateFormat(YMD).parse(endDate);// ??????????????????
        Calendar dd = Calendar.getInstance();// ??????????????????
        dd.setTime(d1);// ????????????????????????
        ArrayList<String> dates = new ArrayList<String>();
        while (dd.getTime().getTime() <= d2.getTime()) {// ???????????????????????????
            SimpleDateFormat sdf = new SimpleDateFormat(YMD);
            String str = sdf.format(dd.getTime());
            dates.add(str);
            dd.add(Calendar.DAY_OF_MONTH, 1);// ???????????????????????????1
        }
        return dates;
    }

    /**
     * Convert timestamp to UTC
     * 
     * @param timestamp
     * @return
     */
    public static String timeToUTC(long timestamp) {
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df2.setTimeZone(TimeZone.getTimeZone("UTC"));
        String re = df2.format(new Date(timestamp));
        return re;
    }

    /**
     * Convert date string to UTC
     * 
     * @param date
     * @return
     */
    public static String dateToUTC(String date) {
        SimpleDateFormat sdf1 = new SimpleDateFormat(UTC);
        SimpleDateFormat sdf2 = new SimpleDateFormat(YMD_HMS);
        String str = null;
        try {
            str = sdf1.format(sdf2.parse(date));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return str;
    }

    public static String getNowTime() {
        SimpleDateFormat sf = new SimpleDateFormat(YMD_HMS);
        return sf.format(new Date());
    }

    public static String getNowTime(String format) {
        SimpleDateFormat sf = new SimpleDateFormat(format);
        return sf.format(new Date());
    }

    public static String ymdhmsToymd(String date) {
        return date.substring(0, 10);
    }

    /**
     * Seconds before getting the current time
     * 
     * @param beforeSec
     *            A few seconds ago
     * @return
     */
    public static String getCurrentBeforeTimeBySec(int beforeSec) {
        SimpleDateFormat sdf = new SimpleDateFormat(YMD_HMS);
        Calendar beforeTime = Calendar.getInstance();
        beforeTime.add(Calendar.SECOND, beforeSec);// ????????????????????????
        Date beforeD = beforeTime.getTime();
        String time = sdf.format(beforeD);
        return time;
    }

    /**
     * Minutes before getting the current time
     * 
     * @param beforeMin
     *            A few minutes ago
     * @return
     */
    public static String getCurrentBeforeTimeByMin(int beforeMin) {
        SimpleDateFormat sdf = new SimpleDateFormat(YMD_HMS);
        Calendar beforeTime = Calendar.getInstance();
        beforeTime.add(Calendar.MINUTE, beforeMin);// ????????????????????????
        Date beforeD = beforeTime.getTime();
        String time = sdf.format(beforeD);
        return time;
    }

    /**
     * Hours before getting the current time
     * 
     * @param beforeHour
     *            A few hours ago
     * @return
     */
    public static String getCurrentBeforeTimeByHour(int beforeHour) {
        SimpleDateFormat sdf = new SimpleDateFormat(YMD_HMS);
        Calendar beforeTime = Calendar.getInstance();
        beforeTime.add(Calendar.HOUR, beforeHour);
        Date beforeD = beforeTime.getTime();
        String time = sdf.format(beforeD);
        return time;
    }

    /**
     * ??????????????????????????? ?????????yyyy-MM-dd???
     */
    public static String getDate() {
        return getDate("yyyy-MM-dd");
    }

    /**
     * ??????????????????????????? ?????????yyyy-MM-dd??? pattern????????????"yyyy-MM-dd" "HH:mm:ss" "E"
     */
    public static String getDate(String pattern) {
        return DateFormatUtils.format(new Date(), pattern);
    }

    /**
     * ????????????????????? ???????????????yyyy-MM-dd??? pattern????????????"yyyy-MM-dd" "HH:mm:ss" "E"
     */
    public static String formatDate(Date date, Object... pattern) {
        notNull(date, "null Date");
        if (pattern != null && pattern.length > 0) {
            return DateFormatUtils.format(date, pattern[0].toString());
        }
        return DateFormatUtils.format(date, "yyyy-MM-dd");
    }

    /**
     * ?????????????????????????????????????????????yyyy-MM-dd HH:mm:ss???
     */
    public static String formatDateTime(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * ??????????????????????????? ?????????HH:mm:ss???
     */
    public static String getTime() {
        return formatDate(new Date(), "HH:mm:ss");
    }

    /**
     * ???????????????????????????????????? ?????????yyyy-MM-dd HH:mm:ss???
     */
    public static String getDateTime() {
        return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * ??????????????????????????? ?????????yyyy???
     */
    public static String getYear() {
        return formatDate(new Date(), "yyyy");
    }

    /**
     * ??????????????????????????? ?????????MM???
     */
    public static String getMonth() {
        return formatDate(new Date(), "MM");
    }

    /**
     * ????????????????????? ?????????dd???
     */
    public static String getDay() {
        return formatDate(new Date(), "dd");
    }

    /**
     * ??????????????????????????? ?????????E????????????
     */
    public static String getWeek() {
        return formatDate(new Date(), "E");
    }

    /**
     * ????????????????????????????????? ?????? { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm",
     * "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy.MM.dd",
     * "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm" }
     */
    public static Date parseDate(Object str) {
        if (str == null || str.toString().trim().length() <= 0) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * ?????????????????????
     * 
     * @param date
     * @return
     */
    public static long pastDays(Date date) {
        long t = new Date().getTime() - date.getTime();
        return t / (24 * 60 * 60 * 1000);
    }

    /**
     * ?????????????????????
     * 
     * @param date
     * @return
     */
    public static long pastHour(Date date) {
        long t = new Date().getTime() - date.getTime();
        return t / (60 * 60 * 1000);
    }

    /**
     * ?????????????????????
     * 
     * @param date
     * @return
     */
    public static long pastMinutes(Date date) {
        long t = new Date().getTime() - date.getTime();
        return t / (60 * 1000);
    }

    /**
     * ?????????????????????,???:???:???.?????????
     * 
     * @param timeMillis
     * @return
     */
    public static String formatDateTime(long timeMillis) {
        long day = timeMillis / (24 * 60 * 60 * 1000);
        long hour = (timeMillis / (60 * 60 * 1000) - day * 24);
        long min = ((timeMillis / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (timeMillis / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        long sss = (timeMillis - day * 24 * 60 * 60 * 1000 - hour * 60 * 60 * 1000 - min * 60 * 1000 - s * 1000);
        return (day > 0 ? day + "," : "") + hour + ":" + min + ":" + s + "." + sss;
    }

    /**
     * ?????????????????????????????????
     * 
     * @param before
     * @param after
     * @return
     */
    public static double getDistanceOfTwoDate(Date before, Date after) {
        long beforeTime = before.getTime();
        long afterTime = after.getTime();
        return (afterTime - beforeTime) / (1000 * 60 * 60 * 24);
    }

}