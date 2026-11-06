package com.shoreline.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageView;

import com.shoreline.app.R;

/**
 * Maritime Deck Environment & Theme Helper
 * Manages dynamic visual mode switching between:
 * 1. Night Helm (Cockpit view with glowing cyan instruments & starry sky)
 * 2. Day Deck (Sunlit yacht teak deck overlooking coastal blue waters)
 */
public class DeckThemeHelper {

    private static final String PREF_NAME = "shoreline_theme_prefs";
    private static final String KEY_DECK_MODE = "deck_environment_mode";

    public static final String MODE_NIGHT_HELM = "NIGHT_HELM";
    public static final String MODE_DAY_DECK = "DAY_DECK";

    public static String getDeckMode(Context context) {
        if (context == null) return MODE_NIGHT_HELM;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_DECK_MODE, MODE_NIGHT_HELM);
    }

    public static void setDeckMode(Context context, String mode) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_DECK_MODE, mode).apply();
    }

    public static boolean isDayDeck(Context context) {
        return MODE_DAY_DECK.equalsIgnoreCase(getDeckMode(context));
    }

    public static int getHeroBackgroundRes(Context context) {
        return isDayDeck(context) ? R.drawable.bg_deck_yacht_view : R.drawable.bg_yacht_night_helm;
    }

    public static void applyDeckBackground(Context context, ImageView backgroundImageView) {
        if (backgroundImageView != null && context != null) {
            backgroundImageView.setImageResource(getHeroBackgroundRes(context));
        }
    }
}

