package com.shoreline.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.shoreline.app.data.local.entity.NodeWeatherEntity;

import java.util.List;

/**
 * SYLLABUS: Room Data Access Object (DAO) - Weather Cache Queries
 * Enforces compile-time SQL verification for cached forecast retrievals.
 */
@Dao
public interface NodeWeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<NodeWeatherEntity> entities);

    @Query("SELECT * FROM node_weather WHERE nodeId = :nodeId AND forecastTime = :forecastTime LIMIT 1")
    NodeWeatherEntity getNodeWeather(String nodeId, String forecastTime);

    @Query("SELECT * FROM node_weather WHERE nodeId = :nodeId ORDER BY ABS(strftime('%s', forecastTime) - strftime('%s', :targetTime)) ASC LIMIT 1")
    NodeWeatherEntity getNodeWeatherNearest(String nodeId, String targetTime);

    @Query("SELECT * FROM node_weather WHERE nodeId IN (:nodeIds)")
    List<NodeWeatherEntity> getWeatherForNodes(List<String> nodeIds);

    @Query("SELECT * FROM node_weather WHERE nodeId = :nodeId ORDER BY fetchedAt DESC LIMIT 1")
    NodeWeatherEntity getLatestForNode(String nodeId);

    @Query("DELETE FROM node_weather WHERE fetchedAt < :olderThanTimestamp")
    void deleteOldWeather(long olderThanTimestamp);

    @Query("DELETE FROM node_weather")
    void clearAll();
}
