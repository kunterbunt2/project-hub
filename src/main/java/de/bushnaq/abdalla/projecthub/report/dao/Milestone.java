package de.bushnaq.abdalla.projecthub.report.dao;

import java.awt.*;
import java.time.LocalDate;

public class Milestone implements Comparable<Milestone> {
    public Color     color;
    public boolean   hidden;
    public String    name;
    public boolean   nowLine = false;
    public String    symbol;
    public LocalDate time;

    public Milestone(LocalDate time, String symbol, String name, Color color) {
        this.time   = time;
        this.symbol = symbol;
        this.name   = name;
        this.color  = color;
    }

    public Milestone(LocalDate time, String symbol, String name, Color color, boolean nowLine) {
        this.time    = time;
        this.symbol  = symbol;
        this.name    = name;
        this.color   = color;
        this.nowLine = nowLine;
    }

    @Override
    public int compareTo(Milestone o) {
        return time.compareTo(o.time);
        //        return Long.compare(time, o.time);
    }
}
