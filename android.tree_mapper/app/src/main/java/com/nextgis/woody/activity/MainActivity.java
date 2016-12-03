package com.nextgis.woody.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.nextgis.woody.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!mPreferences.getBoolean(SettingsConstants.KEY_PREF_INTRO, false)) {
            startActivity(new Intent(this, IntroActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main_no_permissions);
        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.app_name));

        mCurrentViewState = savedInstanceState != null ? savedInstanceState.getInt(KEY_CURRENT_VIEW) : -1;
        mCurrentTab = savedInstanceState != null ? savedInstanceState.getInt(KEY_CURRENT_TAB, 1) : 1;
        findViewById(R.id.grant_permissions).setOnClickListener(this);

        if (!hasPermissions())
            requestPermissions();
        else
            start();
    }
}
