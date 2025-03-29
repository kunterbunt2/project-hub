package de.bushnaq.abdalla.svg.util;

import java.awt.*;

/**
 * supports svg link and/or svg tooltip via javascript
 */
public class ExtendedRectangle extends Rectangle {

    private String  link;
    private String  toolTip;
    private boolean visible = true;

    public ExtendedRectangle() {
    }

    public ExtendedRectangle(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public ExtendedRectangle(int x, int y, int width, int height, String toolTip, String link) {
        super(x, y, width, height);
        this.toolTip = toolTip;
        this.setLink(link);
    }

    public ExtendedRectangle(ExtendedRectangle r) {
        super(r);
        this.toolTip = r.toolTip;
        this.setLink(r.getLink());
    }

    public String getLink() {
        return link;
    }

    public String getToolTip() {
        return toolTip;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}