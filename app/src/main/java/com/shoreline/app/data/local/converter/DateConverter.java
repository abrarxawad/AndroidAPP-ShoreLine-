package com.shoreline.app.data.local.converter;

import androidx.room.TypeConverter;
import java.util.Date;

/**
 * SYLLABUS: Room TypeConverter
 * Enables storing complex Java objects (Date) as SQLite primitives (Long).
 */
public class DateConverter {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
