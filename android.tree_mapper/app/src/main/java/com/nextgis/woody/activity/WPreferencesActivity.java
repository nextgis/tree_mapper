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

import com.nextgis.maplibui.activity.NGPreferenceActivity;
import com.nextgis.maplibui.fragment.NGPreferenceHeaderFragment;
import com.nextgis.maplibui.fragment.NGPreferenceSettingsFragment;

/**
 * Created by bishop on 03.12.16.
 */

public class WPreferencesActivity extends NGPreferenceActivity {
    @Override
    protected String getPreferenceHeaderFragmentTag() {
        return null;
    }

    @Override
    protected NGPreferenceHeaderFragment getNewPreferenceHeaderFragment() {
        return null;
    }

    @Override
    protected String getPreferenceSettingsFragmentTag() {
        return null;
    }

    @Override
    protected NGPreferenceSettingsFragment getNewPreferenceSettingsFragment(String subScreenKey) {
        return null;
    }

    @Override
    public String getTitleString() {
        return null;
    }
}
