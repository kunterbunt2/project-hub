package de.bushnaq.abdalla.svg.util;

import java.awt.*;

/**
 * supports svg link and/or svg tooltip via javascript
 */
public class ExtendedPolygon extends Polygon {

    private String  link;
    private String  toolTip;
    private boolean visible = true;

    public ExtendedPolygon() {
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