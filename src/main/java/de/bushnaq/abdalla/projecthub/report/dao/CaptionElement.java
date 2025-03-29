package de.bushnaq.abdalla.projecthub.report.dao;

import de.bushnaq.abdalla.projecthub.report.Canvas;
import de.bushnaq.abdalla.svg.util.ExtendedGraphics2D;

import java.awt.*;

public class CaptionElement {
    private final Color  backgroundColor = Color.white;
    private final Color  color           = new Color(0x2c, 0x7b, 0xf4);
    public        Font   font            = new Font("Arial", Font.PLAIN, 18);
    public        int    height          = 26;
    private       String imageMap        = "";
    private final String relateCssPath;
    String text;
    public int width;
    public int x = 3;
    public int y = 0;

    public CaptionElement(String text, String relateCssPath, int chartWidth, int chartHeight) {
        this.text          = text;
        this.relateCssPath = relateCssPath;
        this.width         = chartWidth;
        if (text == null) {
            height = 0;
        }
    }

    public void draw(ExtendedGraphics2D graphics2D) {

        if (text != null) {
            graphics2D.setColor(backgroundColor);
            graphics2D.fillRect(0, y, width, height);
            graphics2D.setColor(color);
            graphics2D.setFont(font);
            FontMetrics fm           = graphics2D.getFontMetrics();
            int         ascent       = fm.getAscent();
            int         captionWidth = fm.stringWidth(text);
            //            father.graphics2D.fillRect(x, y, width, height);
            String link = relateCssPath + text + ".png";
            graphics2D.drawStringWithLink(text, x, y + height / 2 + (ascent - 2) / 2 - 1, link);
            link = replaceSpaceWithDash(link);
            imageMap += String.format("<area shape=\"rect\" coords=\"%d,%d,%d,%d\" href=\"%s\" >\n", Canvas.transformToMapX(x), Canvas.transformToMapY(y),
                    Canvas.transformToMapX(x + captionWidth), Canvas.transformToMapY(y + height), link);
        }
    }

    public String getImageMap() {
        return imageMap;
    }

    private String replaceSpaceWithDash(String link) {
        return link.replace(' ', '-').toLowerCase();
    }

}