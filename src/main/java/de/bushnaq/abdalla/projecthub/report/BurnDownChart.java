package de.bushnaq.abdalla.projecthub.report;


import de.bushnaq.abdalla.projecthub.report.renderer.RenderDao;
import de.bushnaq.abdalla.projecthub.report.renderer.burndown.BurnDownRenderer;

public class BurnDownChart extends AbstractChart {

    public BurnDownChart(String relativeCssPath, RenderDao dao) throws Exception {
        super("Work Burn Down Chart", dao.sprint.getName(), relativeCssPath, dao.column, dao.sprintName, "work_burn_down_map", dao.link, dao.cssClass, dao.graphicsTheme);
        getRenderers().add(new BurnDownRenderer(dao));
        this.setChartWidth(getRenderers().get(0).chartWidth);
        this.setChartHeight(getRenderers().get(0).chartHeight + captionElement.height + footerElement.height - 1);
        captionElement.width = dao.chartWidth;
        footerElement.y      = getRenderers().get(0).chartHeight + captionElement.height;
    }

    @Override
    protected void createReport() throws Exception {
        getRenderers().get(0).draw(graphics2D, 0, captionElement.height);
    }

}
