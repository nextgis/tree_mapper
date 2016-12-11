package com.nextgis.woody.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.ILayerView;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.datasource.ngw.Connection;
import com.nextgis.maplib.datasource.ngw.INGWResource;
import com.nextgis.maplib.datasource.ngw.Resource;
import com.nextgis.maplib.datasource.ngw.ResourceGroup;
import com.nextgis.maplib.map.Layer;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.NGException;
import com.nextgis.maplibui.activity.NGActivity;
import com.nextgis.maplibui.fragment.NGWLoginFragment;
import com.nextgis.maplibui.mapui.NGWVectorLayerUI;
import com.nextgis.maplibui.mapui.RemoteTMSLayerUI;
import com.nextgis.woody.MainApplication;
import com.nextgis.woody.R;
import com.nextgis.woody.display.TreeRenderer;
import com.nextgis.woody.fragment.LoginFragment;
import com.nextgis.woody.fragment.MapFragment;
import com.nextgis.woody.fragment.TreeDetailsFragment;
import com.nextgis.woody.util.Constants;
import com.nextgis.woody.util.SettingsConstants;

import org.json.JSONException;

import java.io.IOException;

import static com.nextgis.maplib.util.Constants.NOT_FOUND;

public class MainActivity extends NGActivity implements NGWLoginFragment.OnAddAccountListener, View.OnClickListener {


    protected MapBase mMap;
    protected boolean mFirstRun = true;

