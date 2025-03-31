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

import de.bushnaq.abdalla.projecthub.report.dao.*;
import de.bushnaq.abdalla.svg.util.ExtendedGraphics2D;
import de.bushnaq.abdalla.util.date.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * abstract base class used by all renderer
 *
 * @author abdalla.bushnaq
 */
public abstract class AbstractRenderer {
    public static final    int                   MAX_DAY_WIDTH              = 20;
    protected static final float                 STANDARD_LINE_STROKE_WIDTH = 3.1f;
    protected final        Font                  authorFont                 = new Font("Arial", Font.BOLD, 12);
    protected              Authors               authors                    = new Authors();
    protected              Font                  bankHolidayFont            = null;
    //    private                Map<LocalDate, String> bankHolidays;
    private                boolean               calendarAtBottom;
    public                 CalendarXAxses        calendarXAxses;
    public                 int                   chartHeight;
    public                 int                   chartWidth;
    public                 int                   days                       = 3;
    public final           GraphSquare           diagram                    = new GraphSquare();
    protected              int                   firstDayX                  = 0;
    public                 ExtendedGraphics2D    graphics2D;
    public                 BurnDownGraphicsTheme graphicsTheme;
    protected              String                imageMap                   = "";
    protected final        Logger                logger                     = LoggerFactory.getLogger(this.getClass());
    public                 Milestones            milestones;

    public AbstractRenderer() {

    }

    public AbstractRenderer(RenderDao dao) throws IOException {
//        this.bankHolidays  = dao.context.bankHolidays;
        this.graphicsTheme = dao.graphicsTheme;
        this.chartWidth    = dao.chartWidth;
        this.chartHeight   = dao.chartHeight;
        milestones         = new Milestones(dao.sprintName);
        calendarXAxses     = new CalendarXAxses(this, dao.preRun, dao.postRun);
    }

    public AbstractRenderer(String sprintName/*, Map<LocalDate, String> bankHolidays*/, boolean completed, int chartWidth, int chartHeight, int preRun, int postRun,
                            BurnDownGraphicsTheme graphicsTheme) throws IOException {
//        this.bankHolidays  = bankHolidays;
        this.graphicsTheme = graphicsTheme;
        this.chartWidth    = chartWidth;
        this.chartHeight   = chartHeight;
        milestones         = new Milestones(sprintName);
        calendarXAxses     = new CalendarXAxses(this, preRun, postRun);
    }

    protected int caculateMaxDays() {
        return DateUtil.calculateDays(milestones.firstMilestone, milestones.lastMilestone) + 1 + calendarXAxses.getPriRun() + calendarXAxses.getPostRun();
    }

    protected int calculateChartHeight() {
        return chartHeight;
    }

    protected int calculateChartWidth() {
        return chartWidth;
    }

    protected LocalDate calculateDayFromIndex(int index) {
        LocalDate firstMilestoneDay = milestones.firstMilestone;
        return DateUtil.addDay(firstMilestoneDay, index);
    }

    protected int calculateDayIndex(LocalDate date) {
        LocalDate firstMilestoneDay = milestones.firstMilestone;
        return DateUtil.calculateDays(firstMilestoneDay, date);
    }

    protected int calculateDayIndex(LocalDateTime date) {
        return calculateDayIndex(date.toLocalDate());
    }

    protected void calculateDayWidth() {
        days = caculateMaxDays();
        calendarXAxses.dayOfWeek.setWidth((chartWidth) / days);
    }

    protected int calculateDayX(LocalDate date) {
        LocalDate firstMilestoneDay = milestones.firstMilestone;
        int       firstMilestoneX   = firstDayX + calendarXAxses.dayOfWeek.getWidth() / 2;
        return firstMilestoneX + (DateUtil.calculateDays(firstMilestoneDay, date) + calendarXAxses.getPriRun()) * calendarXAxses.dayOfWeek.getWidth();
    }

