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

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplibui.api.MapViewEventListener;
import com.nextgis.maplibui.mapui.MapViewOverlays;
import com.nextgis.maplibui.overlay.CurrentLocationOverlay;
import com.nextgis.maplibui.util.ConstantsUI;
import com.nextgis.maplibui.util.SettingsConstantsUI;
import com.nextgis.woody.MainApplication;
import com.nextgis.woody.R;
import com.nextgis.woody.activity.MainActivity;
import com.nextgis.woody.display.TreeRenderer;
import com.nextgis.woody.overlay.SelectLocationOverlay;
import com.nextgis.woody.util.Constants;
import com.nextgis.woody.util.SettingsConstants;

import java.util.List;

import static com.nextgis.maplib.util.GeoConstants.CRS_WEB_MERCATOR;
import static com.nextgis.maplib.util.GeoConstants.CRS_WGS84;

public class MapFragment
        extends Fragment
        implements MapViewEventListener, GpsEventListener, View.OnClickListener {

    protected MainApplication mApp;
    protected MapViewOverlays mMap;
    protected RelativeLayout mMapRelativeLayout;
    protected GpsEventSource mGpsEventSource;
    protected CurrentLocationOverlay mCurrentLocationOverlay;
    protected SelectLocationOverlay mSelectLocationOverlay;
    protected boolean mShowSelectLocation;
    protected Location mLocation;

    protected GeoPoint mCurrentCenter;

    protected float mTolerancePX;

    private int initZoom;
    private GeoPoint initCenter;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        mTolerancePX = getResources().getDisplayMetrics().density * ConstantsUI.TOLERANCE_DP;

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mApp = (MainApplication) getActivity().getApplication();

        mMap = new MapViewOverlays(getActivity(), (MapDrawable) mApp.getMap());
        mMap.setId(R.id.map_view);

        mGpsEventSource = mApp.getGpsEventSource();
        if(mShowSelectLocation)
            mMap.setZoomAndCenter(initZoom, initCenter);

        mCurrentLocationOverlay = new CurrentLocationOverlay(getActivity(), mMap);
        mCurrentLocationOverlay.setStandingMarker(R.drawable.ic_location_standing);
        mCurrentLocationOverlay.setMovingMarker(R.drawable.ic_location_moving);
        mSelectLocationOverlay = new SelectLocationOverlay(getActivity(), mMap);
        mSelectLocationOverlay.setVisibility(mShowSelectLocation);

        if (mLocation != null)
            mSelectLocationOverlay.setSelectedLocation(mLocation);

        mMap.addOverlay(mSelectLocationOverlay);
        mMap.addOverlay(mCurrentLocationOverlay);

        //search relative view of map, if not found - add it
        mMapRelativeLayout = view.findViewById(R.id.maprl);
        addMap();

        view.findViewById(R.id.action_locate).setOnClickListener(this);
        view.findViewById(R.id.action_zoom_in).setOnClickListener(this);
        view.findViewById(R.id.action_zoom_out).setOnClickListener(this);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView()
    {
        if (mMap != null) {
            mMap.removeListener(this);
            if (mMapRelativeLayout != null) {
                mMapRelativeLayout.removeView(mMap);
            }
        }

        super.onDestroyView();
    }

    private void addMap() {
        if (mMapRelativeLayout != null) {
            FrameLayout map = mMapRelativeLayout.findViewById(R.id.mapfl);
            map.addView(mMap, 0, new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));
        }
    }

    @Override
    public void onPause()
    {
        pauseGps();

        final SharedPreferences.Editor edit =
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        if (null != mMap && !mShowSelectLocation) {
            edit.putFloat(SettingsConstants.KEY_PREF_ZOOM_LEVEL, mMap.getZoomLevel());
            GeoPoint point = mMap.getMapCenter();
            edit.putLong(SettingsConstants.KEY_PREF_SCROLL_X, Double.doubleToRawLongBits(point.getX()));
            edit.putLong(SettingsConstants.KEY_PREF_SCROLL_Y, Double.doubleToRawLongBits(point.getY()));

            mMap.removeListener(this);
        }
        edit.apply();

        super.onPause();
    }


    @Override
    public void onResume()
    {
        super.onResume();

        final SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (null != mMap && !mShowSelectLocation) {
            if(prefs.getBoolean(SettingsConstants.KEY_PREF_MAP_FIRST_VIEW, true)){
                // Zoom to trees extent
                MapDrawable md = mMap.getMap();
                ILayer layer = md.getLayerByName(Constants.KEY_MAIN);
                if (layer instanceof VectorLayer) {
                    VectorLayer vectorLayer = (VectorLayer) layer;
                    mMap.zoomToExtent(vectorLayer.getExtents());
                }

                final SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean(SettingsConstants.KEY_PREF_MAP_FIRST_VIEW, false);
                edit.apply();
            }
            else {
                float mMapZoom;
                try {
                    mMapZoom = prefs.getFloat(SettingsConstants.KEY_PREF_ZOOM_LEVEL, mMap.getMinZoom());
                } catch (ClassCastException e) {
                    mMapZoom = mMap.getMinZoom();
                }

                double mMapScrollX;
                double mMapScrollY;
                try {
                    mMapScrollX = Double.longBitsToDouble(prefs.getLong(SettingsConstants.KEY_PREF_SCROLL_X, 0));
                    mMapScrollY = Double.longBitsToDouble(prefs.getLong(SettingsConstants.KEY_PREF_SCROLL_Y, 0));
                } catch (ClassCastException e) {
                    mMapScrollX = 0;
                    mMapScrollY = 0;
                }
                mMap.setZoomAndCenter(mMapZoom, new GeoPoint(mMapScrollX, mMapScrollY));
            }
            mMap.addListener(this);
        }

        resumeGps();

        mCurrentCenter = null;
    }

    public void pauseGps() {
        if (null != mCurrentLocationOverlay)
            mCurrentLocationOverlay.stopShowingCurrentLocation();

        if (null != mGpsEventSource)
            mGpsEventSource.removeListener(this);
    }

    public void resumeGps() {
        if (null != mCurrentLocationOverlay) {
            mCurrentLocationOverlay.updateMode(PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(SettingsConstantsUI.KEY_PREF_SHOW_CURRENT_LOC, "3"));
            mCurrentLocationOverlay.startShowingCurrentLocation();
        }

        if (null != mGpsEventSource)
            mGpsEventSource.addListener(this);
    }

    public void setZoomAndCenter(float zoom, GeoPoint center) {
        mMap.setZoomAndCenter(zoom, center);
    }

    public void setCenter(GeoPoint center) {
        mMap.setZoomAndCenter(mMap.getZoomLevel(), center);
    }

    public GeoPoint getCenter() {
        return mMap.getMapCenter();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (mCurrentCenter == null) {
                mCurrentCenter = new GeoPoint();
            }

            mCurrentCenter.setCoordinates(location.getLongitude(), location.getLatitude());
            mCurrentCenter.setCRS(GeoConstants.CRS_WGS84);

            if (!mCurrentCenter.project(GeoConstants.CRS_WEB_MERCATOR)) {
                mCurrentCenter = null;
            }
        }
    }

    @Override
    public void onBestLocationChanged(Location location) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onLongPress(MotionEvent event) {

    }

    @Override
    public void onSingleTapUp(MotionEvent event) {
        selectGeometryInScreenCoordinates(event.getX(), event.getY());
    }

    public void selectGeometryInScreenCoordinates(float x, float y) {
        double dMinX = x - mTolerancePX;
        double dMaxX = x + mTolerancePX;
        double dMinY = y - mTolerancePX;
        double dMaxY = y + mTolerancePX;
        GeoEnvelope screenEnv = new GeoEnvelope(dMinX, dMaxX, dMinY, dMaxY);

        GeoEnvelope mapEnv = mMap.screenToMap(screenEnv);
        if (null == mapEnv)
            return;

        List<Long> items;
        VectorLayer layerTrees = (VectorLayer) mApp.getMap().getLayerByName(Constants.KEY_MAIN);

        if (null != layerTrees) {
            items = layerTrees.query(mapEnv);
            if (!items.isEmpty()) {
                Feature treeFeature = null;
                for(long featureId : items) {
                    treeFeature = layerTrees.getFeatureWithAttaches(featureId);
                    if(null != treeFeature)
                        break;
                }
                if(treeFeature == null)
                    return;
                TreeRenderer treeRenderer = (TreeRenderer)layerTrees.getRenderer();
                if(treeRenderer != null)
                    treeRenderer.setSelectedFeature(treeFeature.getId());
                MainActivity activity = (MainActivity)getActivity();
                activity.showTreeDetails(treeFeature);
            }
        }
    }

    public void unselectGeometry() {
        MapBase mapBase = MapBase.getInstance();
        if(null != mapBase) {
            VectorLayer layerTrees = (VectorLayer) mapBase.getLayerByName(Constants.KEY_MAIN);
            TreeRenderer treeRenderer = (TreeRenderer) layerTrees.getRenderer();
            treeRenderer.setSelectedFeature(-1);
        }
    }

    @Override
    public void panStart(MotionEvent e) {

    }

    @Override
    public void panMoveTo(MotionEvent e) {

    }

    @Override
    public void panStop() {

    }

    @Override
    public void onLayerAdded(int id) {

    }

    @Override
    public void onLayerDeleted(int id) {

    }

    @Override
    public void onLayerChanged(int id) {

    }

    @Override
    public void onExtentChanged(float zoom, GeoPoint center) {
    }

    @Override
    public void onLayersReordered() {

    }

    @Override
    public void onLayerDrawFinished(int id, float percent) {

    }

    @Override
    public void onLayerDrawStarted() {

    }

    public void locateCurrentPosition()
    {
        if (mCurrentCenter != null) {
            mMap.panTo(mCurrentCenter);
        } else {
            Toast.makeText(getActivity(), R.string.error_no_location, Toast.LENGTH_SHORT).show();
        }
    }

    public void setSelectedLocationVisible(boolean isVisible) {
        mShowSelectLocation = isVisible;

        if (mSelectLocationOverlay != null)
            mSelectLocationOverlay.setVisibility(isVisible);
    }

    public void setZoomAndPosition(int i, GeoPoint pt) {
        initZoom = i;
        initCenter = pt;
    }

    public GeoPoint getSelectedPosition() {
        Location location = mSelectLocationOverlay.getSelectedLocation();
        GeoPoint pt = new GeoPoint(location.getLongitude(), location.getLatitude());
        pt.setCRS(CRS_WGS84);
        pt.project(CRS_WEB_MERCATOR);

        return pt;
    }

    public void setSelectedPosition(GeoPoint geoPoint) {
        geoPoint.project(CRS_WGS84);
        mLocation = new Location(LocationManager.GPS_PROVIDER);
        mLocation.setLatitude(geoPoint.getY());
        mLocation.setLongitude(geoPoint.getX());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.action_zoom_in:
                mMap.zoomIn();
                break;
            case R.id.action_zoom_out:
                mMap.zoomOut();
                break;
            case R.id.action_locate:
                locateCurrentPosition();
                break;
        }
    }
}
