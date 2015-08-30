package com.andrebocchini.CMLogTracer.ui;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import com.andrebocchini.CMLogTracer.R;
import com.andrebocchini.CMLogTracer.data.DataSource;
import com.andrebocchini.CMLogTracer.data.LogFile;
import com.andrebocchini.CMLogTracer.data.LogImporter;
import com.andrebocchini.CMLogTracer.data.LogParsingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * User: Andre Bocchini
 * Date: 11/7/13
 * Time: 3:40 PM
 */
public class LogFilesActivity extends Activity implements AsyncLogImportWorkerFragment.AsyncLogImportWorkerFragmentDelegate {

    private ListView logFilesListView;
    private LogFilesListAdapter logFilesListViewAdapter;
    private DataSource dataSource;
    private ArrayList<LogFile> logFiles;
    private LogFile selectedLogFile;
    private AsyncLogImportWorkerFragment asyncLogImportWorkerFragment;
    private ProgressDialog progressDialog;
    private Uri importDataUri;
    private AlertDialog deleteAllLogFilesConfirmationDialog;
    private AlertDialog importDialog;

    public final static String SELECTED_LOG_FILE = "com.andrebocchini.CMLogTracer.SELECTED_LOG_FILE";
    private final static String ASYNC_LOG_IMPORTER = "com.andrebocchini.CMLogTracer.ASYNC_LOG_IMPORTER";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_files_activity);
        getActionBar().setTitle(R.string.log_files_activity_title);

        initializeDataSource();
        initializeLogFilesListView();
        importSampleLogsIfFirstRun();

        // If the Fragment is non-null, then it is currently being
        // retained across an activity destruction/restoration cycle.
        FragmentManager fragmentManager = getFragmentManager();
        asyncLogImportWorkerFragment = (AsyncLogImportWorkerFragment) fragmentManager.findFragmentByTag(ASYNC_LOG_IMPORTER);
        if (asyncLogImportWorkerFragment != null) {
            if (asyncLogImportWorkerFragment.getStatus() != AsyncTask.Status.FINISHED) {
                progressDialog = ProgressDialog.show(this,
                        getString(R.string.import_progress_dialog_title) + " " + asyncLogImportWorkerFragment.getFileName(),
                        asyncLogImportWorkerFragment.getCurrentStateMessage(),
                        true, false);
            }
        } else {
            Intent intent = getIntent();
            if (intent != null) {
                processIntent(intent);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeLogFilesListView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
        if (deleteAllLogFilesConfirmationDialog != null) {
            if (deleteAllLogFilesConfirmationDialog.isShowing()) {
                deleteAllLogFilesConfirmationDialog.dismiss();
            }
        }
        if (importDialog != null) {
            importDialog.cancel();
        }
    }

    private void initializeDataSource() {
        dataSource = new DataSource(this);
    }

    private void initializeLogFilesListView() {
        logFiles = (ArrayList<LogFile>) dataSource.getAllLogFiles();
        logFilesListViewAdapter = new LogFilesListAdapter(this, logFiles);
        logFilesListView = (ListView)findViewById(R.id.logFilesListView);
        logFilesListView.setClickable(true);
        logFilesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                selectedLogFile = new LogFile(logFiles.get(position));
                selectedLogFile.setLogEntries(dataSource.getLogEntriesForLogFile(selectedLogFile));
                loadLogEntriesActivity(logFilesListView);
            }
        });
        logFilesListView.setAdapter(logFilesListViewAdapter);
        registerForContextMenu(logFilesListView);
    }

    private void importSampleLogsIfFirstRun() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        boolean firstRun = settings.getBoolean("firstRun", true);
        InputStream inputStream;
        LogFile logFile;
        String fileName;

        if (firstRun) {
            Log.i(this.getClass().getSimpleName() + ": ", "This is the app's first run.  Performing first run sample file import.");

            inputStream = getResources().openRawResource(R.raw.first_run_sample);
            fileName = getString(R.string.first_run_sample_log_name);
            LogImporter logImporter = new LogImporter();

            Log.i(this.getClass().getSimpleName() + ": ", "Performing first run sample file import: " + fileName);
            try {
                logFile = logImporter.importLogFile(fileName, inputStream);
                logFile = dataSource.addLogFile(logFile);
                logFiles.add(logFile);
                logFilesListViewAdapter.notifyDataSetChanged();

                Log.i(this.getClass().getSimpleName() + ": ", "Finished importing first run sample file: " + fileName);
            } catch (LogParsingException e) {
                Log.e(this.getClass().getSimpleName() + ": ", "Failed to import first run sample file: " + fileName);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(this.getClass().getSimpleName() + ": ", "Unable to close input stream used for first run sample import");
                }
            }
        } else {
            Log.i(this.getClass().getSimpleName() + ": ", "This is not the app's first run.  Skipping sample log import");
        }

        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("firstRun", false);
        editor.commit();
    }

    private void loadLogEntriesActivity(View view) {
        Intent intent = new Intent(this, LogEntriesActivity.class);
        intent.putExtra(SELECTED_LOG_FILE, selectedLogFile);
        startActivity(intent);
    }

    //================================================================================
    // Import handling
    //================================================================================

    @Override
    public void onNewIntent(Intent intent) {
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        String logFileName;
        String action = intent.getAction();
        String scheme = intent.getScheme();
        importDataUri = intent.getData();

        if (Intent.ACTION_VIEW.equals(action)) {
            if (scheme.equals("content")) {
                logFileName = getAttachmentFileNameFromUri(getContentResolver(), importDataUri);
            } else {
                logFileName = getFileNameFromUri(importDataUri);
            }

            getFileNameFromUser(logFileName);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        return uri.getLastPathSegment();
    }

    private String getAttachmentFileNameFromUri(ContentResolver resolver, Uri uri){
        Cursor cursor = resolver.query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
        cursor.moveToFirst();
        int nameIndex = cursor.getColumnIndex(cursor.getColumnNames()[0]);
        if (nameIndex >= 0) {
            return cursor.getString(nameIndex);
        } else {
            return null;
        }
    }

    private void startImport(String importedLogFileName) {
        InputStream inputStream = null;

        try {
            inputStream = getContentResolver().openInputStream(importDataUri);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName() + ": ", "Unable to open input stream from intent");
        }

        if (importedLogFileName != null) {
            FragmentManager fragmentManager = getFragmentManager();
            asyncLogImportWorkerFragment = new AsyncLogImportWorkerFragment(importedLogFileName, inputStream, dataSource);
            fragmentManager.beginTransaction().add(asyncLogImportWorkerFragment, ASYNC_LOG_IMPORTER).commit();

            progressDialog = ProgressDialog.show(this,
                    getString(R.string.import_progress_dialog_title) + " " + importedLogFileName,
                    getString(R.string.import_progress_dialog_title),
                    true, false);
        }
    }

    //================================================================================
    // Action bar
    //================================================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.log_files_actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                displayHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    //================================================================================
    // Context menu
    //================================================================================

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.log_files_context_menu, menu);
        menu.setHeaderTitle(R.string.log_file_edit_menu_header);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_delete_log_file:
                deleteLogFileAtPosition(info.position);
                return true;
            case R.id.action_delete_all_log_files:
                displayDeleteAllLogFilesConfirmationPrompt();
                logFilesListViewAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteLogFileAtPosition(int position) {
        LogFile logFile = logFiles.get(position);
        dataSource.deleteLogFile(logFile);
        logFiles.remove(position);
        logFilesListViewAdapter.notifyDataSetChanged();

        Log.i(this.getClass().getSimpleName() + ": ", "Delete log file at position: " + position);
    }

    private void deleteAllLogFiles() {
        Log.i(this.getClass().getSimpleName() + ": ", "Deleting all log files");
        int numberOfLogs = logFiles.size();
        for (int i=numberOfLogs-1; i>=0; i--) {
            deleteLogFileAtPosition(i);
        }
    }

    //================================================================================
    // AsyncLogImportWorkerFragment.AsyncLogImportWorkerFragmentDelegate
    //================================================================================

    @Override
    public void onPreExecute() {
    }

    @Override
    public void onProgressUpdate(String progress) {
        if (progressDialog.isShowing()) {
            progressDialog.setMessage(progress);
        }
    }

    @Override
    public void onCancelled() {
    }

    @Override
    public void onPostExecute(LogFile logFile) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (logFile != null) {
            logFiles.add(logFile);
            logFilesListViewAdapter.notifyDataSetChanged();
            logFilesListView.setSelection(logFiles.size()-1);
        } else {
            displayLogImportErrorDialog();
        }
    }

    private boolean validateLogFileName(String name) {
        if (name.length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    //================================================================================
    // Custom dialogs
    //================================================================================

    private void getFileNameFromUser(String fileNameSuggestion) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.import_name_prompt_title));
        alert.setMessage(R.string.import_name_prompt_message);

        final EditText input = new EditText(this);
        input.setText(fileNameSuggestion);
        input.setSelection(fileNameSuggestion.length());
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);

                String value = input.getText().toString();
                if (validateLogFileName(value)) {
                    startImport(value);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                setIntent(new Intent());
            }
        });

        importDialog = alert.show();
    }

    private void displayLogImportErrorDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.log_import_error_dialog_title));
        alert.setMessage(R.string.log_import_error_dialog_message);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    private void displayDeleteAllLogFilesConfirmationPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.delete_all_log_files_dialog_title));
        builder.setMessage(getString(R.string.delete_all_log_files_dialog_message));

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAllLogFiles();
            }
        });

        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
            }
        });

        deleteAllLogFilesConfirmationDialog = builder.create();
        deleteAllLogFilesConfirmationDialog.show();
    }
}
