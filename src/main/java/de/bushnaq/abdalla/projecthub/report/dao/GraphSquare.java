package de.bushnaq.abdalla.projecthub.report.dao;

import java.awt.*;

public class GraphSquare {
    public Font    font;
    public Integer height;
    public Font    lableFont;
    public Integer width;
    public Integer x;
    public Integer y;

    public GraphSquare() {
    }

    public GraphSquare(Integer x, Integer y, Integer width, Integer height) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
    }

    public GraphSquare(Integer x, Integer y, Integer width, Integer height, Font font, Font lableFont) {
        this.x         = x;
        this.y         = y;
        this.width     = width;
        this.height    = height;
        this.font      = font;
        this.lableFont = lableFont;
    }

    public void initPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void initSize(int w, int h) {
        width  = w;
        height = h;
    }

}