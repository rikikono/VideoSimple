package com.example.videoproject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class to manage dark mode theme switching.
 * Uses SharedPreferences to persist the user's theme choice.
 */
public class ThemeHelper {

    private static final String PREF_NAME = "video_player_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    /**
     * Check if dark mode is enabled.
     */
    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    /**
     * Set dark mode preference.
     */
    public static void setDarkMode(Context context, boolean darkMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DARK_MODE, darkMode).apply();
    }

    /**
     * Toggle dark mode on/off.
     *
     * @return New dark mode state
     */
    public static boolean toggleDarkMode(Context context) {
        boolean newState = !isDarkMode(context);
        setDarkMode(context, newState);
        return newState;
    }

    /**
     * Apply the saved theme to an activity.
     * Must be called BEFORE setContentView() in onCreate().
     */
    public static void applyTheme(Activity activity) {
        if (isDarkMode(activity)) {
            activity.setTheme(R.style.AppTheme_Dark);
        } else {
            activity.setTheme(R.style.AppTheme_Light);
        }
    }
}
