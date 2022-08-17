package tr.com.sgveteris.coin.util;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeFormatter;

@Component
public class DateUtil implements Serializable {

    private static final String YYYY_MM_DD_FORMAT = "yyyyMMdd";
    private static final String YYYY_MM_FORMAT = "yyyyMM";
    private static final String ZONE_ID = "Europe/Istanbul";
    private static final String MS_DATE_PATTERN ="yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String HH_MM_SS_FORMAT = "HHmmss";
    private static final String HH_MM_SS_REGEX_CHANGE_DATA = "..(?!$)";
    private static final String HH_MM_SS_REGEX_CHANGE_VALUE = "$0:";
    private static final String ZONE_STR = "Z";

    private static final String YYYY_MM_DD_hh_mm_ss_FORMAT="yyyy-MM-dd'T'HH:mm:ss";


    public static String getFormattedDateString(LocalDate date, String pattern) {
        if (date == null || pattern == null)
            return null;

        var dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return dateTimeFormatter.format(date);
    }

    public static String getFormattedTimeString(LocalTime time, String pattern) {
        if (time == null || pattern == null)
            return null;

        var dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return dateTimeFormatter.format(time);
    }

    public static String getFormattedDateString(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null)
            return null;

        var dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return dateTimeFormatter.format(dateTime);
    }

    public static String getFormattedDateString(YearMonth yearMonth, String pattern) {
        if (yearMonth == null || pattern == null)
            return null;

        var dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return dateTimeFormatter.format(yearMonth);
    }

    public static String getZoneFormatDate(LocalDate localDate,String localTimeStr){
        if (localDate == null || localTimeStr == null)
            return null;
        String formatLocalTimeStr = localTimeStr.replaceAll(HH_MM_SS_REGEX_CHANGE_DATA,HH_MM_SS_REGEX_CHANGE_VALUE);
        LocalDateTime localDateTime = LocalDateTime.of(localDate,LocalTime.parse(formatLocalTimeStr));
        return localDateTime.format(DateTimeFormatter.ofPattern(MS_DATE_PATTERN)).concat(ZONE_STR);
    }

    public static String getLocalDateWithFormat(LocalDate localDate,String charText){
        if (localDate == null || charText == null)
            return null;
        String formatText = charText.replaceAll("..(?!$)", "$0:");
        LocalDateTime localDateTime = LocalDateTime.of(localDate,LocalTime.parse(formatText));
        return localDateTime.format(DateTimeFormatter.ofPattern(DateUtil.YYYY_MM_DD_hh_mm_ss_FORMAT));
    }

}