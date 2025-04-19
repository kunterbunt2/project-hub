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
import de.bushnaq.abdalla.projecthub.dto.*;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.gantt.GanttUtil;
import de.bushnaq.abdalla.projecthub.report.AbstractCanvas;
import de.bushnaq.abdalla.projecthub.report.dao.*;
import de.bushnaq.abdalla.svg.util.ExtendedRectangle;
import de.bushnaq.abdalla.svg.util.RectangleWithToolTip;
import de.bushnaq.abdalla.util.ColorUtil;
import de.bushnaq.abdalla.util.GanttErrorHandler;
import de.bushnaq.abdalla.util.TaskUtil;
import de.bushnaq.abdalla.util.date.DateUtil;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectCalendarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * abstract class used by GanttChartRenderer and TeamPlannnerRenderer
 *
 * @author abdalla.bushnaq
 */
public abstract class AbstractGanttRenderer extends AbstractRenderer {
    private static final   float                FINE_LINE_STROKE_WIDTH     = 1.0f;
    private static final   int                  LINE_HEIGHT                = 18;
    private static final   int                  RELATION_CORNER_LENGTH     = 14;
    private static final   float                RELATION_LINE_STROKE_WIDTH = 1f;
    public static final    int                  RESOURCE_NAME_TO_TASK_GAP  = 3;
    //TODO replace with sprint calendar working day
    public static final    int                  SECONDS_PER_DAY            = 85 * 6 * 60;//seconds between work start and work end, including lunch hour
    private static final   int                  TASK_BODY_BORDER           = 4;
    public static final    int                  TASK_NAME_TO_TASK_GAP      = 5 + 8;
    private final static   Font                 graphFont                  = new Font("Arial", Font.PLAIN, 12);
    private final static   Font                 idErrorFont                = new Font("Arial", Font.BOLD, 12);
    private final static   Font                 idFont                     = new Font("Arial", Font.PLAIN, 12);
    protected final static Font                 outOfOfficeFont            = new Font("Arial", Font.BOLD, 20);
    private final static   Font                 storyFont                  = new Font("Arial", Font.BOLD, 12);
    private final static   Font                 taskInlineFont             = new Font("Arial", Font.PLAIN, 12);
    private final static   Font                 taskProgressFont           = new Font("Arial", Font.PLAIN, 8);
    private final static   Font                 taskResourceLocationFont   = new Font("Arial", Font.PLAIN, 8);
    private final static   Font                 taskTickFont               = new Font("Arial", Font.PLAIN, 5);
    protected              AuthorsContribution  authorsContribution        = new AuthorsContribution();// remaining and worked per author
    protected              Context              context;
    private final          DateUtil             dateUtil                   = new DateUtil();
    protected              GanttErrorHandler    geh                        = new GanttErrorHandler();
    private final          Logger               logger                     = LoggerFactory.getLogger(this.getClass());
    private final          int                  numberOfLinesPerTask;
    protected              ResourcesUtilization resourcesUtilization       = new ResourcesUtilization();// only used for out of office
    protected              Map<Long, Integer>   taskHeight                 = new HashMap<>();

    public AbstractGanttRenderer(Context context, String sprintName/*, Map<LocalDate, String> bankHolidays*/, boolean completed/*, int chartWidth, int chartHeight*/, int numberOfLinesPerTask, int preRun, int postRun, BurnDownGraphicsTheme graphicsTheme) throws IOException {
        super(sprintName/*, bankHolidays*/, completed/*, chartWidth, chartHeight*/, preRun, postRun, graphicsTheme);
        this.context              = context;
        this.numberOfLinesPerTask = numberOfLinesPerTask;
    }

    private int addVacation(ResourceUtilization ru, String resourceName, OffDay pce) {
        int days = 0;
//        RecurringData recurring = pce.getRecurring();
//        if (recurring != null) {
//            LocalDate[] dates = recurring.getDates();
//            for (int i = 0; i < dates.length; i++) {
//                LocalDate exceptionDate = dates[i];
//                int       dayIndex      = calculateDayIndex(exceptionDate);
//                if (dayIndex > -1) {
//                    ru.addVacation(dayIndex, 1.0);
//                    days++;
//                    // logger.trace(String.format("%s is out of office on %s.", resourceName,
//                    // dateUtil.createDateString(exceptionDate.getTime())));
//                }
//            }
//        } else
        {
            LocalDate fromDate = pce.getFirstDay();
            while (fromDate.isBefore(pce.getLastDay()) || fromDate.isEqual(pce.getLastDay())) {
                //                logger.trace(String.format("%s is out of office on %s.", resourceName, dateUtil.createDateString(fromDate.getTime())));
                int dayIndex = calculateDayIndex(fromDate);
                if (dayIndex > -1) {
                    ru.addVacation(dayIndex, 1.0);
                    days++;
                }
                fromDate = DateUtil.addDay(fromDate, 1);
            }
        }
        return days;
    }

