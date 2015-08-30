package com.andrebocchini.CMLogTracer.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.andrebocchini.CMLogTracer.R;
import com.andrebocchini.CMLogTracer.data.CMLogTracerUtils;
import com.andrebocchini.CMLogTracer.data.LogEntry;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by: Andre Bocchini
 * Date: 11/6/13
 * Time: 9:12 PM
 */
public class LogEntriesListAdapter extends ArrayAdapter<LogEntry> implements Filterable {
    private final Context context;
    private ArrayList<LogEntry> logEntries;
    private ArrayList<LogEntry> searchResults;
    private String entryTypeFilter;

    public LogEntriesListAdapter(Context context, ArrayList<LogEntry> logEntries) {
        super(context, R.layout.log_entry_listview_row, logEntries);
        this.context = context;
        this.entryTypeFilter = LogEntriesActivity.ENTRY_TYPE_FILTER_ALL;

        this.logEntries = new ArrayList<LogEntry>();
        for (int i=0; i<logEntries.size(); i++) {
            this.logEntries.add(new LogEntry(logEntries.get(i)));
        }
        this.searchResults = new ArrayList<LogEntry>();
        for (int i=0; i<logEntries.size(); i++) {
            this.searchResults.add(new LogEntry(logEntries.get(i)));
        }
    }

    public void setEntryTypeFilter(String entryTypeFilter) {
        this.entryTypeFilter = entryTypeFilter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogEntry logEntry = searchResults.get(position);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.log_entry_listview_row, parent, false);

        TextView textView = (TextView)rowView.findViewById(R.id.logEntryListViewRowMessageTextView);
        textView.setText(logEntry.getMessage());

        TextView dateTimeTextView = (TextView)rowView.findViewById(R.id.logEntryListViewRowDateTimeTextView);
        try {
            dateTimeTextView.setText(CMLogTracerUtils.stringFromDateTime(logEntry.getDateTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TextView lineNumberTextView = (TextView)rowView.findViewById(R.id.logEntryListViewRowLineNumberTextView);
        lineNumberTextView.setText("Line: " + Integer.toString(logEntry.getLineNumber()));

        int logEntryType = Integer.parseInt(logEntry.getType());
        if (logEntryType == 3) {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.error_color));
        } else if(logEntryType == 2) {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.warning_color));
        }

        return rowView;
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                searchResults = (ArrayList<LogEntry>)results.values;
                notifyDataSetChanged();
                clear();
                for(int i = 0, l = searchResults.size(); i < l; i++) {
                    add(new LogEntry(searchResults.get(i)));
                }
                notifyDataSetInvalidated();
            }

            @Override
            protected FilterResults performFiltering(CharSequence searchTerm) {
                FilterResults results = new FilterResults();
                ArrayList<LogEntry> filteredEntries = new ArrayList<LogEntry>();

                if (searchTerm.length() >= 0) {
                    for (int i = 0; i < logEntries.size(); i++) {
                        String message = logEntries.get(i).getMessage();
                        if (message.toLowerCase().contains(searchTerm.toString().toLowerCase()))  {
                            if (!entryTypeFilter.equals(LogEntriesActivity.ENTRY_TYPE_FILTER_ALL)) {
                                if (logEntries.get(i).getType().equals(entryTypeFilter)) {
                                    filteredEntries.add(logEntries.get(i));
                                }
                            } else {
                                filteredEntries.add(logEntries.get(i));
                            }
                        }
                    }

                    results.count = filteredEntries.size();
                    results.values = filteredEntries;
                } else {
                    results.count = logEntries.size();
                    results.values = logEntries;
                }

                return results;
            }
        };
        return filter;
    }
}