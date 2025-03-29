package de.bushnaq.abdalla.projecthub.report.dao;

import java.awt.*;

public class CalendarMilestoneElement {
    public Font    flagFont;
    public Integer flagHeight;
    public Integer flagY;
    public Font    font;
    public Integer height;
    public Integer width;
    public Integer y;

    public CalendarMilestoneElement(Integer x, Integer y, Integer width, Integer height, Font font, Integer flagY, Integer flagHeight, Font flagFont) {
        this.y          = y;
        this.width      = width;
        this.height     = height;
        this.font       = font;
        this.flagY      = flagY;
        this.flagHeight = flagHeight;
        this.flagFont   = flagFont;
    }
}