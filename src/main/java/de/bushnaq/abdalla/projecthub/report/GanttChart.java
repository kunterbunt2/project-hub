package de.bushnaq.abdalla.projecthub.report;

import de.bushnaq.abdalla.projecthub.dao.Context;
import de.bushnaq.abdalla.projecthub.dto.Sprint;
import de.bushnaq.abdalla.projecthub.report.dao.BurnDownGraphicsTheme;
import de.bushnaq.abdalla.projecthub.report.renderer.GanttRenderer;

import java.time.LocalDateTime;
import java.util.List;


public class GanttChart extends Chart {

    public GanttChart(Context context, String projectRequestKey, String relateCssPath, String column, String sprintName, List<Throwable> exception,
                      LocalDateTime now, boolean completed, Sprint sprint, int chartWidth, int chartHeight, String cssClass,
                      BurnDownGraphicsTheme graphicsTheme) throws Exception {
        super("Gantt Chart", projectRequestKey, relateCssPath, column, sprintName, "gantt_map", null, chartWidth, chartHeight, cssClass, graphicsTheme);
        getRenderers().add(new GanttRenderer(context, sprintName, exception, now, completed, sprint, chartWidth, chartHeight,
                cssClass, graphicsTheme));
        this.setChartWidth(getRenderers().get(0).chartWidth);
        this.setChartHeight(getRenderers().get(0).chartHeight + captionElement.height + footerElement.height - 1);
        captionElement.width = chartWidth;
        footerElement.y      = getRenderers().get(0).chartHeight + captionElement.height;
    }

    @Override
    protected void createReport() throws Exception {
        getRenderers().get(0).draw(graphics2D, 0, captionElement.height);
    }

}
