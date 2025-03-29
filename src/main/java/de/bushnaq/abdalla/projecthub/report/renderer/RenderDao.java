package de.bushnaq.abdalla.projecthub.report.renderer;

import de.bushnaq.abdalla.projecthub.dao.Context;
import de.bushnaq.abdalla.projecthub.report.dao.BurnDownGraphicsTheme;

import java.time.Duration;
import java.time.LocalDateTime;

public class RenderDao {
    public int                   chartHeight;
    public int                   chartWidth;//if 0, day width is set to MAX_DAY_WIDTH and cartWidth is calculated from maxDays and dayWidth.
    public String                column;
    public boolean               completed;
    public Context               context;
    public String                cssClass;
    public LocalDateTime         end;
    public Duration              estimatedBestWork;
    public Duration              estimatedWorstWork;
    public LocalDateTime         firstWorklog;
    public BurnDownGraphicsTheme graphicsTheme;
    public LocalDateTime         lastWorklog;
    public String                link;
    public Duration              maxWorked;
    public LocalDateTime         now;
    public int                   postRun;
    public int                   preRun;
    //    public TimeTrackerProject racProject;
    public LocalDateTime         release;
    public Duration              remaining;
    //    public AbstractDevelopmentRequest request;
    public int                   sprintClosed;
    public String                sprintName;
    public LocalDateTime         start;
//    public List<JiraWorklog> worklog;
//    public List<JiraWorklogRemaining> worklogRemaining;

}
