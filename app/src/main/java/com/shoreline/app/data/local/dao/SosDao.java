package com.shoreline.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.shoreline.app.data.local.entity.SosSignalEntity;

import java.util.List;

/**
 * SYLLABUS: Room DAO - Offline SOS Mesh Beacon Cache & Deduplication
 */
@Dao
public interface SosDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSos(SosSignalEntity sos);

    @Query("SELECT * FROM sos_signals WHERE sos_id = :sosId LIMIT 1")
    SosSignalEntity getSosById(String sosId);

    @Query("SELECT * FROM sos_signals WHERE is_synced_to_cloud = 0")
    List<SosSignalEntity> getPendingCloudSyncSos();

    @Query("SELECT * FROM sos_signals ORDER BY timestamp DESC")
    List<SosSignalEntity> getAllSosSignals();

    @Query("UPDATE sos_signals SET is_synced_to_cloud = 1 WHERE sos_id = :sosId")
    void markAsCloudSynced(String sosId);
}
