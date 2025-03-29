package de.bushnaq.abdalla.projecthub.report.renderer;

import de.bushnaq.abdalla.projecthub.dao.Context;
import de.bushnaq.abdalla.projecthub.dto.OffDay;
import de.bushnaq.abdalla.projecthub.dto.Relation;
import de.bushnaq.abdalla.projecthub.dto.Task;
import de.bushnaq.abdalla.projecthub.dto.TaskMode;
import de.bushnaq.abdalla.projecthub.report.Canvas;
import de.bushnaq.abdalla.projecthub.report.dao.*;
import de.bushnaq.abdalla.svg.util.ExtendedRectangle;
import de.bushnaq.abdalla.svg.util.RectangleWithToolTip;
import de.bushnaq.abdalla.util.ColorUtil;
import de.bushnaq.abdalla.util.GanttErrorHandler;
import de.bushnaq.abdalla.util.TaskUtil;
import de.bushnaq.abdalla.util.date.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private static final   int                  LINE_HEIGHT                = 13;
    private static final   float                RELATION_LINE_STROKE_WIDTH = 1f;
    private final static   Font                 graphFont                  = new Font("Arial", Font.PLAIN, 12);
    private final static   Font                 idErrorFont                = new Font("Arial", Font.BOLD, 12);
    private final static   Font                 idFont                     = new Font("Arial", Font.PLAIN, 12);
    protected final static Font                 outOfOfficeFont            = new Font("Arial", Font.BOLD, 20);
    private final static   Font                 storyFont                  = new Font("Arial", Font.BOLD, 12);
    private final static   Font                 taskInlineFont             = new Font("Arial", Font.PLAIN, 12);
    private final static   Font                 taskProgressFont           = new Font("Arial", Font.PLAIN, 8);
    protected              AuthorsContribution  authorsContribution        = new AuthorsContribution();// remaining and worked per author
    protected              Context              context;
    private final          DateUtil             dateUtil                   = new DateUtil();
    protected              GanttErrorHandler    geh                        = new GanttErrorHandler();
    private final          Logger               logger                     = LoggerFactory.getLogger(this.getClass());
    private final          int                  numberOfLinesPerTask;
    protected              ResourcesUtilization resourcesUtilization       = new ResourcesUtilization();// only used for out of office
    protected              Map<Long, Integer>   taskHeight                 = new HashMap<>();

    public AbstractGanttRenderer(Context context, String sprintName/*, Map<LocalDate, String> bankHolidays*/, boolean completed, int chartWidth, int chartHeight,
                                 int numberOfLinesPerTask, int preRun, int postRun, BurnDownGraphicsTheme graphicsTheme) throws IOException {
        super(sprintName/*, bankHolidays*/, completed, chartWidth, chartHeight, preRun, postRun, graphicsTheme);
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
                    int c1 = calculateX(c.range.start, c.range.start.truncatedTo(ChronoUnit.DAYS).withHour(8), 75 * 6 * 60)
                            - calendarXAxses.dayOfWeek.getWidth() / 2;
                    int c2 = calculateX(c.range.end, c.range.end.truncatedTo(ChronoUnit.DAYS).withHour(8), 75 * 6 * 60)
                            - calendarXAxses.dayOfWeek.getWidth() / 2;
                    graphics2D.fillRect(c1, y - getTaskHeight() / 2, c2 - c1 - 1, 2);
                    graphics2D.fillRect(c1, y + getTaskHeight() / 2 - 1, c2 - c1 - 1, 2);
                }
            }
        }
    }

    private void drawCriticalMarker(Task task, int x1, int x2, int y) {
        if (task.isCritical()) {
            graphics2D.setColor(graphicsTheme.ganttCriticalTaskBorderColor);
            graphics2D.drawRect(x1, y - getTaskHeight() / 2 + 2 - 1, x2 - x1 - 1 - 1, getTaskHeight() - 1 - 4 + 2);
        } else {
            graphics2D.setColor(graphicsTheme.ganttTaskBorderColor);
            graphics2D.drawRect(x1, y - getTaskHeight() / 2 + 2 - 1, x2 - x1 - 1 - 1, getTaskHeight() - 1 - 4 + 2);
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
            imageMap += String.format("<area alt=\"<font face=arial>%s\" shape=\"rect\" coords=\"%d,%d,%d,%d\" >\n", md.getError(0),
                    Canvas.transformToMapX(x1 + 1), Canvas.transformToMapY(y - getTaskHeight() / 2), Canvas.transformToMapX(x2 - 1),
                    Canvas.transformToMapY(y + getTaskHeight() / 2));
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
            FontMetrics fm        = graphics2D.getFontMetrics();
            int         maxAscent = fm.getMaxAscent();
            if (md != null) {
                graphics2D.drawString(String.format("%02d", task.getId()), x1 + 4, y + getTaskHeight() / 2 - (maxAscent + 1) / 4, md.getError(0));
            } else {
                graphics2D.drawString(String.format("%02d", task.getId()), x1 + 4, y + getTaskHeight() / 2 - (maxAscent + 1) / 4);
            }
        }
    }

    private void drawManualMarker(Task task, int x1, int y, boolean labelInside, Color textColor) {
        if (task.getTaskMode() == TaskMode.MANUALLY_SCHEDULED) {
            graphics2D.setColor(Color.red);
            graphics2D.fillRect(x1 + 1, y - getTaskHeight() / 2, 1, getTaskHeight());
            graphics2D.setFont(graphFont);
            graphics2D.setColor(textColor);
            FontMetrics fm        = graphics2D.getFontMetrics();
            int         maxAscent = fm.getMaxAscent();
            String      lable     = String.format("%s", dateUtil.createDateTimeString(task.getStart()));
            int         width     = fm.stringWidth(lable);
            if (labelInside) {

            } else {
                graphics2D.drawString(lable, x1 - width, y + getTaskHeight() / 2 - (maxAscent + 1) / 4);
            }
        }
    }

    private void drawOutOfOffice(Task task, int y) {
        //TODO implement this
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
        if (signum > 0) {
            y2 -= getTaskHeight() / 2 + 1 - 2 + 1;
        } else {
            y2 += getTaskHeight() / 2 + 1 - 2 + 1;
        }
        int x1 = calculateX(targetTask.getFinish(), targetTask.getFinish().truncatedTo(ChronoUnit.DAYS).withHour(8), 75 * 6 * 60)
                - calendarXAxses.dayOfWeek.getWidth() / 2 - 1;
        int x2 = 8 + calculateX(sourceTask.getStart(), sourceTask.getStart().truncatedTo(ChronoUnit.DAYS).withHour(8), 75 * 6 * 60)
                - calendarXAxses.dayOfWeek.getWidth() / 2 - 3;
        graphics2D.setStroke(new BasicStroke(RELATION_LINE_STROKE_WIDTH));
        if (sourceTask.isCritical() && targetTask.isCritical()) {
            graphics2D.setColor(graphicsTheme.ganttCriticalRelationColor);

        } else {
            graphics2D.setColor(graphicsTheme.ganttRelationColor);
        }
        graphics2D.drawLine(x1, y1, x2, y1);
        graphics2D.drawLine(x2, y1, x2, y2);
        if (y2 > y1) {
            // x2-d,y2-d x2+D,y2-D
            // x2,Y2

            int   d       = 5;
            int[] xpoints = {x2 - d, x2 + d, x2};
            int[] ypoints = {y2 - d + signum, y2 - d + signum, y2 + signum};
            graphics2D.setColor(graphicsTheme.ganttRelationColor);
            graphics2D.fillPolygon(xpoints, ypoints, xpoints.length);
        } else {
            // x2,Y2
            // x2+3,y2+3 x2-3,y2+3

            int   d       = 5;
            int[] xpoints = {x2 + d, x2 - d, x2};
            int[] ypoints = {y2 + d + signum, y2 + d + signum, y2 + signum};
            graphics2D.setColor(graphicsTheme.ganttRelationColor);
            graphics2D.fillPolygon(xpoints, ypoints, xpoints.length);
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
            graphics2D.setColor(fillColor);
            graphics2D.fillRect(x1 + 1, y - getTaskHeight() / 2 + 2, x2 - x1 - 1, 2);
            graphics2D.fillRect(x1 + 1, y - getTaskHeight() / 2 + 2, 2, getTaskHeight() - 4);
            graphics2D.fillRect(x1 + 1 + x2 - x1 - 1 - 2, y - getTaskHeight() / 2 + 2, 2, getTaskHeight() - 4);
            if (x2 - x1 - 1 > 0) {
                ExtendedRectangle s = new ExtendedRectangle(x1 + 1, y - getTaskHeight() / 2 + 2, x2 - x1 - 1, getTaskHeight() - 4);
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

    public void drawTask(long gantUniqueId, Task task, boolean drawId, boolean drawRelations, boolean labelInside, boolean alien, String marker,
                         List<Conflict> conflict, boolean drawOutOfOffice) throws Exception {
        //TODO implement this
//        if (GanttUtil.isValidTask(task))
        {

            graphics2D.setStroke(new BasicStroke(FINE_LINE_STROKE_WIDTH));
            LocalDateTime start = task.getStart();
            LocalDateTime stop  = task.getFinish();
            int           x1    = calculateX(start, start.truncatedTo(ChronoUnit.DAYS).withHour(8), 75 * 6 * 60) - calendarXAxses.dayOfWeek.getWidth() / 2;
            int           x2    = calculateX(stop, stop.truncatedTo(ChronoUnit.DAYS).withHour(8), 75 * 6 * 60) - calendarXAxses.dayOfWeek.getWidth() / 2;
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
//                        if (!relation.getLag().equals(Duration.getInstance(0, TimeUnit.MINUTES)))
                        {
                            int y1 = taskHeight.get(gantUniqueId * 10000 + targetTask.getId()) + getTaskHeight() / 2;
                            int y2 = taskHeight.get(gantUniqueId * 10000 + sourceTask.getId()) + getTaskHeight() / 2;
                            drawRelation(sourceTask, y2, targetTask, y1);
                        }
                    }
                }
            }
        }
    }

    private void drawTask(Task task, int x1, int x2, int y, boolean labelInside, boolean alien, String marker, List<Conflict> conflict)
            throws Exception {
        Color  fillColor           = graphicsTheme.getAuthorColor(28);
        Color  textColor           = Color.black;
        String resourceName        = null;
        String resourceUtelization = null;
        Number units               = null;

        String taskName = task.getName();
        if (!task.isMilestone()) {
            if (task.getAssignedUser() != null) {
                resourceName = task.getAssignedUser().getName();
                String primaryResourceName = resourceName;
//                units = task.getWork().assignment.getUnits();
//                    resourceUtelization = String.format(" (%.0f%%)", units);
                taskName += String.format(" (%s %.0f%%)", primaryResourceName, units);
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
            String primaryResourceName = resourceName;
            textColor = graphicsTheme.ganttTaskTextColor;
            int authorIndex = authorsContribution.getSortedKeyList().indexOf(primaryResourceName);
            if (authorIndex != -1) {
                fillColor = authors.get(primaryResourceName).color;
            }
        }
        if (task.isMilestone() && task.getChildTasks().size() == 0) {
            // milestone
            if (task.getTaskMode() == TaskMode.MANUALLY_SCHEDULED) {
                graphics2D.setColor(Color.red);
                graphics2D.fillRect(x1 + 1 - calendarXAxses.dayOfWeek.getWidth() / 2, y - getTaskHeight() / 2, 1, getTaskHeight());
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
            FontMetrics fm        = graphics2D.getFontMetrics();
            int         maxAscent = fm.getMaxAscent();
            if (labelInside) {

            } else {
                LocalDateTime start          = task.getStart();
                String        dateTimeString = dateUtil.createDateTimeString(task.getStart());
                graphics2D.drawString(String.format("%s (%s)", taskName, dateUtil.createDateTimeString(task.getStart())), x1 + 2 + 8 + c + milestoneWidth / 2,
                        y + getTaskHeight() / 2 - (maxAscent + 1) / 4);
            }
        } else {
            if (task.getChildTasks().size() != 0) {
                // story
                String tooltip = generateToolTip(task, x1, x2, y, marker, resourceName, resourceUtelization);
                drawStoryBody(task, x1, x2, y, fillColor, marker, tooltip);
                graphics2D.setFont(storyFont);
                graphics2D.setColor(textColor);
                FontMetrics fm        = graphics2D.getFontMetrics();
                int         maxAscent = fm.getMaxAscent();
                if (labelInside) {
                    // only used for team planner chart
                } else {
                    graphics2D.drawString(taskName, x2 + 2 + 8, y + getTaskHeight() / 2 - (maxAscent + 1) / 4);
                }
            } else if (!task.isMilestone()) {
                String tooltip = generateToolTip(task, x1, x2, y, marker, resourceName, resourceUtelization);
                // task
                if (task.getProgress() == null) {
                    drawTaskBody(x1, x2, y, fillColor, alien, 0.0f, tooltip);
                } else {
                    drawTaskBody(x1, x2, y, fillColor, alien, task.getProgress().doubleValue(), tooltip);
                }
                drawConflictMarker(y, conflict);
                drawCriticalMarker(task, x1, x2, y);
                drawManualMarker(task, x1, y, labelInside, textColor);

                // task text
                if (labelInside) {
                    graphics2D.setColor(textColor);
                    graphics2D.setFont(taskInlineFont);
                    FontMetrics fm     = graphics2D.getFontMetrics();
                    int         ascent = fm.getAscent();

                    Shape clip = graphics2D.getClip();
                    graphics2D.setClip(x1 + 1, y - getTaskHeight() / 2 + 3, x2 - x1 - 1 - 2, getTaskHeight() - 6);
                    if (marker != null) {
                        graphics2D.drawString(marker, x1 + 1, y - getTaskHeight() / 2 + 2 + (LINE_HEIGHT) / 2 + (ascent - 2) / 2 + 1);
                    }
                    graphics2D.drawString(task.getName(), x1 + 1, y - getTaskHeight() / 2 + 2 + (LINE_HEIGHT) * 3 / 2 + (ascent - 2) / 2 + 1);
                    if (resourceName != null) {
                        String primaryResourceName = resourceName;
                        graphics2D.drawString(primaryResourceName + resourceUtelization, x1 + 1,
                                y - getTaskHeight() / 2 + 2 + (LINE_HEIGHT) * 5 / 2 + (ascent - 2) / 2 + 1);
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
                            graphics2D.setClip(x1 + 1, y - getTaskHeight() / 2 + 3, x2 - x1 - 1 - 2, getTaskHeight() - 6);
                            graphics2D.drawString(text, x1 + (x2 - x1) / 2 + 1 - width / 2, y + (ascent - 2) / 2 + 1);
                            graphics2D.setClip(clip);
                        }

                    }

                    graphics2D.setColor(textColor);
                    graphics2D.setFont(graphFont);
                    FontMetrics fm     = graphics2D.getFontMetrics();
                    int         ascent = fm.getAscent();
                    graphics2D.drawString(taskName, x2 + 2 + 8, y + (ascent - 2) / 2 + 1);
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
                imageMap += generateToolTip(task, x1, x2, y, marker, resourceName, resourceUtelization);
                imageMap += " >\n";
            }
        }
    }

    private void drawTaskBody(int x1, int x2, int y, Color fillColor, boolean alien, double progress, String toolTip) {
        Color originalColor = fillColor;
        if (!alien) {
            graphics2D.setColor(Color.white);
            int y1 = y - getTaskHeight() / 2 + 2 - 1;
            int h  = getTaskHeight() - 4 + 2;
            graphics2D.setColor(fillColor);
            //            graphics2D.fillRect(x1, y1, x2 - x1 - 1, h);
            //            graphics2D.drawRect(x1, y - getTaskHeight() / 2 + 2 - 1, x2 - x1 - 1 - 1, getTaskHeight() - 1 - 4 + 2);
            if (x2 - x1 - 1 - 1 > 0) {
                //sometimes taks are so small, that we cannot draw them.
                Shape s = new RectangleWithToolTip(x1, y1, x2 - x1 - 1 - 1, h - 1, toolTip);
                graphics2D.fill(s);
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

    private String generateToolTip(Task task, int x1, int x2, int y, String marker, String resourceName, String resourceUtelization)
            throws Exception {
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
        toolTip += String.format("\" shape=\"rect\" coords=\"%d,%d,%d,%d\"", Canvas.transformToMapX(x1 + 1), Canvas.transformToMapY(y - getTaskHeight() / 2),
                Canvas.transformToMapX(x2 - 1), Canvas.transformToMapY(y + getTaskHeight() / 2));
        return toolTip;
    }

    @Override
    protected int getTaskHeight() {
        return LINE_HEIGHT * numberOfLinesPerTask + 4;
    }

    protected void initOutOfOffice(/*GanttInformationList gattInformationList*/) {
        //TODO implement this
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
