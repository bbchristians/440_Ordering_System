package org.rit.swen440.dataLayer;

import java.io.PrintWriter;
import java.util.Date;

public class Logger {
    private PrintWriter writer;

    public Logger(){
        try {
            this.writer = new PrintWriter("log.txt", "UTF-8");
        } catch (Exception e) { }
    }

    public void log(String message){
        Date date = new Date();
        String toLog = date.toString() + "\n";
        toLog += message;
        writer.println(toLog);
    }

    public void closeLog(){
        writer.close();
    }
}
