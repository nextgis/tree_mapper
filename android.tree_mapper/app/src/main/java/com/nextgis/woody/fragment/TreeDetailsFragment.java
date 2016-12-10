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

package com.nextgis.woody.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.keenfin.easypicker.PhotoPicker;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.AttachItem;
import com.nextgis.maplibui.util.ControlHelper;
import com.nextgis.woody.R;
import com.nextgis.woody.activity.MainActivity;
import com.nextgis.woody.util.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by bishop on 10.12.16.
 */

public class TreeDetailsFragment extends Fragment implements View.OnClickListener {

    private TextView tvSpecies;
    private TextView tvCoordinates;
    private TextView tvGirth;
    private TextView tvAge;
    private TextView tvState;
    private TextView tvHeight;
    private Feature currentFeature;
    private FrameLayout frameLayout;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_tree_details, container, false);
        tvSpecies = (TextView) view.findViewById(R.id.species);
        tvCoordinates = (TextView) view.findViewById(R.id.coordinates);
        tvGirth = (TextView) view.findViewById(R.id.girth);
        tvAge = (TextView) view.findViewById(R.id.age);
        tvState = (TextView) view.findViewById(R.id.state);
        tvHeight = (TextView) view.findViewById(R.id.height);

        view.findViewById(R.id.close_action).setOnClickListener(this);
        view.findViewById(R.id.delete_action).setOnClickListener(this);
        view.findViewById(R.id.edit_action).setOnClickListener(this);

        frameLayout = (FrameLayout) view.findViewById(R.id.photo_gallery);

        currentFeature = null;

        return view;
    }

    public void fill(Feature feature) {
        currentFeature = feature;

        tvSpecies.setText(feature.getFieldValueAsString(Constants.KEY_LT_SPECIES));
        GeoPoint pt = (GeoPoint) feature.getGeometry().copy();
        pt.project(4326);
        tvCoordinates.setText( String.format( "%.6f, %.6f", pt.getY(), pt.getX()) );

        Map<String, AttachItem> attaches = feature.getAttachments();
        if (attaches.size() > 0) {
            final ArrayList<String> paths = new ArrayList<>();
            MapBase mapBase = MapBase.getInstance();
            ILayer layer = mapBase.getLayerByName(Constants.KEY_MAIN);

            File attachFolder = new File(layer.getPath(), Long.toString(feature.getId()));
            for(String key : attaches.keySet()) {
                File attachFile = new File(attachFolder, key);
                paths.add(attachFile.getPath());
            }
            final PhotoPicker gallery = new PhotoPicker(getActivity(), true);
            int px = ControlHelper.dpToPx(16, getResources());
            gallery.setDefaultPreview(true);
            gallery.setPadding(px, 0, px, 0);
            gallery.post(new Runnable() {
                @Override
                public void run() {
                    gallery.restoreImages(paths);
                }
            });

            frameLayout.addView(gallery);
        }
        else {
            frameLayout.removeAllViews();
        }


        tvGirth.setText(feature.getFieldValueAsString(Constants.KEY_LT_GIRTH));
        tvAge.setText(feature.getFieldValueAsString(Constants.KEY_LT_AGE));
        tvState.setText(feature.getFieldValueAsString(Constants.KEY_LT_STATE));
        tvHeight.setText(feature.getFieldValueAsString(Constants.KEY_LT_HEIGHT));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_action:
                MapBase mapBase = MapBase.getInstance();
                ILayer layer = mapBase.getLayerByName(Constants.KEY_MAIN);
                if(layer != null) {
                    VectorLayer vectorLayer = (VectorLayer) layer;
                    vectorLayer.deleteAddChanges(currentFeature.getId());
                    currentFeature = null;
                    close();
                }
                break;
            case R.id.edit_action:
                // TODO: start edit activity
                break;
            case R.id.close_action:
                close();
                break;
        }
    }

    private void close() {
        MainActivity activity = (MainActivity) getActivity();
        activity.hideTreeDetails();
    }
}
