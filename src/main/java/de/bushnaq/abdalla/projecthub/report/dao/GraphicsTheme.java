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

public class GraphicsTheme {
    public static final Color  COLOR_DARK_GREEN            = new Color(0x095c09);
    public static final Color  COLOR_DARK_RED              = new Color(0xcc4a31);
    public static final Color  COLOR_GOLD                  = new Color(0xffcc00);
    public static final Color  COLOR_MAGENTA               = new Color(0xe5127d);
    public              Color  calendarYearBackgroundColor = new Color(0xf7f7f7);
    public              Color  calendarYearBoderColor      = Color.white;
    public              Color  calendarYearTextColor       = new Color(80, 80, 80);  // Dark gray for year display
    public              Color  chartBackgroundColor;
    public              Color  chartBorderColor;
    public              ETheme cssTheme                    = ETheme.light;
    public              Color  fillingDayTextColor         = new Color(0xe2dbdb); // very Light gray for filling days before and after the days we are interested in
    public              Color  ganttTaskTickLineColor      = new Color(183, 216, 240);
    public              Color  ganttTaskTickTextColor      = new Color(0, 0, 0, 127);
    public              Color  holidayBgColor              = new Color(183, 216, 240);  // Light blue
    public              Color  holidayColor                = new Color(183, 216, 240);  // Light blue;
    public              Color  holidayTextColor            = new Color(123, 180, 200);  // Light blue
    public              Color  monthNameColor              = new Color(0xff6336);  // Red for month names
    public              Color  normalDayTextColor          = new Color(0x323232);  // almost black
    public              Color  sickBgColor                 = new Color(0xfff2e8); // Light red
    public              Color  sickColor                   = new Color(0xfff2e8); // Light red;
    public              Color  sickTextColor               = new Color(0xff6d5b); // Light red
    public              Color  todayBgColor                = new Color(0xff3a30);  // Red circle for today
    public              Color  todayTextColor              = Color.white;  // White text for today
    public              Color  tripBgColor                 = new Color(183, 183, 183);// new Color(0xfffcea);  // Light yellow
    public              Color  tripColor                   = new Color(183, 183, 183);//new Color(0xfffcea);  // Light yellow;
    public              Color  tripTextColor               = new Color(64, 64, 64);//new Color(0xff931e);  // Light yellow
    public              Color  vacationBgColor             = new Color(183, 240, 216);  // Light green
    public              Color  vacationColor               = new Color(183, 240, 216);  // Light green;
    public              Color  vacationTextColor           = new Color(123, 200, 180);  // Light green
    public              Color  weekDayBgColor              = new Color(0xf7f7f7);
    public              Color  weekDayTextColor            = new Color(180, 180, 180);  // Light gray for weekends
    public              Color  weekendBgColor              = null;
    public              Color  weekendTextColor            = new Color(180, 180, 180);  // Light gray for weekends
//    public              Color  yearTextColor;

}
