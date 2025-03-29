package de.bushnaq.abdalla.projecthub.report.dao;

import de.bushnaq.abdalla.util.date.DateUtil;

import java.io.PrintStream;
import java.time.Duration;

public class AuthorContribution {
    public Duration remaining = Duration.ZERO;
    public Duration worked    = Duration.ZERO;

    public AuthorContribution() {
    }

    public AuthorContribution(Duration worked, Duration remaining) {
        this.worked    = worked;
        this.remaining = remaining;
    }

    public void add(Duration worked, Duration remaining) {
        this.worked    = this.worked.plus(worked);
        this.remaining = this.remaining.plus(remaining);
    }

    public void addRemaining(Duration remaining) {
        this.remaining = this.remaining.plus(remaining);
    }

    public void addWorked(Duration worked) {
        this.worked = this.worked.plus(worked);
    }

    public void print(PrintStream out) {
        out.printf("worked=%14s, remaining=%14s\n", DateUtil.createWorkDayDurationString(worked, false, true, false),
                DateUtil.createWorkDayDurationString(remaining, false, true, false));
    }

    public void set(Duration worked, Duration remaining) {
        this.worked    = worked;
        this.remaining = remaining;
    }
}
