package com.shoreline.app.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.shoreline.app.data.local.converter.DateConverter;
import com.shoreline.app.data.local.dao.NodeWeatherDao;
import com.shoreline.app.data.local.dao.ReportDao;
import com.shoreline.app.data.local.dao.SosDao;
import com.shoreline.app.data.local.dao.UserDao;
import com.shoreline.app.data.local.dao.VesselDao;
import com.shoreline.app.data.local.entity.NodeWeatherEntity;
import com.shoreline.app.data.local.entity.OverloadReportEntity;
import com.shoreline.app.data.local.entity.SosSignalEntity;
import com.shoreline.app.data.local.entity.UserEntity;
import com.shoreline.app.data.local.entity.VesselEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SYLLABUS: Room Database & Offline Persistence Architecture
 *
 * VIVA EXPLANATION (Room vs Raw SQLite / SQLiteOpenHelper):
 * 1. Compile-Time SQL Verification: Room validates @Query syntax during compilation; raw SQLite only fails at runtime.
 * 2. Boilerplate Elimination: Raw SQLite requires manual ContentValues mapping and Cursor row-iteration; Room auto-maps models.
 * 3. Thread Safety: Room enforces background threading and rejects main-thread queries by default.
 * 4. Migration Architecture: Room tracks schema hash versioning with migration objects, avoiding destructive data drops.
 */
@Database(
        entities = {
                UserEntity.class,
                VesselEntity.class,
                OverloadReportEntity.class,
                SosSignalEntity.class,
                NodeWeatherEntity.class
        },
        version = 2,
        exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class ShoreLineDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "shoreline_offline_safety.db";
    private static volatile ShoreLineDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    // SYLLABUS: Background execution service for database read/write operations (avoids Main Thread ANR)
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract UserDao userDao();
    public abstract VesselDao vesselDao();
    public abstract ReportDao reportDao();
    public abstract SosDao sosDao();
    public abstract NodeWeatherDao nodeWeatherDao();

    /**
     * SYLLABUS: Double-Checked Locking Singleton Pattern
     * Guarantees a single open connection pool across the application lifecycle.
     */
    public static ShoreLineDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (ShoreLineDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ShoreLineDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
