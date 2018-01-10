/*
 *  Project:  Woody
 *  Purpose:  Mobile application for trees mapping.
 *  Author:   Dmitry Baryshnikov, dmitry.baryshnikov@nextgis.com
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

package com.nextgis.woody.service;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.joshdholtz.sentry.Sentry;
import com.nextgis.maplib.datasource.ngw.SyncAdapter;
import com.nextgis.maplib.map.LayerGroup;

public class WSyncAdapter extends SyncAdapter {
    public WSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public WSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority, ContentProviderClient contentProviderClient, SyncResult syncResult) {
//        Sentry.captureMessage("WSyncAdapter onPerformSync start");
        super.onPerformSync(account, bundle, authority, contentProviderClient, syncResult);
//        Sentry.captureMessage("WSyncAdapter onPerformSync finish");
    }

    protected void sync(LayerGroup layerGroup, String authority, SyncResult syncResult) {
//        Sentry.captureMessage("WSyncAdapter sync start");
        super.sync(layerGroup, authority, syncResult);
//        Sentry.captureMessage("WSyncAdapter sync finish");
    }
}
