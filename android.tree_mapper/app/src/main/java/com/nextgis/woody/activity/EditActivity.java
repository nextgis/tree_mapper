/*
 *  Project:  Woody
 *  Purpose:  Mobile application for trees mapping.
 *  Author:   Dmitry Baryshnikov, dmitry.baryshnikov@nextgis.com
 *  *****************************************************************************
 *  Copyright (c) 2016 NextGIS, info@nextgis.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.woody.activity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplib.map.NGWVectorLayer;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.MapUtil;
import com.nextgis.maplibui.activity.NGActivity;
import com.nextgis.maplibui.api.IFormControl;
import com.nextgis.maplibui.control.PhotoGallery;
import com.nextgis.woody.R;
import com.nextgis.woody.fragment.ListViewFragment;
import com.nextgis.woody.fragment.MapFragment;
import com.nextgis.woody.fragment.PhotoFragment;
import com.nextgis.woody.util.Constants;

import org.json.JSONException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static com.nextgis.maplib.util.Constants.NOT_FOUND;
import static com.nextgis.maplib.util.Constants.TAG;
import static com.nextgis.woody.util.Constants.WTAG;

/**
 * Created by bishop on 11.12.16.
 */

public class EditActivity extends NGActivity implements View.OnClickListener {

    private Button btLeft, btRight;
    private char currentStep;
    private ContentValues values;
    private long mFeatureId;

    @Override
    public int getThemeId() {
        return R.style.AppTheme;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);
        setToolbar(R.id.main_toolbar);

        btLeft = (Button) findViewById(R.id.left_button);
        btLeft.setOnClickListener(this);
        btRight = (Button) findViewById(R.id.right_button);
        btRight.setOnClickListener(this);

        values = new ContentValues();

        Intent intent = this.getIntent();
        mFeatureId = intent.getLongExtra(Constants.FEATURE_ID, NOT_FOUND);

