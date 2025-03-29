package de.bushnaq.abdalla.util.date;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * calcualteEfficiency calculation.
 * calcualteWorkDaysMiliseconsDelay calculation
 */
public class AddNoneWorkingDaysTest {
    static DateTimeFormatter farmatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Test
    public void endOnSaturdayTest() {
        LocalDateTime start = LocalDateTime.parse("2018-01-09 08:00", farmatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.JANUARY, 9, 8, 0);
        LocalDateTime end            = DateUtil.addNoneWorkingDays(start, 60);
        LocalDateTime expectedResult = LocalDateTime.parse("2018-04-02 08:00", farmatter);
        //        Calendar expectedResult = new GregorianCalendar(2018, Calendar.APRIL, 2, 8, 0);
        assertEquals(expectedResult.format(farmatter), end.format(farmatter));
    }

    @Test
    public void endOnSundayTest() {
        LocalDateTime start = LocalDateTime.parse("2018-01-09 08:00", farmatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.JANUARY, 9, 8, 0);
        LocalDateTime end            = DateUtil.addNoneWorkingDays(start, 61);
        LocalDateTime expectedResult = LocalDateTime.parse("2018-04-03 08:00", farmatter);
        //        Calendar expectedResult = new GregorianCalendar(2018, Calendar.APRIL, 3, 8, 0);
        assertEquals(expectedResult.format(farmatter), end.format(farmatter));
    }

    @Test
    public void noWeekendTest() {
        LocalDateTime start = LocalDateTime.parse("2018-03-12 08:00", farmatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.MARCH, 12, 8, 0);
        LocalDateTime end            = DateUtil.addNoneWorkingDays(start, 5);
        LocalDateTime expectedResult = LocalDateTime.parse("2018-03-16 08:00", farmatter);
        //        Calendar expectedResult = new GregorianCalendar(2018, Calendar.MARCH, 16, 8, 0);
        assertEquals(expectedResult.format(farmatter), end.format(farmatter));
    }

    @Test
    public void oneWeekendTest() {
        LocalDateTime start = LocalDateTime.parse("2018-03-12 08:00", farmatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.MARCH, 12, 8, 0);
        LocalDateTime end            = DateUtil.addNoneWorkingDays(start, 10);
        LocalDateTime expectedResult = LocalDateTime.parse("2018-03-23 08:00", farmatter);
        //        Calendar expectedResult = new GregorianCalendar(2018, Calendar.MARCH, 23, 8, 0);
        assertEquals(expectedResult.format(farmatter), end.format(farmatter));
    }

    @Test
    public void startOnSaturdayTest() {
        LocalDateTime start = LocalDateTime.parse("2018-03-10 08:00", farmatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.MARCH, 10, 8, 0);
        LocalDateTime end            = DateUtil.addNoneWorkingDays(start, 10);
        LocalDateTime expectedResult = LocalDateTime.parse("2018-03-23 08:00", farmatter);
        //        Calendar expectedResult = new GregorianCalendar(2018, Calendar.MARCH, 23, 8, 0);
        assertEquals(expectedResult.format(farmatter), end.format(farmatter));
    }

    @Test
    public void startOnSundayTest() {
        LocalDateTime start = LocalDateTime.parse("2018-03-11 08:00", farmatter);
        //        Calendar start = new GregorianCalendar(2018, Calendar.MARCH, 11, 8, 0);
        LocalDateTime end            = DateUtil.addNoneWorkingDays(start, 10);
        LocalDateTime expectedResult = LocalDateTime.parse("2018-03-23 08:00", farmatter);
        //        Calendar expectedResult = new GregorianCalendar(2018, Calendar.MARCH, 23, 8, 0);
        assertEquals(expectedResult.format(farmatter), end.format(farmatter));
    }

}
