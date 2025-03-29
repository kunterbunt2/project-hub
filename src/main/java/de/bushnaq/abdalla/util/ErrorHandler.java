package de.bushnaq.abdalla.util;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {

    public List<Throwable> exceptions  = new ArrayList<>();
    public boolean         noException = true;

    protected boolean doubleIsDifferent(double d1, double d2, double delta) {
        return (Double.compare(d1, d2) != 0) && (!(Math.abs(d1 - d2) <= delta));
    }

    public boolean isTrue(String message, boolean value) {
        if (!value) {
            noException = false;
            exceptions.add(new Exception(message));
            return false;
        }
        return true;
    }
}
