package com.nextgis.woody.activity;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.ILayerView;
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
import com.nextgis.woody.util.Constants;
import com.nextgis.woody.util.SettingsConstants;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends NGActivity implements NGWLoginFragment.OnAddAccountListener, View.OnClickListener {


    protected MapBase mMap;
    protected boolean mFirstRun = true;

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
                layer.setMaxZoom(GeoConstants.DEFAULT_MAX_ZOOM);
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

                        ngwTable.setName(cityResource.getName());
                        ngwTable.setRemoteId(cityResource.getRemoteId());
                        ngwTable.setAccountName(account.name);
                        ngwTable.setSyncType(com.nextgis.maplib.util.Constants.SYNC_DATA);

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

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentByTag(Constants.FRAGMENT_MAP);

        if (mapFragment == null)
            mapFragment = new MapFragment();

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.map_frame, mapFragment, Constants.FRAGMENT_MAP);
        ft.commit();
    }
}
