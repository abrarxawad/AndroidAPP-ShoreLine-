package com.shoreline.app.ui.admin;

import androidx.annotation.DrawableRes;

/**
 * SYLLABUS: Model representing an Admin Command Center Metric Tile.
 * Rendered using a GridLayoutManager with 2 columns.
 */
public class AdminStatTile {

    public enum TileType {
        ACTIVE_SOS,
        OVERLOAD_REPORTS,
        MONITORED_FLEET,
        STORM_SIGNAL
    }

    private final TileType type;
    private final String title;
    private String value;
    private String subtitle;
    @DrawableRes
    private final int iconRes;
    private final String bgTintColor;
    private final String accentColor;

    public AdminStatTile(TileType type, String title, String value, String subtitle,
                         @DrawableRes int iconRes, String bgTintColor, String accentColor) {
        this.type = type;
        this.title = title;
        this.value = value;
        this.subtitle = subtitle;
        this.iconRes = iconRes;
        this.bgTintColor = bgTintColor;
        this.accentColor = accentColor;
    }

    public TileType getType() { return type; }
    public String getTitle() { return title; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public int getIconRes() { return iconRes; }
    public String getBgTintColor() { return bgTintColor; }
    public String getAccentColor() { return accentColor; }
}

