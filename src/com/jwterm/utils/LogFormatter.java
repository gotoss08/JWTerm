package com.jwterm.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        // Create a simple date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date(record.getMillis()));

        // Format the log message in standard format
        String level = record.getLevel().getName();
        String className = record.getSourceClassName();
        String message = record.getMessage();

        return String.format("[%s] [%s] [%s] - %s%n", timestamp, level, className, message);
    }
}
