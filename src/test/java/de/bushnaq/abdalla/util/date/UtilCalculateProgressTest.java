package de.bushnaq.abdalla.util.date;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilCalculateProgressTest {
    private static final double            EPSILON   = 0.001d;
    static               DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Test
    public void fullProgressTest() {
        Duration worked    = Duration.ofMinutes(10 * ReportUtil.ONE_WORKING_DAY_MINUTES);
        Duration remaining = Duration.ofMinutes(0L);
        //        Long worked = new Long(10 * Util.ONE_WORKING_DAY_MILLIS);
        //        long remaining = 0 * Util.ONE_WORKING_DAY_MILLIS;
        double progress = ReportUtil.calcualteProgress(worked, worked.plus(remaining));
        assertEquals(1.0, progress, EPSILON);
    }

    @Test
    public void halfProgressTest() {
        Duration worked    = Duration.ofMinutes(5 * ReportUtil.ONE_WORKING_DAY_MINUTES);
        Duration remaining = Duration.ofMinutes(5 * ReportUtil.ONE_WORKING_DAY_MINUTES);
        //        Long worked = new Long(5 * Util.ONE_WORKING_DAY_MILLIS);
        //        long remaining = 5 * Util.ONE_WORKING_DAY_MILLIS;
        double progress = ReportUtil.calcualteProgress(worked, worked.plus(remaining));
        assertEquals(0.5, progress, EPSILON);
    }

    @Test
    public void noProgressTest() {
        Duration worked    = Duration.ofMinutes(0L);
        Duration remaining = Duration.ofMinutes(10 * ReportUtil.ONE_WORKING_DAY_MINUTES);
        //        Long worked = new Long(0 * Util.ONE_WORKING_DAY_MILLIS);
        //        long remaining = 10 * Util.ONE_WORKING_DAY_MILLIS;
        double progress = ReportUtil.calcualteProgress(worked, worked.plus(remaining));
        assertEquals(0.0, progress, EPSILON);
    }

    @Test
    public void nullRemainingTest() {
        Duration worked = Duration.ofMinutes(5 * ReportUtil.ONE_WORKING_DAY_MINUTES);
        //        Duration remaining = null;
        //        Long worked = new Long(5 * Util.ONE_WORKING_DAY_MILLIS);
        //        Long remaining = null;
        double progress = ReportUtil.calcualteProgress(worked, null);
        assertEquals(-1, progress, EPSILON);
    }

    @Test
    public void nullWorkedTest() {
        Duration worked    = null;
        Duration remaining = Duration.ofMinutes(5 * ReportUtil.ONE_WORKING_DAY_MINUTES);
        double   progress  = ReportUtil.calcualteProgress(worked, remaining);
        assertEquals(-1, progress, EPSILON);
    }

}
