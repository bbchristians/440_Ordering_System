package org.rit.swen440.dataLayer;

import org.rit.swen440.dataLayer.sqlite.LogSQLiteClient;
import org.sqlite.SQLiteException;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

public class Logger {

    public static Logger OSLogger = new Logger();
    private LogSQLiteClient sqliteClient;

    private Logger(){
        try {
            this.sqliteClient = new LogSQLiteClient("logs.db");
        } catch( DataLayerException e ) {
            System.err.println("Failed to open the log database: " + e.toString());
            System.exit(0);
        }
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

            // Database logging
            sqliteClient.writeLogItem(message, severity);
        } catch (Exception e) {
            System.err.println("Failed to open the log file! Error: " + e.toString());
            System.exit(0);
        }
    }
}
