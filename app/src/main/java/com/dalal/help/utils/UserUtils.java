package com.dalal.help.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

public class UserUtils {
    private static Context context;

    public static UserUtils getInstance(Context contextw) {
        context = contextw;
        return new UserUtils();
    }

    public User getUser() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return new Gson().fromJson(preferences.getString("user", null), User.class);
    }
}
