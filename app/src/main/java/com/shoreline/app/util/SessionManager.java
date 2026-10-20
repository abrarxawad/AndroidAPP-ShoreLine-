package com.shoreline.app.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SYLLABUS: Session Management & SharedPreferences
 * Persists active user authentication state, security tokens, and role permissions.
 */
public class SessionManager {

    private static final String PREF_NAME = "shoreline_user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role"; // "PASSENGER" or "ADMIN"
    private static final String KEY_EMERGENCY_CONTACT = "emergency_contact";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveUserSession(String userId, String email, String fullName, String phone, String role, String emergencyContact) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_ROLE, role != null ? role.toUpperCase() : "PASSENGER");
        editor.putString(KEY_EMERGENCY_CONTACT, emergencyContact);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getFullName() {
        return pref.getString(KEY_FULL_NAME, "Passenger");
    }

    public String getPhoneNumber() {
        return pref.getString(KEY_PHONE, "");
    }

    public String getUserRole() {
        return pref.getString(KEY_ROLE, "PASSENGER");
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getUserRole());
    }

    public String getEmergencyContact() {
        return pref.getString(KEY_EMERGENCY_CONTACT, "999");
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
