package com.andrebocchini.CMLogTracer.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created By: Andre Bocchini
 * Date: 11/6/13
 * Time: 12:22 PM
 */
public class LogEntry implements Parcelable {
    private long id;
    private long logFileId;
    private String component;
    private Date dateTime;
    private String file;
    private String message;
    private String thread;
    private String type;
    private int lineNumber;

    public LogEntry() {}

    public LogEntry(LogEntry logEntry) {
        id = logEntry.getId();
        logFileId = logEntry.getLogFileId();
        component = new String(logEntry.getComponent());
        dateTime = new Date(logEntry.getDateTime().getTime());
        file = new String(logEntry.getFile());
        message = new String(logEntry.getMessage());
        thread = new String(logEntry.getThread());
        type = new String(logEntry.getType());
        lineNumber = logEntry.getLineNumber();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLogFileId() {
        return logFileId;
    }

    public void setLogFileId(long logFileId) {
        this.logFileId = logFileId;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return this.getMessage();
    }

    //================================================================================
    // Parcelable
    //================================================================================

    private LogEntry(Parcel in) {
        id = in.readLong();
        logFileId = in.readLong();
        component = in.readString();
        dateTime = new Date(in.readLong());
        file = in.readString();
        message = in.readString();
        thread = in.readString();
        type = in.readString();
        lineNumber = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeLong(logFileId);
        out.writeString(component);
        out.writeLong(dateTime.getTime());
        out.writeString(file);
        out.writeString(message);
        out.writeString(thread);
        out.writeString(type);
        out.writeInt(lineNumber);
    }

    public static final Parcelable.Creator<LogEntry> CREATOR
            = new Parcelable.Creator<LogEntry>() {
        public LogEntry createFromParcel(Parcel in) {
            return new LogEntry(in);
        }

        public LogEntry[] newArray(int size) {
            return new LogEntry[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
