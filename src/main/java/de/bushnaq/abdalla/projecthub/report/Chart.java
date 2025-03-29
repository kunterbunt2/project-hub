package de.bushnaq.abdalla.projecthub.report;

import de.bushnaq.abdalla.projecthub.report.dao.CaptionElement;
import de.bushnaq.abdalla.projecthub.report.dao.FooterElement;
import de.bushnaq.abdalla.projecthub.report.dao.GraphicsTheme;
import de.bushnaq.abdalla.projecthub.report.renderer.AbstractRenderer;
import de.bushnaq.abdalla.svg.util.ExtendedGraphics2D;
import de.bushnaq.abdalla.util.Util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Chart extends Canvas {
    public        CaptionElement         captionElement;
    public        FooterElement          footerElement;
    private final String                 mapName;
    private final List<AbstractRenderer> renderers = new ArrayList<>();

    public Chart(String caption, String projectRequestKey, String relateCssPath, String column, String imageName, String mapName, String link, int chartWidth,
                 int chartHeight, String cssClass, GraphicsTheme graphicsTheme) throws IOException {
        super(column, imageName, mapName, link, chartWidth, chartHeight, cssClass, graphicsTheme);
        captionElement = new CaptionElement(caption, relateCssPath, chartWidth, chartHeight);
        footerElement  = new FooterElement(Util.generateCopyrightString(LocalDateTime.now()), projectRequestKey, chartWidth,
                chartHeight + captionElement.height);
        this.mapName   = mapName;
    }

    @Override
    protected void createMap() {
        text += String.format("<map name=\"%s\">\n", mapName);
        text += captionElement.getImageMap();
        for (AbstractRenderer worker : getRenderers()) {
            if (!worker.getImageMap().isEmpty()) {
                text += worker.getImageMap();
            }
        }
        text += "\n</map>";
    }

    @Override
    protected void drawBackground() {
        graphics2D.setColor(graphicsTheme.chartBackgroundColor);
        graphics2D.fillRect(0, captionElement.height, getChartWidth() - 1, getChartHeight() - captionElement.height - 1);
    }

    @Override
    protected void drawCaption(ExtendedGraphics2D graphics2d2) {
        captionElement.draw(graphics2d2);
    }

    @Override
    protected void drawFooter(ExtendedGraphics2D graphics2d2) {
        footerElement.draw(graphics2d2);
    }

    public List<AbstractRenderer> getRenderers() {
        return renderers;
    }

    @Override
    public void setChartWidth(int chartWidth) {
        super.setChartWidth(chartWidth);
        if (captionElement != null) {
            captionElement.width = chartWidth;
        }
        if (footerElement != null) {
            footerElement.width = chartWidth;
        }
    }

}
