package de.bushnaq.abdalla.projecthub.report.renderer;

import de.bushnaq.abdalla.projecthub.dto.Worklog;
import de.bushnaq.abdalla.util.date.DateUtil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DayWork {
    public Duration           duration     = Duration.ZERO;
    public List<List<String>> transactions = new ArrayList<>();

    public DayWork() {
    }

    public DayWork(Duration duration) {
        this.duration = duration;
    }

    public void add(Worklog work) {
        List<String> list = new ArrayList<>();
        //TODO get key
//        list.add(String.format("%s", work.getTaskId() );
        list.add(String.format("<b>%s</b>", DateUtil.create24hDurationString(work.getTimeSpent(), false, true, false)));
        list.add(String.format("%s", work.getComment()));
        transactions.add(list);
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public static String transactionsToTooltips(List<List<String>> transactions, String authorName) {
        if (transactions != null && !transactions.isEmpty()) {
            String tooltip = authorName + " <table><tr> <th><b>Key</b></th> <th><b>Work</b></th> <th><b>Summary</b></th></tr>";

            for (List<String> ll : transactions) {
                tooltip += "<tr>";
                for (String s : ll) {
                    tooltip += String.format("<td>%s</td>", s);
                }
                tooltip += "</tr>";
            }
            tooltip += "</table>";
            return tooltip;
        }
        return "";
    }

}
