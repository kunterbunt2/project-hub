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

package de.bushnaq.abdalla.projecthub.report.renderer;

import de.bushnaq.abdalla.projecthub.dao.Context;
import de.bushnaq.abdalla.projecthub.dto.OffDayType;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.report.dao.BurnDownGraphicsTheme;
import de.bushnaq.abdalla.svg.util.ExtendedGraphics2D;
import net.sf.mpxj.DayType;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectCalendarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * renders a gantt chart using ms project mpp file as base
 * Will make out of office time visible
 *
 * @author abdalla.bushnaq
 */
public class CalendarRenderer extends AbstractRenderer {
    final int DAY_SIZE              = 24;
    final int HEADER_HEIGHT         = 25;
    final int LEGEND_HEIGHT         = 24 * 2;
    final int MONTHS_PER_COL        = 3;
    final int MONTHS_PER_ROW        = 4;
    final int MONTH_GAP             = 20;
    final int MONTH_HEIGHT          = 180;
    final int MONTH_WIDTH           = 200;
    final int WEEKDAY_HEADER_HEIGHT = 20;
    final int YEAR_HEIGHT           = 20;
    int cellHeight = 16;
    int cellWidth  = 16;
    public          ExtendedGraphics2D    graphics2D;
    public          BurnDownGraphicsTheme graphicsTheme;
    protected final Logger                logger = LoggerFactory.getLogger(this.getClass());
    private final   LocalDateTime         now;
    private final   User                  user;

    public CalendarRenderer(Context context, User user, LocalDateTime now, String cssClass, BurnDownGraphicsTheme graphicsTheme) throws Exception {
        this.user          = user;
        this.now           = now;
        this.graphicsTheme = graphicsTheme;
        chartWidth         = MONTH_WIDTH * MONTHS_PER_ROW + MONTH_GAP * (MONTHS_PER_ROW - 1);
        chartHeight        = YEAR_HEIGHT + HEADER_HEIGHT + WEEKDAY_HEADER_HEIGHT + MONTH_HEIGHT * MONTHS_PER_COL + MONTH_GAP * (MONTHS_PER_COL - 1) + LEGEND_HEIGHT;
    }

    protected int calculateChartHeight() {
        return cellHeight * 12;
    }

    protected int calculateChartWidth() {
        return cellWidth * 31;
    }

    @Override
    public void draw(ExtendedGraphics2D graphics2D, int x, int y) throws Exception {
        drawCalendar(graphics2D, 2025, now.toLocalDate(), x, y);
    }

