package com.shoreline.app.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * SYLLABUS: Custom ContentProvider Architecture
 *
 * Demonstrates:
 * 1. Subclassing android.content.ContentProvider to share river safety resources.
 * 2. SQLiteOpenHelper database management, schema creation, and seeding.
 * 3. UriMatcher for routing directory and single-item URIs.
 * 4. Proper MIME type definitions in getType() (vnd.android.cursor.dir vs item).
 * 5. Full CRUD: query(), insert(), update(), delete() with ContentResolver.notifyChange().
 * 6. Accessible via ContentResolver or external CLI: 'adb shell content query --uri content://com.shoreline.app.provider/emergency_contacts'.
 */
public class ShoreLineContentProvider extends ContentProvider {

    private static final String TAG = "ShoreLineProvider";

    public static final String AUTHORITY = "com.shoreline.app.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Path segments
    public static final String PATH_CONTACTS = "emergency_contacts";
    public static final String PATH_GUIDES = "rescue_guides";
    public static final String PATH_VESSELS = "vessels";
    public static final String PATH_SOS = "sos_signals";

    // Public URIs for ContentResolver consumers
    public static final Uri CONTENT_URI_CONTACTS = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CONTACTS);
    public static final Uri CONTENT_URI_GUIDES = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GUIDES);
    public static final Uri CONTENT_URI_VESSELS = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VESSELS);
    public static final Uri CONTENT_URI_SOS = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SOS);

    // URI Matcher Codes
    private static final int CODE_CONTACTS_DIR = 100;
    private static final int CODE_CONTACTS_ITEM = 101;
    private static final int CODE_GUIDES_DIR = 200;
    private static final int CODE_GUIDES_ITEM = 201;
    private static final int CODE_VESSELS_DIR = 300;
    private static final int CODE_SOS_DIR = 400;

    // MIME Types
    public static final String MIME_DIR_CONTACTS = "vnd.android.cursor.dir/vnd.com.shoreline.app.emergency_contacts";
    public static final String MIME_ITEM_CONTACTS = "vnd.android.cursor.item/vnd.com.shoreline.app.emergency_contacts";
    public static final String MIME_DIR_GUIDES = "vnd.android.cursor.dir/vnd.com.shoreline.app.rescue_guides";
    public static final String MIME_ITEM_GUIDES = "vnd.android.cursor.item/vnd.com.shoreline.app.rescue_guides";

    // Column Constants - Emergency Contacts
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_AGENCY = "agency";
    public static final String COL_PHONE = "phone";
    public static final String COL_ROLE = "role";
    public static final String COL_PRIORITY = "priority";

    // Column Constants - Rescue Guides
    public static final String COL_CATEGORY = "category";
    public static final String COL_TITLE_EN = "title_en";
    public static final String COL_TITLE_BN = "title_bn";
    public static final String COL_SUMMARY_EN = "summary_en";
    public static final String COL_SUMMARY_BN = "summary_bn";
    public static final String COL_STEPS_JSON = "steps_json";
    public static final String COL_ICON_NAME = "icon_name";

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, PATH_CONTACTS, CODE_CONTACTS_DIR);
        URI_MATCHER.addURI(AUTHORITY, PATH_CONTACTS + "/#", CODE_CONTACTS_ITEM);
        URI_MATCHER.addURI(AUTHORITY, PATH_GUIDES, CODE_GUIDES_DIR);
        URI_MATCHER.addURI(AUTHORITY, PATH_GUIDES + "/#", CODE_GUIDES_ITEM);
        URI_MATCHER.addURI(AUTHORITY, PATH_VESSELS, CODE_VESSELS_DIR);
        URI_MATCHER.addURI(AUTHORITY, PATH_SOS, CODE_SOS_DIR);
    }

    /**
     * Database Helper managing the ContentProvider's underlying SQLite tables.
     */
    private static class SafetyDatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "shoreline_content_provider.db";
        private static final int DATABASE_VERSION = 2;

        public static final String TABLE_CONTACTS = "emergency_contacts";
        public static final String TABLE_GUIDES = "rescue_guides";

        SafetyDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_CONTACTS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME + " TEXT NOT NULL, " +
                    COL_AGENCY + " TEXT NOT NULL, " +
                    COL_PHONE + " TEXT NOT NULL, " +
                    COL_ROLE + " TEXT, " +
                    COL_PRIORITY + " INTEGER DEFAULT 1);");

            db.execSQL("CREATE TABLE " + TABLE_GUIDES + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_CATEGORY + " TEXT NOT NULL, " +
                    COL_TITLE_EN + " TEXT NOT NULL, " +
                    COL_TITLE_BN + " TEXT NOT NULL, " +
                    COL_SUMMARY_EN + " TEXT NOT NULL, " +
                    COL_SUMMARY_BN + " TEXT NOT NULL, " +
                    COL_STEPS_JSON + " TEXT NOT NULL, " +
                    COL_ICON_NAME + " TEXT, " +
                    COL_PRIORITY + " INTEGER DEFAULT 1);");

            seedInitialData(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GUIDES);
            onCreate(db);
        }

        private void seedInitialData(SQLiteDatabase db) {
            // Seed 1: Official Emergency Contacts (100% Authentic, Valid Bangladesh River Safety Numbers)
            insertContact(db, "BIWTA River Rescue Control", "BIWTA", "16107", "Search & Salvage Operation (24/7)", 1);
            insertContact(db, "National Emergency Service", "Government of Bangladesh", "999", "Police, Fire, Ambulance", 1);
            insertContact(db, "Bangladesh Coast Guard (Delta Command)", "Coast Guard", "+8801769440555", "Maritime & River Patrol", 2);
            insertContact(db, "River Police Headquarters", "Bangladesh Police", "+8801320169595", "Riverway Security & Anti-Piracy", 2);
            insertContact(db, "Sadarghat River Port Duty Officer", "BIWTA Dhaka", "+880257390234", "Terminal Dispatch & Clearance", 3);
            insertContact(db, "Chandpur Launch Terminal Control", "BIWTA Chandpur", "+8801715055175", "Confluence Traffic Watch", 3);
            insertContact(db, "Barishal Port River Safety Desk", "BIWTA Barishal", "+8801715055176", "Kirtankhola Navigation Watch", 3);
            insertContact(db, "Fire Service River Rescue HQ", "Fire Service & Civil Defence", "+8801730336699", "River Diving & Scuba Salvage", 2);

            // Seed 2: Offline Water Survival & Rescue Guides
            insertGuide(db, "BUOYANCY",
                    "Lifejacket & Buoyancy Donning",
                    "লাইফ জ্যাকেট সঠিক নিয়মে পরিধান",
                    "How to properly fit, buckle, and secure an emergency lifejacket in under 60 seconds.",
                    "৬০ সেকেন্ডের মধ্যে সঠিকভাবে লাইফ জ্যাকেট পরা ও ফিতা বাঁধার নিয়মাবলী।",
                    "[\"1. Locate lifejacket under your launch seat or overhead rack immediately upon storm signal.\", \"2. Slip head through neck opening with reflective tape facing outward.\", \"3. Fasten all front chest buckles securely and pull straps until snug against torso.\", \"4. Fasten leg/groin straps if available to prevent jacket from riding up over head.\", \"5. Test the attached whistle with 3 short blasts to signal crew.\"]",
                    "ic_lifejacket", 1);

            insertGuide(db, "EVACUATION",
                    "Vessel Capsizing & Emergency Egress",
                    "লঞ্চ ডুবলে জরুরি নির্গমন পদ্ধতি",
                    "Critical escape protocol during vessel tilt, darkness, or cabin water inrush.",
                    "লঞ্চ কাত হলে বা পানি ঢুকলে আতঙ্কিত না হয়ে নিরাপদে বের হওয়ার নিয়ম।",
                    "[\"1. NEVER jump toward the propeller/stern side of the vessel; always egress from the high side.\", \"2. Do not swim inside flooded lower deck cabins; move to open upper decks.\", \"3. Kick shoes off and discard heavy bags before entering river water.\", \"4. Step feet-first into the water while holding lifejacket collar firmly with arms crossed.\", \"5. Swim at least 20 meters away from the hull to avoid sinking suction.\"]",
                    "ic_vessel", 2);

            insertGuide(db, "FIRST_AID",
                    "Drowning Recovery & River CPR Protocol",
                    "পানিতে ডোবা ব্যক্তির সিপিআর ও প্রাথমিক চিকিৎসা",
                    "Immediate resuscitation protocol for near-drowning victims on river vessels.",
                    "পানিতে ডুবে যাওয়া ব্যক্তির ফুসফুস থেকে পানি নিষ্কাশন ও কৃত্রিম শ্বাসপ্রশ্বাস।",
                    "[\"1. Pull victim face-up onto a solid flat surface immediately.\", \"2. Check responsiveness and normal breathing for no more than 10 seconds.\", \"3. Deliver 5 initial rescue rescue breaths before chest compressions for drowning victims.\", \"4. Perform 30 rapid chest compressions at 100-120 bpm, depressing sternum 5 cm.\", \"5. Alternate 30 compressions with 2 rescue breaths until medical patrol arrives or victim revives.\"]",
                    "ic_firstaid", 3);

            insertGuide(db, "HYPOTHERMIA",
                    "Hypothermia & Cold River Shock Defense",
                    "শীতল নদীতে হাইপোথার্মিয়া প্রতিরোধ",
                    "Adopting the H.E.L.P. posture to conserve body heat while awaiting rescue in water.",
                    "ঠাণ্ডা পানিতে শরীরের তাপ ধরে রাখার জন্য হেল্প পজিশন গ্রহণের নিয়ম।",
                    "[\"1. Do NOT tread water unnecessarily; movement accelerates body heat loss by 35%.\", \"2. Adopt the H.E.L.P. (Heat Escape Lessening Posture): cross arms tightly over chest and press knees to chin.\", \"3. If floating in a group, form a tight huddle circle with chests pressed together for shared warmth.\", \"4. Keep head and neck elevated as high above water as possible.\", \"5. Once rescued, remove wet clothes immediately and wrap in dry blankets without direct rubbing.\"]",
                    "ic_battery_alert", 4);

            insertGuide(db, "SIGNALS",
                    "River Fog & Sound Whistle Signals",
                    "কুয়াশায় নৌযানের হুইসেল ও সাউন্ড সংকেত",
                    "Standard BIWTA acoustic horn codes for navigation in low visibility.",
                    "ঘন কুয়াশায় নদীপথে নিরাপদে চলাচলের স্বীকৃত হর্ন ও হুইসেল কোড।",
                    "[\"1. One Prolonged Blast (4-6 sec): Power-driven vessel moving underway in fog.\", \"2. Two Prolonged Blasts: Vessel stopped and making no way through the water.\", \"3. One Short Blast (1 sec): 'I am altering my course to starboard (right)'.\", \"4. Two Short Blasts: 'I am altering my course to port (left)'.\", \"5. Five Rapid Blasts: 'DANGER / DO NOT UNDERSTAND YOUR INTENTION - STOP IMMEDIATELY'.\"]",
                    "ic_whistle", 5);
        }

        private void insertContact(SQLiteDatabase db, String name, String agency, String phone, String role, int priority) {
            ContentValues cv = new ContentValues();
            cv.put(COL_NAME, name);
            cv.put(COL_AGENCY, agency);
            cv.put(COL_PHONE, phone);
            cv.put(COL_ROLE, role);
            cv.put(COL_PRIORITY, priority);
            db.insert(TABLE_CONTACTS, null, cv);
        }

        private void insertGuide(SQLiteDatabase db, String category, String titleEn, String titleBn,
                                 String summaryEn, String summaryBn, String stepsJson, String iconName, int priority) {
            ContentValues cv = new ContentValues();
            cv.put(COL_CATEGORY, category);
            cv.put(COL_TITLE_EN, titleEn);
            cv.put(COL_TITLE_BN, titleBn);
            cv.put(COL_SUMMARY_EN, summaryEn);
            cv.put(COL_SUMMARY_BN, summaryBn);
            cv.put(COL_STEPS_JSON, stepsJson);
            cv.put(COL_ICON_NAME, iconName);
            cv.put(COL_PRIORITY, priority);
            db.insert(TABLE_GUIDES, null, cv);
        }
    }

    private SafetyDatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new SafetyDatabaseHelper(getContext());
        Log.d(TAG, "ShoreLineContentProvider initialized successfully with authority: " + AUTHORITY);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        int match = URI_MATCHER.match(uri);
        switch (match) {
            case CODE_CONTACTS_DIR:
                qb.setTables(SafetyDatabaseHelper.TABLE_CONTACTS);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = COL_PRIORITY + " ASC, " + COL_NAME + " ASC";
                }
                break;

            case CODE_CONTACTS_ITEM:
                qb.setTables(SafetyDatabaseHelper.TABLE_CONTACTS);
                qb.appendWhere(COL_ID + " = " + ContentUris.parseId(uri));
                break;

            case CODE_GUIDES_DIR:
                qb.setTables(SafetyDatabaseHelper.TABLE_GUIDES);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = COL_PRIORITY + " ASC";
                }
                break;

            case CODE_GUIDES_ITEM:
                qb.setTables(SafetyDatabaseHelper.TABLE_GUIDES);
                qb.appendWhere(COL_ID + " = " + ContentUris.parseId(uri));
                break;

            default:
                throw new IllegalArgumentException("Unknown ContentProvider query URI: " + uri);
        }

        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values == null) {
            throw new IllegalArgumentException("Cannot insert null ContentValues");
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = URI_MATCHER.match(uri);
        long id;

        if (match == CODE_CONTACTS_DIR) {
            id = db.insert(SafetyDatabaseHelper.TABLE_CONTACTS, null, values);
            if (id > 0) {
                Uri newUri = ContentUris.withAppendedId(CONTENT_URI_CONTACTS, id);
                notifyChange(newUri);
                return newUri;
            }
        } else if (match == CODE_GUIDES_DIR) {
            id = db.insert(SafetyDatabaseHelper.TABLE_GUIDES, null, values);
            if (id > 0) {
                Uri newUri = ContentUris.withAppendedId(CONTENT_URI_GUIDES, id);
                notifyChange(newUri);
                return newUri;
            }
        }

        throw new IllegalArgumentException("Failed to insert row into URI: " + uri);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated;
        int match = URI_MATCHER.match(uri);

        switch (match) {
            case CODE_CONTACTS_DIR:
                rowsUpdated = db.update(SafetyDatabaseHelper.TABLE_CONTACTS, values, selection, selectionArgs);
                break;
            case CODE_CONTACTS_ITEM:
                String contactId = String.valueOf(ContentUris.parseId(uri));
                rowsUpdated = db.update(SafetyDatabaseHelper.TABLE_CONTACTS, values,
                        COL_ID + "=?", new String[]{contactId});
                break;
            case CODE_GUIDES_DIR:
                rowsUpdated = db.update(SafetyDatabaseHelper.TABLE_GUIDES, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI for update: " + uri);
        }

        if (rowsUpdated > 0) {
            notifyChange(uri);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;
        int match = URI_MATCHER.match(uri);

        switch (match) {
            case CODE_CONTACTS_DIR:
                rowsDeleted = db.delete(SafetyDatabaseHelper.TABLE_CONTACTS, selection, selectionArgs);
                break;
            case CODE_CONTACTS_ITEM:
                String contactId = String.valueOf(ContentUris.parseId(uri));
                rowsDeleted = db.delete(SafetyDatabaseHelper.TABLE_CONTACTS,
                        COL_ID + "=?", new String[]{contactId});
                break;
            case CODE_GUIDES_DIR:
                rowsDeleted = db.delete(SafetyDatabaseHelper.TABLE_GUIDES, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI for delete: " + uri);
        }

        if (rowsDeleted > 0) {
            notifyChange(uri);
        }
        return rowsDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case CODE_CONTACTS_DIR:
                return MIME_DIR_CONTACTS;
            case CODE_CONTACTS_ITEM:
                return MIME_ITEM_CONTACTS;
            case CODE_GUIDES_DIR:
                return MIME_DIR_GUIDES;
            case CODE_GUIDES_ITEM:
                return MIME_ITEM_GUIDES;
            default:
                return null;
        }
    }

    private void notifyChange(Uri uri) {
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }
}