    protected int calculateX(LocalDateTime date, LocalDateTime startTime, long secondsPeerDay) {
        LocalDate firstMilestoneDay = milestones.firstMilestone;
        int       firstMilestoneX   = firstDayX + calendarXAxses.dayOfWeek.getWidth() / 2;
        // String createDateTimeString = DateUtil.createDateTimeString(date);
        //        LocalDateTime timeOfDay = LocalDateTimeUtil.getTimeOfDayInMillis(date);
        Duration workedToday = Duration.between(startTime, date);
        int dayX = firstMilestoneX
                + (DateUtil.calculateDays(firstMilestoneDay, DateUtil.toDayPrecision(date)) + calendarXAxses.getPriRun()) * calendarXAxses.dayOfWeek.getWidth();
        int timeOfDayX = (int) (((workedToday.getSeconds()) * calendarXAxses.dayOfWeek.getWidth()) / secondsPeerDay);
        return dayX + timeOfDayX;
    }

    public abstract void draw(ExtendedGraphics2D graphics2D, int x, int y) throws Exception;

    protected void drawAuthor(int x, int y, int with, Color fillColor, String text, Color textColor, Font font) {
        graphics2D.setFont(font);
        graphics2D.setColor(fillColor);
        graphics2D.fillRect(x, y - calendarXAxses.milestone.height / 2, with + 4, calendarXAxses.milestone.height - 1);
        graphics2D.setColor(textColor);
        FontMetrics fm        = graphics2D.getFontMetrics();
        int         maxAscent = fm.getMaxAscent();
        graphics2D.drawString(text, x + 2, y + maxAscent / 2 - 2);
    }

    protected void drawAuthorLegend(int x, int y) {
        int authorLegendWidth = 20;
        for (Author author : authors.getList()) {
//            String primaryAuthorName = Authors.mapToPrimaryLoginName(author.name);
//            if (primaryAuthorName == null) {
//                primaryAuthorName = author.name;
//            }
            FontMetrics metrics = graphics2D.getFontMetrics(calendarXAxses.milestone.font);
            int         adv     = metrics.stringWidth(author.name);
            authorLegendWidth = Math.max(authorLegendWidth, adv);
        }
        int lineHeight = 14;
        int ay         = y + lineHeight * authors.getList().size();
        for (Author author : authors.getList()) {
//            String primaryAuthorName = Authors.mapToPrimaryLoginName(author.name);
//            if (primaryAuthorName == null) {
//                primaryAuthorName = author.name;
//            }
            drawAuthor(x, ay, authorLegendWidth, author.color, author.name, graphicsTheme.tickTextColor, calendarXAxses.milestone.font);
            ay -= lineHeight;
        }
    }

    protected void drawCalendar() {
        drawCalendar(true);
    }

    protected void drawCalendar(boolean drawDays) {
        calendarXAxses.drawCalendar(drawDays);
    }

    public void drawDayBars(LocalDate currentDay) {
        //        Calendar day = Calendar.getInstance();
        //        day.setTimeInMillis(currentDay);
        Color color = GraphColorUtil.getDayOfWeekStripeColor(graphicsTheme/*, bankHolidays*/, currentDay);
        int   x     = calculateDayX(currentDay);
        graphics2D.setColor(color);
        //day vertical bar
        graphics2D.fillRect(x - (calendarXAxses.dayOfWeek.getWidth() / 2 - 1), diagram.y, calendarXAxses.dayOfWeek.getWidth() - 1, diagram.height);
        //draw vertical lines
        graphics2D.setColor(graphicsTheme.ganttGridColor);
        //        graphics2D.setStroke(new BasicStroke(RELATION_LINE_STROKE_WIDTH));
        //        graphics2D.drawLine(x - (calendarXAxses.dayOfWeek.getWidth() / 2 - 1) + (calendarXAxses.dayOfWeek.getWidth() - 1), diagram.y,x - (calendarXAxses.dayOfWeek.getWidth() / 2 - 1) + (calendarXAxses.dayOfWeek.getWidth() - 1), diagram.y + diagram.height);
        //        graphics2D.draw(new Line2D.Double(x - (calendarXAxses.dayOfWeek.getWidth() / 2 - 1) + (calendarXAxses.dayOfWeek.getWidth() - 1), diagram.y,x - (calendarXAxses.dayOfWeek.getWidth() / 2 - 1) + (calendarXAxses.dayOfWeek.getWidth() - 1), diagram.y + diagram.height));
        graphics2D.fillRect(x - (calendarXAxses.dayOfWeek.getWidth() / 2 - 1) + (calendarXAxses.dayOfWeek.getWidth() - 1), diagram.y, 1, diagram.height);
        //TODO: draw bank holiday
//        if (bankHolidays.get(currentDay) != null) {
//            x += calendarXAxses.dayOfWeek.getWidth() / 2;
//            graphics2D.setColor(graphicsTheme.bankHolidayColor);
//            graphics2D.setFont(bankHolidayFont);
//            FontMetrics fm     = graphics2D.getFontMetrics();
//            String      string = bankHolidays.get(currentDay);
//            int         width  = fm.stringWidth(string);
//            int         y;
//            y = diagram.y + width;
//            AffineTransform transform = graphics2D.getTransform();
//            graphics2D.rotate(-Math.PI / 2, x, y);
//            graphics2D.drawString(string, x, y);
//            graphics2D.setTransform(transform);
//        }
    }