    void drawCalendar(Graphics2D graphics2D, int year, LocalDate today, int x, int y) {
        int holidays = 0;

        // Colors
        Color yearColor = new Color(80, 80, 80);  // Dark gray for year display

        Color xxxBgColor   = new Color(0xfffcea);  // Light yellow
        Color xxxTextColor = new Color(0xff931e);  // Light yellow

        Color vacationBgColor   = new Color(183, 240, 216);  // Light blue
        Color vacationTextColor = new Color(123, 200, 180);  // Light blue
        Color sickBgColor       = new Color(0xfff2e8); // Light red
        Color sickTextColor     = new Color(0xff6d5b); // Light red
        Color holidayBgColor    = new Color(183, 216, 240);  // Light yellow
        Color holidayTextColor  = new Color(123, 180, 200);  // Light yellow

        Color weekendBgColor   = null;
        Color weekendTextColor = new Color(180, 180, 180);  // Light gray for weekends
        Color fillingTextColor = new Color(0xe2dbdb); // very Light gray for filling days

        Color normalDayBgColor = new Color(0x323232);  // almost black

        // Save original font and color
        Font  originalFont  = graphics2D.getFont();
        Color originalColor = graphics2D.getColor();

        // Fonts
        Font yearFont  = new Font("Helvetica", Font.PLAIN, 24);
        Font smallFont = new Font("Helvetica", Font.PLAIN, 6);

        // Draw the year at the top
        graphics2D.setFont(yearFont);
        graphics2D.setColor(yearColor);
        String yearText = String.valueOf(year);

        // Calculate year text dimensions
        FontMetrics yearMetrics    = graphics2D.getFontMetrics(yearFont);
        int         yearTextHeight = yearMetrics.getHeight();
        int         yearTextWidth  = yearMetrics.stringWidth(yearText);

        // Center the year text
        graphics2D.drawString(yearText, x + 5, y + yearMetrics.getAscent());

        // Calculate offset for calendar to position below the year text
        int yearOffset = yearTextHeight + 20; // Add some padding

        // For each month
        for (int monthIndex = 0; monthIndex < 12; monthIndex++) {
            int row = monthIndex / MONTHS_PER_ROW;
            int col = monthIndex % MONTHS_PER_ROW;

            int monthStartX = x + col * (MONTH_WIDTH + MONTH_GAP);
            int monthStartY = y + yearOffset + row * (MONTH_HEIGHT + MONTH_GAP); // Apply the year offset

            // Create LocalDate for first day of month
            LocalDate firstOfMonth = LocalDate.of(year, monthIndex + 1, 1);
            String    monthName    = firstOfMonth.getMonth().toString();

            drawMonthName(graphics2D, monthStartX, monthStartY, monthName);

            drawWeekDays(graphics2D, monthStartX, monthStartY, firstOfMonth);

            // Determine first day of week for this month (0 = Sunday, 6 = Saturday)
            int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
            if (firstDayOfWeek == 0) firstDayOfWeek = 7;  // Convert Sunday from 0 to 7
            firstDayOfWeek--; // Adjust to 0-based index

            // Last day of month
            int lastDay = firstOfMonth.lengthOfMonth();

            // Calculate starting Y position for the first week, adding adequate space after weekday headers
            int daysStartY = monthStartY + HEADER_HEIGHT + WEEKDAY_HEADER_HEIGHT + 10; // Added 10px extra spacing

            // Draw days from previous month to fill the gaps at the beginning
            if (firstDayOfWeek > 0) {
                // Get the previous month
                LocalDate prevMonth        = firstOfMonth.minusMonths(1);
                int       prevMonthLastDay = prevMonth.lengthOfMonth();

                // Draw the days from the previous month
                for (int i = 0; i < firstDayOfWeek; i++) {
                    int prevDay    = prevMonthLastDay - firstDayOfWeek + i + 1;
                    int dayX       = monthStartX + 10 + (i * DAY_SIZE);
                    int dayY       = daysStartY;
                    int dayCenterX = dayX + (DAY_SIZE / 2);
                    int dayCenterY = dayY + (DAY_SIZE / 2) - 6;

                    // Draw with filling text color
                    graphics2D.setColor(fillingTextColor);
                    drawDayOfMonth(graphics2D, dayCenterX, dayCenterY, prevDay);
                }
            }

            for (int day = 1; day <= lastDay; day++) {
                LocalDate currentDate = LocalDate.of(year, monthIndex + 1, day);
                int       dayOfWeek   = (firstDayOfWeek + day - 1) % 7;
                int       weekNum     = (firstDayOfWeek + day - 1) / 7;
                int       dayX        = monthStartX + 10 + (dayOfWeek * DAY_SIZE);
                int       dayY        = daysStartY + (weekNum * DAY_SIZE);
                // Calculate center position for the day cell
                int dayCenterX = dayX + (DAY_SIZE / 2);
                int dayCenterY = dayY + (DAY_SIZE / 2) - 6; // Adjusted to center the text better

                ProjectCalendar pc        = user.getCalendar();
                DayType         dayType   = pc.getCalendarDayType(currentDate.getDayOfWeek());
                boolean         isWeekend = dayType == DayType.NON_WORKING;
                // Check if today
                boolean isToday = currentDate.equals(today);

                // Check off days
                Color bgColor   = null;
                Color textColor = normalDayBgColor;

                if (isWeekend) {
                    bgColor   = weekendBgColor;
                    textColor = weekendTextColor;
                } else {
                    ProjectCalendarException exception = pc.getException(currentDate);
                    if (exception != null) {
                        String name = exception.getName();
                        if (exception.getName().equals(OffDayType.VACATION.name())) {
                            bgColor   = vacationBgColor;
                            textColor = vacationTextColor;
                        } else if (exception.getName().equals(OffDayType.SICK.name())) {
                            bgColor   = sickBgColor;
                            textColor = sickTextColor;
                        } else {
                            bgColor   = holidayBgColor;
                            textColor = holidayTextColor;
                        }
                    }
                }

                if (bgColor != null) {
                    graphics2D.setColor(bgColor);
                    graphics2D.fillRect(dayCenterX - (DAY_SIZE / 2) + 1, dayCenterY - (DAY_SIZE / 2) + 1, DAY_SIZE - 1, DAY_SIZE - 1);
                }

                graphics2D.setColor(textColor);
                //today can be any day, even weekend, holiday, vacation or sock day.
                if (isToday) {
                    drawToday(graphics2D, dayCenterX, dayCenterY);
                }
                drawDayOfMonth(graphics2D, dayCenterX, dayCenterY, day);
            }

            // Draw days from next month to fill the gaps at the end
            int lastDayOfWeek = (firstDayOfWeek + lastDay - 1) % 7;
            if (lastDayOfWeek < 6) { // If the month doesn't end on a Sunday
                int daysToAdd = 6 - lastDayOfWeek;

                for (int i = 1; i <= daysToAdd; i++) {
                    int weekNum    = (firstDayOfWeek + lastDay - 1 + i) / 7;
                    int dayOfWeek  = (firstDayOfWeek + lastDay - 1 + i) % 7;
                    int dayX       = monthStartX + 10 + (dayOfWeek * DAY_SIZE);
                    int dayY       = daysStartY + (weekNum * DAY_SIZE);
                    int dayCenterX = dayX + (DAY_SIZE / 2);
                    int dayCenterY = dayY + (DAY_SIZE / 2) - 6;

                    // Draw with filling text color
                    graphics2D.setColor(fillingTextColor);
                    drawDayOfMonth(graphics2D, dayCenterX, dayCenterY, i);
                }
            }
        }
        int legendStartY = y + (6 * DAY_SIZE) + yearOffset + 2 * (MONTH_HEIGHT + MONTH_GAP) + HEADER_HEIGHT + WEEKDAY_HEADER_HEIGHT + 10; // Added 10px extra spacing
        drawLegend(graphics2D, x + 15, legendStartY);

        // Restore original settings
        graphics2D.setFont(originalFont);
        graphics2D.setColor(originalColor);
    }

