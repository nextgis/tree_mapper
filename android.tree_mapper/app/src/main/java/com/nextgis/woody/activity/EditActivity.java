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

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.NGWLookupTable;
import com.nextgis.maplibui.activity.NGActivity;
import com.nextgis.woody.R;
import com.nextgis.woody.fragment.ListViewFragment;
import com.nextgis.woody.fragment.MapFragment;
import com.nextgis.woody.util.Constants;

import java.util.Map;

/**
 * Created by bishop on 11.12.16.
 */

public class EditActivity extends NGActivity implements View.OnClickListener {

    private Button btLeft, btRight;
    private char currentStep;

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
        lvFragment.fill(data.values());
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
        lvFragment.fill(data.values());
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
        lvFragment.fill(data.values());
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
        lvFragment.fill(data.values());
    }

    private void sixthStep() {
        currentStep = 6;
        setTitle(getText(R.string.photo));
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setSubtitle("6/6");
        btRight.setText(R.string.finish);

    }

    private void onNext() {
        switch (currentStep) {
            case 1:
                secondStep();
                break;
            case 2:
                thirdStep();
                break;
            case 3:
                fourthStep();
                break;
            case 4:
                fifthStep();
                break;
            case 5:
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
                firstStep();
                break;
            case 3:
                secondStep();
                break;
            case 4:
                thirdStep();
                break;
            case 5:
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
        // add photos

        finish();
    }
}
