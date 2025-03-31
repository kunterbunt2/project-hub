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
