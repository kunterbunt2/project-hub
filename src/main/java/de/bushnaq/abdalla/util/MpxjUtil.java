package de.bushnaq.abdalla.util;

import de.bushnaq.abdalla.util.date.DateUtil;
import net.sf.mpxj.Duration;
import net.sf.mpxj.TimeUnit;

public class MpxjUtil {

    public static String createDurationString(Duration duration, boolean aUseSeconds, boolean aUseCharacters, boolean aPrintLeadingZeros) {
        if (duration == null) {
            return "NA";
        }
        long seconds = Math.round(duration.getDuration() * timeUnitToSeconds(duration.getUnits()));
        return DateUtil.createDurationString(java.time.Duration.ofSeconds(seconds), aUseSeconds, aUseCharacters, aPrintLeadingZeros);
    }

    public static long timeUnitToSeconds(TimeUnit tu) {
        long factor;
        switch (tu) {
            case MINUTES:
                factor = 60;
                break;
            case HOURS:
                factor = 60L * 60;
                break;
            case DAYS:
                factor = 75L * 6 * 60;
                break;
            case WEEKS:
                factor = 7L * 24 * 60 * 60;
                break;
            default:
                throw new IllegalArgumentException(String.format("Unexpected TimeUnit '%s'.", tu.name()));
        }
        return factor;
    }

    public static java.time.Duration toJavaDuration(Duration duration) {
        if (duration == null) {
            return null;
        }
        long secondsDuration = Math.round(duration.getDuration() * timeUnitToSeconds(duration.getUnits()));
        return java.time.Duration.ofSeconds(secondsDuration);
    }

    public static Duration toMpjxDuration(java.time.Duration duration) {
        return Duration.getInstance(((double) duration.getSeconds()) / 60, TimeUnit.MINUTES);
    }

}
