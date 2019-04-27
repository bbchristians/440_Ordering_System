package org.rit.swen440.dataLayer;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

public class Logger {

    public static Logger OSLogger = new Logger();

    private Logger(){
        // This class has no state!
    }

    public void log(String severity, String message){
        try {
            OutputStream os = new FileOutputStream("log.txt", true);
            PrintWriter pw = new PrintWriter(os);
            Date date = new Date();
            String toLog = date.toString() + " | " + severity + ":\n";
            toLog += "  " + message;
            pw.println(toLog);
            pw.close();
        } catch (Exception e) {
            System.err.println("Failed to open the log file! Error: " + e.toString());
            System.exit(0);
        }
    }
}
