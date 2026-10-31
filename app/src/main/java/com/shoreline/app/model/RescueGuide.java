package com.shoreline.app.model;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing an Offline River Rescue & Survival Guide.
 * Populated from ShoreLineContentProvider cursor.
 */
public class RescueGuide {

    private final long id;
    private final String category;
    private final String titleEn;
    private final String titleBn;
    private final String summaryEn;
    private final String summaryBn;
    private final String stepsJson;
    private final String iconName;
    private final int priority;

    public RescueGuide(long id, String category, String titleEn, String titleBn,
                       String summaryEn, String summaryBn, String stepsJson,
                       String iconName, int priority) {
        this.id = id;
        this.category = category;
        this.titleEn = titleEn;
        this.titleBn = titleBn;
        this.summaryEn = summaryEn;
        this.summaryBn = summaryBn;
        this.stepsJson = stepsJson;
        this.iconName = iconName;
        this.priority = priority;
    }

    public long getId() { return id; }
    public String getCategory() { return category; }
    public String getTitleEn() { return titleEn; }
    public String getTitleBn() { return titleBn; }
    public String getSummaryEn() { return summaryEn; }
    public String getSummaryBn() { return summaryBn; }
    public String getStepsJson() { return stepsJson; }
    public String getIconName() { return iconName; }
    public int getPriority() { return priority; }

    @NonNull
    public List<String> getStepsList() {
        List<String> list = new ArrayList<>();
        if (stepsJson != null && !stepsJson.trim().isEmpty()) {
            try {
                JSONArray arr = new JSONArray(stepsJson);
                for (int i = 0; i < arr.length(); i++) {
                    list.add(arr.getString(i));
                }
            } catch (Exception e) {
                list.add(stepsJson);
            }
        }
        return list;
    }
}

