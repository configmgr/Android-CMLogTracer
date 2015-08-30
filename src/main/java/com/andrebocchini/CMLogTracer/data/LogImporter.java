package com.andrebocchini.CMLogTracer.data;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * User: Andre Bocchini
 * Date: 11/6/13
 * Time: 1:24 PM
 */
public class LogImporter {
    public LogFile importLogFile(String logFileName, InputStream inputStream) throws LogParsingException {
        String[] lines;
        LogFile logFile;
        LogEntry[] logEntries;

        logFile = new LogFile();
        logFile.setName(logFileName);

        lines = this.getLogFileLines(inputStream);
        logEntries = new LogEntry[lines.length-1];

        for(int i=1; i<lines.length; i++) {
            logEntries[i-1] = this.createLogEntryFromLine(lines[i], i);
        }

        logFile.setDateAdded(new Date());
        logFile.setLogEntries(logEntries);

        return logFile;
    }

    private String[] getLogFileLines(InputStream inputStream) {
        final String messageStartIndicator = Pattern.quote("<![LOG[");

        StringBuffer logFileContentsBuffer = new StringBuffer();
        String logFileContents;
        String currentLine;
        String[] lines;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            try {
                while ((currentLine = bufferedReader.readLine()) != null) {
                    logFileContentsBuffer.append(currentLine);
                }
            } finally {
                bufferedReader.close();
            }
        } catch (IOException e) {
            Log.i(this.getClass().getSimpleName() + ": ", "Failed to read contents of log file");
        }

        logFileContents = logFileContentsBuffer.toString().trim();
        lines = logFileContents.split(messageStartIndicator, 0);

        return lines;
    }

    private LogEntry createLogEntryFromLine(String line, int lineNumber) throws LogParsingException {
        final String messageEndIndicator = Pattern.quote("]LOG]!>");

        LogEntry logEntry;
        String date = "";
        String time = "";
        String message;
        String messageProperties;
        String[] lineTokens;
        String[] messagePropertiesTokens;

        lineTokens = line.split(messageEndIndicator);
        if (lineTokens.length >= 2) {
            message = lineTokens[0].trim();
            messageProperties = lineTokens[1].trim();
            messageProperties = messageProperties.substring(1, messageProperties.length()-1);

            logEntry = new LogEntry();
            logEntry.setMessage(message);
            logEntry.setLineNumber(lineNumber);

            messagePropertiesTokens = messageProperties.split(" ");
            for (int j=0; j<messagePropertiesTokens.length; j++) {
                String[] messagePropertyTokens = messagePropertiesTokens[j].split("=");

                if (messagePropertyTokens.length >= 2) {
                    String propertyName = messagePropertyTokens[0];
                    String propertyValue = messagePropertyTokens[1];
                    propertyValue = propertyValue.substring(1, propertyValue.length()-1);

                    if (propertyName.equals("component")) {
                        logEntry.setComponent(propertyValue);
                    } else if (propertyName.equals("thread")) {
                        logEntry.setThread(propertyValue);
                    } else if (propertyName.equals("file")) {
                        logEntry.setFile(propertyValue);
                    } else if (propertyName.equals("type")) {
                        logEntry.setType(propertyValue);
                    } else if (propertyName.equals("date")) {
                        date = propertyValue;
                    } else if(propertyName.equals("time")) {
                        time = propertyValue;
                    }
                } else {
                    throw new LogParsingException();
                }
            }

            try {
                String dateTimeString;

                dateTimeString = time.substring(1, time.length()-4);
                dateTimeString = new StringBuilder().append(dateTimeString).append(" ").append(date).toString();

                logEntry.setDateTime(CMLogTracerUtils.dateFromDateTimeString(dateTimeString));
            } catch (ParseException e) {
                throw new LogParsingException();
            }
        } else {
            throw new LogParsingException();
        }

        return logEntry;
    }
}
