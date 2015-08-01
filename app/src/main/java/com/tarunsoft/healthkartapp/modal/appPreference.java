package com.tarunsoft.healthkartapp.modal;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class appPreference {
    private static final String USER_PREFS = "USER_PREFS";
    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;
    private String page_no = "user_api_pageno_prefs";
    private String page_count = "user_id_prefs";

    public appPreference(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(USER_PREFS, Activity.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }

    public int getPage_no() {
        return appSharedPrefs.getInt(page_no, 0);
    }

    public void setPage_no(int _page_no) {
        prefsEditor.putInt(page_no, _page_no).commit();
    }

    public int getPage_count() {
        return appSharedPrefs.getInt(page_count, 10);
    }

    public void setPage_count(int _page_count) {
        prefsEditor.putInt(page_count, _page_count).commit();
    }

}