    private void drawConflictMarker(int y, List<Conflict> conflict) {
        if (conflict != null) {
            // only used in team planner chart
            graphics2D.setColor(Color.red);
            for (Conflict c : conflict) {
                if (c.originalConflict) {
                    int c1 = calculateX(c.range.start, c.range.start.truncatedTo(ChronoUnit.DAYS).withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                    int c2 = calculateX(c.range.end, c.range.end.truncatedTo(ChronoUnit.DAYS).withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                    graphics2D.fillRect(c1, y - getTaskHeight() / 2, c2 - c1 - 1, 2);
                    graphics2D.fillRect(c1, y + getTaskHeight() / 2 - 1, c2 - c1 - 1, 2);
                }
            }
        }
    }

    private void drawCriticalMarker(Task task, int x1, int x2, int y) {
        if (task.isCritical()) {
            graphics2D.setColor(graphicsTheme.ganttCriticalTaskBorderColor);
        } else {
            graphics2D.setColor(graphicsTheme.ganttTaskBorderColor);
        }
        User user = task.getAssignedUser();
        if (user != null && user.getCalendar() != null) {
            ProjectCalendar pc   = user.getCalendar();
            int             days = (int) Duration.between(task.getStart().truncatedTo(ChronoUnit.DAYS), task.getFinish().truncatedTo(ChronoUnit.DAYS)).toDays();
            for (int day = 0; day <= days; day++) {
                LocalDateTime currentDay = task.getStart().truncatedTo(ChronoUnit.DAYS).plusDays(day);
                if (pc.isWorkingDate(currentDay.toLocalDate())) {
                    if (day == 0) {
                        //is this the left end?
                        int xStart  = calculateX(task.getStart(), currentDay.withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                        int xFinish = calculateX(currentDay.withHour(8).plusSeconds(SECONDS_PER_DAY), currentDay.withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                        graphics2D.fillRect(xStart, y - getTaskHeight() / 2 + TASK_BODY_BORDER, xFinish - xStart, 1);//upper -
                        graphics2D.fillRect(xStart, y + getTaskHeight() / 2 - TASK_BODY_BORDER - 1, xFinish - xStart, 1);//lower -
                        graphics2D.fillRect(xStart, y - getTaskHeight() / 2 + TASK_BODY_BORDER + 1, 1, getTaskHeight() - TASK_BODY_BORDER * 2 - 2);//start |
                    } else if (day == days) {
                        //or is this the right end?
                        int xStart  = calculateX(currentDay.withHour(8), currentDay.withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                        int xFinish = calculateX(task.getFinish(), currentDay.withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                        graphics2D.fillRect(xStart, y - getTaskHeight() / 2 + TASK_BODY_BORDER, xFinish - xStart + 1, 1);//upper -
                        graphics2D.fillRect(xStart, y + getTaskHeight() / 2 - TASK_BODY_BORDER - 1, xFinish - xStart + 1, 1);//lower -
                        graphics2D.fillRect(xFinish, y - getTaskHeight() / 2 + TASK_BODY_BORDER + 1, 1, getTaskHeight() - TASK_BODY_BORDER * 2 - 2);

                    } else {
                        int xStart = calculateX(currentDay.withHour(8), currentDay.withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                        graphics2D.fillRect(xStart, y - getTaskHeight() / 2 + TASK_BODY_BORDER, calendarXAxses.dayOfWeek.getWidth(), 1);
                        graphics2D.fillRect(xStart, y + getTaskHeight() / 2 - TASK_BODY_BORDER - 1, calendarXAxses.dayOfWeek.getWidth(), 1);
                    }
                } else {
                    for (int i = 0; i < calendarXAxses.dayOfWeek.getWidth() - 1; i++) {
                        int xStart = calculateX(currentDay.withHour(8), currentDay.withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                        int x      = i + xStart;
                        if (x % 4 == 0) {
                            graphics2D.fillRect(x, y - getTaskHeight() / 2 + TASK_BODY_BORDER, 2, 1);
                            graphics2D.fillRect(x, y + getTaskHeight() / 2 - TASK_BODY_BORDER - 1, 2, 1);
                        }
                    }
                }
            }

        }
    }

    private void drawId(Task task, int y) {
        int      x1        = firstDayX;
        int      x2        = x1 + calendarXAxses.dayOfWeek.getWidth();
        Color    fillColor = graphicsTheme.ganttIdColor;
        Color    textColor = graphicsTheme.ganttIdTextColor;
        MetaData md        = TaskUtil.getTaskMetaData(task);
        // HashMap<Task, MetaData> mdMap = (HashMap<Task, MetaData>)
        // task.getParentFile().getProjectProperties().getCustomProperties().get(MetaData.METADATA);
        // MetaData md = mdMap.get(task);
        if (md != null) {
            graphics2D.setFont(idErrorFont);
            fillColor = graphicsTheme.ganttIdErrorColor;
            textColor = graphicsTheme.ganttIdTextErrorColor;
            imageMap += String.format("<area alt=\"<font face=arial>%s\" shape=\"rect\" coords=\"%d,%d,%d,%d\" >\n", md.getError(0), AbstractCanvas.transformToMapX(x1 + 1), AbstractCanvas.transformToMapY(y - getTaskHeight() / 2), AbstractCanvas.transformToMapX(x2 - 1), AbstractCanvas.transformToMapY(y + getTaskHeight() / 2));
        } else {
            graphics2D.setFont(idFont);
            fillColor = graphicsTheme.ganttIdColor;
            textColor = graphicsTheme.ganttIdTextColor;
        }
        graphics2D.setColor(fillColor);
        graphics2D.fillRect(x1 + 1, y - getTaskHeight() / 2, x2 - x1 - 1, getTaskHeight());
        // graphics2D.drawRect(x1 + 1, y - getTaskHeight() / 2, x2 - x1 - 2,
        // getTaskHeight() - 1);
        {
            graphics2D.setColor(textColor);
            FontMetrics fm = graphics2D.getFontMetrics();
//            int         maxAscent = fm.getMaxAscent();
            int yShift = fm.getAscent() - fm.getHeight() / 2;

            if (md != null) {
                graphics2D.drawString(String.format("%02d", task.getId()), x1 + 4, y + yShift, md.getError(0));
            } else {
                graphics2D.drawString(String.format("%02d", task.getId()), x1 + 4, y + yShift);
            }
        }
    }

    private void drawManualMarker(Task task, int x1, int y, boolean labelInside, Color textColor) {
        if (task.getTaskMode() == TaskMode.MANUALLY_SCHEDULED) {
            graphics2D.setColor(Color.red);
            graphics2D.fillRect(x1, y - getTaskHeight() / 2, 1, getTaskHeight());
            graphics2D.setFont(graphFont);
            graphics2D.setColor(textColor);
            FontMetrics fm     = graphics2D.getFontMetrics();
            int         yShift = fm.getAscent() - fm.getHeight() / 2;
            String      label  = String.format("%s", dateUtil.createDateTimeString(task.getStart()));
            int         width  = fm.stringWidth(label);
            if (labelInside) {

            } else {
                graphics2D.drawString(label, x1 - width, y + yShift);
            }
        }
    }

    private void drawMilestoneTask(Task task, int x1, int y, boolean labelInside, Color fillColor, Color textColor, String taskName) {
        if (task.getTaskMode() == TaskMode.MANUALLY_SCHEDULED) {
            graphics2D.setColor(Color.red);
//            graphics2D.fillRect(x1 - calendarXAxses.dayOfWeek.getWidth() / 2, y - getTaskHeight() / 2, 1, getTaskHeight());
            graphics2D.setColor(fillColor);
        } else {
            graphics2D.setColor(Color.gray);
        }
        int   milestoneWidth = getTaskHeight() / 2;
        int   c              = 0;
        int[] xpoints        = {x1 + c, x1 + c + milestoneWidth, x1 + c, x1 + c - milestoneWidth, x1 + c};
        int[] ypoints        = {y - getTaskHeight() / 2, y, y + getTaskHeight() / 2, y, y - getTaskHeight() / 2};
        int   npoints        = 5;
        graphics2D.fillPolygon(xpoints, ypoints, npoints);
        graphics2D.drawPolygon(xpoints, ypoints, npoints);
        graphics2D.setFont(graphFont);
        graphics2D.setColor(textColor);
        FontMetrics fm     = graphics2D.getFontMetrics();
        int         yShift = fm.getAscent() - fm.getHeight() / 2;
//        graphics2D.drawString(taskName, x2 + 5 + 8, y + yShift);
        if (labelInside) {

        } else {
            LocalDateTime start          = task.getStart();
            String        dateTimeString = dateUtil.createDateTimeString(task.getStart());
            graphics2D.drawString(String.format("%s (%s)", taskName, dateTimeString), x1 + 2 + 8 + c + milestoneWidth / 2, y + yShift);
        }
    }

    private void drawOutOfOffice(Task task, int y) {
        //TODO reintroduce bank holidays and vacations

        if (task.getAssignedUser() != null) {
            for (ProjectCalendarException calendarException : task.getAssignedUser().getCalendar().getCalendarExceptions()) {
                for (LocalDate currentDay = calendarException.getFromDate(); currentDay.isBefore(calendarException.getToDate()) || currentDay.isEqual(calendarException.getToDate()); currentDay = currentDay.plusDays(1)) {
//                    int    dayIndex = calculateDayIndex(calendarException.getFromDate());
                    String name = calendarException.getName();
                    if (name.equals(OffDayType.VACATION.name()) || name.equals(OffDayType.HOLIDAY.name()) || name.equals(OffDayType.SICK.name()) || name.equals(OffDayType.TRIP.name())) {
                        int daysX = calculateDayX(currentDay);
                        if (daysX > 0 && daysX < calendarXAxses.getWidth()) {
                            graphics2D.setFont(outOfOfficeFont);
                            graphics2D.setColor(graphicsTheme.ganttOutOfOfficeColor);
//                            graphics2D.fillRect(daysX - (calendarXAxses.dayOfWeek.getWidth() / 2 - 1), y - (calendarXAxses.dayOfWeek.getWidth() / 2) + 2, calendarXAxses.dayOfWeek.getWidth() - 1, getTaskHeight());
                            graphics2D.setColor(Color.WHITE);
                            FontMetrics fm        = graphics2D.getFontMetrics();
                            String      text      = "O";
                            int         width     = fm.stringWidth(text);
                            int         maxAscent = fm.getMaxAscent();
//                            graphics2D.drawString(text, daysX - width / 2, y + (maxAscent + 1) / 2 - 2);
                        }
                    }
                }
            }
        }


//        ProjectFile parentFile = task.getParentFile();
//        LocalDate   start      = parentFile.getEarliestStartDate().toLocalDate();
//        LocalDate   finish     = parentFile.getLatestFinishDate().toLocalDate();
//        String resourceName = null;
//        if (!task.getMilestone() && task.getResourceAssignments().size() == 1) {
//            for (ResourceAssignment assignment : task.getResourceAssignments()) {
//                Resource resource = assignment.getResource();
//                if (resource != null) {
//                    resourceName = resource.getName();
//                    break;
//                }
//            }
//        }
//        if (resourceName != null) {
//            ResourceUtilization ru = resourcesUtilization.get(Authors.mapToPrimaryLoginName(resourceName));
//            for (LocalDate currentDay = start; currentDay.isBefore(finish); currentDay = currentDay.plusDays(1)) {
//                int dayIndex = calculateDayIndex(currentDay);
//                if (ru != null && (ru.getVacation(dayIndex) != null || ru.getSickness(dayIndex) != null)) {
//                    int daysX = calculateDayX(currentDay);
//                    // int x1 = daysX - (calendarXAxses.dayOfWeek.width / 2 - 1);
//                    // int y1 = y - (calendarXAxses.dayOfWeek.width / 2) + 2;
//                    // graphics2D.drawImage(outOfOffice, null, x1, y1);
//                    graphics2D.setFont(outOfOfficeFont);
//                    graphics2D.setColor(graphicsTheme.ganttOutOfOfficeColor);
//                    graphics2D.fillRect(daysX - (calendarXAxses.dayOfWeek.getWidth() / 2 - 1), y - (calendarXAxses.dayOfWeek.getWidth() / 2) + 2,
//                            calendarXAxses.dayOfWeek.getWidth() - 1, getTaskHeight());
//                    graphics2D.setColor(Color.WHITE);
//                    FontMetrics fm        = graphics2D.getFontMetrics();
//                    String      text      = "O";
//                    int         width     = fm.stringWidth(text);
//                    int         maxAscent = fm.getMaxAscent();
//                    graphics2D.drawString(text, daysX - width / 2, y + (maxAscent + 1) / 2 - 2);
//                }
//            }
//        }
    }

    /**
     * T------------| | | C
     *
     * @param sourceTask
     * @param targetTask
     * @throws Exception
     */
    private void drawRelation(Task sourceTask, int y2, Task targetTask, int y1) throws Exception {

        int signum = (int) Math.signum(y2 - y1);
        int y3;
        if (signum > 0) {
            y2 -= getTaskHeight() / 2 - TASK_BODY_BORDER;
            y3 = y2 - 5;
        } else {
            y2 += getTaskHeight() / 2 - TASK_BODY_BORDER;
            y3 = y2 + 5;
        }
        int x1 = calculateX(targetTask.getFinish(), targetTask.getFinish().truncatedTo(ChronoUnit.DAYS).withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
        int x2 = RELATION_CORNER_LENGTH + calculateX(sourceTask.getStart(), sourceTask.getStart().truncatedTo(ChronoUnit.DAYS).withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2 - RESOURCE_NAME_TO_TASK_GAP;
        graphics2D.setStroke(new BasicStroke(RELATION_LINE_STROKE_WIDTH));
        if (sourceTask.isCritical() && targetTask.isCritical()) {
            graphics2D.setColor(graphicsTheme.ganttCriticalRelationColor);

        } else {
            graphics2D.setColor(graphicsTheme.ganttRelationColor);
        }
//        graphics2D.drawLine(x1, y1, x2, y1);
        graphics2D.fillRect(x1 + 1, y1, x2 - x1, 1);// -
//        graphics2D.drawLine(x2, y1, x2, y2);
        graphics2D.fillRect(x2, y1 + 1, 1, y3 - y1);// |
        if (y2 > y1) {
            //arrow head down
            // x2-d,y2-d x2+D,y2-D
            // x2,Y2

            int   d       = 5;
            int[] xPoints = {x2 - d, x2 + d, x2};
            int[] yPoints = {y2 - d + signum, y2 - d + signum, y2 + signum};
            graphics2D.setColor(graphicsTheme.ganttRelationColor);
            graphics2D.fillPolygon(xPoints, yPoints, xPoints.length);
        } else {
            //arrow head up
            // x2,Y2
            // x2+3,y2+3 x2-3,y2+3

            int   d       = 5;
            int[] xPoints = {x2 + d, x2 - d, x2};
            int[] yPoints = {y2 + d + signum, y2 + d + signum, y2 + signum};
            graphics2D.setColor(graphicsTheme.ganttRelationColor);
            graphics2D.fillPolygon(xPoints, yPoints, xPoints.length);
        }

    }

    private void drawRibbon(Graphics2D graphics2D, int y1, int x1, int y2, int delta1, int delta2, Color ribbonColor) {
        int[] xpoints = {x1, x1 + delta1, x1 + delta1 + delta2, x1 + delta2};
        int[] ypoints = {y2, y1, y1, y2};
        graphics2D.setColor(ribbonColor);
        graphics2D.fillPolygon(xpoints, ypoints, xpoints.length);
    }

    private void drawStoryBody(Task task, int x1, int x2, int y, Color fillColor, String marker, String toolTip) {
        Color originalColor = fillColor;
        if (marker == null) {
            drawTick(task.getStart(), x1, y);
            drawTick(task.getFinish(), x2, y);
            int y1 = y + TASK_BODY_BORDER;
            //rounded corners
//            graphics2D.setColor(fillColor);
//            int radius = 10;
//            int y1     = y + 6;
//            graphics2D.setStroke(new BasicStroke(2.0f));
//            graphics2D.drawLine(x1 + radius, y1 - getTaskHeight() / 2, x2 - radius, y1 - getTaskHeight() / 2);//upper -
//
//            graphics2D.drawArc(x1, y1 - getTaskHeight() / 2, radius * 2, radius * 2, 180, -90);//left corner
//            graphics2D.drawLine(x1, y1 - getTaskHeight() / 2 + radius, x1, y1);//left |
//
//            graphics2D.drawArc(x2 - radius * 2, y1 - getTaskHeight() / 2, radius * 2, radius * 2, 90, -90);//right corner
//            graphics2D.drawLine(x2, y1 - getTaskHeight() / 2 + radius, x2, y1);//right |

            int thickness = 2;
            graphics2D.fillRect(x1, y1 - getTaskHeight() / 2, x2 - x1 + 1, thickness);//upper ---
            graphics2D.fillRect(x1, y1 - getTaskHeight() / 2 + thickness, thickness, getTaskHeight() - TASK_BODY_BORDER * 2 - thickness);//left |
            graphics2D.fillRect(x1 + x2 - x1 - 1, y1 - getTaskHeight() / 2 + thickness, thickness, getTaskHeight() - TASK_BODY_BORDER * 2 - thickness);//right |
            if (x2 - x1 - 1 > 0) {
                ExtendedRectangle s = new ExtendedRectangle(x1 + 1, y1 - getTaskHeight() / 2, x2 - x1 - 1, getTaskHeight() - thickness * 2);
                s.setToolTip(toolTip);
                s.setVisible(false);
                graphics2D.fill(s);
            }
        } else {
            // int x1 = dayX1 - (calendarXAxses.dayOfWeek.width / 2 - 1);
            // int x2 = x1 + width;
            int   y1   = y - getTaskHeight() / 2 + 1;
            int   y2   = y1 + getTaskHeight() - 2;
            Shape clip = graphics2D.getClip();
            graphics2D.setClip(x1 + 1, y - getTaskHeight() / 2 + 2, x2 - x1 - 1, getTaskHeight() - 4);
            {
                // mark that we do not have a gantt chart
                int   delta1      = 25;// ribbon offset
                int   delta2      = 16;// ribbon width
                Color ribbonColor = originalColor;
                for (int x = x1 - delta2; x < x2; x += delta2) {
                    drawRibbon(graphics2D, y1, x, y2, delta1, delta2 - 1, ribbonColor);
                    if (ribbonColor == originalColor) {
                        ribbonColor = Color.white;
                    } else {
                        ribbonColor = originalColor;
                    }
                }
            }
            graphics2D.setClip(clip);
        }
    }

    public void drawTask(long gantUniqueId, Task task, boolean drawId, boolean drawRelations, boolean labelInside, boolean alien, String marker, List<Conflict> conflict, boolean drawOutOfOffice) throws Exception {
        //TODO implement this fixed
        if (GanttUtil.isValidTask(task)) {

            graphics2D.setStroke(new BasicStroke(FINE_LINE_STROKE_WIDTH));
            LocalDateTime start = task.getStart();
            LocalDateTime stop  = task.getFinish();
            int           x1    = calculateX(start, start.truncatedTo(ChronoUnit.DAYS).withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
            int           x2    = calculateX(stop, stop.truncatedTo(ChronoUnit.DAYS).withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
            Integer       lane  = taskHeight.get(gantUniqueId * 10000 + task.getId());
            int           y     = lane + getTaskHeight() / 2;
            if (drawOutOfOffice) {
                drawOutOfOffice(task, y);
            }
            drawTask(task, x1, x2, y, labelInside, alien, marker, conflict);
            if (drawId) {
                drawId(task, y);
            }
            if (drawRelations) {
                List<Relation> predecessors = task.getPredecessors();
                if (predecessors != null && !predecessors.isEmpty()) {
                    for (Relation relation : predecessors) {
                        Task sourceTask = task;
                        Task targetTask = task.getSprint().getTaskById(relation.getPredecessorId());
                        //TODO implement this
                        if (relation.isVisible()) {
                            int y1 = taskHeight.get(gantUniqueId * 10000 + targetTask.getId()) + getTaskHeight() / 2;
                            int y2 = taskHeight.get(gantUniqueId * 10000 + sourceTask.getId()) + getTaskHeight() / 2;
                            drawRelation(sourceTask, y2, targetTask, y1);
                        }
                    }
                }
            }
        }
    }

    private void drawTask(Task task, int x1, int x2, int y, boolean labelInside, boolean alien, String marker, List<Conflict> conflict) throws Exception {
        Color  fillColor           = graphicsTheme.getAuthorColor(28);
        Color  textColor           = graphicsTheme.ganttTaskTextColor;
        Color  textInfoColor       = graphicsTheme.ganttTaskTextColor;
        String resourceName        = null;
        String resourceUtilization = null;
        Number units               = null;

        String taskName = task.getName();
        if (!task.isMilestone()) {
            if (task.getAssignedUser() != null) {
                resourceName = task.getAssignedUser().getName();
//                String primaryResourceName = resourceName;
                units               = task.getAssignedUser().getAvailabilities().getLast().getAvailability() * 100;
                resourceUtilization = String.format("%.0f%%", units);
//                resourceName += resourceUtilization;
//                units = task.getAssignedUser().getAvailabilities().getLast().getAvailability() * 100;
//                taskName += String.format(" (%s %.0f%%)", primaryResourceName, units);
            }
        }
        if (task.isMilestone() && task.getChildTasks().size() == 0) {
            // ---Milestone, but not a story (MS Project marks stories with 0 duration as milestones
            fillColor = graphicsTheme.ganttMilestoneColor;
            textColor = graphicsTheme.ganttMilestoneTextColor;
        } else if (task.getChildTasks().size() != 0) {
            // ---Story
            fillColor = graphicsTheme.ganttStoryColor;
            textColor = graphicsTheme.ganttStoryTextColor;
        } else if (resourceName != null && units != null) {
            //task
            textColor = graphicsTheme.ganttTaskTextColor;
            int authorIndex = authorsContribution.getSortedKeyList().indexOf(resourceName);
            if (authorIndex != -1) {
                fillColor = authors.get(resourceName).color;
            }
        }
        if (task.isMilestone() && task.getChildTasks().size() == 0) {
            // milestone
            drawMilestoneTask(task, x1, y, labelInside, fillColor, textColor, taskName);
        } else {
            if (task.getChildTasks().size() != 0) {
                // story
                String tooltip = generateToolTip(task, x1, x2, y, marker, resourceName, resourceUtilization);
                drawStoryBody(task, x1, x2, y, fillColor, marker, tooltip);
                graphics2D.setFont(storyFont);
                graphics2D.setColor(textColor);
                FontMetrics fm     = graphics2D.getFontMetrics();
                int         yShift = fm.getAscent() - fm.getHeight() / 2;
                if (labelInside) {
                    // only used for team planner chart
                } else {
                    graphics2D.drawString(taskName, x2 + 2 + 8, y + yShift);
                }
            } else if (!task.isMilestone()) {
                //task
                String tooltip = generateToolTip(task, x1, x2, y, marker, resourceName, resourceUtilization);
                if (task.getProgress() == null) {
                    drawTaskBody(task, x1, x2, y, fillColor, alien, 0.0f, tooltip);
                } else {
                    drawTaskBody(task, x1, x2, y, fillColor, alien, task.getProgress().doubleValue(), tooltip);
                }
                drawConflictMarker(y, conflict);
                drawCriticalMarker(task, x1, x2, y);
                drawManualMarker(task, x1, y, labelInside, textColor);

                // task text
                if (labelInside) {
                    graphics2D.setColor(textColor);
                    graphics2D.setFont(taskInlineFont);
                    FontMetrics fm     = graphics2D.getFontMetrics();
                    int         yShift = fm.getAscent() - fm.getHeight() / 2;
                    Shape       clip   = graphics2D.getClip();
                    graphics2D.setClip(x1 + 1, y - getTaskHeight() / 2 + RESOURCE_NAME_TO_TASK_GAP, x2 - x1 - 1 - 2, getTaskHeight() - 6);
                    if (marker != null) {
                        graphics2D.drawString(marker, x1 + 1, y - getTaskHeight() / 2 + (LINE_HEIGHT) / 2 + yShift);
                    }
                    graphics2D.drawString(task.getName(), x1 + 1, y - getTaskHeight() / 2 + (LINE_HEIGHT) * RESOURCE_NAME_TO_TASK_GAP / 2 + yShift + 1);
                    if (resourceName != null) {
                        int stringWidth = fm.stringWidth(resourceName);
                        graphics2D.drawString(resourceName + resourceUtilization, x1 - stringWidth, y + (LINE_HEIGHT) * 5 / 2 + yShift);
                    }
                    graphics2D.setClip(clip);
                } else {
                    // progress text
                    if (task.getProgress() != null) {
                        Color blendedColor = ColorUtil.calculateColorBlending(fillColor, Color.white);
                        if (task.getProgress().doubleValue() > 0.5) {
                            blendedColor = ColorUtil.calculateColorBlending(fillColor, blendedColor);// we are drawing
                            // two times
                        }
                        Color heighestContrast = ColorUtil.heighestContrast(blendedColor);
                        graphics2D.setColor(heighestContrast);
                        graphics2D.setFont(taskProgressFont);
                        String text  = String.format("%2.0f%%", task.getProgress().doubleValue() * 100);
                        int    width = graphics2D.getFontMetrics().stringWidth(text);
                        // will the progess text fit into the task?
                        if (width < x2 - x1) {
                            FontMetrics fm     = graphics2D.getFontMetrics();
                            int         ascent = fm.getAscent();
                            Shape       clip   = graphics2D.getClip();
                            graphics2D.setClip(x1 + 1, y - getTaskHeight() / 2 + RESOURCE_NAME_TO_TASK_GAP, x2 - x1 - 1 - 2, getTaskHeight() - 6);
                            graphics2D.drawString(text, x1 + (x2 - x1) / 2 + 1 - width / 2, y + (ascent - 2) / 2);
                            graphics2D.setClip(clip);
                        }

                    }

                    {
                        //Task name
                        graphics2D.setColor(textColor);
                        graphics2D.setFont(graphFont);
                        FontMetrics fm     = graphics2D.getFontMetrics();
                        int         yShift = fm.getAscent() - fm.getHeight() / 2;
                        graphics2D.drawString(taskName, x2 + TASK_NAME_TO_TASK_GAP, y + yShift);
                    }
                    if (resourceName != null) {
                        //resource name+info
                        graphics2D.setColor(textColor);
                        graphics2D.setFont(graphFont);
                        FontMetrics fm1               = graphics2D.getFontMetrics();
                        int         yShift            = fm1.getAscent();
                        int         resourceNameWidth = fm1.stringWidth(resourceName);
                        int         resourceNameX     = x1 - resourceNameWidth - RESOURCE_NAME_TO_TASK_GAP;
                        graphics2D.drawString(resourceName, resourceNameX, y - getTaskHeight() / 2 + yShift);
                        {
                            graphics2D.setColor(textInfoColor);
                            graphics2D.setFont(taskResourceLocationFont);
                            FontMetrics fm2   = graphics2D.getFontMetrics();
                            int         infoY = y + getTaskHeight() / 2 - TASK_BODY_BORDER;
                            graphics2D.drawString(resourceUtilization, resourceNameX, infoY);

                            String location      = task.getAssignedUser().getLocations().getLast().getCountry() + "/" + task.getAssignedUser().getLocations().getLast().getState();
                            int    locationWidth = fm2.stringWidth(location);
                            int    locationX     = x1 - locationWidth - RESOURCE_NAME_TO_TASK_GAP;
//                            graphics2D.setColor(Color.orange);
//                            graphics2D.fillRect(locationX, y - getTaskHeight() / 2 + 1, locationWidth, 5);
//                            graphics2D.setColor(Color.black);
                            graphics2D.drawString(location, locationX, infoY);
                        }
                    }
                }
            }
            {
                // image map
                //                String start = DateUtil.createDateString(task.getStart(), dateUtil.dtfymdhms);
                //                String finish = DateUtil.createDateString(task.getFinish(), dateUtil.dtfymdhms);
                //                String progress = null;
                //                if (task.getPercentageComplete() != null) {
                //                    progress = String.format("%2.0f%%", task.getPercentageComplete().doubleValue() * 100);
                //
                //                }
                //                String duration = MpxjUtil.createDurationString(task.getDuration(), true, true, true);
                imageMap += "<area alt=\"<font face=arial>";
                imageMap += generateToolTip(task, x1, x2, y, marker, resourceName, resourceUtilization);
                imageMap += " >\n";
            }
        }
    }

    private void drawTaskBody(Task task, int x1, int x2, int y, Color fillColor, boolean alien, double progress, String toolTip) {
        Color originalColor = fillColor;
        if (!alien) {
            int y1 = y - getTaskHeight() / 2 + TASK_BODY_BORDER - 1;
            int h  = getTaskHeight() - TASK_BODY_BORDER * 2;
            if (x2 - x1 - 1 - 1 > 0) {
                //sometimes tasks are so small, that we cannot draw them.
                drawTick(task.getStart(), x1, y);
                drawTick(task.getFinish(), x2, y);

                User user = task.getAssignedUser();
                if (user != null && user.getCalendar() != null) {
                    int             days = (int) Duration.between(task.getStart().truncatedTo(ChronoUnit.DAYS), task.getFinish().truncatedTo(ChronoUnit.DAYS)).toDays();
                    ProjectCalendar pc   = user.getCalendar();
                    for (int day = 0; day <= days; day++) {
                        LocalDateTime currentDay = task.getStart().truncatedTo(ChronoUnit.DAYS).plusDays(day);
                        if (pc.isWorkingDate(currentDay.toLocalDate())) {
                            graphics2D.setColor(fillColor);
                            if (day == 0) {
                                //is this the left end?
                                int   xStart  = calculateX(task.getStart(), currentDay.withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                                int   xFinish = calculateX(currentDay.withHour(8).plusSeconds(SECONDS_PER_DAY), currentDay.withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                                Shape s       = new RectangleWithToolTip(xStart, y1 + 1, xFinish - xStart, h, toolTip);
                                graphics2D.fill(s);
                            } else if (day == days) {
                                //or is this the right end?
                                int   xStart  = calculateX(currentDay.withHour(8), currentDay.withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                                int   xFinish = calculateX(task.getFinish(), currentDay.withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                                Shape s       = new RectangleWithToolTip(xStart, y1 + 1, x2 - xStart + 1, h, toolTip);
                                graphics2D.fill(s);

                            } else {
                                int   xStart = calculateX(currentDay.withHour(8), currentDay.withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                                Shape s      = new RectangleWithToolTip(xStart, y1 + 1, calendarXAxses.dayOfWeek.getWidth(), h, toolTip);
                                graphics2D.fill(s);
                            }
                        } else {
                            graphics2D.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 64));//very trabsparent
                            int   xStart = calculateX(currentDay.withHour(8), currentDay.withHour(8), SECONDS_PER_DAY) - calendarXAxses.dayOfWeek.getWidth() / 2;
                            Shape s      = new RectangleWithToolTip(xStart, y1 + 1, calendarXAxses.dayOfWeek.getWidth(), h, toolTip);
                            graphics2D.fill(s);
                        }
                    }
                }
            }

            if (progress > 0.0 && numberOfLinesPerTask == 1) {
                graphics2D.fillRect(x1, y - getTaskHeight() / 2 + 2 + 2 - 1, (int) ((x2 - x1) * progress - 1), getTaskHeight() - 4 - 4 + 2);
                if (progress < 1.0) {
                    graphics2D.setColor(Color.black);
                    graphics2D.fillRect(x1 + (int) ((x2 - x1) * progress - 1), y - getTaskHeight() / 2 + 2, 1, getTaskHeight() - 4);
                }
            }
        } else {
            int   y1   = y - getTaskHeight() / 2 + 1;
            int   y2   = y1 + getTaskHeight() - 1;
            Shape clip = graphics2D.getClip();
            graphics2D.setClip(x1, y - getTaskHeight() / 2 + 2, x2 - x1 - 1, getTaskHeight() - 4);
            {
                // mark that we do not have a gantt chart
                int   delta1      = 25;// ribbon offset
                int   delta2      = 16;// ribbon width
                Color ribbonColor = originalColor;
                for (int x = x1 - delta2; x < x2; x += delta2) {
                    drawRibbon(graphics2D, y1, x, y2, delta1, delta2 - 1, ribbonColor);
                    if (ribbonColor == originalColor) {
                        ribbonColor = Color.white;
                    } else {
                        ribbonColor = originalColor;
                    }
                }
            }
            graphics2D.setClip(clip);
        }
    }

    private void drawTick(LocalDateTime time, int x, int y) {
        DateTimeFormatter formatter  = DateTimeFormatter.ofPattern("HH:mm");
        String            timeString = time.format(formatter);
        graphics2D.setColor(graphicsTheme.ganttTaskTickLineColor);
        graphics2D.fillRect(x, y - getTaskHeight() / 2 + TASK_BODY_BORDER - 2, 1, 2);
        graphics2D.setFont(taskTickFont);
        graphics2D.setColor(graphicsTheme.ganttTaskTickTextColor);
        FontMetrics fm    = graphics2D.getFontMetrics();
        int         width = fm.stringWidth(timeString);
        graphics2D.drawString(timeString, x - width / 2, y - getTaskHeight() / 2 + TASK_BODY_BORDER - RESOURCE_NAME_TO_TASK_GAP);
    }

    private String generateToolTip(Task task, int x1, int x2, int y, String marker, String resourceName, String resourceUtelization) throws Exception {
        String start    = DateUtil.createDateString(task.getStart(), dateUtil.dtfymdhms);
        String finish   = DateUtil.createDateString(task.getFinish(), dateUtil.dtfymdhms);
        String progress = null;
        if (task.getProgress() != null) {
            progress = String.format("%2.0f%%", task.getProgress().doubleValue() * 100);

        }
        String duration = DateUtil.createDurationString(task.getDuration(), true, true, true);
        String toolTip  = "";
        if (marker != null) {
            toolTip += String.format("%s<br>", marker);
        } else {
        }
        if (resourceName != null) {
            String primaryResourceName = resourceName;
            toolTip += String.format("<b>%s</b><br>", task.getName(), primaryResourceName + resourceUtelization, start, finish, task.getNotes());
        }
        if (task.getChildTasks().size() == 0 || resourceName != null) {
            String primaryResourceName = resourceName;
            toolTip += String.format("<b>Resource</b> %s<br>", primaryResourceName + resourceUtelization);
        }
        toolTip += String.format("<b>Duration</b> %s<br>", duration);
        toolTip += String.format("<b>Start</b> %s<br>", start);
        toolTip += String.format("<b>Finish</b> %s<br>", finish);
        if (task.getChildTasks().size() == 0 && progress != null) {
            toolTip += String.format("<b>Progress</b> %s<br>", progress);
        }
        toolTip += String.format("<b>Notes</b> %s", task.getNotes());
        toolTip += String.format("\" shape=\"rect\" coords=\"%d,%d,%d,%d\"", AbstractCanvas.transformToMapX(x1 + 1), AbstractCanvas.transformToMapY(y - getTaskHeight() / 2), AbstractCanvas.transformToMapX(x2 - 1), AbstractCanvas.transformToMapY(y + getTaskHeight() / 2));
        return toolTip;
    }

    @Override
    protected int getTaskHeight() {
        return LINE_HEIGHT * numberOfLinesPerTask + 4;
    }

    protected void initOutOfOffice(/*GanttInformationList gattInformationList*/) {
//TODO reintroduce bank holidays and vacations

//        int vacations = 0;
//        for (GanttInformation ganttInformation : gattInformationList) {
//            if (context.debug.filterGantt(ganttInformation)) {
//                logger.trace("Extracting out-of-Office information from " + ganttInformation.projectFileName);
//                for (Resource resource : ganttInformation.projectFile.getResources()) {
//                    String                                                     primaryResourcename = Authors.mapToPrimaryLoginName(resource.getName());
//                    com.ricoh.sdced.projects.dashboard.dao.ResourceUtilization ru                  = resourcesUtilization.get(primaryResourcename);
//                    if (ru == null && primaryResourcename != null) {
//                        ru = new ResourceUtilization(milestones.firstMilestone, days);
//                        ru.setMaxUnit(0.7);
//                        resourcesUtilization.put(primaryResourcename, ru);
//                    }
//                    ProjectCalendar resourceCalendar = resource.getCalendar();
//                    if (resourceCalendar != null && ru != null) {
//                        for (ProjectCalendarException pce : resourceCalendar.getCalendarExceptions()) {
//                            vacations += addVacation(ru, resource.getName(), pce);
//                        }
//                    } else if (resource.getID() == 0) {
//                        // we do not expect a calendar
//                    } else {
//                        logger.trace(resource.getName() + " has no calendar");
//                    }
//                }
//            }
//        }
//        logger.info(String.format("Included %d vacation days from %d gantt charts.", vacations, gattInformationList.size()));

    }

//    protected void initOutOfOffice(TimeTracker timeTracker) {
//        if (timeTracker != null) {
//            for (TimeTrackerUser user : timeTracker.timeTrackerUserList) {
//                for (TimeTrackerPunch punch : user.racPunchList) {
//                    String                                                     resourceName        = timeTracker.userIdToUserNameMap.get(punch.userId).name;
//                    String                                                     primaryResourcename = Authors.mapToPrimaryLoginName(resourceName);
//                    com.ricoh.sdced.projects.dashboard.dao.ResourceUtilization ru                  = resourcesUtilization.get(primaryResourcename);
//                    if (ru == null && primaryResourcename != null) {
//                        ru = new ResourceUtilization(milestones.firstMilestone, days);
//                        ru.setMaxUnit(0.7);
//                        resourcesUtilization.put(primaryResourcename, ru);
//                    }
//                    if (ru != null) {
//                        int startDayIndex = calculateDayIndex(punch.in);
//                        if (startDayIndex > -1) {
//                            if (punch.projectId == 1) {
//                                switch (punch.taskId) {
//                                    case 285:// vacation
//                                        ru.addVacation(startDayIndex, 1.0);
//                                        break;
//                                    case 286:// sickness
//                                        ru.addSickness(startDayIndex, 1.0);
//                                        break;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}
