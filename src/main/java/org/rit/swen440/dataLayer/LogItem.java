package org.rit.swen440.dataLayer;

import java.util.Date;

public class LogItem {

    public String message;
    public String severity;
    public Date date;

    public LogItem(String message, String severity, Date date) {
        this.message = message;
        this.severity = severity;
        this.date = date;
    }
}
