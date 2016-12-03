package com.nextgis.woody.activity;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.ngw.Connection;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.MapUtil;
import com.nextgis.maplib.util.NGException;
import com.nextgis.maplibui.activity.NGActivity;
import com.nextgis.maplibui.fragment.NGWLoginFragment;
import com.nextgis.maplibui.mapui.NGWVectorLayerUI;
import com.nextgis.maplibui.mapui.RemoteTMSLayerUI;
import com.nextgis.woody.MainApplication;
import com.nextgis.woody.R;
import com.nextgis.woody.fragment.LoginFragment;
import com.nextgis.woody.util.Constants;
import com.nextgis.woody.util.SettingsConstants;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends NGActivity implements NGWLoginFragment.OnAddAccountListener, View.OnClickListener {


    MapBase mMap;

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onAddAccount(Account account, String token, boolean accountAdded) {
        Log.d(Constants.WTAG, "No account. " + Constants.ACCOUNT_NAME + " created. Run first step.");

        if(accountAdded) {
            // TODO: final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            final MainApplication app = (MainApplication) getApplication();
            app.setUserData(account.name, Constants.KEY_IS_AUTHORIZED, token);

            //set sync with server
            ContentResolver.setSyncAutomatically(account, app.getAuthority(), true);
            ContentResolver.addPeriodicSync( account, app.getAuthority(), Bundle.EMPTY,
                    com.nextgis.maplib.util.Constants.DEFAULT_SYNC_PERIOD);

            // load data
            loadData();

            // goto step 2
            refreshActivityView();
        } else
            Toast.makeText(this, R.string.error_init, Toast.LENGTH_SHORT).show();
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
        mMap = app.getMap();
        final Account account = app.getAccount(Constants.ACCOUNT_NAME);
        // check if has safe forest account
        if (account == null || mCurrentViewState == CURRENT_VIEW.ACCOUNT.ordinal()) {
            createAccountView();
        } else {
            // check basic layers
            if (!hasBasicLayers(app.getMap()) || mCurrentViewState == CURRENT_VIEW.INITIAL.ordinal()) {
                Log.d(Constants.WTAG, "Account " + Constants.ACCOUNT_NAME + " created. Run second step.");
                loadData();
            }

            Log.d(Constants.WTAG, "Account " + Constants.ACCOUNT_NAME + " created. Layers created. Run normal view.");
            mFirstRun = false;
            createNormalView();
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

    protected void loadData() {
        final MainApplication app = (MainApplication) getApplication();
        final Account account = app.getAccount(Constants.ACCOUNT_NAME);
        final String sLogin = app.getAccountLogin(account);
        final String sPassword = app.getAccountPassword(account);
        String URL = app.getAccountUrl(account);

        if (null == URL || null == sLogin) {
            return;
        }

        Connection connection = new Connection("tmp", sLogin, sPassword, URL);

        if (!connection.connect(false)) {
            return;
        }

        // add background layer
        RemoteTMSLayerUI layer = new RemoteTMSLayerUI(getApplicationContext(), mMap.createLayerStorage());
        layer.setName(SettingsConstants.BASEMAP_NAME);
        layer.setURL(SettingsConstants.BASEMAP_URL);
        layer.setTMSType(GeoConstants.TMSTYPE_OSM);
        layer.setMaxZoom(GeoConstants.DEFAULT_MAX_ZOOM);
        layer.setMinZoom(GeoConstants.DEFAULT_MIN_ZOOM);
        layer.setVisible(true);
        mMap.addLayer(layer);

        // TODO: scan SettingsConstants.CITY_ID to get layers id's

        // add trees
        NGWVectorLayerUI ngwVectorLayer = new NGWVectorLayerUI(getApplicationContext(), mMap.createLayerStorage(Constants.KEY_MAIN));

        ngwVectorLayer.setName(Constants.KEY_MAIN);
        ngwVectorLayer.setRemoteId(10); //(mKeys.get(layerName).getRemoteId());
        ngwVectorLayer.setAccountName(account.name);
        ngwVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_ALL);
        ngwVectorLayer.setMinZoom(GeoConstants.DEFAULT_MIN_ZOOM);
        ngwVectorLayer.setMaxZoom(GeoConstants.DEFAULT_MAX_ZOOM);
        ngwVectorLayer.setVisible(true);

        mMap.addLayer(ngwVectorLayer);

        try {
            ngwVectorLayer.createFromNGW(null);
        } catch (NGException | IOException | JSONException e) {
            e.printStackTrace();
            return;
        }

        mMap.save();

    }

    protected boolean hasBasicLayers(MapBase map) {
        return map != null && map.getLayerByName(Constants.KEY_MAIN) != null;
    }

    protected void createNormalView() {

    }
}
