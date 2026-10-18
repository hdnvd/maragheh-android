package com.sweetsoft.util;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Persian resources and RTL layout for the app's own screens, even when the phone is set to English.
 *
 * Applied per Context instead of through Android's per-app language (AppCompatDelegate.setApplicationLocales):
 * that system setting makes Gboard switch the keyboard to Persian whenever the app is opened.
 * Locale.getDefault() is left alone, so the keyboard language and number formatting are untouched.
 */
public final class AppLocale {
    public static final Locale FA = new Locale("fa");

    private AppLocale() {}

    public static Context wrap(Context base) {
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(FA); // also sets the layout direction to RTL
        return base.createConfigurationContext(config);
    }
}
