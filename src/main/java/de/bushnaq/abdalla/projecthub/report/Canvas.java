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

package de.bushnaq.abdalla.projecthub.report;

import de.bushnaq.abdalla.profiler.Profiler;
import de.bushnaq.abdalla.profiler.SampleType;
import de.bushnaq.abdalla.projecthub.report.dao.GraphicsTheme;
import de.bushnaq.abdalla.projecthub.report.html.dao.ReportLink;
import de.bushnaq.abdalla.shadow.Shadow;
import de.bushnaq.abdalla.svg.util.ExtendedGraphics2D;
import de.bushnaq.abdalla.svg.util.ExtendedSvgGraphics2D;
import de.bushnaq.abdalla.util.FileUtil;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;


public abstract class Canvas extends ReportLink {
    private static final float              fine_LINE_STROKE_WIDTH = 1f;
    private              int                borderWidth            = 1;
    private              int                chartHeight;
    private              int                chartWidth;
    public static        boolean            generateVvg            = true;
    protected            ExtendedGraphics2D graphics2D;
    protected            GraphicsTheme      graphicsTheme;
    protected            String             imageName;
    private final        Logger             logger                 = LoggerFactory.getLogger(this.getClass());
    protected            SVGGraphics2D      svgGenerator;

    public Canvas(String column, String imageName, String mapName, String link, int chartWidth, int chartHeight, String cssClass, GraphicsTheme graphicsTheme)
            throws IOException {
        super(column, generateCellText(cssClass, mapName, imageName), link);
        this.imageName = imageName;
        this.setChartWidth(chartWidth);
        this.setChartHeight(chartHeight);
        this.graphicsTheme = graphicsTheme;
    }

    private BufferedImage castShadow(BufferedImage image) throws IOException {
        Shadow        shadow       = new Shadow();
        BufferedImage passepartout = shadow.drop(image);
        return passepartout;
    }

    protected abstract void createMap();

    protected abstract void createReport() throws Exception;

    protected void drawBackground() {
        graphics2D.setColor(graphicsTheme.chartBackgroundColor);
        graphics2D.fillRect(0, 0, getChartWidth(), getChartHeight());
    }

    private void drawBorder(ExtendedGraphics2D g2) {
        graphics2D.setStroke(new BasicStroke(fine_LINE_STROKE_WIDTH));
        g2.setColor(graphicsTheme.chartBorderColor);
        g2.drawRect(0, 0, getChartWidth() - 1, getChartHeight() - 1);
    }

    protected abstract void drawCaption(ExtendedGraphics2D graphics2d2);

    protected abstract void drawFooter(ExtendedGraphics2D graphics2d2);

    private static String generateCellText(String cssClass, String mapName, String imageName) {
        String extension = generateVvg ? "svg" : "png";
        return String.format("<img class=\"%s\" usemap=\"#%s\" border=\"0\" src=\"%s.%s\">", cssClass, mapName, imageName, extension);
    }

    public void generateImage(String copyright, String description, String path) throws Exception {
        try (Profiler p1 = new Profiler(SampleType.GPU)) {
            if (generateVvg) {
                String imageFileName;
                if (path.isEmpty()) {
                    imageFileName = String.format("%s.svg", imageName);
                } else {
                    imageFileName = String.format(path + "/%s.svg", imageName);
                }
                prepareSvgGraphics();
                drawBackground();
                drawCaption(graphics2D);
                createReport();
                createMap();
                drawFooter(graphics2D);
                drawBorder(graphics2D);
                try (Profiler p2 = new Profiler(SampleType.FILE)) {
                    boolean          useCSS = true; // we want to use CSS style attributes
                    FileOutputStream o      = new FileOutputStream(imageFileName);
                    Writer           out    = new OutputStreamWriter(o, StandardCharsets.UTF_8);
                    svgGenerator.stream(out, useCSS);
                }
                text = FileUtil.loadFile(null, imageFileName).replace("<svg ", "<svg class=\"qtip-shadow\"");
            } else {
                if (getChartWidth() * getChartHeight() > 5000000) {
                    logger.warn(String.format("image size is over 5M pixel width = %d height = %d", getChartWidth(), getChartHeight()));
                }
                BufferedImage _image = new BufferedImage(getChartWidth(), getChartHeight(), BufferedImage.TYPE_INT_ARGB);
                String        imageFileName;
                if (path.isEmpty()) {
                    imageFileName = String.format("%s.png", imageName);
                } else {
                    imageFileName = String.format(path + "/%s.png", imageName);
                }
                prepareGraphics(_image);
                drawBackground();
                drawCaption(graphics2D);
                createReport();
                createMap();
                drawFooter(graphics2D);
                drawBorder(graphics2D);
                _image = castShadow(_image);

                try (Profiler p2 = new Profiler(SampleType.FILE)) {
                    File output = new File(imageFileName);
                    ImageIO.write(_image, "png", output);
                }
            }
        }
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public int getChartHeight() {
        return chartHeight;
    }

    public int getChartWidth() {
        return chartWidth;
    }

    public String getImageName() {
        return imageName;
    }

    protected void prepareGraphics(final BufferedImage aImage) throws IOException {
        graphics2D = new ExtendedGraphics2D((Graphics2D) aImage.getGraphics());
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    protected void prepareSvgGraphics() throws IOException {
        // Get a DOMImplementation.
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        // Create an instance of org.w3c.dom.Document.
        String   svgNS    = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document document = domImpl.createDocument(svgNS, "svg", null);
        // Create an instance of the SVG Generator.
        svgGenerator = new ExtendedSvgGraphics2D(document);
        // Ask the test to render into the SVG Graphics2D implementation.
        svgGenerator.setSVGCanvasSize(new Dimension(chartWidth, chartHeight));
        graphics2D = new ExtendedGraphics2D(svgGenerator);
        //        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //        graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public void setChartHeight(int chartHeight) {
        this.chartHeight = chartHeight + getBorderWidth();
    }

    public void setChartWidth(int chartWidth) {
        this.chartWidth = chartWidth + getBorderWidth();
    }

    public static int transformToMapX(int x) {
        if (generateVvg) {
            return x;
        } else {
            return x + Shadow.LEFT_TOP;
        }
    }

    public static int transformToMapY(int y) {
        if (generateVvg) {
            return y;
        } else {
            return y + Shadow.LEFT_TOP;
        }
    }

}
