package de.bushnaq.abdalla.projecthub.report.dao;

import java.awt.*;

public class CalendarElement {
    private Font    font;
    private Integer height;
    private Integer width;
    private Integer y;

    public CalendarElement(Font font, Integer y, Integer width, Integer height) {
        this.setFont(font);
        this.setY(y);
        this.setWidth(width);
        this.setHeight(height);
    }

    public Font getFont() {
        return font;
    }

    public Integer getHeight() {
        return height;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getY() {
        return y;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}