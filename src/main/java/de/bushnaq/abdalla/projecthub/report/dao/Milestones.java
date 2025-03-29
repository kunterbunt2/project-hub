package de.bushnaq.abdalla.projecthub.report.dao;

import de.bushnaq.abdalla.util.date.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Milestones {
    private static final LocalDate dummy    = LocalDate.parse("1900-01-01");
    private final        DateUtil  dateUtil = new DateUtil();
    public               LocalDate firstMilestone;
    public               LocalDate lastMilestone;
    ArrayList<Milestone> list = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    Map<String, Milestone> map = new HashMap<>();
    //    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Util.locale);
    private String sprintName;

    public Milestones(Milestone[] ms) {
    }

    public Milestones(String sprintName) {
        this.sprintName = sprintName;
    }

    public void add(LocalDate time, String symbol, String name, Color color) {
        add(time, symbol, name, color, false);
    }

    public void add(LocalDate time, String symbol, String name, Color color, boolean hidden) {
        if (time != null && time != dummy) {
            Milestone m = new Milestone(time, symbol, name, color);
            m.hidden = hidden;
            add(m);
        }
    }

    public void add(Milestone m) {
        list.add(m);
        map.put(m.symbol, m);
    }

    public void calculate() {
        Collections.sort(list);
        firstMilestone = list.get(0).time;
        lastMilestone  = list.get(list.size() - 1).time;
    }

    public void clear() {
        list.clear();
        map.clear();
    }

    public boolean empty() {
        for (Milestone m : list) {
            if (!m.hidden) {
                return false;
            }
        }
        return true;
    }

    public Milestone get(String name) {
        return map.get(name);
    }

    public ArrayList<Milestone> getList() {
        return list;
    }

    public void print() {
        String logString = "";
        logger.trace("---------------------------------------------------------------------------------------------------");
        logger.trace(String.format("sprintName='%s'", sprintName));
        for (Milestone m : list) {
            logger.trace(String.format("%s %s", m.symbol, DateUtil.createDateString(m.time, dateUtil.dtfymd)));
        }
        logger.trace(String.format("first %s", DateUtil.createDateString(firstMilestone, dateUtil.dtfymd)));
        logger.trace(String.format("last %s", DateUtil.createDateString(lastMilestone, dateUtil.dtfymd)));
        logger.trace("---------------------------------------------------------------------------------------------------");
        logger.trace(logString);
    }

    public void remove(String name) {
        for (Milestone m : list) {
            if (m.symbol.equals(name)) {
                list.remove(m);
                return;
            }
        }
    }
}