    private static void drawDayOfMonth(Graphics2D graphics2D, int dayCenterX, int dayCenterY, int day) {
        // Draw day number
        String      dayStr    = String.valueOf(day);
        FontMetrics metrics   = graphics2D.getFontMetrics();
        int         textWidth = metrics.stringWidth(dayStr);
        Font        dayFont   = new Font("Helvetica", Font.BOLD, 10);
        graphics2D.setFont(dayFont);
        graphics2D.drawString(dayStr, dayCenterX - (textWidth / 2), dayCenterY + (metrics.getAscent() / 2));
    }

    // Helper method to draw example day numbers in legend
    private void drawExampleDayNumber(Graphics2D graphics2D, int x, int y, int squareSize, String dayNumber) {
        Font originalFont = graphics2D.getFont();
        // Use the same font style as in the actual calendar
        Font dayFont = new Font("Helvetica", Font.BOLD, 10);
        graphics2D.setFont(dayFont);

        FontMetrics metrics   = graphics2D.getFontMetrics();
        int         textWidth = metrics.stringWidth(dayNumber);

        // Center the day number in the square
        int centerX = x + squareSize / 2;
        int centerY = y + squareSize / 2;

        graphics2D.drawString(dayNumber, centerX - (textWidth / 2), centerY + (metrics.getAscent() / 2) - 2);

        // Restore original font
        graphics2D.setFont(originalFont);
    }

