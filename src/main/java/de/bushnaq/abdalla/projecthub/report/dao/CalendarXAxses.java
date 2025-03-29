package de.bushnaq.abdalla.projecthub.report.dao;

import de.bushnaq.abdalla.projecthub.report.Canvas;
import de.bushnaq.abdalla.projecthub.report.renderer.AbstractRenderer;
import de.bushnaq.abdalla.util.Util;
import de.bushnaq.abdalla.util.date.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class CalendarXAxses {
    public static final  int              DAY_OF_MONTH_MIN_DAY_WIDTH = 16;
    private static final int              DAY_OF_WEEK_MIN_DAY_WIDTH  = 10;
    private static final int              MONTH_MIN_DAY_WIDTH        = 1;
    private static final int              WEEK_MIN_DAY_WIDTH         = 2;
    // private final SimpleDateFormat sdtmf = new SimpleDateFormat("yyyy.MMM.dd
    // HH:mm:ss.SSS", Util.locale);
    private              boolean          calendarAtBottom;
    public               CalendarElement  dayOfMonth;
    public               CalendarElement  dayOfWeek;
    private final        AbstractRenderer father;

    private final DateTimeFormatter        imageMapSdf = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Util.locale);
    private final Logger                   logger      = LoggerFactory.getLogger(this.getClass());
    public        CalendarMilestoneElement milestone;
    private final Milestones               milestones;
    public        CalendarElement          month;
    private final int                      postRun;
    private final int                      preRun;
    private final DateTimeFormatter        sdf         = DateTimeFormatter.ofPattern("d", Util.locale);
    private final DateTimeFormatter        sdfMMMdd    = DateTimeFormatter.ofPattern("MMM.d", Util.locale);
    public        CalendarElement          week;
    int width = 0;
    private int             x;
    public  CalendarElement year;

    /**
     * @param father
     * @param priRun,  how many days to draw prior to the time range we are
     *                 interested in
     * @param postRun, how many days to draw post to the time range we are
     *                 interested in
     */
    public CalendarXAxses(AbstractRenderer father, int preRun, int postRun) {
        this.father     = father;
        this.preRun     = preRun;
        this.postRun    = postRun;
        this.milestones = father.milestones;
        int margine = 4;
        year       = new CalendarElement(new Font("Arial", Font.BOLD, 14), null, null, 13 + margine);
        month      = new CalendarElement(new Font("Arial", Font.BOLD, 12), null, null, 12 + margine);
        week       = new CalendarElement(new Font("Arial", Font.BOLD, 10), null, null, 10 + margine);
        dayOfMonth = new CalendarElement(new Font("Arial", Font.PLAIN, 10), null, 20, 10 + margine);
        dayOfWeek  = new CalendarElement(new Font("Arial", Font.PLAIN, 10), null, 20, 10 + margine);
        milestone  = new CalendarMilestoneElement(null, null, 11, 10 + margine, new Font("Arial", Font.BOLD, 10), null, 13, new Font("Arial", Font.PLAIN, 11));
    }

    protected int calculateDayX(LocalDate date) {
        int firstMilestoneX = x + dayOfWeek.getWidth() / 2;
        return firstMilestoneX + (DateUtil.calculateDays(milestones.firstMilestone, date) + preRun) * dayOfWeek.getWidth();
    }

    public void drawCalendar(boolean drawDays) {
        LocalDate firstDay          = DateUtil.addDay(milestones.firstMilestone, -preRun);
        LocalDate lastDay           = DateUtil.max(milestones.lastMilestone, DateUtil.addDay(milestones.firstMilestone, father.days - 1));
        boolean   yearWasDrawn      = false;
        boolean   monthWasDrawn     = false;
        boolean   firstWeekWasDrawn = false;

        // logger.trace(String.format("%s first=%s, last=%s, day.width=%d",
        // father.getImageName(), DateUtil.createDateString(firstDay, sdtmf),
        // DateUtil.createDateString(lastDay, sdtmf), dayOfWeek.width));
        for (int phase = 0; phase < 5; phase++) {
            for (LocalDate currentDay = firstDay; currentDay.isBefore(lastDay) || currentDay.isEqual(lastDay); currentDay = currentDay.plusDays(1)) {
                // if (phase == 4)
                // logger.trace(String.format("first=%s, last=%s, current=%s",
                // Util.createDateString(firstDay, sdtmf), Util.createDateString(lastDay,
                // sdtmf), Util.createDateString(currentDay, sdtmf)));
                int       daysX    = calculateDayX(currentDay);
                String[]  weekDays = {"M", "T", "W", "T", "F", "S", "S"};
                String[]  months   = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                LocalDate startCal = currentDay;

                if (phase == 4 && ((startCal.getDayOfMonth() == 1 && startCal.getMonth() == Month.JANUARY) || !yearWasDrawn)) {
                    // --year
                    {
                        LocalDate end = startCal.withMonth(12).withDayOfMonth(31);// new years eve
                        //                        end.set(Calendar.MONTH, 11); // 11 = december
                        //                        end.set(Calendar.DAY_OF_MONTH, 31); // new years eve
                        if (end.isAfter(lastDay)) {
                            end = lastDay;
                        }
                        int x2 = calculateDayX(end) - dayOfWeek.getWidth() / 2;
                        drawTextBox(daysX - (dayOfWeek.getWidth() / 2 - 1), x2 + dayOfWeek.getWidth(), year.getY(), year.getHeight(),
                                String.valueOf(startCal.getYear()), father.graphicsTheme.yearTextColor, father.graphicsTheme.yearBackgroundColor,
                                father.graphicsTheme.yearBoderColor, year.getFont(), false);
                        yearWasDrawn = true;
                    }
                } else if (phase == 3 && (startCal.getDayOfMonth() == 1 || !monthWasDrawn) && isMonthVisible()) {
                    // --month
                    {
                        LocalDate end = startCal.plusMonths(1).withDayOfMonth(1).minusDays(1);
                        if (end.isAfter(lastDay)) {
                            end = lastDay;
                        }
                        int   x2              = calculateDayX(end) - dayOfWeek.getWidth() / 2;
                        Color backgroundColor = father.graphicsTheme.monthColor[startCal.getMonth().getValue() - 1];
                        drawTextBox(daysX - (dayOfWeek.getWidth() / 2 - 1), x2 + dayOfWeek.getWidth(), month.getY(), month.getHeight(),
                                months[startCal.getMonth().getValue() - 1], father.graphicsTheme.monthTextColor, backgroundColor,
                                father.graphicsTheme.monthBorderColor, month.getFont(), false);
                        monthWasDrawn = true;
                    }
                } else if (phase == 2 && (startCal.getDayOfWeek() == DayOfWeek.MONDAY || !firstWeekWasDrawn) && isWeekVisible()) {
                    // --beginning of the week
                    {
                        //StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                        LocalDate end = DateUtil.getWeekSunday(startCal);
                        //                        end.setFirstDayOfWeek(Calendar.MONDAY);// Otherwise next instruction will fail on system that have Sunday as first day of week
                        //                        end.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        // if (stackTrace[3].getFileName().equals("ProjectOverviewRenderer.java"))
                        // {
                        // logger.trace(String.format("Drawing week [%s]",
                        // DateUtil.createDateString(end.getTimeInMillis(), sdtmf)));
                        // end.getFirstDayOfWeek();
                        // }
                        // if (stackTrace[3].getFileName().equals("ProjectOverviewRenderer.java"))
                        // logger.trace(String.format("Drawing week [%s]",
                        // DateUtil.createDateString(end.getTimeInMillis(), sdtmf)));
                        if (end.isAfter(lastDay)) {
                            end = lastDay;
                        }
                        int    x2 = calculateDayX(end) - dayOfWeek.getWidth() / 2;
                        String calendarWeek;
                        if (isDayOfWeekVisible()) {
                            TemporalField woy = WeekFields.of(Locale.CANADA).weekOfWeekBasedYear();
                            //                            LocalDate ld = Instant.ofEpochMilli(currentDay).atZone(ZoneId.systemDefault()).toLocalDate();
                            calendarWeek = "W" + currentDay.get(woy);
                        } else {
                            calendarWeek = DateUtil.createDateString(currentDay, sdf);
                        }
                        // if (stackTrace[3].getFileName().equals("ProjectOverviewRenderer.java"))
                        // logger.trace(String.format("Drawing week [%s][%s]-[%s] in %s %d %d
                        // weekWidth=%d", calendarWeek, DateUtil.createDateString(currentDay, sdtmf),
                        // DateUtil.createDateString(end.getTimeInMillis(), sdtmf),
                        // stackTrace[3].getFileName(), daysX - (dayOfWeek.width / 2 - 1), x2 +
                        // dayOfWeek.width, dayOfWeek.width));
                        drawTextBox(daysX - (dayOfWeek.getWidth() / 2 - 1), x2 + dayOfWeek.getWidth(), week.getY(), week.getHeight(), calendarWeek,
                                father.graphicsTheme.weekTextColor, father.graphicsTheme.weekBackgroundColor, father.graphicsTheme.weekBoderColor,
                                week.getFont(), false);
                        firstWeekWasDrawn = true;
                    }
                } else if (phase == 1 && isDayOfWeekVisible()) {
                    // --day of Month
                    {
                        Color color;
                        if (startCal.getDayOfWeek() == DayOfWeek.SATURDAY || startCal.getDayOfWeek() == DayOfWeek.SUNDAY) {
                            color = GraphColorUtil.getDayOfWeekColor(father.graphicsTheme, startCal);
                            drawTextBox(daysX - (dayOfMonth.getWidth() / 2 - 1), daysX - (dayOfMonth.getWidth() / 2 - 1) + (dayOfMonth.getWidth() - 1),
                                    dayOfMonth.getY(), dayOfMonth.getHeight(), "" + startCal.getDayOfMonth(), Color.BLACK, color,
                                    father.graphicsTheme.dayOfMonthBorderColor, dayOfMonth.getFont(), true);
                        } else {
                            color = father.graphicsTheme.dayOfMonthBackgroundColor;
                            drawTextBox(daysX - (dayOfMonth.getWidth() / 2 - 1), daysX - (dayOfMonth.getWidth() / 2 - 1) + (dayOfMonth.getWidth() - 1),
                                    dayOfMonth.getY(), dayOfMonth.getHeight(), "" + startCal.getDayOfMonth(), father.graphicsTheme.dayOfMonthTextColor, color,
                                    father.graphicsTheme.dayOfMonthBorderColor, dayOfMonth.getFont(), true);
                        }

                        // father.graphics2D.setColor(graphicsTheme.dayDiagramBorderColor);
                    }
                    // --day of week
                    {
                        Color color = GraphColorUtil.getDayOfWeekColor(father.graphicsTheme, startCal);
                        // if (startCal.getTime().getTime() < milestones.get("N").time) {
                        // //past
                        // color = graphicsTheme.pastWorkDayRequestColor;
                        // if (startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                        // color = graphicsTheme.pastWeekendRequestColor;
                        // } else if (startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        // color = graphicsTheme.pastWeekendRequestColor;
                        // }
                        // } else {
                        // //future
                        // color = graphicsTheme.futureWorkDayRequestColor;
                        // if (startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                        // color = graphicsTheme.futureWeekendRequestColor;
                        // } else if (startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        // color = graphicsTheme.futureWeekendRequestColor;
                        // }
                        // }
                        drawTextBox(daysX - (dayOfWeek.getWidth() / 2 - 1), daysX - (dayOfWeek.getWidth() / 2 - 1) + (dayOfWeek.getWidth() - 1),
                                dayOfWeek.getY(), dayOfWeek.getHeight(), weekDays[startCal.getDayOfWeek().getValue() - 1], father.graphicsTheme.dayTextColor,
                                color, father.graphicsTheme.dayBorderColor, dayOfWeek.getFont(), true);
                        father.graphics2D.setColor(father.graphicsTheme.dayDiagramBorderColor);
                        father.graphics2D.fillRect(daysX - (dayOfWeek.getWidth() / 2 - 1) + (dayOfWeek.getWidth() - 1), milestone.flagY, 1,
                                milestone.flagHeight - 1);
                    }
                } else if (phase == 0) {
                    {
                        if (drawDays && isDayBarsVisible()) {
                            father.drawDayBars(currentDay);
                        }
                    }
                    if (milestonesVisible()) {
                        Color color = GraphColorUtil.getDayOfWeekColor(father.graphicsTheme, startCal);
                        father.graphics2D.setColor(color);
                        //                        father.graphics2D.setColor(Color.red);
                        father.graphics2D.fillRect(daysX - (dayOfWeek.getWidth() / 2 - 1), milestone.flagY, dayOfWeek.getWidth(), milestone.flagHeight - 1);
                    }
                }
            }
        }

        // {
        // long dayIndex = milestones.get("N").time - ONE_DAY_MILLIS * (width / 2 + 60)
        // / day.width;
        // Calendar startCal = Calendar.getInstance();
        // startCal.setTimeInMillis(dayIndex);
        // father.graphics2D.setFont(year.font);
        // father.graphics2D.setColor(graphicsTheme.yearTextColor);
        // father.graphics2D.drawString(String.valueOf(startCal.get(Calendar.YEAR)), 1,
        // year.y + 1);
        // }
    }

    public String drawMilestone(Milestone m, LocalDate time, int x, Color fillColor, String text, boolean drawMilestone, Color flagTextColor) {
        return drawMilestone(m, time, x, milestone.y, fillColor, text, drawMilestone, milestone.flagY, flagTextColor, true, true);
    }

    public String drawMilestone(Milestone m, LocalDate time, int x, int y, Color fillColor, String text, boolean drawMilestone, Integer flagY,
                                Color flagTextColor, boolean drawFlag, boolean drawNowLine) {
        String imageMap = "";
        int    centerX  = x;
        int    centerY  = y;
        if (text.startsWith("N")) {
            // now line
            if (drawNowLine) {
                father.graphics2D.setColor(GraphicsTheme.COLOR_DARK_RED);
                father.graphics2D.fillRect(x, father.diagram.y, 2, father.diagram.height);
                int d = Math.max(dayOfWeek.getWidth() / 3, 6);
                if (calendarAtBottom) {
                    father.graphics2D.fillOval(x + 1 - d / 2, father.diagram.y - d / 2, d, d);
                } else {
                    father.graphics2D.fillOval(x + 1 - d / 2, father.diagram.y + father.diagram.height - d, d, d);
                }
            }
        }
        if (drawMilestone) {
            // draw milestone
            father.graphics2D.setFont(milestone.font);
            father.graphics2D.setColor(fillColor);
            father.graphics2D.fillRect(centerX - milestone.width / 2, centerY, milestone.width, milestone.height - 1);
            //            if (m != null) {
            //                String lable = DateUtil.createDateString(time, imageMapSdf);
            //                String toolTip = String.format("<b>%s</b> = %s<br>%s.<br>", text, m.name, lable);
            //                Shape s = new TitledRectangle(centerX - milestone.width / 2, centerY, milestone.width, milestone.height - 1, toolTip);
            //                father.graphics2D.fill(s);
            //            } else {
            //                Shape s = new Rectangle(centerX - milestone.width / 2, centerY, milestone.width, milestone.height - 1);
            //                father.graphics2D.fill(s);
            //            }
            father.graphics2D.setColor(father.graphicsTheme.milestoneTextColor);
            FontMetrics fm        = father.graphics2D.getFontMetrics();
            int         maxAscent = fm.getMaxAscent();
            {
                if (m != null) {
                    String lable   = DateUtil.createDateString(time, imageMapSdf);
                    String toolTip = String.format("<b>%s</b> = %s<br>%s.<br>", text, m.name, lable);
                    father.graphics2D.drawString(text, x - milestone.width / 2 + 2, centerY + milestone.height / 2 + maxAscent / 2 - 2, toolTip);
                } else {
                    father.graphics2D.drawString(text, x - milestone.width / 2 + 2, centerY + milestone.height / 2 + maxAscent / 2 - 2);
                }
            }
            if (m != null) {
                // imageMap
                String lable = DateUtil.createDateString(time, imageMapSdf);
                imageMap += "<area alt=\"<font face=arial>";
                imageMap += String.format("<b>%s</b> = %s<br>%s.<br>", text, m.name, lable);
                imageMap += String.format("\" shape=\"rect\" coords=\"%d,%d,%d,%d\"", Canvas.transformToMapX(centerX - milestone.width / 2),
                        Canvas.transformToMapY(centerY), Canvas.transformToMapX(centerX + milestone.width / 2),
                        Canvas.transformToMapY(centerY + milestone.height - 1));
                imageMap += " >\n";
            }
        }
        if (drawMilestone && drawFlag) {
            father.graphics2D.setFont(milestone.flagFont);
            FontMetrics fm    = father.graphics2D.getFontMetrics();
            String      lable = DateUtil.createDateString(time, sdfMMMdd);
            int         width = fm.stringWidth(lable);
            // flag background
            father.graphics2D.setColor(father.graphicsTheme.milestoneFlagColor);
            father.graphics2D.fillRect(x - milestone.width / 2, flagY, width, milestone.flagHeight);
            if (calendarAtBottom) {
                // flag pole
                father.graphics2D.setColor(flagTextColor);
                father.graphics2D.fillRect(centerX, milestone.flagY + milestone.flagHeight - 4, 1, 3);
                // flag
                father.graphics2D.drawString(lable, x - milestone.width / 2, milestone.flagY + milestone.flagHeight - 5);
            } else {
                // flag pole
                father.graphics2D.setColor(flagTextColor);
                father.graphics2D.fillRect(centerX, centerY + milestone.height, 1, 3);
                // flag
                father.graphics2D.drawString(lable, x - milestone.width / 2, milestone.flagY + milestone.flagHeight - 1);
            }
        }
        return imageMap;
    }

    public String drawMilestones() {
        String            imageMap     = "";
        DateTimeFormatter sdfyyyyMMMdd = DateTimeFormatter.ofPattern("yyyy.MMM.d", Util.locale);
        //        logger.info(String.format("%s x=%d daysWidth=%d", DateUtil.createDateString(milestones.lastMilestone, sdfyyyyMMMdd), calculateDayX(milestones.lastMilestone), dayOfWeek.getWidth()));
        for (Milestone milestone : milestones.getList()) {
            int x = calculateDayX(milestone.time);
            imageMap += drawMilestone(milestone, milestone.time, x, father.graphicsTheme.requestMilestoneColor, milestone.symbol, !milestone.hidden,
                    father.graphicsTheme.futureEventColor);// start
        }
        return imageMap;
    }

    public void drawTextBox(int x1, int x2, Integer y1, int height, String text, Color textColor, Color backgroundColor, Color borderColor, Font font,
                            boolean centered) {
        father.graphics2D.setColor(backgroundColor);
        father.graphics2D.setFont(font);
        FontMetrics fm    = father.graphics2D.getFontMetrics();
        int         width = fm.stringWidth(text);
        // int fontHeight = fm.getHeight()+fm.getMaxAscent()+fm.getMaxDescent();
        father.graphics2D.fillRect(x1, y1, x2 - x1, height - 1);
        father.graphics2D.setColor(borderColor);
        father.graphics2D.fillRect(x2, y1, 1, height - 1);
        father.graphics2D.fillRect(x1, y1 + height - 1, x2 - x1 + 1, 1);
        father.graphics2D.setColor(textColor);
        int maxAscent = fm.getMaxAscent();
        if (centered) {
            father.graphics2D.drawString(text, x1 + (x2 - x1) / 2 - width / 2, y1 + height / 2 + (maxAscent + 1) / 2 - 2);
        } else {
            father.graphics2D.drawString(text, x1 + 1 + 1, y1 + height / 2 + (maxAscent + 1) / 2 - 2);
        }
    }

    public int getHeight() {
        int height = year.getHeight();
        if (isMonthVisible()) {
            height += month.getHeight();
        }
        if (isWeekVisible()) {
            height += week.getHeight();
        }
        if (isDayOfWeekVisible()) {
            height += dayOfWeek.getHeight();
            height += dayOfMonth.getHeight();
        } else if (milestonesVisible()) {
            height += milestone.height;
        }
        if (milestonesVisible()) {
            height += milestone.flagHeight;
        }
        return height;
    }

    public int getPostRun() {
        return postRun;
    }

    public int getPriRun() {
        return preRun;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return year.getY();
    }

    public void initPosition(int x, int y) {
        this.x = x;
        if (calendarAtBottom) {
            // flag
            // milestone, dayOfWeek
            // dayOfMonth
            // week
            // month
            // year

            milestone.flagY = y;
            milestone.y     = milestone.flagY + milestone.flagHeight;
            dayOfWeek.setY(milestone.y);
            dayOfMonth.setY(dayOfWeek.getY() + dayOfWeek.getHeight());
            if (isDayOfWeekVisible()) {
                if (isDayOfMonthVisible()) {
                    week.setY(dayOfMonth.getY() + dayOfMonth.getHeight());
                } else {
                    week.setY(dayOfWeek.getY() + dayOfWeek.getHeight());
                }
            } else {
                week.setY(milestone.y + milestone.height);
            }
            if (isWeekVisible()) {
                month.setY(week.getY() + week.getHeight());
            } else {
                month.setY(dayOfWeek.getY() + dayOfWeek.getHeight());
            }
            year.setY(month.getY() + month.getHeight());
        } else {
            // year
            // month
            // week
            // dayOfMonth
            // milestone, dayOfWeek
            // flag
            year.setY(y);
            month.setY(year.getY() + year.getHeight());
            week.setY(month.getY() + month.getHeight());
            dayOfMonth.setY(week.getY() + week.getHeight());
            if (isDayOfMonthVisible()) {
                dayOfWeek.setY(dayOfMonth.getY() + dayOfMonth.getHeight());
            } else {
                dayOfWeek.setY(week.getY() + week.getHeight());
            }
            milestone.y     = dayOfWeek.getY();
            milestone.flagY = dayOfWeek.getY() + milestone.height;
        }
    }

    public void initSize(int width, int dayWidth, boolean calendarAtBottom) {
        this.calendarAtBottom = calendarAtBottom;
        this.width            = width;
        dayOfWeek.setWidth(dayWidth);
        dayOfMonth.setWidth(dayWidth);
    }

    public boolean isCalendarAtBottom() {
        return calendarAtBottom;
    }

    private boolean isDayBarsVisible() {
        return dayOfWeek.getWidth() >= 4;
    }

    private boolean isDayOfMonthVisible() {
        return dayOfWeek.getWidth() >= DAY_OF_MONTH_MIN_DAY_WIDTH;
    }

    private boolean isDayOfWeekVisible() {
        return dayOfWeek.getWidth() >= DAY_OF_WEEK_MIN_DAY_WIDTH;
    }

    private boolean isMonthVisible() {
        return dayOfWeek.getWidth() >= MONTH_MIN_DAY_WIDTH;
    }

    private boolean isWeekVisible() {
        return dayOfWeek.getWidth() >= WEEK_MIN_DAY_WIDTH;
    }

    private boolean milestonesVisible() {
        return !milestones.empty();
    }

}
