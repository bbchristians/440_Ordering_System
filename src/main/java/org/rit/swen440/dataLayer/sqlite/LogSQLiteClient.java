package org.rit.swen440.dataLayer.sqlite;

import org.rit.swen440.dataLayer.DataLayerException;
import org.rit.swen440.dataLayer.LogItem;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LogSQLiteClient extends SQLiteClient {

    public LogSQLiteClient(String db) throws DataLayerException {
        super(db);
    }

    public List<LogItem> getLogs(long startDate, long endDate) throws DataLayerException {
        try {
            PreparedStatement stmt = this.conn.prepareStatement("SELECT * FROM LOG WHERE DATE >= ? AND DATE <= ?");
            stmt.setLong(1, startDate);
            stmt.setLong(2, endDate);
            ResultSet rs = stmt.executeQuery();
            List results = new ArrayList<LogItem>();
            // Fetch each row from the result set
            while (rs.next()) {
                String message = rs.getString("message");
                String severity = rs.getString("severity");
                long date = rs.getLong("date");

                // Make Log Item
                LogItem item = new LogItem(message, severity, new Date(date * 1000));

                results.add(item);
            }
            return results;
        } catch (SQLException e) {
            LOGGER.log("ERROR", "System encountered an SQL exception while reading log database. Error readout: \r\n" + e.toString());
        }
        return new ArrayList<>();
    }

    public void writeLogItem(String message, String severity) {
        writeLogItem(new LogItem(message, severity, new Date(System.currentTimeMillis())));
    }

    public void writeLogItem(LogItem item) {
        try {
            PreparedStatement stmt = this.conn.prepareStatement("INSERT INTO LOG (MESSAGE, SEVERITY, DATE) VALUES (?, ?, ?)");
            stmt.setString(1, item.message);
            stmt.setString(2, item.severity);
            stmt.setLong(3, item.date.getTime()/1000);
            stmt.execute();
        } catch (SQLException e) {
            LOGGER.log("ERROR", "System encountered an SQL exception while writing to log database. Error readout: \r\n" + e.toString());
        }
    }
}