    private void drawLegend(Graphics2D graphics2D, int x, int y) {
        // Save original font and color
        Font  originalFont  = graphics2D.getFont();
        Color originalColor = graphics2D.getColor();

        // Legend colors
        Color vacationBgColor   = new Color(183, 240, 216);
        Color vacationTextColor = new Color(123, 200, 180);
        Color sickBgColor       = new Color(0xfff2e8);
        Color sickTextColor     = new Color(0xff6d5b);
        Color holidayBgColor    = new Color(183, 216, 240);
        Color holidayTextColor  = new Color(123, 180, 200);

        Font legendFont = new Font("Helvetica", Font.PLAIN, 12);
        graphics2D.setFont(legendFont);

        int itemWidth  = 100;
        int itemHeight = 20;
        int squareSize = 16;
        int gap        = 10;

        // Draw vacation legend item
        graphics2D.setColor(vacationBgColor);
        graphics2D.fillRect(x, y, squareSize, squareSize);
        graphics2D.setColor(vacationTextColor);
        // Add example day number inside the vacation box
        drawExampleDayNumber(graphics2D, x, y, squareSize, "15");
        graphics2D.drawString("Vacation", x + squareSize + 5, y + squareSize - 2);

        // Draw sick day legend item
        graphics2D.setColor(sickBgColor);
        graphics2D.fillRect(x + itemWidth, y, squareSize, squareSize);
        graphics2D.setColor(sickTextColor);
        // Add example day number inside the sick box
        drawExampleDayNumber(graphics2D, x + itemWidth, y, squareSize, "7");
        graphics2D.drawString("Sick", x + itemWidth + squareSize + 5, y + squareSize - 2);

        // Draw holiday legend item
        graphics2D.setColor(holidayBgColor);
        graphics2D.fillRect(x + itemWidth * 2, y, squareSize, squareSize);
        graphics2D.setColor(holidayTextColor);
        // Add example day number inside the holiday box
        drawExampleDayNumber(graphics2D, x + itemWidth * 2, y, squareSize, "25");
        graphics2D.drawString("Holiday", x + itemWidth * 2 + squareSize + 5, y + squareSize - 2);

        // Restore original settings
        graphics2D.setFont(originalFont);
        graphics2D.setColor(originalColor);
    }

    private void drawMonthName(Graphics2D graphics2D, int monthStartX, int monthStartY, String monthName) {
        // Draw month name
        Color monthNameColor = new Color(0xff6336);  // Red for month names
        Font  monthFont      = new Font("Helvetica", Font.PLAIN, 14);
        graphics2D.setFont(monthFont);
        graphics2D.setColor(monthNameColor);
        FontMetrics monthMetrics = graphics2D.getFontMetrics();
        int         monthAscent  = monthMetrics.getAscent();
        graphics2D.drawString(monthName, monthStartX + 10 + DAY_SIZE / 2 - 5, monthStartY + monthAscent);
    }

    private void drawToday(Graphics2D graphics2D, int dayCenterX, int dayCenterY) {
        Color todayBgColor   = new Color(0xff3a30);  // Red circle for today
        Color todayTextColor = Color.white;  // White text for today
        graphics2D.setColor(todayBgColor);
        graphics2D.fillOval(dayCenterX - (DAY_SIZE / 2) + 1, dayCenterY - (DAY_SIZE / 2) + 1, DAY_SIZE - 1, DAY_SIZE - 1);
        graphics2D.setColor(todayTextColor);
    }

    private void drawWeekDays(Graphics2D graphics2D, int monthStartX, int monthStartY, LocalDate firstOfMonth) {
        // Draw weekday headers
        // Day of week labels
        String[] weekdays    = {"M", "T", "W", "T", "F", "S", "S"};
        Font     weekdayFont = new Font("Helvetica", Font.BOLD, 10);
        graphics2D.setFont(weekdayFont);
        FontMetrics weekdayMetrics = graphics2D.getFontMetrics();
        int         weekdayAscent  = weekdayMetrics.getAscent();
        for (int i = 0; i < 7; i++) {
            int weekdayX = monthStartX + 10 + (i * DAY_SIZE);
            // Center the weekday text vertically in the weekday header area
            int weekdayY = monthStartY + HEADER_HEIGHT + (WEEKDAY_HEADER_HEIGHT / 2) + (weekdayAscent / 2);
            // Get the calendar day type from the user's calendar
            ProjectCalendar pc      = user.getCalendar();
            LocalDate       dayDate = firstOfMonth.withDayOfMonth(1).plusDays(i);
//                DayType         dayType = pc.getCalendarDayType(dayDate.getDayOfWeek());
            Color weekDayTextColor = new Color(180, 180, 180);  // Light gray for weekends
            graphics2D.setColor(weekDayTextColor);
            graphics2D.drawString(weekdays[i], weekdayX + DAY_SIZE / 2 - 5, weekdayY);
        }
    }
}

