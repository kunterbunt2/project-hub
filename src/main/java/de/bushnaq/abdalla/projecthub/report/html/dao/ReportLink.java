package de.bushnaq.abdalla.projecthub.report.html.dao;

public class ReportLink extends ReportCell {

    public String link;

    public ReportLink(String column, String text, String link) {
        super(column, text);
        this.link = link;
    }

    public ReportLink(String column, String text, String link, boolean hidden) {
        super(column, text, hidden);
        this.link = link;
    }

    public ReportLink(String column, String text, String icon, String link) {
        super(column, text, icon);
        this.link = link;
    }

    public ReportLink(String column, String text, String icon, String link, boolean hidden) {
        super(column, text, icon, hidden);
        this.link = link;
    }

}
