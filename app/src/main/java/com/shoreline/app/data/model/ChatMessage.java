package com.shoreline.app.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ShoreLine AI Assistant Chat Message Model
 * Supports user inquiries, AI advice, and contextual one-tap action triggers.
 */
public class ChatMessage {

    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;

    public static final int ACTION_NONE = 0;
    public static final int ACTION_REPORT_OVERLOAD = 1;
    public static final int ACTION_OFFLINE_SOS = 2;
    public static final int ACTION_SAFE_ROUTE = 3;
    public static final int ACTION_FAMILY_TRACKER = 4;
    public static final int ACTION_VESSEL_CHECK = 5;
    public static final int ACTION_CALL_999 = 6;
    public static final int ACTION_CALL_16107 = 7;

    private String text;
    private int type;
    private long timestamp;
    private int actionType;
    private String actionLabel;

    public ChatMessage(String text, int type) {
        this(text, type, ACTION_NONE, null);
    }

    public ChatMessage(String text, int type, int actionType, String actionLabel) {
        this.text = text;
        this.type = type;
        this.actionType = actionType;
        this.actionLabel = actionLabel;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}

