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
import de.bushnaq.abdalla.projecthub.dto.Sprint;
import de.bushnaq.abdalla.projecthub.dto.Task;
import de.bushnaq.abdalla.projecthub.dto.User;
import de.bushnaq.abdalla.projecthub.gantt.GanttUtil;
import de.bushnaq.abdalla.projecthub.report.dao.*;
import de.bushnaq.abdalla.svg.util.ExtendedGraphics2D;
import de.bushnaq.abdalla.util.ErrorException;
import de.bushnaq.abdalla.util.GanttErrorHandler;
import de.bushnaq.abdalla.util.TaskUtil;
import de.bushnaq.abdalla.util.date.DateUtil;
import net.sf.mpxj.ProjectCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * renders a gantt chart using ms project mpp file as base
 * Will make out of office time visible
 *
 * @author abdalla.bushnaq
 */
public class GanttRenderer extends AbstractGanttRenderer {

    private static final int    ONE_WEEK = 7;
    private final        Logger logger   = LoggerFactory.getLogger(this.getClass());
    private final        Sprint sprint;


    public GanttRenderer(Context context, String sprintName, List<Throwable> exceptions, LocalDateTime now, boolean completed,
                         Sprint sprint/*, int chartWidth, int chartHeight*/, String cssClass, BurnDownGraphicsTheme graphicsTheme)
            throws Exception {
        super(context, sprintName/*, context.bankHolidays*/, completed/*, chartWidth, chartHeight*/, 1, 7, 14, graphicsTheme);
//        this.ganttFileName = ganttFileName;
        this.sprint = sprint;
//        this.timeTracker   = context.timeTracker;
        this.graphicsTheme = graphicsTheme;
//        geh.exceptions     = exceptions;
        milestones.add(now.toLocalDate(), "N", "Now (current date)", Color.blue);
        milestones.add(sprint.getEarliestStartDate().toLocalDate(), "S", "Start (Start of project)", Color.blue);
        milestones.add(sprint.getLatestFinishDate().toLocalDate(), "E", "End (End of project)", Color.blue);
        if (completed) {
            //We do not want to keep drawing the graph further and further to include the current date, if it is closed.
            if (now.isAfter(DateUtil.addDay(sprint.getLatestFinishDate(), ONE_WEEK))) {
                milestones.remove("N");
            }
        }
        milestones.calculate();
        processInit(sprintName);
    }

    @Override
    protected int calculateChartHeight() {
        return GanttUtil.calculateNumberOfTasks(sprint) * (getTaskHeight() + 1) + calendarXAxses.getHeight();
    }

    @Override
    protected int calculateChartWidth() {
        //TODO why are we ignoring postRun days?
        return calendarXAxses.dayOfWeek.getWidth() * (days + calendarXAxses.getPriRun() /*+ calendarXAxses.getPostRun()*/);
    }

    @Override
    protected void calculateDayWidth() {
        super.calculateDayWidth();
        calendarXAxses.dayOfWeek.setWidth(20);
    }

    protected void calculateTaskHightMap(int yOffset) {
        int y = yOffset;
        for (Task task : sprint.getTasks()) {
            if (GanttUtil.isValidTask(task)) {
                taskHeight.put(task.getId(), y);
                y += getTaskHeight() + 1;
            }
        }
    }

    @Override
    public void draw(ExtendedGraphics2D graphics2D, int x, int y) throws Exception {
        this.graphics2D = graphics2D;
        initPosition(x, y);
        calculateTaskHightMap(y + calendarXAxses.getHeight());
        drawCalendar();
        drawMilestones();
        drawGanttChart();
    }

    @Override
    public void drawDayBars(LocalDate currentDay) {
        int gantUniqueId = 0;
        for (Task task : sprint.getTasks()) {
            User            user = task.getAssignedUser();
            ProjectCalendar pc;
            if (user != null) {
                pc = user.getCalendar();
            } else {
                pc = sprint.getDefaultCalendar();
            }
            Color color = GraphColorUtil.getDayStripeColor(graphicsTheme, pc, currentDay);
            graphics2D.setColor(color);

            Integer lane = taskHeight.get(gantUniqueId * 10000 + task.getId());
            int     y    = lane + getTaskHeight() / 2;
            int     x    = calculateDayX(currentDay);
            int     y1   = y - getTaskHeight() / 2;
            int     x1   = x - (calendarXAxses.dayOfWeek.getWidth() / 2 - 1);

            Shape s = new Rectangle(x1, y1, calendarXAxses.dayOfWeek.getWidth() - 1, getTaskHeight());
            graphics2D.fill(s);
        }
    }

    protected void drawGanttChart() throws Exception {
        drawGanttChart(calendarXAxses.getHeight());
        //        drawAuthorLegend();
        //        drawGraphFrame(firstDayX);
    }

    protected void drawGanttChart(int yOffset) throws Exception {
        for (Task task : sprint.getTasks()) {
            if (GanttUtil.isValidTask(task)) {
                drawTask(0, task, true, true, false, false, null, null, true);
            }
        }
    }

    private void processInit(String ganttFileName) throws Exception {
        initSize(0, 0, false);
        for (Task task : sprint.getTasks()) {
            if (GanttUtil.isValidTask(task)) {
                if (task.getAssignedUser() != null && task.getAssignedUser().getName() != null) {
                    String primaryResourceName = task.getAssignedUser().getName();
                    if (primaryResourceName == null) {
                        primaryResourceName = task.getAssignedUser().getName();
                    }
                    AuthorContribution ew = authorsContribution.get(primaryResourceName);
                    if (ew == null) {
                        ew = new AuthorContribution();
                        authorsContribution.put(primaryResourceName, ew);
                    }
                }
            }
        }
        for (String authorName : authorsContribution.getSortedKeyList()) {
            if (authorName != null) {
                authors.add(authorName);
            }
        }
        authors.calculateColors(graphicsTheme, true);
        authors.sort();
        GanttUtil gu = new GanttUtil(context);
        //we only generate resource dependencies if it is not a genuin Gantt chart exported from MS Project online
        if (ganttFileName.toLowerCase().endsWith(".xml")) {
            gu.createResourceDependencies(sprint);
        }

        GanttErrorHandler eh = new GanttErrorHandler();
        gu.testRelationsAreHonored(eh, sprint);
        if (!eh.noException) {
            for (Throwable e : eh.exceptions) {
                String message = String.format(e.getMessage() + " in Gantt chart '%s'.", ganttFileName);
                geh.exceptions.add(new ErrorException(message));
                logger.warn(message);
            }
        }
//TODO reintroduce bank holidays and vacations

//        if (context.parameters.captureOutOfOfficeFromGantt) {
//            initOutOfOffice(context.ganttInformationList);
//        } else if (context.parameters.captureOutOfOfficeFromRam) {
//            initOutOfOffice(timeTracker);
//        }
    }

    public void setConflictingTasks(ConflictingTasks conflictingTasks) {
        //        this.conflictingTask = conflictingTasks;
        for (Task task : sprint.getTasks()) {
            if (GanttUtil.isValidTask(task)) {
                List<Conflict> conflicList = conflictingTasks.get(task);
                if (conflicList != null) {
                    MetaData md = TaskUtil.getTaskMetaData(task);
                    if (md == null) {
                        md = new MetaData();
                        TaskUtil.putTaskMetaData(task, md);
                    }
                    for (Conflict conflict : conflicList) {
                        md.errors.add(String.format("resource conflict with task '%s'[%s]\n", conflict.task.getName(), conflict.projectName));
                    }
                }
            }
        }
    }

}
