/*
 *
 * Copyright (C) 2025-2025 Abdalla Bushnaq
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.bushnaq.abdalla.projecthub.report.dao;

import de.bushnaq.abdalla.projecthub.dto.OffDayType;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectCalendarException;

import java.awt.*;
import java.time.LocalDate;


public class GraphColorUtil {

    public static String getDayLetter(BurnDownGraphicsTheme graphicsTheme, ProjectCalendar pc, LocalDate currentDate) {
        if (!pc.isWorkingDate(currentDate)) {
            ProjectCalendarException exception = pc.getException(currentDate);
            if (exception != null) {
                if (exception.getName().equals(OffDayType.VACATION.name())) {
                    return "V";
                } else if (exception.getName().equals(OffDayType.TRIP.name())) {
                    return "T";
                } else if (exception.getName().equals(OffDayType.SICK.name())) {
                    return "S";
                } else {
                    return "H";
                }
            }
        }
        return null;
    }

//    public static Color getDayOfWeekColor(BurnDownGraphicsTheme graphicsTheme, LocalDate startCal) {
//
//        switch (startCal.getDayOfWeek()) {
//            case FRIDAY:
//                return graphicsTheme.fridayColor;
//            case MONDAY:
//                return graphicsTheme.ganttMondayColor;
//            case SATURDAY:
//                return graphicsTheme.saturdayColor;
//            case SUNDAY:
//                return graphicsTheme.sundayColor;
//            case THURSDAY:
//                return graphicsTheme.thursdayColor;
//            case TUESDAY:
//                return graphicsTheme.tuesdayColor;
//            case WEDNESDAY:
//                return graphicsTheme.wednesdayColor;
//            default:
//                return null;
//
//        }
//
//    }

    public static Color getDayOfWeekStripeColor(BurnDownGraphicsTheme graphicsTheme/*, Map<LocalDate, String> bankHolidays*/, LocalDate startCal) {
        //TODO: bank holidays
//        if (bankHolidays.get(startCal) != null) {
//            return graphicsTheme.sundayStripeColor;
//        }

        switch (startCal.getDayOfWeek()) {
            case FRIDAY:
                return graphicsTheme.fridayStripeColor;
            case MONDAY:
                return graphicsTheme.ganttMondayColor;
            case SATURDAY:
                return graphicsTheme.saturdayStripeColor;
            case SUNDAY:
                return graphicsTheme.ganttSundayStripeColor;
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

    public static Color getGanttDayStripeColor(BurnDownGraphicsTheme graphicsTheme, ProjectCalendar pc, LocalDate currentDate) {
        if (pc.isWorkingDate(currentDate)) {
            return graphicsTheme.ganttMondayColor;
        } else {
            ProjectCalendarException exception = pc.getException(currentDate);
            if (exception != null) {
                if (exception.getName().equals(OffDayType.VACATION.name())) {
                    return graphicsTheme.ganttVacationColor;
                } else if (exception.getName().equals(OffDayType.TRIP.name())) {
                    return graphicsTheme.ganttTripColor;
                } else if (exception.getName().equals(OffDayType.SICK.name())) {
                    return graphicsTheme.ganttSickColor;
                } else {
                    return graphicsTheme.ganttHolidayColor;
                }
            }
            return graphicsTheme.ganttSundayStripeColor;
        }
    }
}
