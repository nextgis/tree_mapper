package com.nextgis.woody.activity;

import android.accounts.Account;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplibui.activity.NGActivity;
import com.nextgis.maplibui.fragment.NGWLoginFragment;
import com.nextgis.woody.MainApplication;
import com.nextgis.woody.R;
import com.nextgis.woody.fragment.LoginFragment;
import com.nextgis.woody.util.Constants;
import com.nextgis.woody.util.SettingsConstants;

public class MainActivity extends NGActivity implements NGWLoginFragment.OnAddAccountListener, View.OnClickListener {

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onAddAccount(Account account, String token, boolean accountAdded) {

    }

    enum CURRENT_VIEW {ACCOUNT, INITIAL, NORMAL}
    protected boolean mFirstRun = true;
    protected int mCurrentViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.app_name));

        start();
    }

    private void start() {
        final MainApplication app = (MainApplication) getApplication();
        final Account account = app.getAccount(Constants.ACCOUNT_NAME);
        // check if has safe forest account
        if (account == null || mCurrentViewState == CURRENT_VIEW.ACCOUNT.ordinal()) {
            createAccountView();
        } else {
            // check basic layers
            /*if (!hasBasicLayers(app.getMap()) || mCurrentViewState == CURRENT_VIEW.INITIAL.ordinal()) {
                Log.d(Constants.WTAG, "Account " + Constants.ACCOUNT_NAME + " created. Run second step.");
                createSetupView();
            } else {
                Log.d(Constants.WTAG, "Account " + Constants.ACCOUNT_NAME + " created. Layers created. Run normal view.");
                mFirstRun = false;
                createNormalView();
            }*/
        }
    }

    @Override
    protected void setToolbar(int toolbarId){
        Toolbar toolbar = (Toolbar) findViewById(toolbarId);
        toolbar.getBackground().setAlpha(getToolbarAlpha());
        setSupportActionBar(toolbar);
    }

    protected void createAccountView() {
        mCurrentViewState = CURRENT_VIEW.ACCOUNT.ordinal();
        setContentView(R.layout.activity_main_first);
        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.first_run));

        FragmentManager fm = getSupportFragmentManager();
        NGWLoginFragment ngwLoginFragment = (NGWLoginFragment) fm.findFragmentByTag(Constants.FRAGMENT_LOGIN);

        if (ngwLoginFragment == null)
            ngwLoginFragment = new LoginFragment();

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(com.nextgis.maplibui.R.id.login_frame, ngwLoginFragment, Constants.FRAGMENT_LOGIN);
        ft.commit();
        ngwLoginFragment.setForNewAccount(true);
        ngwLoginFragment.setOnAddAccountListener(this);
        ngwLoginFragment.setUrlText(SettingsConstants.SITE_URL);
    }
}
