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

public class ScheduleGraphicsTheme extends GraphicsTheme {
    protected static final int   MAX_HEAT_COLOR = 6;
    public                 Color dayBackgroundColor;
    public                 Color dayBorderColor;
    public                 Color dayDiagramBorderColor;

    public Color dayOfMonthBackgroundColor;
    public Color dayOfMonthBorderColor;
    public Color dayOfMonthTextColor;
    public Color dayTextColor;

    public Color   delayClosedEventColor;
    public Color   delayEventColor;
    public Color   fridayColor;
    public Color   fridayStripeColor;
    public Color   futureEventColor;
    public Color   futureWeekendRequestColor;
    public Color   futureWorkDayRequestColor;
    public Color   ganttCriticalRelationColor;
    public Color   ganttCriticalTaskBorderColor;
    public Color   ganttGridColor;
    public Color   ganttIdColor;
    public Color   ganttIdErrorColor;
    public Color   ganttIdTextColor;
    public Color   ganttIdTextErrorColor;
    public Color   ganttMilestoneColor;
    public Color   ganttMilestoneTextColor;
    public Color   ganttOutOfOfficeColor;
    public Color   ganttRelationColor;
    public Color   ganttStoryColor;
    public Color   ganttStoryTextColor;
    public Color   ganttTaskBorderColor;
    public Color   ganttTaskTextColor;
    public Color[] heatColor = new Color[MAX_HEAT_COLOR];

    public Color inTimeColor;
    public Color linkColor;

    public Color milestoneFlagColor;
    public Color milestoneTextColor;
    public Color mondayColor;

    public Color   mondayStripeColor;
    public Color   monthBorderColor;
    public Color[] monthColor = new Color[12];
    public Color   monthTextColor;
    //    public Color monthTextColor;
    public Color   nowEventColor;
    public Color   pastEventColor;
    public Color   pastWeekendRequestColor;
    public Color   pastWorkDayRequestColor;
    public Color   saturdayColor;
    public Color   saturdayStripeColor;
    public Color   sundayColor;
    public Color   sundayStripeColor;

    public Color thursdayColor;
    public Color thursdayStripeColor;
    public Color ticksBackgroundColor;

    public Color tuesdayColor;
    public Color tuesdayStripeColor;
    public Color wednesdayColor;

    public Color wednesdayStripeColor;
    public Color weekBackgroundColor;
    public Color weekBoderColor;
    public Color weekTextColor;
    public Color yearBackgroundColor;
    public Color yearBoderColor;
    public Color yearTextColor;//already exists in parent class

}
