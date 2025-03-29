package de.bushnaq.abdalla.svg.util;

import java.awt.*;

/**
 * hidden element (opacity=0), only there to show a pop-up suing title
 */
public class RectangleWithToolTip extends Rectangle {

    private String toolTip;

    public RectangleWithToolTip() {
    }

    public RectangleWithToolTip(int x, int y, int width, int height, String toolTip) {
        super(x, y, width, height);
        this.toolTip = toolTip;
    }

    public RectangleWithToolTip(RectangleWithToolTip r) {
        super(r);
        this.toolTip = r.toolTip;
    }

    public String getTitle() {
        return toolTip;
    }

    public void setTitle(String title) {
        this.toolTip = title;
    }
}