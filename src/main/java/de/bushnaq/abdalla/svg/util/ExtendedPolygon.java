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