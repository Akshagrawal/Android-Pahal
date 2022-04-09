package com.pahaltutorials.pahaltutorials.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ClassPreferences {

    private static final String PREF_CURRENT_CLASS = "currentClass";

    public static String getCurrentClass(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_CURRENT_CLASS,  null);
    }

    public static void setCurrentClass(Context context, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREF_CURRENT_CLASS, value);
        editor.apply();
    }
}
