package com.sweetsoft.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.sweetsoft.data.model.User;

/** Stores the logged-in app user's API token. */
public final class Session {
    private static final String PREFS = "session";
    private static SharedPreferences prefs;

    private Session() {}

    public static void init(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isLoggedIn() {
        return getToken() != null;
    }

    public static String getToken() {
        return prefs.getString("token", null);
    }

    public static String getName() {
        return prefs.getString("name", "");
    }

    public static String getPhone() {
        return prefs.getString("phone", "");
    }

    public static void save(String token, User user) {
        prefs.edit()
                .putString("token", token)
                .putString("name", user != null ? user.name : "")
                .putString("phone", user != null ? user.phone : "")
                .apply();
    }

    public static void clear() {
        prefs.edit().clear().apply();
    }
}
