package com.shoreline.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

/**
 * Global Typography & Accessibility Scale Helper
 * Manages dynamic text scaling across the entire application lifecycle.
 */
public class FontScaleHelper {

    private static final String PREF_NAME = "shoreline_font_prefs";
    private static final String KEY_FONT_SCALE = "selected_font_scale";

    public static final float SCALE_SMALL = 0.85f;
    public static final float SCALE_DEFAULT = 1.00f;
    public static final float SCALE_LARGE = 1.15f;
    public static final float SCALE_EXTRA_LARGE = 1.30f;

    public static float getFontScale(Context context) {
        if (context == null) return SCALE_DEFAULT;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_FONT_SCALE, SCALE_DEFAULT);
    }

    public static void setFontScale(Context context, float scale) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat(KEY_FONT_SCALE, scale).apply();
    }

    public static Context applyFontScale(Context context) {
        if (context == null) return null;
        float scale = getFontScale(context);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.fontScale = scale;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.createConfigurationContext(config);
        } else {
            res.updateConfiguration(config, res.getDisplayMetrics());
            return context;
        }
    }

    @SuppressWarnings("deprecation")
    public static void applyFontScaleToConfiguration(Context context) {
        if (context == null) return;
        float scale = getFontScale(context);
        Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        config.fontScale = scale;
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}

