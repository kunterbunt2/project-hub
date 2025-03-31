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