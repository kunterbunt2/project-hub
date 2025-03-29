package de.bushnaq.abdalla.projecthub.report.dao;

import java.awt.*;

public class FooterElement {
    private final Color backgroundColor = Color.white;
    private final Color color           = new Color(0x2c, 0x7b, 0xf4);
    public        int   height          = 14;
    private final String imageMap = "";
    private final String projectRequestKey;
    private final Font   signFont        = new Font("Arial", Font.PLAIN, 10);
    //    private Graphics2D graphics2D;
    String text;
    public int width;
    public int x = 3;
    public int y = 1;

    public FooterElement(String text, String projectRequestKey, int chartWidth, int chartHeight) {
        this.text              = text;
        this.projectRequestKey = projectRequestKey;
        this.width             = chartWidth;
        y                      = chartHeight;
        if (text == null) {
            height = 0;
        }
    }

    public void draw(Graphics2D g2) {

        if (text != null) {
            g2.setColor(backgroundColor);
            g2.fillRect(0, y, width, height);
            g2.setColor(color);
            g2.setFont(signFont);
            FontMetrics fm        = g2.getFontMetrics();
            int         maxAscent = fm.getMaxAscent();
            g2.setColor(Color.darkGray);
            g2.drawString(text, x, y + maxAscent - 2);
            int textWidth = g2.getFontMetrics().stringWidth(projectRequestKey);
            g2.drawString(projectRequestKey, width - textWidth - 1, y + maxAscent - 2);
        }
    }

    public String getImageMap() {
        return imageMap;
    }

}