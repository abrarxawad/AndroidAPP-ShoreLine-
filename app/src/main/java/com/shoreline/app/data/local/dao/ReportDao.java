package com.shoreline.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.shoreline.app.data.local.entity.OverloadReportEntity;

import java.util.List;

/**
 * SYLLABUS: Room DAO - Offline Overload Reports Queue
 */
@Dao
public interface ReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReport(OverloadReportEntity report);

    // SYLLABUS: Offline queue query - fetch un-synced reports to push when signal restores
    @Query("SELECT * FROM overload_reports WHERE is_synced = 0 ORDER BY timestamp ASC")
    List<OverloadReportEntity> getPendingSyncReports();

    @Query("UPDATE overload_reports SET is_synced = 1, sync_timestamp = :syncTime WHERE report_id = :reportId")
    void markAsSynced(String reportId, long syncTime);

    @Query("SELECT * FROM overload_reports ORDER BY timestamp DESC")
    List<OverloadReportEntity> getAllReports();

    @Query("SELECT * FROM overload_reports WHERE vessel_id = :vesselId ORDER BY timestamp DESC")
    List<OverloadReportEntity> getReportsForVessel(String vesselId);
}
