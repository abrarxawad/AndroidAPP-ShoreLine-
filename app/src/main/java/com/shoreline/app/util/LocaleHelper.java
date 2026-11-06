package com.shoreline.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

/**
 * SYLLABUS: Localization & Locale Configuration Helper
 * Provides seamless runtime language switching between Bangla (bn) and English (en)
 * across the entire application lifecycle, persisted across restarts in SharedPreferences.
 */
public class LocaleHelper {

    private static final String PREF_NAME = "shoreline_locale_prefs";
    private static final String KEY_LANGUAGE = "selected_app_language";

    public static final String LANG_BANGLA = "bn";
    public static final String LANG_ENGLISH = "en";

    /**
     * Retrieves the persisted language code (defaulting to English "en").
     */
    public static String getLanguage(Context context) {
        if (context == null) return LANG_ENGLISH;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANG_ENGLISH);
    }

    /**
     * Checks if current language is Bangla.
     */
    public static boolean isBangla(Context context) {
        return LANG_BANGLA.equals(getLanguage(context));
    }

    /**
     * Changes the application language, persists it, and returns a localized Context.
     */
    public static Context setLocale(Context context, String language) {
        persistLanguage(context, language);

        // Modern Android 13+ Per-App Language support
        try {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language));
        } catch (Exception ignored) {}

        return updateResources(context, language);
    }

    /**
     * Applies the persisted language to the provided context.
     * Called inside BaseActivity.attachBaseContext().
     */
    public static Context applyLocale(Context context) {
        String language = getLanguage(context);
        return updateResources(context, language);
    }

    /**
     * Updates activity resources configuration directly.
     * Called inside BaseActivity.onCreate() or onResume().
     */
    @SuppressWarnings("deprecation")
    public static void applyLocaleToConfiguration(Context context) {
        if (context == null) return;
        String language = getLanguage(context);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
        } else {
            configuration.locale = locale;
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    private static void persistLanguage(Context context, String language) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    @SuppressWarnings("deprecation")
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            return context.createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }
}

