package com.andrebocchini.CMLogTracer.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.andrebocchini.CMLogTracer.R;

/**
 * Created by: Andre Bocchini
 * Date: 11/13/13
 * Time: 6:29 AM
 */
public class HelpActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_activity);
        getActionBar().setTitle("Help");
    }

    public void gotItButtonClicked(View view) {
        this.finish();
    }
}