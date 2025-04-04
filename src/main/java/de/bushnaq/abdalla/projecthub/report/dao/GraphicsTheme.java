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

package de.bushnaq.abdalla.projecthub.report.dao;

import java.awt.*;

public class GraphicsTheme {
    public static final Color  COLOR_DARK_GREEN = new Color(0x095c09);
    public static final Color  COLOR_DARK_RED   = new Color(0xcc4a31);
    public static final Color  COLOR_GOLD       = new Color(0xffcc00);
    public static final Color  COLOR_MAGENTA    = new Color(0xe5127d);
    public              Color  chartBackgroundColor;
    public              Color  chartBorderColor;
    public              ETheme cssTheme         = ETheme.light;
    public              Color  holidayColor     = new Color(183, 216, 240);  // Light blue;
    public              Color  sickColor        = new Color(0xfff2e8); // Light red;
    public              Color  tripColor        = new Color(0xfffcea);  // Light yellow;
    public              Color  vacationColor    = new Color(183, 240, 216);  // Light green;
}
