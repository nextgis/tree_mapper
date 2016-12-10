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
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.nextgis.maplibui.activity.NGActivity;
import com.nextgis.woody.R;

/**
 * Created by bishop on 11.12.16.
 */

public class EditActivity extends NGActivity implements View.OnClickListener {

    private Button btLeft, btRight;
    private FrameLayout frameLayout;
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
        frameLayout = (FrameLayout) findViewById(R.id.central_frame);

        firstStep();
    }

    private void firstStep() {
        currentStep = 1;
        setTitle(getText(R.string.point_on_map));
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setSubtitle("1/6");

        btLeft.setText(R.string.cancel);
    }

    private void secondStep() {
        currentStep = 2;
        setTitle(getText(R.string.species));
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setSubtitle("2/6");
        btLeft.setText(R.string.back);
    }

    private void thirdStep() {
        currentStep = 3;
        setTitle(getText(R.string.status));
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setSubtitle("3/6");
    }

    private void forthStep() {
        currentStep = 4;
        setTitle(getText(R.string.age));
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setSubtitle("4/6");
    }

    private void fithStep() {
        currentStep = 5;
        setTitle(getText(R.string.year));
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setSubtitle("5/6");
        btRight.setText(R.string.next);
    }

    private void sixStep() {
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
                forthStep();
                break;
            case 4:
                fithStep();
                break;
            case 5:
                sixStep();
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
                forthStep();
                break;
            case 6:
                fithStep();
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
