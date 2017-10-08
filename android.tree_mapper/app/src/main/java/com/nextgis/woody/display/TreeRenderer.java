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

package com.nextgis.woody.display;


import android.graphics.Color;

import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.display.SimpleFeatureRenderer;
import com.nextgis.maplib.display.SimpleMarkerStyle;
import com.nextgis.maplib.display.Style;
import com.nextgis.maplib.map.Layer;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.woody.util.Constants;

import static com.nextgis.maplib.display.SimpleMarkerStyle.MarkerStyleCircle;

/**
 * Created by bishop on 07.12.16.
 */

public class TreeRenderer extends SimpleFeatureRenderer {

    private long selectedFeature;

    public TreeRenderer(Layer layer) {
        super(layer);
        SimpleMarkerStyle simpleFeatureRenderer = new SimpleMarkerStyle(Color.parseColor("#ff9b9b9b"), Color.parseColor("#329b9b9b"), 10, MarkerStyleCircle);
        simpleFeatureRenderer.setWidth(12);
        mStyle = simpleFeatureRenderer;
        selectedFeature = -1;
    }

    public void setSelectedFeature(long featureId) {
        selectedFeature = featureId;
    }

    @Override
    protected Style getStyle(long featureId) {
        Feature feature = ((VectorLayer) mLayerRef.get()).getFeature(featureId);
        String status = (String) feature.getFieldValue(Constants.KEY_LT_STATE);
        int color, outColor;
        if(featureId == selectedFeature) {
            color = Color.parseColor("#ff039be5");
            outColor = Color.parseColor("#32000000");
        }
        else if(status == null || status.equals("Здоровое")) {
            color = Color.parseColor("#ff59e09a");
            outColor = Color.parseColor("#3259e09a");
        }
        else if(status.equals("Ослабленное")) {
            color = Color.parseColor("#fff0d773");
            outColor = Color.parseColor("#32f0d773");
        }
        else if(status.equals("Сильно ослабленное")) {
            color = Color.parseColor("#fff7bc66");
            outColor = Color.parseColor("#32f7bc66");
        }
        else if(status.equals("Отмирающее")) {
            color = Color.parseColor("#fff76f56");
            outColor = Color.parseColor("#32f76f56");
        }
        else if(status.equals("Сухостой")) {
            color = Color.parseColor("#ff9b9b9b");
            outColor = Color.parseColor("#329b9b9b");
        }
        else {
            color = Color.parseColor("#ff59e09a");
            outColor = Color.parseColor("#3259e09a");
        }
        SimpleMarkerStyle style = (SimpleMarkerStyle) mStyle;
        style.setColor(color);
        style.setOutColor(outColor);
        return style;
    }
}
