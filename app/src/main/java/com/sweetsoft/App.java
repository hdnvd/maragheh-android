package com.sweetsoft;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.sweetsoft.data.Session;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Session.init(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}
