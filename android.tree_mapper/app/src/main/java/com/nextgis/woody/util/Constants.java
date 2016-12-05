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

import java.sql.Struct;

/**
 * Created by bishop on 03.12.16.
 */

public interface Constants {
    String ACCOUNT_NAME                 = "Woody";
    String ANONYMOUS                    = "anonymous";

    String FRAGMENT_LOGIN            = "NGWLogin";
    String FRAGMENT_ACCOUNT          = "woody_account";

    String PASSWORD_PATTERN = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$";
    String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    String WTAG = "woody";
    String KEY_IS_AUTHORIZED = "is_authorised";

    /* keys */
    String KEY_LT_AGE = "age";
    String KEY_LT_HEIGHT = "height";
    String KEY_LT_PLACEMENT = "placement";
    String KEY_MAIN = "trees";
    String KEY_LT_STATE = "state";
    String KEY_LT_GIRTH = "girth";
    String KEY_LT_YEAR = "year";
    String KEY_LT_INJURY = "injury";
    String KEY_LT_SPECIES = "species";
    int KEY_COUNT = 9; // mast be equal of KEY_ strings count
}
