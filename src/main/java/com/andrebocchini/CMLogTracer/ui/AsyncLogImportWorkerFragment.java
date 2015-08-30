package com.andrebocchini.CMLogTracer.ui;

/**
 * Created by: Andre Bocchini
 * Date: 11/8/13
 * Time: 5:11 PM
 */

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.andrebocchini.CMLogTracer.R;
import com.andrebocchini.CMLogTracer.data.DataSource;
import com.andrebocchini.CMLogTracer.data.LogFile;
import com.andrebocchini.CMLogTracer.data.LogImporter;
import com.andrebocchini.CMLogTracer.data.LogParsingException;

import java.io.InputStream;

/**
 * This Fragment manages a single background task and retains
 * itself across configuration changes.
 */
public class AsyncLogImportWorkerFragment extends Fragment {

    private final String fileName;
    private final InputStream inputStream;
    private final DataSource dataSource;
    private String currentStateMessage;

    public AsyncLogImportWorkerFragment(String fileName, InputStream inputStream, DataSource dataSource) {
        this.fileName = fileName;
        this.inputStream = inputStream;
        this.dataSource = dataSource;
    }

    public String getFileName() {
        return fileName;
    }

    public String getCurrentStateMessage() {
        return currentStateMessage;
    }

    public AsyncTask.Status getStatus() {
        return logImportAsyncTask.getStatus();
    }

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    static interface AsyncLogImportWorkerFragmentDelegate {
        void onPreExecute();
        void onProgressUpdate(String currentLine);
        void onCancelled();
        void onPostExecute(LogFile logFile);
    }

    private AsyncLogImportWorkerFragmentDelegate delegate;
    private LogImportAsyncTask logImportAsyncTask;

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        delegate = (AsyncLogImportWorkerFragmentDelegate) activity;
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        // Create and execute the background task.
        logImportAsyncTask = new LogImportAsyncTask();
        logImportAsyncTask.execute(fileName);
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        delegate = null;
    }

    /**
     * A dummy task that performs some (dumb) background work and
     * proxies progress updates and results back to the Activity.
     *
     * Note that we need to check if the callbacks are null in each
     * method in case they are invoked after the Activity's and
     * Fragment's onDestroy() method have been called.
     */
    private class LogImportAsyncTask extends AsyncTask<String, String, LogFile> {

        @Override
        protected void onPreExecute() {
            if (delegate != null) {
                delegate.onPreExecute();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected LogFile doInBackground(String... parameters) {
            LogFile logFile;
            LogImporter logImporter = new LogImporter();
            String fileName = parameters[0];

            Log.i(this.getClass().getSimpleName() + ": ", "Starting to import file: " + fileName);

            publishProgress(getString(R.string.import_progress_dialog_message_1));
            try {
                logFile = logImporter.importLogFile(fileName, inputStream);
                publishProgress(getString(R.string.import_progress_dialog_message_2));

                Log.i(this.getClass().getSimpleName() + ": ", "Finished importing file: " + fileName);
                logFile = dataSource.addLogFile(logFile);
            } catch (LogParsingException e) {
                logFile = null;
            }

            return logFile;
        }

        @Override
        protected void onCancelled() {
            if (delegate != null) {
                delegate.onCancelled();
            }
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            if (delegate != null) {
                currentStateMessage = progress[0];
                delegate.onProgressUpdate(progress[0]);
            }
        }

        @Override
        protected void onPostExecute(LogFile logFile) {
            if (delegate != null) {
                delegate.onPostExecute(logFile);
            }
        }
    }
}