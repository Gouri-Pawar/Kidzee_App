package com.mgm.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "MyPreference";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String KEY_MOBILE = "mbNo";
    private static final String KEY_PASSWORD = "password";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    // First time launch
    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    // Save Login Data
    public void saveLogin(String mbNo, String password) {
        editor.putString(KEY_MOBILE, mbNo);
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }

    // Get Mobile Number
    public String getMobile() {
        return pref.getString(KEY_MOBILE, null);
    }

    // Get Password
    public String getPassword() {
        return pref.getString(KEY_PASSWORD, null);
    }
}