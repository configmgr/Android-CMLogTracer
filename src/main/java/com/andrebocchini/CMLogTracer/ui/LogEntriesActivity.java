package com.andrebocchini.CMLogTracer.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import com.andrebocchini.CMLogTracer.R;
import com.andrebocchini.CMLogTracer.data.LogEntry;
import com.andrebocchini.CMLogTracer.data.LogFile;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by: Andre Bocchini
 * Date: 11/6/13
 * Time: 6:09 PM
 */
public class LogEntriesActivity extends Activity {
    private ListView logEntriesListView;
    private LogEntriesListAdapter logEntriesListViewAdapter;
    private EditText searchField;
    private String entryTypeFilter;

    private LogFile logFile;
    private LogEntry selectedLogEntry;

    private final static String ENTRY_TYPE_FILTER = "com.andrebocchini.CMLogTracer.ENTRY_TYPE_FILTER";
    private final static String SEARCH_TERM = "com.andrebocchini.CMLogTracer.SEARCH_TERM";

    public final static String LOG_FILE = "com.andrebocchini.CMLogTracer.LOG_FILE";
    public final static String SELECTED_ENTRY = "com.andrebocchini.CMLogTracer.SELECTED_ENTRY";

    public final static String ENTRY_TYPE_FILTER_ALL = "1";
    public final static String ENTRY_TYPE_FILTER_WARNING = "2";
    public final static String ENTRY_TYPE_FILTER_ERROR = "3";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_entries_activity);

        String searchTerm = "";
        if (savedInstanceState != null) {
            logFile = savedInstanceState.getParcelable(LOG_FILE);
            entryTypeFilter = savedInstanceState.getString(ENTRY_TYPE_FILTER);
            searchTerm = savedInstanceState.getString(SEARCH_TERM);
        } else {
            Intent intent = getIntent();
            logFile = intent.getParcelableExtra(LogFilesActivity.SELECTED_LOG_FILE);
            entryTypeFilter = ENTRY_TYPE_FILTER_ALL;
        }

        initializeActionBar();
        initializeLogEntriesListView();
        initializeSearchField(searchTerm);
        initializeEntryTypeFilterCheckboxes();
        forceSearchUpdate();
    }

    private void initializeActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setTitle(logFile.getName() + String.format(getString(R.string.log_entries_activity_title), logFile.getLogEntries().length));
    }

    private void initializeLogEntriesListView() {
        ArrayList<LogEntry> logEntries = new ArrayList<LogEntry>(Arrays.asList(logFile.getLogEntries()));
        logEntriesListViewAdapter = new LogEntriesListAdapter(this, logEntries);
        logEntriesListView = (ListView)findViewById(R.id.logEntriesListView);
        logEntriesListView.setClickable(true);
        logEntriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                selectedLogEntry = logEntriesListViewAdapter.getItem(position);
                loadLogEntryDetailView();
            }
        });
        logEntriesListView.setAdapter(logEntriesListViewAdapter);
    }

    private void initializeSearchField(String searchTerm) {
        searchField = (EditText)findViewById(R.id.searchField);
        searchField.setText(searchTerm);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence searchTerm, int arg1, int arg2, int arg3) {
                forceSearchUpdate();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
    }

    private void initializeEntryTypeFilterCheckboxes() {
        if (entryTypeFilter.equals(ENTRY_TYPE_FILTER_ERROR)) {
            ((RadioButton)findViewById(R.id.radioButtonError)).setChecked(true);
        } else if (entryTypeFilter.equals(ENTRY_TYPE_FILTER_WARNING)) {
            ((RadioButton)findViewById(R.id.radioButtonWarning)).setChecked(true);
        } else {
            ((RadioButton)findViewById(R.id.radioButtonAll)).setChecked(true);
        }
    }

    private void loadLogEntryDetailView() {
        Intent intent = new Intent(this, LogEntryDetailsActivity.class);
        intent.putExtra(LOG_FILE, logFile);
        intent.putExtra(SELECTED_ENTRY, selectedLogEntry);
        startActivity(intent);
    }

    public void onRadioButtonClicked(View view) {
        switch(view.getId()) {
            case R.id.radioButtonAll:
                entryTypeFilter = ENTRY_TYPE_FILTER_ALL;
                break;
            case R.id.radioButtonError:
                entryTypeFilter = ENTRY_TYPE_FILTER_ERROR;
                break;
            case R.id.radioButtonWarning:
                entryTypeFilter = ENTRY_TYPE_FILTER_WARNING;
                break;
        }
        forceSearchUpdate();
    }

    private void forceSearchUpdate() {
        logEntriesListViewAdapter.setEntryTypeFilter(entryTypeFilter);
        logEntriesListViewAdapter.getFilter().filter(searchField.getText().toString());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LOG_FILE, logFile);
        outState.putString(ENTRY_TYPE_FILTER, entryTypeFilter);
        outState.putString(SEARCH_TERM, searchField.getText().toString());

        Log.i(this.getClass().getSimpleName() + ": ", "Saving instance state");
    }
}