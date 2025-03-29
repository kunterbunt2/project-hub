package de.bushnaq.abdalla.projecthub.report.dao;

import java.awt.*;
import java.time.LocalDate;


public class GraphColorUtil {

    public static Color getDayOfWeekColor(BurnDownGraphicsTheme graphicsTheme, LocalDate startCal) {

        switch (startCal.getDayOfWeek()) {
            case FRIDAY:
                return graphicsTheme.fridayColor;
            case MONDAY:
                return graphicsTheme.mondayColor;
            case SATURDAY:
                return graphicsTheme.saturdayColor;
            case SUNDAY:
                return graphicsTheme.sundayColor;
            case THURSDAY:
                return graphicsTheme.thursdayColor;
            case TUESDAY:
                return graphicsTheme.tuesdayColor;
            case WEDNESDAY:
                return graphicsTheme.wednesdayColor;
            default:
                return null;

        }

    }

    public static Color getDayOfWeekStripeColor(BurnDownGraphicsTheme graphicsTheme/*, Map<LocalDate, String> bankHolidays*/, LocalDate startCal) {
        //TODO: bank holidays
//        if (bankHolidays.get(startCal) != null) {
//            return graphicsTheme.sundayStripeColor;
//        }

        switch (startCal.getDayOfWeek()) {
            case FRIDAY:
                return graphicsTheme.fridayStripeColor;
            case MONDAY:
                return graphicsTheme.wednesdayColor;
            case SATURDAY:
                return graphicsTheme.saturdayStripeColor;
            case SUNDAY:
                return graphicsTheme.sundayStripeColor;
            case THURSDAY:
                return graphicsTheme.thursdayStripeColor;
            case TUESDAY:
                return graphicsTheme.tuesdayStripeColor;
            case WEDNESDAY:
                return graphicsTheme.wednesdayStripeColor;
            default:
                return null;

        }

    }
}
