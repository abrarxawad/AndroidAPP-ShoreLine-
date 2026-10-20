package com.shoreline.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.shoreline.app.data.local.entity.VesselEntity;

import java.util.List;

/**
 * SYLLABUS: Room DAO - Vessel cache operations
 */
@Dao
public interface VesselDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateVessel(VesselEntity vessel);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<VesselEntity> vessels);

    @Query("SELECT * FROM vessels WHERE registration_number = :regNumber LIMIT 1")
    VesselEntity getVesselByRegistrationNumber(String regNumber);

    @Query("SELECT * FROM vessels WHERE vessel_id = :vesselId LIMIT 1")
    VesselEntity getVesselById(String vesselId);

    @Query("SELECT * FROM vessels ORDER BY vessel_name ASC")
    List<VesselEntity> getAllVessels();

    @Query("SELECT * FROM vessels WHERE is_flagged = 1")
    List<VesselEntity> getFlaggedVessels();

    @Query("DELETE FROM vessels")
    void clearAllVessels();
}