    @Override
    public int getThemeId() {
        return R.style.AppTheme;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_tree:
                editTree(NOT_FOUND);
                break;
        }
    }

    public void editTree(long i) {
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentByTag(Constants.FRAGMENT_MAP);
        if(mapFragment != null)
            mapFragment.unselectGeometry();

        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(Constants.FEATURE_ID, i);
        GeoPoint pt = mapFragment.getCenter();
        intent.putExtra(SettingsConstants.KEY_PREF_SCROLL_X, pt.getX());
        intent.putExtra(SettingsConstants.KEY_PREF_SCROLL_Y, pt.getY());
        startActivity(intent);
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

            // goto step 2
            refreshActivityView();
        } else
            Toast.makeText(this, R.string.error_init, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start();
    }

    private void start() {
        final MainApplication app = (MainApplication) getApplication();
        mMap = app.getMap();
        final Account account = app.getAccount(Constants.ACCOUNT_NAME);
        // check if has safe forest account
        if (account == null ) {
            createAccountView();
        } else {
            // check basic layers
            if (!hasBasicLayers(app.getMap())) {
                Log.d(Constants.WTAG, "Account " + Constants.ACCOUNT_NAME + " created. Run second step.");
                createFirstRunView();
            }
            else {
                Log.d(Constants.WTAG, "Account " + Constants.ACCOUNT_NAME + " created. Layers created. Run normal view.");
                mFirstRun = false;
                createNormalView();
            }
        }
    }

    @Override
    protected void setToolbar(int toolbarId){
        Toolbar toolbar = (Toolbar) findViewById(toolbarId);
        toolbar.getBackground().setAlpha(getToolbarAlpha());
        setSupportActionBar(toolbar);
    }

    protected void createAccountView() {
        setContentView(R.layout.activity_main_first);
        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.first_run));

        FragmentManager fm = getSupportFragmentManager();
        NGWLoginFragment ngwLoginFragment = (NGWLoginFragment) fm.findFragmentByTag(Constants.FRAGMENT_LOGIN);

        if (ngwLoginFragment == null)
            ngwLoginFragment = new LoginFragment();

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.login_frame, ngwLoginFragment, Constants.FRAGMENT_LOGIN);
        ft.commit();
        ngwLoginFragment.setForNewAccount(true);
        ngwLoginFragment.setOnAddAccountListener(this);
        ngwLoginFragment.setUrlText(SettingsConstants.SITE_URL);
    }

    protected void createFirstRunView() {
        setContentView(R.layout.activity_main_load);
        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.message_loading));

        loadData();
    }

    protected void loadData() {
        final MainApplication app = (MainApplication) getApplication();
        final Account account = app.getAccount(Constants.ACCOUNT_NAME);


        class DownloadTask extends AsyncTask<Account, Integer, String> {
            private ProgressDialog mProgressDialog;
            private String mCurrentMessage;

            @Override
            protected void onPreExecute() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    mProgressDialog = new ProgressDialog(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                else
                    mProgressDialog = new ProgressDialog(MainActivity.this);

                mProgressDialog.setTitle(R.string.processing);
                mProgressDialog.setMax(Constants.KEY_COUNT + 2);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);

                mProgressDialog.show();
            }

            @Override
            protected String doInBackground(Account... params) {
                final String sLogin = app.getAccountLogin(account);
                final String sPassword = app.getAccountPassword(account);
                final String URL = app.getAccountUrl(account);

                if (null == URL || null == sLogin) {
                    return getString(R.string.error_auth);
                }

                int progress = 0;

                Connection connection = new Connection("tmp", sLogin, sPassword, URL);

                if (!connection.connect(false)) {
                    return getString(R.string.error_sign_up);
                }

                // Setup progress dialog.
                mCurrentMessage = getString(R.string.look_for_city);
                publishProgress(progress++);
                connection.loadChildren();

                // 1. Get city resource by key.
                INGWResource resource = null;
                for(int i = 0; i < connection.getChildrenCount(); ++i) {
                    resource = connection.getChild(i);
                    if(resource.getKey().equals(SettingsConstants.CITY_KEY)) {
                        break;
                    }
                    resource = null;
                }

                // Check if the city is found.
                if(null == resource) {
                    return getString(R.string.error_city_found);
                }

                mCurrentMessage = getString(R.string.create_base_map);
                publishProgress(progress++);

                // 2. Add background layer on map.

                RemoteTMSLayerUI layer = new RemoteTMSLayerUI(getApplicationContext(), mMap.createLayerStorage());
                layer.setName(SettingsConstants.BASEMAP_NAME);
                layer.setURL(SettingsConstants.BASEMAP_URL);
                layer.setTMSType(GeoConstants.TMSTYPE_OSM);
                layer.setMaxZoom(20);
                layer.setMinZoom(GeoConstants.DEFAULT_MIN_ZOOM);
                layer.setVisible(true);
                layer.setCacheSizeMultiply(2);
                mMap.addLayer(layer);

                // 3. Get tables for map.

                mCurrentMessage = getString(R.string.start_fill_layer);
                publishProgress(progress++);

                ResourceGroup cityGroup = (ResourceGroup) resource;
                cityGroup.loadChildren();
                Resource cityResource;
                for(int i = 0; i < cityGroup.getChildrenCount(); ++i) {
                    cityResource = (Resource) cityGroup.getChild(i);

                    if(cityResource.getKey().equals(Constants.KEY_MAIN)) {
                        publishProgress(progress++);

                        // Add trees layer on map.
                        NGWVectorLayerUI ngwVectorLayer = new NGWVectorLayerUI(getApplicationContext(), mMap.createLayerStorage(cityResource.getKey()));

                        ngwVectorLayer.setName(Constants.KEY_MAIN);
                        ngwVectorLayer.setRemoteId(cityResource.getRemoteId());
                        ngwVectorLayer.setAccountName(account.name);
                        ngwVectorLayer.setSyncType(com.nextgis.maplib.util.Constants.SYNC_ALL);
                        ngwVectorLayer.setMinZoom(GeoConstants.DEFAULT_MIN_ZOOM);
                        ngwVectorLayer.setMaxZoom(GeoConstants.DEFAULT_MAX_ZOOM);
                        ngwVectorLayer.setVisible(true);

                        // Set style based on state field.

                        mMap.addLayer(ngwVectorLayer);

                        try {
                            ngwVectorLayer.createFromNGW(null);
                        } catch (NGException | IOException | JSONException e) {
                            mMap.delete();

                            e.printStackTrace();
                            return e.getLocalizedMessage();
                        }
                    }
                    else if(isLookupTable(cityResource)) {
                        publishProgress(progress++);

                        NGWLookupTable ngwTable =
                                new NGWLookupTable(getApplicationContext(), mMap.createLayerStorage(cityResource.getKey()));

                        ngwTable.setName(cityResource.getKey());
                        ngwTable.setRemoteId(cityResource.getRemoteId());
                        ngwTable.setAccountName(account.name);
                        ngwTable.setSyncType(com.nextgis.maplib.util.Constants.SYNC_DATA);
                        mMap.addLayer(ngwTable);

                        try {
                            ngwTable.fillFromNGW(null);
                        } catch (NGException | IOException | JSONException e) {
                            mMap.delete();

                            e.printStackTrace();
                            return e.getLocalizedMessage();
                        }
                    }
                }

                mMap.save();

                return getString(R.string.success_filled);
            }

            @Override
            protected void onProgressUpdate(Integer... progress) {
                mProgressDialog.setProgress(progress[0]);
                mProgressDialog.setMessage(mCurrentMessage);
            }

            @Override
            protected void onPostExecute(String result) {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
                refreshActivityView();
            }
        }

        new DownloadTask().execute(account);
    }

    private boolean isLookupTable(INGWResource resource) {
        return resource.getKey().equals(Constants.KEY_LT_AGE) ||
                resource.getKey().equals(Constants.KEY_LT_GIRTH) ||
                resource.getKey().equals(Constants.KEY_LT_GIRTH) ||
                resource.getKey().equals(Constants.KEY_LT_HEIGHT) ||
                resource.getKey().equals(Constants.KEY_LT_INJURY) ||
                resource.getKey().equals(Constants.KEY_LT_PLACEMENT) ||
                resource.getKey().equals(Constants.KEY_LT_SPECIES) ||
                resource.getKey().equals(Constants.KEY_LT_STATE) ||
                resource.getKey().equals(Constants.KEY_LT_YEAR);
    }

    protected boolean hasBasicLayers(MapBase map) {
        return map != null && map.getLayerByName(Constants.KEY_MAIN) != null;
    }

    protected void createNormalView() {

        ILayer layer = mMap.getLayerByName(Constants.KEY_MAIN);
        if(null == layer)
            return;

        ILayerView layerView = (ILayerView) layer;
        layerView.setRenderer(new TreeRenderer((Layer) layer));

        //PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.activity_main);
        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.app_name));

        findViewById(R.id.add_tree).setOnClickListener(this);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentByTag(Constants.FRAGMENT_MAP);

        if (mapFragment == null)
            mapFragment = new MapFragment();

        TreeDetailsFragment treeDetailsFragment = (TreeDetailsFragment)
                fm.findFragmentByTag(Constants.FRAGMENT_TREE_DETAILS);

        if (treeDetailsFragment == null)
            treeDetailsFragment = new TreeDetailsFragment();

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.map_frame, mapFragment, Constants.FRAGMENT_MAP);
        ft.replace(R.id.tree_details, treeDetailsFragment, Constants.FRAGMENT_TREE_DETAILS);
        ft.commit();

        hideTreeDetails();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showTreeDetails(Feature treeFeature) {
        FrameLayout fl = (FrameLayout) findViewById(R.id.tree_details);
        if (fl.getVisibility() == View.GONE) {
            // hide fab
            findViewById(R.id.add_tree).setVisibility(View.GONE);
            // show details fragment
            fl.setVisibility(View.VISIBLE);
            // fill fragment with data
        }

        FragmentManager fm = getSupportFragmentManager();

        TreeDetailsFragment treeDetailsFragment = (TreeDetailsFragment) fm.findFragmentByTag(Constants.FRAGMENT_TREE_DETAILS);
        treeDetailsFragment.fill(treeFeature);

        MapFragment mapFragment = (MapFragment) fm.findFragmentByTag(Constants.FRAGMENT_MAP);
        GeoPoint pt = (GeoPoint) treeFeature.getGeometry();
        mapFragment.setCenter(pt);
    }

    public void hideTreeDetails() {
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentByTag(Constants.FRAGMENT_MAP);
        if(mapFragment != null)
            mapFragment.unselectGeometry();
        findViewById(R.id.add_tree).setVisibility(View.VISIBLE);
        findViewById(R.id.tree_details).setVisibility(View.GONE);
    }
}
