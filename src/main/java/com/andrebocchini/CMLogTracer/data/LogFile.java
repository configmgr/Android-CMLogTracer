package com.andrebocchini.CMLogTracer.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created By: Andre Bocchini
 * Date: 11/6/13
 * Time: 12:22 PM
 */
public class LogFile implements Parcelable {
    private long id;
    private String name;
    private Date dateAdded;
    private LogEntry[] logEntries;

    public LogFile() {}

    public LogFile(LogFile logFile) {
        this.id = logFile.id;
        this.name = logFile.name;
        this.dateAdded = logFile.dateAdded;
        this.logEntries = logFile.logEntries;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public LogEntry[] getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(LogEntry[] logEntries) {
        this.logEntries = logEntries;
    }

    //================================================================================
    // Parcelable
    //================================================================================

    private LogFile(Parcel in) {
        id = in.readLong();
        name = in.readString();
        dateAdded = new Date(in.readLong());
        logEntries = new LogEntry[in.readInt()];
        in.readTypedArray(logEntries, LogEntry.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(name);
        out.writeLong(dateAdded.getTime());
        out.writeInt(logEntries.length);
        out.writeTypedArray(logEntries, 0);
    }

    public static final Parcelable.Creator<LogFile> CREATOR
            = new Creator<LogFile>() {
        public LogFile createFromParcel(Parcel in) {
            return new LogFile(in);
        }

        public LogFile[] newArray(int size) {
            return new LogFile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
