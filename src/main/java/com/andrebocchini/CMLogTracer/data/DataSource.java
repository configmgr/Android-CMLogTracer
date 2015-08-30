package com.andrebocchini.CMLogTracer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: Andre Bocchini
 * Date: 11/7/13
 * Time: 1:42 PM
 */
public class DataSource extends SQLiteOpenHelper {

    private static abstract class LogFiles implements BaseColumns {
        protected static final String TABLE_NAME = "log_files";
        protected static final String COLUMN_ID = "_id";
        protected static final String COLUMN_NAME = "name";
        protected static final String COLUMN_DATE_ADDED = "date_added";
    }

    private static abstract class LogEntries implements BaseColumns {
        protected static final String TABLE_NAME = "log_entries";
        protected static final String COLUMN_ID = "_id";
        protected static final String COLUMN_COMPONENT = "component";
        protected static final String COLUMN_DATE_TIME = "date_time";
        protected static final String COLUMN_FILE = "file";
        protected static final String COLUMN_MESSAGE = "message";
        protected static final String COLUMN_THREAD = "thread";
        protected static final String COLUMN_TYPE = "type";
        protected static final String COLUMN_LINE_NUMBER = "line_number";
        protected static final String COLUMN_LOG_FILE_ID = "log_file";
    }

    private final SQLiteDatabase database;
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_LOG_FILES_TABLE_QUERY = "create table " +
            LogFiles.TABLE_NAME + "(" +
            LogFiles.COLUMN_ID + " integer primary key autoincrement, " +
            LogFiles.COLUMN_NAME + " text not null, " +
            LogFiles.COLUMN_DATE_ADDED + " timestamp);";
    private static final String CREATE_LOG_ENTRIES_TABLE_QUERY = "create table " +
            LogEntries.TABLE_NAME + "(" +
            LogEntries.COLUMN_ID + " integer primary key autoincrement, " +
            LogEntries.COLUMN_COMPONENT + " text not null, " +
            LogEntries.COLUMN_FILE + " text not null, " +
            LogEntries.COLUMN_MESSAGE + " text not null, " +
            LogEntries.COLUMN_THREAD + " text not null, " +
            LogEntries.COLUMN_TYPE + " text not null, " +
            LogEntries.COLUMN_LINE_NUMBER + " integer, " +
            LogEntries.COLUMN_DATE_TIME + " timestamp, " +
            LogEntries.COLUMN_LOG_FILE_ID + " integer, " +
            " foreign key(" + LogEntries.COLUMN_LOG_FILE_ID + ") references " + LogFiles.TABLE_NAME + "(" + LogFiles.COLUMN_ID + ")" +
            ");";

    public DataSource(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_LOG_FILES_TABLE_QUERY);
        database.execSQL(CREATE_LOG_ENTRIES_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Implement this at some point
    }

    public LogFile addLogFile(LogFile logFile) {
        ContentValues values;

        values = new ContentValues();
        values.put(LogFiles.COLUMN_NAME, logFile.getName());
        values.put(LogFiles.COLUMN_DATE_ADDED, logFile.getDateAdded().getTime());

        long insertId = database.insert(LogFiles.TABLE_NAME, null, values);
        logFile.setId(insertId);
        addLogEntriesForLogFile(logFile);
        logFile.setLogEntries(getLogEntriesForLogFile(logFile));

        return logFile;
    }

    public boolean deleteLogFile(LogFile logFile) {
        deleteLogEntriesForLogFile(logFile);

        long id = logFile.getId();
        database.delete(LogFiles.TABLE_NAME, LogFiles.COLUMN_ID
                + " = " + id, null);
        return true;
    }

    public List<LogFile> getAllLogFiles() {
        List<LogFile> logFiles = new ArrayList<LogFile>();
        String[] logFilesTableColumns = { LogFiles.COLUMN_ID,
                LogFiles.COLUMN_NAME,
                LogFiles.COLUMN_DATE_ADDED };

        Cursor cursor = database.query(LogFiles.TABLE_NAME,
                logFilesTableColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            LogFile logFile = new LogFile();

            logFile.setId(cursor.getInt(0));
            logFile.setName(cursor.getString(1));
            logFile.setDateAdded(new Date(cursor.getLong(2)));

            logFiles.add(logFile);
            cursor.moveToNext();
        }

        cursor.close();
        return logFiles;
    }

    private void addLogEntriesForLogFile(LogFile logFile) {
        LogEntry[] logEntries;
        ArrayList<ContentValues> allContentValues = new ArrayList<ContentValues>();

        logEntries = logFile.getLogEntries();
        for(int i=0; i<logEntries.length; i++) {
            LogEntry logEntry;
            ContentValues values;

            logEntry = logEntries[i];
            logEntry.setLogFileId(logFile.getId());

            values = new ContentValues();
            values.put(LogEntries.COLUMN_COMPONENT, logEntry.getComponent());
            values.put(LogEntries.COLUMN_DATE_TIME, logEntry.getDateTime().getTime());
            values.put(LogEntries.COLUMN_FILE, logEntry.getFile());
            values.put(LogEntries.COLUMN_MESSAGE, logEntry.getMessage());
            values.put(LogEntries.COLUMN_THREAD, logEntry.getThread());
            values.put(LogEntries.COLUMN_TYPE, logEntry.getType());
            values.put(LogEntries.COLUMN_LINE_NUMBER, logEntry.getLineNumber());
            values.put(LogEntries.COLUMN_LOG_FILE_ID, logEntry.getLogFileId());

            allContentValues.add(values);
        }

        // Batch insert
        database.beginTransaction();
        for (ContentValues values : allContentValues) {
            database.insert(LogEntries.TABLE_NAME, null, values);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    private boolean deleteLogEntriesForLogFile(LogFile logFile) {
        long id = logFile.getId();
        database.delete(LogEntries.TABLE_NAME, LogEntries.COLUMN_LOG_FILE_ID
                + " = " + id, null);
        return true;
    }

    public LogEntry[] getLogEntriesForLogFile(LogFile logFile) {
        List<LogEntry> logEntries = new ArrayList<LogEntry>();
        String[] logEntriesTableColumns = { LogEntries.COLUMN_ID,
                LogEntries.COLUMN_COMPONENT,
                LogEntries.COLUMN_FILE,
                LogEntries.COLUMN_MESSAGE,
                LogEntries.COLUMN_THREAD,
                LogEntries.COLUMN_TYPE,
                LogEntries.COLUMN_LINE_NUMBER,
                LogEntries.COLUMN_DATE_TIME,
                LogEntries.COLUMN_LOG_FILE_ID };

        Cursor cursor = database.query(LogEntries.TABLE_NAME,
                logEntriesTableColumns,
                LogEntries.COLUMN_LOG_FILE_ID + "=" + logFile.getId(),
                null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            LogEntry logEntry = new LogEntry();

            logEntry.setId(cursor.getInt(0));
            logEntry.setComponent(cursor.getString(1));
            logEntry.setFile(cursor.getString(2));
            logEntry.setMessage(cursor.getString(3));
            logEntry.setThread(cursor.getString(4));
            logEntry.setType(cursor.getString(5));
            logEntry.setLineNumber(cursor.getInt(6));
            logEntry.setDateTime(new Date(cursor.getLong(7)));
            logEntry.setLogFileId(cursor.getInt(8));

            logEntries.add(logEntry);
            cursor.moveToNext();
        }

        LogEntry[] logEntriesArray = new LogEntry[logEntries.size()];
        logEntries.toArray(logEntriesArray);
        logFile.setLogEntries(logEntriesArray);

        cursor.close();
        return logEntriesArray;
    }
}