        if(NOT_FOUND != mFeatureId) {
            MapBase mapBase = MapBase.getInstance();
            NGWVectorLayer vectorLayer = (NGWVectorLayer) mapBase.getLayerByName(Constants.KEY_MAIN);
            Feature feature = vectorLayer.getFeature(mFeatureId);
            values = feature.getContentValues(true);
        }
        firstStep();
    }

    private void firstStep() {
        currentStep = 1;
        setTitle(getText(R.string.point_on_map));
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setSubtitle("1/6");

        btLeft.setText(R.string.cancel);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentByTag(Constants.FRAGMENT_MAP);

        if (mapFragment == null)
            mapFragment = new MapFragment();

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.central_frame, mapFragment, Constants.FRAGMENT_MAP);
        ft.commit();
    }

    private void secondStep() {
        currentStep = 2;
        setTitle(getText(R.string.species));
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setSubtitle("2/6");
        btLeft.setText(R.string.back);

        FragmentManager fm = getSupportFragmentManager();
        ListViewFragment lvFragment = (ListViewFragment) fm.findFragmentByTag(Constants.FRAGMENT_LISTVIEW);

        if (lvFragment == null)
            lvFragment = new ListViewFragment();

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.central_frame, lvFragment, Constants.FRAGMENT_LISTVIEW);
        ft.commit();

        MapBase mapBase = MapBase.getInstance();
        NGWLookupTable table = (NGWLookupTable) mapBase.getLayerByName(Constants.KEY_LT_SPECIES);
        Map<String, String> data = table.getData();
        lvFragment.fill(data.values(), (String) values.get(Constants.KEY_LT_SPECIES));
    }

    private void thirdStep() {
        currentStep = 3;
        setTitle(getText(R.string.status));
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setSubtitle("3/6");

        FragmentManager fm = getSupportFragmentManager();
        ListViewFragment lvFragment = (ListViewFragment) fm.findFragmentByTag(Constants.FRAGMENT_LISTVIEW);
        MapBase mapBase = MapBase.getInstance();
        NGWLookupTable table = (NGWLookupTable) mapBase.getLayerByName(Constants.KEY_LT_STATE);
        Map<String, String> data = table.getData();
        lvFragment.fill(data.values(), (String) values.get(Constants.KEY_LT_STATE));
    }

    private void fourthStep() {
        currentStep = 4;
        setTitle(getText(R.string.age));
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setSubtitle("4/6");

        FragmentManager fm = getSupportFragmentManager();
        ListViewFragment lvFragment = (ListViewFragment) fm.findFragmentByTag(Constants.FRAGMENT_LISTVIEW);
        MapBase mapBase = MapBase.getInstance();
        NGWLookupTable table = (NGWLookupTable) mapBase.getLayerByName(Constants.KEY_LT_AGE);
        Map<String, String> data = table.getData();
        lvFragment.fill(data.values(), (String) values.get(Constants.KEY_LT_AGE));
    }

    private void fifthStep() {
        currentStep = 5;
        setTitle(getText(R.string.year));
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setSubtitle("5/6");
        btRight.setText(R.string.next);

        FragmentManager fm = getSupportFragmentManager();
        ListViewFragment lvFragment = (ListViewFragment) fm.findFragmentByTag(Constants.FRAGMENT_LISTVIEW);

        if (lvFragment == null)
            lvFragment = new ListViewFragment();

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.central_frame, lvFragment, Constants.FRAGMENT_LISTVIEW);
        ft.commit();

        MapBase mapBase = MapBase.getInstance();
        NGWLookupTable table = (NGWLookupTable) mapBase.getLayerByName(Constants.KEY_LT_YEAR);
        Map<String, String> data = table.getData();
        lvFragment.fill(data.values(), (String) values.get(Constants.KEY_LT_YEAR));
    }

    private void sixthStep() {
        currentStep = 6;
        setTitle(getText(R.string.photo));
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setSubtitle("6/6");
        btRight.setText(R.string.finish);

        FragmentManager fm = getSupportFragmentManager();
        PhotoFragment photoFragment = (PhotoFragment) fm.findFragmentByTag(Constants.FRAGMENT_PHOTO);

        if (photoFragment == null)
            photoFragment = new PhotoFragment();

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.central_frame, photoFragment, Constants.FRAGMENT_PHOTO);
        ft.commit();

        photoFragment.setmFeatureId(mFeatureId);
    }

    private void onNext() {
        switch (currentStep) {
            case 1:
                saveCoordinates();
                secondStep();
                break;
            case 2:
                saveLookUpValue(Constants.KEY_LT_SPECIES);
                thirdStep();
                break;
            case 3:
                saveLookUpValue(Constants.KEY_LT_STATE);
                fourthStep();
                break;
            case 4:
                saveLookUpValue(Constants.KEY_LT_AGE);
                fifthStep();
                break;
            case 5:
                saveLookUpValue(Constants.KEY_LT_YEAR);
                sixthStep();
                break;
            case 6:
                save();
                break;
        }
    }

    private void onPrevious() {
        switch (currentStep) {
            case 1:
                finish();
                break;
            case 2:
                saveLookUpValue(Constants.KEY_LT_SPECIES);
                firstStep();
                break;
            case 3:
                saveLookUpValue(Constants.KEY_LT_STATE);
                secondStep();
                break;
            case 4:
                saveLookUpValue(Constants.KEY_LT_AGE);
                thirdStep();
                break;
            case 5:
                saveLookUpValue(Constants.KEY_LT_YEAR);
                fourthStep();
                break;
            case 6:
                fifthStep();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_button:
                onPrevious();
                break;
            case R.id.right_button:
                onNext();
                break;
        }
    }

    private void save() {

        // create or change feature
        IGISApplication app = (IGISApplication) getApplication();

        if (null == app) {
            throw new IllegalArgumentException("Not a IGISApplication");
        }

        Uri uri = Uri.parse(
                "content://" + app.getAuthority() + "/" + Constants.KEY_MAIN);

        if (mFeatureId == NOT_FOUND) {
            // we need to get proper mFeatureId for new features first
            Uri result = getContentResolver().insert(uri, values);
            if (result == null) {
                Toast.makeText(this, getText(com.nextgis.maplibui.R.string.error_db_insert), Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                mFeatureId = Long.parseLong(result.getLastPathSegment());
            }
        }
        else {
            Uri updateUri = ContentUris.withAppendedId(uri, mFeatureId);
            boolean valuesUpdated = getContentResolver().update(updateUri, values, null, null) == 1;
            if (!valuesUpdated) {
                Toast.makeText(this, getText(com.nextgis.maplibui.R.string.error_db_update), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // add photos
        putAttaches();

        finish();
    }

    private void saveCoordinates() {

    }

    private void saveLookUpValue(String key) {
        FragmentManager fm = getSupportFragmentManager();
        ListViewFragment lvFragment = (ListViewFragment) fm.findFragmentByTag(Constants.FRAGMENT_LISTVIEW);

        values.put(key, lvFragment.getSelection());
    }

    private void putAttaches() {
        PhotoGallery gallery = (PhotoGallery) findViewById(com.nextgis.maplibui.R.id.pg_photos);

        if (gallery != null && mFeatureId != NOT_FOUND) {
            List<Integer> deletedAttaches = gallery.getDeletedAttaches();
            IGISApplication application = (IGISApplication) getApplication();
            Uri uri = Uri.parse("content://" + application.getAuthority() + "/" +
                    Constants.KEY_MAIN + "/" + mFeatureId + "/" + com.nextgis.maplib.util.Constants.URI_ATTACH);

            int size = deletedAttaches.size();
            String[] args = new String[size];
            for (int i = 0; i < size; i++)
                args[i] = deletedAttaches.get(i).toString();

            if (size > 0)
                getContentResolver().delete(uri, MapUtil.makePlaceholders(size), args);

            List<String> imagesPath =  gallery.getNewAttaches();
            for (String path : imagesPath) {
                String[] segments = path.split("/");
                String name = segments.length > 0 ? segments[segments.length - 1] : "image.jpg";
                ContentValues values = new ContentValues();
                values.put(VectorLayer.ATTACH_DISPLAY_NAME, name);
                values.put(VectorLayer.ATTACH_MIME_TYPE, "image/jpeg");

                Uri result = getContentResolver().insert(uri, values);
                if (result == null) {
                    Toast.makeText(this, getText(com.nextgis.maplibui.R.string.photo_fail_attach), Toast.LENGTH_SHORT).show();
                    Log.d(WTAG, "attach insert failed");
                } else {
                    copyToStream(result, path);
                    Log.d(WTAG, "attach insert success: " + result.toString());
                }
            }
        }
    }

    private void copyToStream(Uri uri, String path) {
        try {
            OutputStream outStream = getContentResolver().openOutputStream(uri);

            if (outStream != null) {
                InputStream inStream = new FileInputStream(path);
                byte[] buffer = new byte[8192];
                int counter;

                while ((counter = inStream.read(buffer, 0, buffer.length)) > 0) {
                    outStream.write(buffer, 0, counter);
                    outStream.flush();
                }

                outStream.close();
                inStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        PhotoGallery gallery = (PhotoGallery) findViewById(com.nextgis.maplibui.R.id.pg_photos);
        if (gallery != null)
            gallery.onActivityResult(requestCode, resultCode, data);
    }
}
