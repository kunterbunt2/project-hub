package de.bushnaq.abdalla.projecthub.report.dao;

import java.awt.*;

public class BurnDownGraphicsTheme extends ScheduleGraphicsTheme {
    public static final int     MAX_AUTHOR_COLOR = 12/*(9-4) * 3*//*+ 3*//*+1*/;
    public              Color   bankHolidayColor;
    public              Color   burnDownBorderColor;
    protected           Color[] burnDownColor    = new Color[MAX_AUTHOR_COLOR];
    public              Color   graphTextBackgroundColor;
    public              Color   optimaleGuideColor;
    public              Color   plannedGuideColor;
    public              Color   requestMilestoneColor;
    public              Color   surroundingSquareColor;
    public              Color   tickTextColor;
    public              Color   ticksColor;
    public              Color   watermarkColor;

    public Color getAuthorColor(int index) {
        if (index < 0) {
            index = -index;
        }
        int colorIndex = index % MAX_AUTHOR_COLOR;
        return burnDownColor[colorIndex];
    }

    public Color getHeatColor(int index) {
        if (index < 0) {
            index = -index;
        }
        int colorIndex = index % MAX_HEAT_COLOR;
        return heatColor[colorIndex];
    }

}
