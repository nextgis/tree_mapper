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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplibui.api.IFormControl;
import com.nextgis.maplibui.control.PhotoGallery;
import com.nextgis.woody.R;
import com.nextgis.woody.activity.EditActivity;
import com.nextgis.woody.activity.ExpertsActivity;
import com.nextgis.woody.util.Constants;

import org.json.JSONException;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by bishop on 11.12.16.
 */

public class PhotoFragment extends Fragment {
    public static final int EXPERTS = 123;
    private long mFeatureId;
    private ArrayList<String> mImages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_empty, container, false);
        view.findViewById(R.id.experts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent experts = new Intent(getActivity(), ExpertsActivity.class);
                startActivityForResult(experts, EXPERTS);
            }
        });

        LinearLayout layout = view.findViewById(R.id.photo_holder);
        IFormControl control = (PhotoGallery) getActivity().getLayoutInflater().inflate(com.nextgis.maplibui.R.layout.formtemplate_photo, layout, false);

        MapBase mapBase = MapBase.getInstance();
        VectorLayer layer = (VectorLayer) mapBase.getLayerByName(Constants.KEY_MAIN);
        ((PhotoGallery) control).init(layer, mFeatureId);
        try {
            control.init(null, null, null, null, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mImages != null)
            ((PhotoGallery) control).restoreImages(mImages);

        control.addToLayout(layout);
        return view;
    }

    public void setmFeatureId(long mFeatureId) {
        this.mFeatureId = mFeatureId;
    }

    public void setImages(ArrayList<String> images) {
        mImages = images;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoFragment.EXPERTS && resultCode == RESULT_OK)
            ((EditActivity) getActivity()).putExtras(data);
    }
}
