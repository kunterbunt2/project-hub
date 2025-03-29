package de.bushnaq.abdalla.projecthub.report.dao;

import java.time.LocalDateTime;

public class Range {
    public LocalDateTime end;

    public LocalDateTime start;

    public Range(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end   = end;
    }
}
