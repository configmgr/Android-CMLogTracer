package com.andrebocchini.CMLogTracer.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.andrebocchini.CMLogTracer.R;
import com.andrebocchini.CMLogTracer.data.CMLogTracerUtils;
import com.andrebocchini.CMLogTracer.data.LogFile;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * User: Andre Bocchini
 * Date: 11/7/13
 * Time: 3:40 PM
 */
public class LogFilesListAdapter extends ArrayAdapter<LogFile> {
    private final Context context;
    private final ArrayList<LogFile> logFiles;

    public LogFilesListAdapter(Context context, ArrayList<LogFile> logFiles) {
        super(context, R.layout.log_file_listview_row, logFiles);
        this.context = context;
        this.logFiles = logFiles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogFile logFile = logFiles.get(position);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.log_file_listview_row, parent, false);

        TextView textView = (TextView)rowView.findViewById(R.id.logFileListViewRowNameTextView);
        textView.setText(logFile.getName());

        TextView dateTimeTextView = (TextView)rowView.findViewById(R.id.logFileListViewRowDateAddedTextView);
        try {
            dateTimeTextView.setText(context.getString(R.string.log_files_activity_added_on) + CMLogTracerUtils.stringFromDateTime(logFile.getDateAdded()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return rowView;
    }
}
