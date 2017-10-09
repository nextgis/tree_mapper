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
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nextgis.maplib.util.NGWUtil;
import com.nextgis.maplibui.fragment.NGWLoginFragment;
import com.nextgis.maplibui.service.HTTPLoader;
import com.nextgis.woody.R;
import com.nextgis.woody.util.Constants;
import com.nextgis.woody.util.UiUtil;
import com.vk.sdk.VKSdk;

import java.util.regex.Pattern;

/**
 * Created by bishop on 03.12.16.
 */

public class LoginFragment extends NGWLoginFragment {
    protected ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable
                    ViewGroup container,
            @Nullable
                    Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.fragment_login, container, false);
        mLogin = (EditText) view.findViewById(R.id.login);
        mPassword = (EditText) view.findViewById(R.id.password);
        mSignInButton = (Button) view.findViewById(R.id.signup);
        view.findViewById(R.id.vk).setOnClickListener(this);

        mLogin.addTextChangedListener(new EmailWatcher());
        mPassword.addTextChangedListener(new PasswordWatcher());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            mProgressDialog = new ProgressDialog(getActivity(), android.R.style.Theme_Material_Light_Dialog_Alert);
        else
            mProgressDialog = new ProgressDialog(getActivity());

        return view;
    }

    private void validatePassword(String password) {
        if (!Pattern.matches(Constants.PASSWORD_PATTERN, password) && password.length() > 0)
            ((TextInputLayout) mPassword.getParent()).setError(getString(R.string.error_weak_password));
        else
            ((TextInputLayout) mPassword.getParent()).setErrorEnabled(false);
    }

    private void validateEmail(String email) {
        if (!UiUtil.isEmailValid(email) && email.length() > 0)
            ((TextInputLayout) mLogin.getParent()).setError(getString(R.string.email_not_valid));
        else
            ((TextInputLayout) mLogin.getParent()).setErrorEnabled(false);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.vk:
                VKSdk.login(getActivity(), "email");
                break;
            default:
                signup();
                break;
        }
    }

    private void signup() {
        if (!UiUtil.isEmailValid(mLogin.getText().toString())) {
            Toast.makeText(getActivity(), R.string.email_not_valid, Toast.LENGTH_SHORT).show();
            return;
        }
        mSignInButton.setEnabled(false);
        Runnable signUp = new Runnable() {
            @Override
            public void run() {
                final boolean[] result = new boolean[1];

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        result[0] = NGWUtil.signUp(mUrlText, mLogin.getText().toString(), mPassword.getText().toString(), null, null);
                    }
                });
                t.start();

                while (t.isAlive()) {
                    SystemClock.sleep(300);
                }

                if (result[0]) {
                    getLoaderManager().restartLoader(R.id.auth_token_loader, null, LoginFragment.this);
                }
                else {
                    Toast.makeText(getActivity(), R.string.error_sign_up, Toast.LENGTH_LONG).show();
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
            return new HTTPLoader(
                    getActivity().getApplicationContext(), mUrlText, mLogin.getText().toString(), mPassword.getText().toString());
        }
        else if (id == R.id.non_auth_token_loader) {
            return new HTTPLoader(
                    getActivity().getApplicationContext(), mUrlText, null, null);
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
            }
            else {
                Toast.makeText(getActivity(), R.string.error_login, Toast.LENGTH_SHORT).show();

                mSignInButton.setEnabled(true);
            }
        }
        else if(loader.getId() == R.id.non_auth_token_loader) {
            onTokenReceived(Constants.ACCOUNT_NAME, Constants.ANONYMOUS);
        }
    }

    public class LocalTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    public class PasswordWatcher extends LocalTextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            validatePassword(s.toString());
        }
    }

    public class EmailWatcher extends LocalTextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            validateEmail(s.toString());
        }
    }
}
