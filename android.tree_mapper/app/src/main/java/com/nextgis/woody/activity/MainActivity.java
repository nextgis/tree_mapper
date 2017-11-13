/*
 * Project:  Woody
 * Purpose:  Mobile application for trees mapping.
 * Author:   Dmitry Baryshnikov, dmitry.baryshnikov@nextgis.com
 * Author:   Stanislav Petriakov, becomeglory@gmail.com
 * *****************************************************************************
 * Copyright (c) 2016-2017 NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.woody.activity;

import android.Manifest;
import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.joshdholtz.sentry.Sentry;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.api.ILayerView;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.datasource.ngw.Connection;
import com.nextgis.maplib.datasource.ngw.INGWResource;
import com.nextgis.maplib.datasource.ngw.Resource;
import com.nextgis.maplib.datasource.ngw.ResourceGroup;
import com.nextgis.maplib.display.TMSRenderer;
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
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import static com.nextgis.maplib.util.Constants.NOT_FOUND;
import static com.nextgis.maplibui.util.SettingsConstantsUI.KEY_PREF_SYNC_PERIOD;

public class MainActivity extends NGActivity implements NGWLoginFragment.OnAddAccountListener, View.OnClickListener {
    protected final static int PERMISSIONS_REQUEST = 1;
    protected final static int EDIT_REQUEST = 25;

    private static final int VK_SIGN_IN = 10485;
    private static final int FB_SIGN_IN = 64206;

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
            case R.id.permissioons:
                checkPermissions();
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
        if (mapFragment != null) {
            GeoPoint pt = mapFragment.getCenter();
            if (i == NOT_FOUND && mapFragment.getCurrentLocation() != null)
                pt = mapFragment.getCurrentLocation();
            intent.putExtra(SettingsConstants.KEY_PREF_SCROLL_X, pt.getX());
            intent.putExtra(SettingsConstants.KEY_PREF_SCROLL_Y, pt.getY());
        }
        startActivityForResult(intent, EDIT_REQUEST);
    }

    @Override
    public void onAddAccount(Account account, String token, boolean accountAdded) {
        Log.d(Constants.WTAG, "No account. " + Constants.ACCOUNT_NAME + " created. Run first step.");

        if(accountAdded) {
            // TODO: final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            final MainApplication app = (MainApplication) getApplication();
            app.setUserData(account.name, Constants.KEY_IS_AUTHORIZED, token);
            long interval = com.nextgis.maplib.util.Constants.DEFAULT_SYNC_PERIOD;
            Bundle bundle = new Bundle();
            bundle.putString(KEY_PREF_SYNC_PERIOD, interval + "");

            //set sync with server
            ContentResolver.setSyncAutomatically(account, app.getAuthority(), true);
            ContentResolver.addPeriodicSync(account, app.getAuthority(), bundle, interval);

            // goto step 2
            refreshActivityView();
        } else
            Toast.makeText(this, R.string.error_init, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createPermissionsView();
    }

    private void start() {
        final MainApplication app = (MainApplication) getApplication();
        mMap = app.getMap();
        final Account account = app.getAccount(Constants.ACCOUNT_NAME);
        // check if has safe forest account
        if (account == null) {
            Sentry.captureMessage("MainActivity account is null");
            createAccountView();
        } else {
            // check basic layers
            if (!hasBasicLayers(app.getMap())) {
                Sentry.captureMessage("MainActivity create basic layers");
                Log.d(Constants.WTAG, "Account " + Constants.ACCOUNT_NAME + " created. Run second step.");
                createFirstRunView();
            } else {
                Sentry.captureMessage("MainActivity start main view");
                Log.d(Constants.WTAG, "Account " + Constants.ACCOUNT_NAME + " created. Layers created. Run normal view.");
                mFirstRun = false;
                createNormalView();
            }
        }
    }

    @Override
    protected void setToolbar(int toolbarId){
        Toolbar toolbar = findViewById(toolbarId);
        toolbar.getBackground().setAlpha(getToolbarAlpha());
        setSupportActionBar(toolbar);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                if (isGrantResultsOk(grantResults))
                    start();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean isGrantResultsOk(int[] grantResults) {
        if (grantResults.length == 4) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED)
                    return false;
            }

            return true;
        }
        return false;
    }

    protected void createPermissionsView() {
        setContentView(R.layout.activity_main_first);
        findViewById(R.id.permissioons).setOnClickListener(this);
        setToolbar(R.id.main_toolbar);
        setTitle(getText(R.string.first_run));
        checkPermissions();
    }

    private void checkPermissions() {
        // Alert for Android 6 or higher
        if (!hasPermissions()) {
            String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.GET_ACCOUNTS,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(R.string.message_important, R.string.message_need_permissions, PERMISSIONS_REQUEST, permissions);
        } else
            start();
    }

    protected void createAccountView() {
        Sentry.captureMessage("MainActivity create account view");
        final MainApplication app = (MainApplication) getApplication();
        final Account account = app.getAccount(Constants.ACCOUNT_NAME);
        if (account != null) {
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        LoginFragment ngwLoginFragment = (LoginFragment) fm.findFragmentByTag(Constants.FRAGMENT_LOGIN);

        if (ngwLoginFragment == null) {
            ngwLoginFragment = new LoginFragment();
            Sentry.captureMessage("MainActivity create new login fragment");
        }

        FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getFBUserData(loginResult);
                Sentry.captureMessage("MainActivity FB success");
            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, R.string.canceled, Toast.LENGTH_SHORT).show();
                Sentry.captureMessage("MainActivity FB cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(MainActivity.this, R.string.error_auth, Toast.LENGTH_SHORT).show();
                Sentry.captureMessage("MainActivity FB error: " + exception.getMessage());
            }
        };

        ngwLoginFragment.setCallback(callback);
        ((LinearLayout) findViewById(R.id.login_frame)).removeAllViews();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.login_frame, ngwLoginFragment, Constants.FRAGMENT_LOGIN);
        ft.commitAllowingStateLoss();
        ngwLoginFragment.setForNewAccount(true);
        ngwLoginFragment.setOnAddAccountListener(this);
        ngwLoginFragment.setUrlText(SettingsConstants.SITE_URL);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case VK_SIGN_IN:
                handleVKResult(requestCode, resultCode, data);
                break;
            case FB_SIGN_IN:
                handleFBResult(requestCode, resultCode, data);
                break;
            case EDIT_REQUEST:
                if (resultCode == RESULT_OK)
                    updateTreeView();
                break;
        }
    }

    private void updateTreeView() {
        FragmentManager fm = getSupportFragmentManager();
        TreeDetailsFragment treeDetailsFragment = (TreeDetailsFragment) fm.findFragmentByTag(Constants.FRAGMENT_TREE_DETAILS);
        if (treeDetailsFragment != null)
            treeDetailsFragment.refill();
    }

    private void handleFBResult(int requestCode, int resultCode, Intent data) {
        FragmentManager fm = getSupportFragmentManager();
        LoginFragment ngwLoginFragment = (LoginFragment) fm.findFragmentByTag(Constants.FRAGMENT_LOGIN);
        if (ngwLoginFragment != null) {
            CallbackManager callbackManager = ngwLoginFragment.getCallbackManager();
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleVKResult(int requestCode, int resultCode, Intent data) {
        VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                getVKUserData(res.email);
                Sentry.captureMessage("MainActivity VK success");
            }

            @Override
            public void onError(VKError error) {
                int message = R.string.error_auth;
                if (error.errorCode == VKError.VK_CANCELED) {
                    message = R.string.canceled;
                    Sentry.captureMessage("MainActivity VK cancel");
                } else {
                    Sentry.captureMessage("MainActivity VK error: " + error.errorMessage);
                }

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFBUserData(LoginResult loginResult) {
        AccessToken token = loginResult.getAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject user, GraphResponse response) {
                Sentry.captureMessage("MainActivity FB request completed");
                String email = user.optString("email");
                String name = user.optString("name");
                signup(name, email);
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void getVKUserData(final String email) {
        VKApi.users().get().executeWithListener(new VKRequest.VKRequestListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onComplete(VKResponse response) {
                Sentry.captureMessage("MainActivity VK request completed");
                VKApiUser user = ((VKList<VKApiUser>)response.parsedModel).get(0);
                signup(user.first_name + " " + user.last_name, email);
            }
        });
    }

    private void signup(String displayName, String email) {
        FragmentManager fm = getSupportFragmentManager();
        LoginFragment ngwLoginFragment = (LoginFragment) fm.findFragmentByTag(Constants.FRAGMENT_LOGIN);
        if (ngwLoginFragment != null) {
            String login = genUser();
            String password = genPass();
            ngwLoginFragment.signup(login, password, displayName, email);
        }
    }

    public static String genUser() {
        return "user" + System.currentTimeMillis();
    }

    // https://stackoverflow.com/a/5683362/2088273
    private String genPass() {
        char[] chars = "xgl5iNf0PUdTBIkVAcWLhO7jKumzRFSQets9JoHrGvpwb8na3C1XE6ZM42YqyD".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    protected boolean hasPermissions() {
        return isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) &&
                isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                isPermissionGranted(Manifest.permission.GET_ACCOUNTS) &&
                isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
                Sentry.captureMessage("MainActivity load data from " + URL + " for user " + sLogin);

                if (TextUtils.isEmpty(URL) || TextUtils.isEmpty(sLogin)) {
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

                TMSRenderer tmsRenderer  = (TMSRenderer) layer.getRenderer();
                tmsRenderer.setContrastBrightness(1, 0, true);
//                layer.setRenderer(tmsRenderer);

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
                if (mProgressDialog.isShowing() && !MainActivity.this.isFinishing())
                    dismissWithExceptionHandling(mProgressDialog);
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
                refreshActivityView();
            }
        }

        new DownloadTask().execute(account);
    }

    public void dismissWithExceptionHandling(ProgressDialog dialog) {
        try {
            dialog.dismiss();
        } catch (final IllegalArgumentException ignored) {
        } catch (final Exception ignored) {}
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
        FrameLayout fl = findViewById(R.id.tree_details);
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

    @Override
    protected void onResume() {
        super.onResume();

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentByTag(Constants.FRAGMENT_MAP);
        FragmentTransaction ft = fm.beginTransaction();
        ft.detach(mapFragment);
        ft.attach(mapFragment);
        ft.commit();
    }
}
