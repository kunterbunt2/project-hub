package de.bushnaq.abdalla.projecthub.report.dao;

import java.util.ArrayList;
import java.util.List;

public class MetaData {
    public static final String       METADATA    = "METADATA";
    public              List<String> errors      = new ArrayList<>();
    public              Double       maxDuration = null;
    public              Double       risk        = null;

    public void addError(String message) {
        errors.add(message);
    }

    public String getError(int i) {
        return errors.get(i);
    }

}
