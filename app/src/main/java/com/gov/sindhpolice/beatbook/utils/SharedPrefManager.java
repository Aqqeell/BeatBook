package com.gov.sindhpolice.beatbook.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String PREF_NAME = "user_session";
    private static final String TOKEN_KEY = "auth_token";
    private static SharedPrefManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Private constructor to enforce singleton pattern
    private SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Singleton method to ensure only one instance is created
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    // Method to save token
    public void saveToken(String token) {
        editor.putString(TOKEN_KEY, token);
        editor.apply();
    }

    // Method to retrieve token
    public String getToken() {
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    // Method to clear the token (e.g., on logout)
    public void clearToken() {
        editor.remove(TOKEN_KEY);
        editor.apply();
    }
}
