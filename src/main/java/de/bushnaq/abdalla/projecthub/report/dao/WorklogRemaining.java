package de.bushnaq.abdalla.projecthub.report.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class WorklogRemaining {
    private final String   author;
    private final Long     issueId;
    private final String   key;
    private final Logger   logger = LoggerFactory.getLogger(this.getClass());
    private final Duration remaining;
    private final Long     sprintId;
    private final Duration timeworked;

    public WorklogRemaining(Long sprintId, Long issueId, String key, String author, Duration timeworked, Duration remaining) {
        this.sprintId   = sprintId;
        this.issueId    = issueId;
        this.key        = key;
        this.author     = author;
        this.timeworked = timeworked;
        this.remaining  = remaining;
    }

    public String getAuthor() {
        return author;
    }

    public Long getIssueId() {
        return issueId;
    }

    public String getKey() {
        return key;
    }

    public Duration getRemaining() {
        return remaining;
    }

    public Long getSprintId() {
        return sprintId;
    }

    public Duration getTimeworked() {
        return timeworked;
    }

    public void print() {
        //        DateUtil dateUtil = new DateUtil();
        String logString = "";
        logString += String.format(", worklog.sprint_id=" + sprintId);
        logString += String.format(", worklog.issue_id=" + issueId);
        logString += String.format(", worklog.author=" + author);
        logString += String.format(", worklog.timeworked=" + timeworked);
        logString += String.format(", worklog.remaining=" + remaining);
        logger.trace(logString);
    }

}