    protected void drawGraphText(int x, int y, String text, Color textColor, Font font, TextAlignment aligned) {
        // flag
        graphics2D.setFont(font);
        FontMetrics fm    = graphics2D.getFontMetrics();
        int         width = fm.stringWidth(text);
        graphics2D.setColor(graphicsTheme.graphTextBackgroundColor);
        switch (aligned) {
            case left:
                graphics2D.fillRect(x, y - 9 + 2, width, 12);
                graphics2D.setColor(textColor);
                graphics2D.drawString(text, x, y + 2);
                break;
            case right:
                graphics2D.fillRect(x - width, y - 9 + 2, width, 12);
                graphics2D.setColor(textColor);
                graphics2D.drawString(text, x - width, y + 2);
                break;
        }

    }

    protected void drawLegend(int x, int y, Color interpolationColor) {
        int lineHeight  = 14;
        int legendY     = y + lineHeight;
        int legendX1    = x;
        int legendX2    = legendX1 + 10;
        int legendTextY = legendY + 1;
        int legendTextX = legendX2 + 4;
        int milestoneX  = legendX1 + 5;
        int milestoneY  = legendY - calendarXAxses.milestone.height / 2;

        graphics2D.setStroke(new BasicStroke(STANDARD_LINE_STROKE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));
        graphics2D.setColor(graphicsTheme.surroundingSquareColor);
        graphics2D.drawLine(legendX1, legendY, legendX2, legendY);
        drawGraphText(legendTextX, legendTextY, "Guideline", graphicsTheme.tickTextColor, calendarXAxses.dayOfWeek.getFont(), TextAlignment.left);

        legendY += lineHeight;
        legendTextY += lineHeight;
        milestoneY += lineHeight;
        graphics2D.setColor(interpolationColor);
        graphics2D.drawLine(legendX1, legendY, legendX2, legendY);
        drawGraphText(legendTextX, legendTextY, "extrapolated release date", graphicsTheme.tickTextColor, calendarXAxses.dayOfWeek.getFont(),
                TextAlignment.left);

        legendY += lineHeight;
        legendTextY += lineHeight;
        milestoneY += lineHeight;
        graphics2D.setStroke(new BasicStroke(STANDARD_LINE_STROKE_WIDTH));
        graphics2D.setColor(graphicsTheme.burnDownBorderColor);
        graphics2D.drawLine(legendX1, legendY, legendX2, legendY);
        drawGraphText(legendTextX, legendTextY, "Remaining work", graphicsTheme.tickTextColor, calendarXAxses.dayOfWeek.getFont(), TextAlignment.left);

        legendY += lineHeight;
        legendTextY += lineHeight;
        milestoneY += lineHeight;
        calendarXAxses.drawMilestone(null, null, milestoneX, milestoneY, graphicsTheme.pastEventColor, "S", true, null, null, false, false);// start
        drawGraphText(legendTextX, legendTextY, "Start date (sprint)", graphicsTheme.tickTextColor, calendarXAxses.dayOfWeek.getFont(), TextAlignment.left);

        legendY += lineHeight;
        legendTextY += lineHeight;
        milestoneY += lineHeight;
        calendarXAxses.drawMilestone(null, null, milestoneX, milestoneY, graphicsTheme.nowEventColor, "N", true, null, null, false, false);// now
        drawGraphText(legendTextX, legendTextY, "Now date", graphicsTheme.tickTextColor, calendarXAxses.dayOfWeek.getFont(), TextAlignment.left);

        legendY += lineHeight;
        legendTextY += lineHeight;
        milestoneY += lineHeight;
        calendarXAxses.drawMilestone(null, null, milestoneX, milestoneY, graphicsTheme.delayEventColor, "R", true, null, null, false, false);// release
        drawGraphText(legendTextX, legendTextY, "Release date", graphicsTheme.tickTextColor, calendarXAxses.dayOfWeek.getFont(), TextAlignment.left);

        legendY += lineHeight;
        legendTextY += lineHeight;
        milestoneY += lineHeight;
        calendarXAxses.drawMilestone(null, null, milestoneX, milestoneY, graphicsTheme.futureEventColor, "E", true, null, null, false, false);// end
        drawGraphText(legendTextX, legendTextY, "End date (sprint)", graphicsTheme.tickTextColor, calendarXAxses.dayOfWeek.getFont(), TextAlignment.left);

        legendY += lineHeight;
        legendTextY += lineHeight;
        milestoneY += lineHeight;
        calendarXAxses.drawMilestone(null, null, milestoneX, milestoneY, graphicsTheme.futureEventColor, "F", true, null, null, false, false);// first
        drawGraphText(legendTextX, legendTextY, "First punch-in", graphicsTheme.tickTextColor, calendarXAxses.dayOfWeek.getFont(), TextAlignment.left);

        legendY += lineHeight;
        legendTextY += lineHeight;
        milestoneY += lineHeight;
        calendarXAxses.drawMilestone(null, null, milestoneX, milestoneY, graphicsTheme.futureEventColor, "L", true, null, null, false, false);// Last
        drawGraphText(legendTextX, legendTextY, "Last punch-out", graphicsTheme.tickTextColor, calendarXAxses.dayOfWeek.getFont(), TextAlignment.left);
    }

