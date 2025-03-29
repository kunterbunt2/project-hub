package de.bushnaq.abdalla.svg.util;

import java.awt.*;

/**
 * hidden element (opacity=0), only there to show a pop-up suing title
 */
public class RectangleWithLink extends Rectangle {

    private String link;

    public RectangleWithLink() {
    }

    public RectangleWithLink(int x, int y, int width, int height, String link) {
        super(x, y, width, height);
        this.link = link;
    }

    public RectangleWithLink(RectangleWithLink r) {
        super(r);
        this.link = r.link;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}