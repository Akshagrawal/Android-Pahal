package com.pahaltutorials.pahaltutorials.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PagePreferences {

    private static final String PREF_PAGE = "page_";

    public static int getCurrentPage(Context context, String documentId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_PAGE + documentId, 0);
    }

    public static void setCurrentPage(Context context, String documentId, int pageNumber) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(PREF_PAGE + documentId, pageNumber);
        editor.apply();
    }
}