    protected void drawMilestones() {
        imageMap += calendarXAxses.drawMilestones();
    }

    public int getDayWidth() {
        return calendarXAxses.dayOfWeek.getWidth();
    }

    public String getImageMap() {
        return imageMap;
    }

    protected int getTaskHeight() {
        return 13 + 4;
    }

    protected void initPosition(int x, int y) throws IOException {
        firstDayX = x;
        if (calendarAtBottom) {
            calendarXAxses.initPosition(x, y);
            diagram.initPosition(x, y);
            calendarXAxses.initPosition(x, diagram.y + diagram.height + 1);
        } else {
            calendarXAxses.initPosition(x, y);
            diagram.initPosition(x, calendarXAxses.year.getY() + calendarXAxses.getHeight());
        }
    }

    protected void initSize(int x, int y, boolean calendarAtBottom) throws IOException {
        this.calendarAtBottom = calendarAtBottom;
        // this.calendarAtBottom = calendarAtBottom;
        calculateDayWidth();
        chartWidth      = calculateChartWidth();
        chartHeight     = calculateChartHeight();
        firstDayX       = x;
        bankHolidayFont = new Font("Arial", Font.PLAIN, Math.min(14, (int) (calendarXAxses.dayOfWeek.getWidth() * 1.1)));

        if (calendarAtBottom) {
            calendarXAxses.initSize(chartWidth, calendarXAxses.dayOfWeek.getWidth(), calendarAtBottom);
            diagram.initSize(chartWidth - x, chartHeight - calendarXAxses.getHeight());
            calendarXAxses.initSize(chartWidth, calendarXAxses.dayOfWeek.getWidth(), calendarAtBottom);
        } else {
            calendarXAxses.initSize(chartWidth, calendarXAxses.dayOfWeek.getWidth(), calendarAtBottom);
            diagram.initSize(chartWidth, chartHeight - calendarXAxses.getHeight());
        }
    }

    public void setDayWidth(int dayWidth) {
        calendarXAxses.dayOfWeek.setWidth(dayWidth);
        calendarXAxses.dayOfMonth.setWidth(dayWidth);
        chartWidth  = calculateChartWidth();
        chartHeight = calculateChartHeight();
    }

}
