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

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.nextgis.maplib.util.NGWUtil;
import com.nextgis.maplibui.fragment.NGWLoginFragment;
import com.nextgis.maplibui.service.HTTPLoader;
import com.nextgis.woody.R;
import com.nextgis.woody.util.Constants;
import com.nextgis.woody.util.UiUtil;
import com.vk.sdk.VKSdk;

public class LoginFragment extends NGWLoginFragment {
    protected ProgressDialog mProgressDialog;
    private CallbackManager mCallbackManager;
    private LoginButton mFacebookButton;
    private FacebookCallback<LoginResult> mCallback;
    private View mFB, mVK;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable
                    ViewGroup container,
            @Nullable
                    Bundle savedInstanceState)
    {
        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id));
        //noinspection deprecation
        FacebookSdk.sdkInitialize(getContext().getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        final View view = inflater.inflate(R.layout.fragment_login, container, false);
        mSignInButton = new Button(getContext());
        mVK = view.findViewById(R.id.vk);
        mVK.setOnClickListener(this);
        mFB = view.findViewById(R.id.fb);
        mFB.setOnClickListener(this);

        mFacebookButton = (LoginButton) view.findViewById(R.id.fb_button);
        mFacebookButton.setReadPermissions("email");
        mFacebookButton.registerCallback(mCallbackManager, mCallback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            mProgressDialog = new ProgressDialog(getActivity(), android.R.style.Theme_Material_Light_Dialog_Alert);
        else
            mProgressDialog = new ProgressDialog(getActivity());

        return view;
    }

    public CallbackManager getCallbackManager() {
        return mCallbackManager;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.vk:
                VKSdk.login(getActivity(), "email");
                break;
            case R.id.fb:
                LoginManager.getInstance().logOut();
                mFacebookButton.performClick();
                break;
        }
    }

    public void signup(final String login, final String password, final String displayName, final String email) {
        if (!UiUtil.isEmailValid(email)) {
            Toast.makeText(getActivity(), R.string.email_not_valid, Toast.LENGTH_SHORT).show();
            return;
        }

        mSignInButton.setEnabled(false);
        mVK.setEnabled(false);
        mFB.setEnabled(false);
        Runnable signUp = new Runnable() {
            @Override
            public void run() {
                final boolean[] result = new boolean[1];

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        result[0] = NGWUtil.signUp(mUrlText, login, password, displayName, email);
                    }
                });
                t.start();

                while (t.isAlive()) {
                    SystemClock.sleep(300);
                }

                if (result[0]) {
                    mLoginText = login;
                    mPasswordText = password;
                    getLoaderManager().restartLoader(R.id.auth_token_loader, null, LoginFragment.this);
                } else {
                    Toast.makeText(getActivity(), R.string.error_sign_up, Toast.LENGTH_LONG).show();
                    mSignInButton.setEnabled(true);
                    mVK.setEnabled(true);
                    mFB.setEnabled(true);
                }

                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        };

        new Handler().post(signUp);
    }

    @Override
    public Loader<String> onCreateLoader(
            int id,
            Bundle args)
    {
        if (id == R.id.auth_token_loader) {
            return new HTTPLoader(getActivity().getApplicationContext(), mUrlText, mLoginText, mPasswordText);
        }
        else if (id == R.id.non_auth_token_loader) {
            return new HTTPLoader(getActivity().getApplicationContext(), mUrlText, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(
            Loader<String> loader,
            String token)
    {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        if (loader.getId() == R.id.auth_token_loader) {
            if (token != null && token.length() > 0) {
                onTokenReceived(Constants.ACCOUNT_NAME, token);
            } else {
                Toast.makeText(getActivity(), R.string.error_login, Toast.LENGTH_SHORT).show();
                mSignInButton.setEnabled(true);
                mVK.setEnabled(true);
                mFB.setEnabled(true);
            }
        } else if(loader.getId() == R.id.non_auth_token_loader) {
            onTokenReceived(Constants.ACCOUNT_NAME, Constants.ANONYMOUS);
        }
    }

    public void setCallback(FacebookCallback<LoginResult> callback) {
        mCallback = callback;
    }
}
