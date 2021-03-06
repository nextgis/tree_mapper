/*
 *  Project:  Woody
 *  Purpose:  Mobile application for trees mapping.
 *  Author:   Dmitry Baryshnikov, dmitry.baryshnikov@nextgis.com
 *  Author:   Stanislav Petriakov, becomeglory@gmail.com
 *  *****************************************************************************
 *  Copyright (c) 2016-2017 NextGIS, info@nextgis.com
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.keenfin.easypicker.PhotoPicker;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.AttachItem;
import com.nextgis.maplibui.control.PhotoGallery;
import com.nextgis.woody.MainApplication;
import com.nextgis.woody.R;
import com.nextgis.woody.activity.MainActivity;
import com.nextgis.woody.util.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
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
    private TextView tvYear;
    private Feature currentFeature;
    private PhotoGallery gallery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tree_details, container, false);
        tvSpecies = view.findViewById(R.id.species);
        tvCoordinates = view.findViewById(R.id.coordinates);
        tvGirth = view.findViewById(R.id.girth);
        tvAge = view.findViewById(R.id.age);
        tvState = view.findViewById(R.id.state);
        tvYear = view.findViewById(R.id.year);
        tvHeight = view.findViewById(R.id.height);
        gallery = view.findViewById(R.id.photo_gallery);

        view.findViewById(R.id.close_action).setOnClickListener(this);
        view.findViewById(R.id.delete_action).setOnClickListener(this);
        view.findViewById(R.id.edit_action).setOnClickListener(this);

        currentFeature = null;

        return view;
    }

    public void fill(Feature feature) {
        currentFeature = feature;

        tvSpecies.setText(feature.getFieldValueAsString(Constants.KEY_LT_SPECIES));
        GeoPoint pt = (GeoPoint) feature.getGeometry().copy();
        pt.project(4326);
        tvCoordinates.setText(String.format(Locale.UK, "%.6f, %.6f", pt.getY(), pt.getX()));

        Map<String, AttachItem> attaches = feature.getAttachments();
        if (attaches.size() > 0) {
            PhotoPicker.PhotoAdapter adapter = gallery.new PhotoAdapter(true);
            gallery.setAdapter(adapter);

            MapBase mapBase = MapBase.getInstance();
            ILayer layer = mapBase.getLayerByName(Constants.KEY_MAIN);

            final ArrayList<String> paths = new ArrayList<>();
            File attachFolder = new File(layer.getPath(), Long.toString(feature.getId()));
            for (String key : attaches.keySet()) {
                File attachFile = new File(attachFolder, key);
                paths.add(attachFile.getPath());
            }

            gallery.post(new Runnable() {
                @Override
                public void run() {
                    gallery.restoreImages(paths);
                }
            });
        } else
            gallery.setVisibility(View.GONE);

        int id = feature.getFieldValueIndex(Constants.KEY_LT_GIRTH);
        ((RelativeLayout) tvGirth.getParent().getParent()).setVisibility(feature.isValuePresent(id) ? View.VISIBLE : View.GONE);
        tvGirth.setText(feature.getFieldValueAsString(Constants.KEY_LT_GIRTH));
        id = feature.getFieldValueIndex(Constants.KEY_LT_AGE);
        ((RelativeLayout) tvAge.getParent().getParent()).setVisibility(feature.isValuePresent(id) ? View.VISIBLE : View.GONE);
        tvAge.setText(feature.getFieldValueAsString(Constants.KEY_LT_AGE));
        id = feature.getFieldValueIndex(Constants.KEY_LT_STATE);
        ((RelativeLayout) tvState.getParent().getParent()).setVisibility(feature.isValuePresent(id) ? View.VISIBLE : View.GONE);
        tvState.setText(feature.getFieldValueAsString(Constants.KEY_LT_STATE));
        id = feature.getFieldValueIndex(Constants.KEY_LT_HEIGHT);
        ((RelativeLayout) tvHeight.getParent().getParent()).setVisibility(feature.isValuePresent(id) ? View.VISIBLE : View.GONE);
        tvHeight.setText(feature.getFieldValueAsString(Constants.KEY_LT_HEIGHT));
        id = feature.getFieldValueIndex(Constants.KEY_LT_YEAR);
        ((RelativeLayout) tvYear.getParent().getParent()).setVisibility(feature.isValuePresent(id) ? View.VISIBLE : View.GONE);
        tvYear.setText(feature.getFieldValueAsString(Constants.KEY_LT_YEAR));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete_action:
                MapBase mapBase = MapBase.getInstance();
                ILayer layer = mapBase.getLayerByName(Constants.KEY_MAIN);
                if (layer != null) {
                    VectorLayer vectorLayer = (VectorLayer) layer;
                    vectorLayer.deleteAddChanges(currentFeature.getId());
                    currentFeature = null;
                    close();
                }
                break;
            case R.id.edit_action:
                edit();
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

    private void edit() {
        if (null == currentFeature)
            return;
        MainActivity activity = (MainActivity) getActivity();
        activity.editTree(currentFeature.getId());
    }

    public void refill() {
        IGISApplication app = (MainApplication) getActivity().getApplication();
        VectorLayer layerTrees = (VectorLayer) app.getMap().getLayerByName(Constants.KEY_MAIN);

        if (null != layerTrees) {
            Feature treeFeature = layerTrees.getFeatureWithAttaches(currentFeature.getId());
            if (null != treeFeature) {
                currentFeature = treeFeature;
                fill(currentFeature);
            }
        }
    }
}
