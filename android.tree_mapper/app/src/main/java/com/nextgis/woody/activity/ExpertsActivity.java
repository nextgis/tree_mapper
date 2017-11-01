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

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.nextgis.maplibui.activity.NGActivity;
import com.nextgis.woody.R;

import static com.nextgis.woody.util.Constants.*;

public class ExpertsActivity extends NGActivity {
    @Override
    public int getThemeId() {
        return R.style.AppTheme;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert);
        setToolbar(R.id.main_toolbar);
        findViewById(R.id.finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveValues();
            }
        });
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
        EditText girth = findViewById(R.id.girth);
        EditText height = findViewById(R.id.height);
        EditText crown_begin = findViewById(R.id.crown_begin);
        EditText crown_s = findViewById(R.id.crown_s);
        EditText crown_w = findViewById(R.id.crown_w);
        EditText crown_n = findViewById(R.id.crown_n);
        EditText crown_e = findViewById(R.id.crown_e);
        EditText placement = findViewById(R.id.placement);
        EditText injury = findViewById(R.id.injury);
        Intent data = new Intent();
        data.putExtra(KEY_LT_GIRTH, girth.getText().toString());
        data.putExtra(KEY_LT_HEIGHT, height.getText().toString());
        data.putExtra(KEY_LT_CROWN_BEG, crown_begin.getText().toString());
        data.putExtra(KEY_LT_CROWN_RADE, crown_e.getText().toString());
        data.putExtra(KEY_LT_CROWN_RADW, crown_w.getText().toString());
        data.putExtra(KEY_LT_CROWN_RADN, crown_n.getText().toString());
        data.putExtra(KEY_LT_CROWN_RADS, crown_s.getText().toString());
        data.putExtra(KEY_LT_PLACEMENT, placement.getText().toString());
        data.putExtra(KEY_LT_INJURY, injury.getText().toString());
        setResult(RESULT_OK, data);
        finish();
    }
}
