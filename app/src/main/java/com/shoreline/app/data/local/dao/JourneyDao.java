package com.shoreline.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.shoreline.app.data.local.entity.ActiveJourneyEntity;
import com.shoreline.app.data.local.entity.JourneyBreadcrumbEntity;

import java.util.List;

/**
 * SYLLABUS: Room Data Access Object (DAO) - Journey Tracking Operations
 * Handles active journey state, breadcrumbs trail storage, and offline synchronization.
 */
@Dao
public interface JourneyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertJourney(ActiveJourneyEntity journey);

    @Update
    void updateJourney(ActiveJourneyEntity journey);

    @Query("SELECT * FROM active_journeys WHERE journeyCode = :code LIMIT 1")
    ActiveJourneyEntity getJourneyByCode(String code);

    @Query("SELECT * FROM active_journeys WHERE status = 'ACTIVE' LIMIT 1")
    ActiveJourneyEntity getActiveJourney();

    @Query("SELECT * FROM active_journeys ORDER BY startedAt DESC")
    List<ActiveJourneyEntity> getAllJourneys();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertBreadcrumb(JourneyBreadcrumbEntity breadcrumb);

    @Query("SELECT * FROM journey_breadcrumbs WHERE journeyCode = :code ORDER BY timestamp ASC")
    List<JourneyBreadcrumbEntity> getBreadcrumbsForJourney(String code);

    @Query("SELECT * FROM journey_breadcrumbs WHERE isSynced = 0 ORDER BY timestamp ASC LIMIT 50")
    List<JourneyBreadcrumbEntity> getUnsyncedBreadcrumbs();

    @Query("UPDATE journey_breadcrumbs SET isSynced = 1 WHERE id IN (:ids)")
    void markBreadcrumbsSynced(List<Long> ids);

    @Query("UPDATE active_journeys SET status = 'STOPPED' WHERE journeyCode = :code")
    void markJourneyStopped(String code);

    @Query("DELETE FROM journey_breadcrumbs WHERE journeyCode = :code")
    void deleteBreadcrumbsForJourney(String code);

    @Query("DELETE FROM active_journeys")
    void clearAllJourneys();
}
