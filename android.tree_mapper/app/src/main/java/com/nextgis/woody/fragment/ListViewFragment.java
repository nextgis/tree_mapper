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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nextgis.woody.R;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by bishop on 11.12.16.
 */

public class ListViewFragment extends Fragment {

    private ListView listView;
    private ArrayList<String> listContent;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        listView = (ListView) view.findViewById(R.id.list_view);
        if(listContent != null) {
            setAdapter();
        }
        return view;
    }

    public void fill(Collection<String> data) {
        listContent = new ArrayList<>(data);
        setAdapter();
    }

    private void setAdapter() {
        if(listView != null) {
            ArrayAdapter<String> adapter;
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB){
                adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_list_item_activated_1, listContent );
            }
            else{
                adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_list_item_single_choice, listContent );
            }

            listView.setAdapter(adapter);

            listView.setItemChecked(0, true);
            adapter.notifyDataSetChanged();
        }
    }
}
