package com.andrebocchini.CMLogTracer.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.content.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;
import com.andrebocchini.CMLogTracer.R;
import com.andrebocchini.CMLogTracer.data.CMLogTracerUtils;
import com.andrebocchini.CMLogTracer.data.LogEntry;
import com.andrebocchini.CMLogTracer.data.LogFile;

import java.text.ParseException;

/**
 * Created by: Andre Bocchini
 * Date: 11/7/13
 * Time: 6:35 AM
 */
public class LogEntryDetailsActivity extends Activity {
    private LogFile logFile;
    private LogEntry logEntry;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_entry_details_activity);

        Intent intent = getIntent();
        logFile = intent.getParcelableExtra(LogEntriesActivity.LOG_FILE);
        logEntry = intent.getParcelableExtra(LogEntriesActivity.SELECTED_ENTRY);

        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setTitle(logFile.getName() + String.format(getString(R.string.log_entry_details_title), logEntry.getLineNumber()));

        TextView dateTimeTextView = (TextView)findViewById(R.id.logEntryDetailsValueRowDateTime);
        try {
            dateTimeTextView.setText(CMLogTracerUtils.stringFromDateTime(logEntry.getDateTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        registerForContextMenu(dateTimeTextView);

        TextView componentTextView = (TextView)findViewById(R.id.logEntryDetailsValueRowComponent);
        componentTextView.setText(logEntry.getComponent());
        registerForContextMenu(componentTextView);

        TextView typeTextView = (TextView)findViewById(R.id.logEntryDetailsValueRowType);
        typeTextView.setText(logEntry.getType());
        if (logEntry.getType().equals("2")) {
            typeTextView.setBackgroundColor(getResources().getColor(R.color.warning_color));
        } else if (logEntry.getType().equals("3")) {
            typeTextView.setBackgroundColor(getResources().getColor(R.color.error_color));
        }
        registerForContextMenu(typeTextView);

        TextView threadTextView = (TextView)findViewById(R.id.logEntryDetailsValueRowThread);
        threadTextView.setText(logEntry.getThread());
        registerForContextMenu(threadTextView);

        TextView fileTextView = (TextView)findViewById(R.id.logEntryDetailsValueRowFile);
        fileTextView.setText(logEntry.getFile());
        registerForContextMenu(fileTextView);

        TextView messageTextView = (TextView)findViewById(R.id.logEntryDetailsValueRowMessage);
        messageTextView.setText(logEntry.getMessage());
        registerForContextMenu(messageTextView);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        logEntry = savedState.getParcelable("logentry");

        Log.i(this.getClass().getSimpleName() + ": ", "Restoring instance state");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("logentry", logEntry);
        super.onSaveInstanceState(outState);

        Log.i(this.getClass().getSimpleName() + ": ", "Saving instance state");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, view.getId(), 0, getString(R.string.log_entry_details_copy_context_menu));

        TextView textView = (TextView)view;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setText(textView.getText());
    }
}