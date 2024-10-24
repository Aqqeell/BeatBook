package models;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import org.json.JSONObject;

public class AppSharedPreferences {
    private static final String filename = "BeatBook";
    private static SharedPreferences preferences;
    private static AppSharedPreferences mAppSharedPreferences;

    private AppSharedPreferences(Context context) {
        preferences = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
    }

    public static synchronized AppSharedPreferences getInstance(Context context) {
        if (mAppSharedPreferences == null) {
            mAppSharedPreferences = new AppSharedPreferences(context);
        }
        return mAppSharedPreferences;
    }

    public boolean saveUser(JSONObject obj) {
        try {
            preferences.edit().putString("user", new Gson().toJson(obj)).apply();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public JSONObject getUser() {
        try {
            String jsonString = preferences.getString("user", null);

            return new Gson().fromJson(jsonString, JSONObject.class);
        } catch (Exception ex) {
            return null;
        }
    }

//    public void delUser() {
//        try {
//            preferences.edit().remove("user").apply();
//        } catch (Exception ex) {
//
//        }
//    }
//
//    public Boolean getUserLoginStatus() {
//        return preferences.getBoolean("user_login_status", false);
//    }
//
//    public void setUserLoginStatus(boolean alarmStatus) {
//        preferences.edit().putBoolean("user_login_status", alarmStatus).apply();
//    }
//
//    public String getUserLoginInformation() {
//        return preferences.getString("user_login_information", "");
//    }
//
//    public void setUserLoginInformation(String alarmStatus) {
//        preferences.edit().putString("user_login_information", alarmStatus).apply();
//    }
}
