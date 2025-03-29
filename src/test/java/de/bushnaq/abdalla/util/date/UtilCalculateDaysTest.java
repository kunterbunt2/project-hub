package de.bushnaq.abdalla.util.date;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilCalculateDaysTest {
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Test
    public void endOnSaturdayTest() {
        LocalDateTime start = LocalDateTime.parse("2018-01-09 08:00", formatter);
        LocalDateTime end   = LocalDateTime.parse("2018-04-02 08:00", formatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.JANUARY, 9, 8, 0);
        //        Calendar end = new GregorianCalendar(2018, Calendar.APRIL, 2, 8, 0);
        int workingDays = DateUtil.calculateDays(start, end);
        assertEquals(60 + 24 - 1, workingDays);
    }

    @Test
    public void endOnSundayTest() {
        LocalDateTime start = LocalDateTime.parse("2018-01-09 08:00", formatter);
        LocalDateTime end   = LocalDateTime.parse("2018-04-03 08:00", formatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.JANUARY, 9, 8, 0);
        //        Calendar end = new GregorianCalendar(2018, Calendar.APRIL, 3, 8, 0);
        int workingDays = DateUtil.calculateDays(start, end);
        assertEquals(61 + 24 - 1, workingDays);
    }

    @Test
    public void noWeekendTest() {
        LocalDateTime start = LocalDateTime.parse("2018-03-12 08:00", formatter);
        LocalDateTime end   = LocalDateTime.parse("2018-03-16 08:00", formatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.MARCH, 12, 8, 0);
        //        Calendar end = new GregorianCalendar(2018, Calendar.MARCH, 16, 8, 0);
        int workingDays = DateUtil.calculateDays(start, end);
        assertEquals(5 - 1, workingDays);
    }

    @Test
    public void oneWeekendTest() {
        LocalDateTime start = LocalDateTime.parse("2018-03-12 08:00", formatter);
        LocalDateTime end   = LocalDateTime.parse("2018-03-23 08:00", formatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.MARCH, 12, 8, 0);
        //        Calendar end = new GregorianCalendar(2018, Calendar.MARCH, 23, 8, 0);
        int workingDays = DateUtil.calculateDays(start, end);
        assertEquals(10 + 2 - 1, workingDays);
    }

    @Test
    public void sameTime() {
        LocalDateTime start = LocalDateTime.parse("2018-10-28 00:00", formatter);
        LocalDateTime end   = LocalDateTime.parse("2018-10-28 00:00", formatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.OCTOBER, 28, 0, 0);
        //        Calendar end = new GregorianCalendar(2018, Calendar.OCTOBER, 28, 0, 0);
        int workingDays = DateUtil.calculateDays(start, end);
        assertEquals(0, workingDays);
    }

    @Test
    public void severalWeekendsTest() {
        LocalDateTime start = LocalDateTime.parse("2018-08-28 08:00", formatter);
        LocalDateTime end   = LocalDateTime.parse("2018-09-13 08:00", formatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.AUGUST, 28, 11, 17);
        //        Calendar end = new GregorianCalendar(2018, Calendar.SEPTEMBER, 13, 15, 53);
        int workingDays = DateUtil.calculateDays(start, end);
        assertEquals(13 + 4 - 1, workingDays);
    }

    @Test
    public void startOnSaturdayTest() {
        LocalDateTime start = LocalDateTime.parse("2018-03-10 08:00", formatter);
        LocalDateTime end   = LocalDateTime.parse("2018-03-23 08:00", formatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.MARCH, 10, 8, 0);
        //        Calendar end = new GregorianCalendar(2018, Calendar.MARCH, 23, 8, 0);
        int workingDays = DateUtil.calculateDays(start, end);
        assertEquals(10 + 4 - 1, workingDays);
    }

    @Test
    public void startOnSundayTest() {
        LocalDateTime start = LocalDateTime.parse("2018-03-11 08:00", formatter);
        LocalDateTime end   = LocalDateTime.parse("2018-03-23 08:00", formatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.MARCH, 11, 8, 0);
        //        Calendar end = new GregorianCalendar(2018, Calendar.MARCH, 23, 8, 0);
        int workingDays = DateUtil.calculateDays(start, end);
        assertEquals(10 + 3 - 1, workingDays);
    }

    @Test
    public void winterTimeTest() {
        LocalDateTime start = LocalDateTime.parse("2018-10-28 08:00", formatter);
        LocalDateTime end   = LocalDateTime.parse("2018-11-01 08:00", formatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.OCTOBER, 28, 11, 17);
        //        Calendar end = new GregorianCalendar(2018, Calendar.NOVEMBER, 1, 15, 53);
        int workingDays = DateUtil.calculateDays(start, end);
        assertEquals(5 - 1, workingDays);
    }

    @Test
    public void winterTimeTestAtMidnight() {
        LocalDateTime start = LocalDateTime.parse("2018-10-28 00:00", formatter);
        LocalDateTime end   = LocalDateTime.parse("2018-11-01 00:00", formatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.OCTOBER, 28, 0, 0);
        //        Calendar end = new GregorianCalendar(2018, Calendar.NOVEMBER, 1, 0, 0);
        int workingDays = DateUtil.calculateDays(start, end);
        assertEquals(5 - 1, workingDays);
    }

}
