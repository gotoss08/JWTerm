package com.jwterm.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.*;

public class LoggingUtility {

    public static class LogFormatter extends Formatter {
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

    public static ArrayList<Logger> loggers = new ArrayList<>();

    public static void setLogLevel(Level level) {
        for (Logger logger : loggers) {
            logger.setLevel(level);
        }
    }

    public static Logger getLogger(String name) {

        Logger logger = Logger.getLogger(name);

        if (loggers.contains(logger)) return logger;
        loggers.add(logger);

        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        // Console handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new LogFormatter());
        logger.addHandler(consoleHandler);

        try {
            FileHandler fileHandler = new FileHandler("application.log", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new LogFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return logger;
    }
}
