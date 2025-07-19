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

package de.bushnaq.abdalla.projecthub.ui.util;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import de.bushnaq.abdalla.projecthub.Context;
import de.bushnaq.abdalla.projecthub.ParameterOptions;
import de.bushnaq.abdalla.projecthub.dto.Sprint;
import de.bushnaq.abdalla.projecthub.report.burndown.BurnDownChart;
import de.bushnaq.abdalla.projecthub.report.burndown.RenderDao;
import de.bushnaq.abdalla.projecthub.report.gantt.GanttChart;
import de.bushnaq.abdalla.util.Util;
import de.bushnaq.abdalla.util.date.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RenderUtil {
    public static final String BURNDOWN_CHART = "burndown-chart";
    public static final String GANTT_CHART    = "gantt-chart";
    final static        Logger logger         = LoggerFactory.getLogger(RenderUtil.class);

    private static RenderDao createRenderDao(Context context, Sprint sprint, String column, LocalDateTime now, int chartWidth, int chartHeight, String link) {
        RenderDao dao = new RenderDao();
        dao.context            = context;
        dao.column             = column;
        dao.sprintName         = column + "-burn-down";
        dao.link               = link;
        dao.start              = sprint.getStart();
        dao.now                = now;
        dao.end                = sprint.getEnd();
        dao.release            = sprint.getReleaseDate();
        dao.chartWidth         = chartWidth;
        dao.chartHeight        = chartHeight;
        dao.sprint             = sprint;
        dao.estimatedBestWork  = DateUtil.add(sprint.getWorked(), sprint.getRemaining());
        dao.estimatedWorstWork = null;
        dao.maxWorked          = DateUtil.add(sprint.getWorked(), sprint.getRemaining());
        dao.remaining          = sprint.getRemaining();
        dao.worklog            = sprint.getWorklogs();
        dao.worklogRemaining   = sprint.getWorklogRemaining();
        dao.cssClass           = "scheduleWithMargin";
        dao.graphicsTheme      = context.parameters.graphicsTheme;
        return dao;
    }

    public static Image generateBurnDownImage(Context context, Sprint sprint) {
        StreamResource resource = new StreamResource("burndown.svg", () -> {
            try (ByteArrayOutputStream outputStream = renderBurnDownChart(context, sprint)) {
                // Convert ByteArrayOutputStream to ByteArrayInputStream
                return new ByteArrayInputStream(outputStream.toByteArray());
            } catch (Exception e) {
                logger.error("Error creating burndown chart", e);
                return new ByteArrayInputStream(new byte[0]); // Empty stream in case of error
            }
        });
        // Create an image component with the SVG resource
        Image burndownChart = new Image(resource, "Sprint Burndown Chart");
        burndownChart.setId(BURNDOWN_CHART);
        burndownChart.setWidthFull();
        return burndownChart;
    }

    public static Image generateGanttChartImage(Context context, Sprint sprint) throws Exception {
        List<Throwable> exceptions = new ArrayList<>();
        GanttChart      chart      = new GanttChart(context, "", "/", "Gantt Chart", sprint.getName() + "-gant-chart", exceptions, ParameterOptions.getLocalNow(), false, sprint/*, 1887, 1000*/, "scheduleWithMargin", context.parameters.graphicsTheme);
        Image           ganttChart = RenderUtil.renderGanttImage(chart);
        ganttChart.setId(GANTT_CHART);
        ganttChart.setWidth(chart.getChartWidth() + "px");
        return ganttChart;
    }


    private static ByteArrayOutputStream renderBurnDownChart(Context context, Sprint sprint) {
        RenderDao             dao = createRenderDao(context, sprint, "burn-down", ParameterOptions.getLocalNow(), 640, 400, "sprint-" + sprint.getId() + "/sprint.html");
        ByteArrayOutputStream o   = new ByteArrayOutputStream(64 * 1024); // 64 KB
        try {
            BurnDownChart chart = new BurnDownChart("/", dao);
            chart.render(Util.generateCopyrightString(ParameterOptions.getLocalNow()), o);
            return o;
        } catch (Exception e) {
            try {
                o.close(); // Close the stream in case of error
            } catch (Exception closeException) {
                logger.warn("Failed to close output stream", closeException);
            }
            throw new RuntimeException(e);
        }
    }

    private static ByteArrayOutputStream renderGanttChart(GanttChart chart) {
        ByteArrayOutputStream o = new ByteArrayOutputStream(64 * 1024); //begin size 64 KB
        try {
            chart.render(Util.generateCopyrightString(ParameterOptions.getLocalNow()), o);
            return o;
        } catch (Exception e) {
            try {
                o.close(); // Close the stream in case of error
            } catch (Exception closeException) {
                logger.warn("Failed to close output stream", closeException);
            }
            throw new RuntimeException(e);
        }
    }

    private static Image renderGanttImage(GanttChart chart) {
        StreamResource resource = new StreamResource("gantt.svg", () -> {
            try (ByteArrayOutputStream outputStream = renderGanttChart(chart)) {
                // Convert ByteArrayOutputStream to ByteArrayInputStream
                return new ByteArrayInputStream(outputStream.toByteArray());
            } catch (Exception e) {
                logger.error("Error creating gantt chart", e);
                return new ByteArrayInputStream(new byte[0]); // Empty stream in case of error
            }
        });
        // Create an image component with the SVG resource
        Image ganttChart = new Image(resource, "Sprint Gantt Chart");
        return ganttChart;
    }


}
