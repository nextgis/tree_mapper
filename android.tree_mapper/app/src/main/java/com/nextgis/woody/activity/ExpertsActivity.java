/*
 *  Project:  Woody
 *  Purpose:  Mobile application for trees mapping.
 *  Author:   Stanislav Petriakov, becomeglory@gmail.com
 *  *****************************************************************************
 *  Copyright (c) 2017 NextGIS, info@nextgis.com
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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.nextgis.maplibui.activity.NGActivity;
import com.nextgis.woody.R;

import static com.nextgis.woody.util.Constants.*;

public class ExpertsActivity extends NGActivity {
    EditText girth, height, crown_begin, crown_s, crown_w, crown_n, crown_e, placement, injury;

    @Override
    public int getThemeId() {
        return R.style.AppTheme;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert);
        setToolbar(R.id.main_toolbar);

        girth = findViewById(R.id.girth);
        height = findViewById(R.id.height);
        crown_begin = findViewById(R.id.crown_begin);
        crown_s = findViewById(R.id.crown_s);
        crown_w = findViewById(R.id.crown_w);
        crown_n = findViewById(R.id.crown_n);
        crown_e = findViewById(R.id.crown_e);
        placement = findViewById(R.id.placement);
        injury = findViewById(R.id.injury);

        findViewById(R.id.finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveValues();
            }
        });

        if (getIntent() != null && getIntent().getExtras() != null)
            setValues(getIntent().getExtras());
    }

    @SuppressLint("SetTextI18n")
    private void setValues(Bundle extras) {
        if (extras.containsKey(KEY_LT_GIRTH))
            girth.setText(extras.getDouble(KEY_LT_GIRTH) + "");
        if (extras.containsKey(KEY_LT_HEIGHT))
            height.setText(extras.getString(KEY_LT_HEIGHT));
        if (extras.containsKey(KEY_LT_CROWN_BEG))
            crown_begin.setText(extras.getDouble(KEY_LT_CROWN_BEG) + "");
        if (extras.containsKey(KEY_LT_CROWN_RADS))
            crown_s.setText(extras.getDouble(KEY_LT_CROWN_RADS) + "");
        if (extras.containsKey(KEY_LT_CROWN_RADW))
            crown_w.setText(extras.getDouble(KEY_LT_CROWN_RADW) + "");
        if (extras.containsKey(KEY_LT_CROWN_RADN))
            crown_n.setText(extras.getDouble(KEY_LT_CROWN_RADN) + "");
        if (extras.containsKey(KEY_LT_CROWN_RADE))
            crown_e.setText(extras.getDouble(KEY_LT_CROWN_RADE) + "");
        if (extras.containsKey(KEY_LT_PLACEMENT))
            placement.setText(extras.getString(KEY_LT_PLACEMENT));
        if (extras.containsKey(KEY_LT_INJURY))
            injury.setText(extras.getString(KEY_LT_INJURY));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                saveValues();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveValues() {
        Intent data = new Intent();
        if (!TextUtils.isEmpty(girth.getText().toString()))
            data.putExtra(KEY_LT_GIRTH, Double.parseDouble(girth.getText().toString()));
        if (!TextUtils.isEmpty(height.getText().toString()))
            data.putExtra(KEY_LT_HEIGHT, height.getText().toString());
        if (!TextUtils.isEmpty(crown_begin.getText().toString()))
            data.putExtra(KEY_LT_CROWN_BEG, Double.parseDouble(crown_begin.getText().toString()));
        if (!TextUtils.isEmpty(crown_e.getText().toString()))
            data.putExtra(KEY_LT_CROWN_RADE, Double.parseDouble(crown_e.getText().toString()));
        if (!TextUtils.isEmpty(crown_w.getText().toString()))
            data.putExtra(KEY_LT_CROWN_RADW, Double.parseDouble(crown_w.getText().toString()));
        if (!TextUtils.isEmpty(crown_n.getText().toString()))
            data.putExtra(KEY_LT_CROWN_RADN, Double.parseDouble(crown_n.getText().toString()));
        if (!TextUtils.isEmpty(crown_s.getText().toString()))
            data.putExtra(KEY_LT_CROWN_RADS, Double.parseDouble(crown_s.getText().toString()));
        if (!TextUtils.isEmpty(placement.getText().toString()))
            data.putExtra(KEY_LT_PLACEMENT, placement.getText().toString());
        if (!TextUtils.isEmpty(injury.getText().toString()))
            data.putExtra(KEY_LT_INJURY, injury.getText().toString());
        setResult(RESULT_OK, data);
        finish();
    }
}
