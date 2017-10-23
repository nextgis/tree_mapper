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

package com.nextgis.woody.util;

/**
 * Created by bishop on 03.12.16.
 */

public interface SettingsConstants {
    String AUTHORITY = "com.nextgis.woody.provider";
    String SITE_URL = "http://176.9.38.120/tree_mapping";
    String CITY_KEY = "kaliningrad";

    String BASEMAP_NAME = "base map";
//    String BASEMAP_URL = "https://api.mapbox.com/styles/v1/nasnimal/cir3nj27y004kcmkgfnw6u68o/tiles/256/{z}/{x}/{y}/?access_token=pk.eyJ1IjoibmFzbmltYWwiLCJhIjoiY2lvNXcxb29nMDA0YXc2bHkwc2hpNTB2MSJ9.C6eEm-ifqAKsgBIC_5mGZw";
    String BASEMAP_URL = "http://a.tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png";

    String KEY_PREF_USERMINX = "user_minx";
    String KEY_PREF_USERMINY = "user_miny";
    String KEY_PREF_USERMAXX = "user_maxx";
    String KEY_PREF_USERMAXY = "user_maxy";

    String KEY_PREF_SCROLL_X      = "map_scroll_x";
    String KEY_PREF_SCROLL_Y      = "map_scroll_y";
    String KEY_PREF_ZOOM_LEVEL    = "map_zoom_level";

    String KEY_PREF_MAP_FIRST_VIEW = "map_first_view";
}
