package com.demo.util.date;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Created by Arthur on 2017/1/21 0021.
 */
public class LocalDateUtil {

    public static LocalDate convertToLocalDate(Date date){
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime convertToLocalDateTime(Date date){
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime convertToLocalDateTime(long timestamp){
        return LocalDateTime.ofEpochSecond(timestamp,0,ZoneOffset.ofHours(8));
    }

    public static LocalTime convertToLocalTime(Date date){
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    public static LocalDate convertToLocalDate(String dateStr,String format){
        return LocalDate.parse(dateStr,DateTimeFormatter.ofPattern(format));
    }

    public static LocalDateTime convertToLocalDateTime(String dateStr,String format){
        return LocalDateTime.parse(dateStr,DateTimeFormatter.ofPattern(format));
    }

    public static Date convertToDate(LocalDate localDate){
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date convertToDate(LocalDateTime localDateTime){
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static long convertToLong(LocalDate localDate){
        return localDate.toEpochDay();
    }

    public static long convertToLong(LocalDateTime localDateTime){
        return localDateTime.toEpochSecond(ZoneOffset.ofHours(8));
    }

    public static String format(LocalDate localDate,String format){
        return localDate.format(DateTimeFormatter.ofPattern(format));
    }

    public static String format(LocalDateTime localDateTime,String format){
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    public static long diffYears(LocalDate startDate,LocalDate endDate){
        return startDate.until(endDate, ChronoUnit.YEARS);
    }

    public static long diffMonths(LocalDate startDate,LocalDate endDate){
        return startDate.until(endDate, ChronoUnit.MONTHS);
    }

    public static long diffDays(LocalDate startDate,LocalDate endDate){
        return startDate.until(endDate, ChronoUnit.DAYS);
    }

    public static long diffWeeks(LocalDate startDate,LocalDate endDate){
        return startDate.until(endDate, ChronoUnit.WEEKS);
    }

    public static long diffHours(LocalDateTime startTime,LocalDateTime endTime){
        return startTime.until(endTime,ChronoUnit.HOURS);
    }

    public static long diffMinutes(LocalDateTime startTime,LocalDateTime endTime){
        return startTime.until(endTime,ChronoUnit.MINUTES);
    }

    public static long diffSeconds(LocalDateTime startTime,LocalDateTime endTime){
        return startTime.until(endTime,ChronoUnit.SECONDS);
    }

    public static Period diffPeriod(LocalDate startDate,LocalDate endDate){
        return Period.between(startDate,endDate);
    }




    /**
     *
     * @Description: 结束日期会+1，所以开始日期必须小于结束日期
     * @param startDate
     * @param endDate
     * @param pattern
     * @return
     * @throws
     */
    public static boolean isBefore(String startDate,String endDate,String pattern){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        return start.isBefore(end);
    }

    /**
     *
     * @Description: 结束日期会+1，比如统计9月8号当天的数据，时间区间应该是09-08至09-09
     * 			    所以跟当前日期比较时，应该把当前日期也+1
     * @param pattern
     * @param dates
     * @return
     * @throws
     */

    public static boolean compareToNow(String pattern,String... dates){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDate now = LocalDate.now().plusDays(1L);

        for (String dateStr : dates) {
            LocalDate date = LocalDate.parse(dateStr, formatter);
            if(date.isAfter(now)){
                return false;
            }
        }
        return true;
    }

}
