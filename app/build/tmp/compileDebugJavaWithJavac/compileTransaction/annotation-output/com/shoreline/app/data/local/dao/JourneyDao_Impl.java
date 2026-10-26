package com.shoreline.app.data.local.dao;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shoreline.app.data.local.entity.ActiveJourneyEntity;
import com.shoreline.app.data.local.entity.JourneyBreadcrumbEntity;
import java.lang.Class;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class JourneyDao_Impl implements JourneyDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ActiveJourneyEntity> __insertionAdapterOfActiveJourneyEntity;

  private final EntityInsertionAdapter<JourneyBreadcrumbEntity> __insertionAdapterOfJourneyBreadcrumbEntity;

  private final EntityDeletionOrUpdateAdapter<ActiveJourneyEntity> __updateAdapterOfActiveJourneyEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkJourneyStopped;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBreadcrumbsForJourney;

  private final SharedSQLiteStatement __preparedStmtOfClearAllJourneys;

  public JourneyDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfActiveJourneyEntity = new EntityInsertionAdapter<ActiveJourneyEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `active_journeys` (`journeyCode`,`passengerId`,`passengerName`,`passengerPhone`,`emergencyContactPhone`,`vesselName`,`originGhatId`,`originGhatName`,`destGhatId`,`destGhatName`,`startedAt`,`lastUpdatedAt`,`status`,`currentLatitude`,`currentLongitude`,`currentSpeedKmh`,`totalDistanceKm`,`nearestGhatName`,`isSynced`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, ActiveJourneyEntity value) {
        if (value.getJourneyCode() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getJourneyCode());
        }
        if (value.getPassengerId() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getPassengerId());
        }
        if (value.getPassengerName() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getPassengerName());
        }
        if (value.getPassengerPhone() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getPassengerPhone());
        }
        if (value.getEmergencyContactPhone() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getEmergencyContactPhone());
        }
        if (value.getVesselName() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getVesselName());
        }
        if (value.getOriginGhatId() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getOriginGhatId());
        }
        if (value.getOriginGhatName() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getOriginGhatName());
        }
        if (value.getDestGhatId() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getDestGhatId());
        }
        if (value.getDestGhatName() == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindString(10, value.getDestGhatName());
        }
        stmt.bindLong(11, value.getStartedAt());
        stmt.bindLong(12, value.getLastUpdatedAt());
        if (value.getStatus() == null) {
          stmt.bindNull(13);
        } else {
          stmt.bindString(13, value.getStatus());
        }
        stmt.bindDouble(14, value.getCurrentLatitude());
        stmt.bindDouble(15, value.getCurrentLongitude());
        stmt.bindDouble(16, value.getCurrentSpeedKmh());
        stmt.bindDouble(17, value.getTotalDistanceKm());
        if (value.getNearestGhatName() == null) {
          stmt.bindNull(18);
        } else {
          stmt.bindString(18, value.getNearestGhatName());
        }
        final int _tmp = value.isSynced() ? 1 : 0;
        stmt.bindLong(19, _tmp);
      }
    };
    this.__insertionAdapterOfJourneyBreadcrumbEntity = new EntityInsertionAdapter<JourneyBreadcrumbEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `journey_breadcrumbs` (`id`,`journeyCode`,`latitude`,`longitude`,`speedKmh`,`timestamp`,`isSynced`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, JourneyBreadcrumbEntity value) {
        stmt.bindLong(1, value.getId());
        if (value.getJourneyCode() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getJourneyCode());
        }
        stmt.bindDouble(3, value.getLatitude());
        stmt.bindDouble(4, value.getLongitude());
        stmt.bindDouble(5, value.getSpeedKmh());
        stmt.bindLong(6, value.getTimestamp());
        final int _tmp = value.isSynced() ? 1 : 0;
        stmt.bindLong(7, _tmp);
      }
    };
    this.__updateAdapterOfActiveJourneyEntity = new EntityDeletionOrUpdateAdapter<ActiveJourneyEntity>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `active_journeys` SET `journeyCode` = ?,`passengerId` = ?,`passengerName` = ?,`passengerPhone` = ?,`emergencyContactPhone` = ?,`vesselName` = ?,`originGhatId` = ?,`originGhatName` = ?,`destGhatId` = ?,`destGhatName` = ?,`startedAt` = ?,`lastUpdatedAt` = ?,`status` = ?,`currentLatitude` = ?,`currentLongitude` = ?,`currentSpeedKmh` = ?,`totalDistanceKm` = ?,`nearestGhatName` = ?,`isSynced` = ? WHERE `journeyCode` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, ActiveJourneyEntity value) {
        if (value.getJourneyCode() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getJourneyCode());
        }
        if (value.getPassengerId() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getPassengerId());
        }
        if (value.getPassengerName() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getPassengerName());
        }
        if (value.getPassengerPhone() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getPassengerPhone());
        }
        if (value.getEmergencyContactPhone() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getEmergencyContactPhone());
        }
        if (value.getVesselName() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getVesselName());
        }
        if (value.getOriginGhatId() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getOriginGhatId());
        }
        if (value.getOriginGhatName() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getOriginGhatName());
        }
        if (value.getDestGhatId() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getDestGhatId());
        }
        if (value.getDestGhatName() == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindString(10, value.getDestGhatName());
        }
        stmt.bindLong(11, value.getStartedAt());
        stmt.bindLong(12, value.getLastUpdatedAt());
        if (value.getStatus() == null) {
          stmt.bindNull(13);
        } else {
          stmt.bindString(13, value.getStatus());
        }
        stmt.bindDouble(14, value.getCurrentLatitude());
        stmt.bindDouble(15, value.getCurrentLongitude());
        stmt.bindDouble(16, value.getCurrentSpeedKmh());
        stmt.bindDouble(17, value.getTotalDistanceKm());
        if (value.getNearestGhatName() == null) {
          stmt.bindNull(18);
        } else {
          stmt.bindString(18, value.getNearestGhatName());
        }
        final int _tmp = value.isSynced() ? 1 : 0;
        stmt.bindLong(19, _tmp);
        if (value.getJourneyCode() == null) {
          stmt.bindNull(20);
        } else {
          stmt.bindString(20, value.getJourneyCode());
        }
      }
    };
    this.__preparedStmtOfMarkJourneyStopped = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "UPDATE active_journeys SET status = 'STOPPED' WHERE journeyCode = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteBreadcrumbsForJourney = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM journey_breadcrumbs WHERE journeyCode = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAllJourneys = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM active_journeys";
        return _query;
      }
    };
  }

  @Override
  public void insertJourney(final ActiveJourneyEntity journey) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfActiveJourneyEntity.insert(journey);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public long insertBreadcrumb(final JourneyBreadcrumbEntity breadcrumb) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      long _result = __insertionAdapterOfJourneyBreadcrumbEntity.insertAndReturnId(breadcrumb);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateJourney(final ActiveJourneyEntity journey) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfActiveJourneyEntity.handle(journey);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void markJourneyStopped(final String code) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfMarkJourneyStopped.acquire();
    int _argIndex = 1;
    if (code == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, code);
    }
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfMarkJourneyStopped.release(_stmt);
    }
  }

  @Override
  public void deleteBreadcrumbsForJourney(final String code) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteBreadcrumbsForJourney.acquire();
    int _argIndex = 1;
    if (code == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, code);
    }
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteBreadcrumbsForJourney.release(_stmt);
    }
  }

  @Override
  public void clearAllJourneys() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllJourneys.acquire();
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfClearAllJourneys.release(_stmt);
    }
  }

  @Override
  public ActiveJourneyEntity getJourneyByCode(final String code) {
    final String _sql = "SELECT * FROM active_journeys WHERE journeyCode = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (code == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, code);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfJourneyCode = CursorUtil.getColumnIndexOrThrow(_cursor, "journeyCode");
      final int _cursorIndexOfPassengerId = CursorUtil.getColumnIndexOrThrow(_cursor, "passengerId");
      final int _cursorIndexOfPassengerName = CursorUtil.getColumnIndexOrThrow(_cursor, "passengerName");
      final int _cursorIndexOfPassengerPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "passengerPhone");
      final int _cursorIndexOfEmergencyContactPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "emergencyContactPhone");
      final int _cursorIndexOfVesselName = CursorUtil.getColumnIndexOrThrow(_cursor, "vesselName");
      final int _cursorIndexOfOriginGhatId = CursorUtil.getColumnIndexOrThrow(_cursor, "originGhatId");
      final int _cursorIndexOfOriginGhatName = CursorUtil.getColumnIndexOrThrow(_cursor, "originGhatName");
      final int _cursorIndexOfDestGhatId = CursorUtil.getColumnIndexOrThrow(_cursor, "destGhatId");
      final int _cursorIndexOfDestGhatName = CursorUtil.getColumnIndexOrThrow(_cursor, "destGhatName");
      final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
      final int _cursorIndexOfLastUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedAt");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfCurrentLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "currentLatitude");
      final int _cursorIndexOfCurrentLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "currentLongitude");
      final int _cursorIndexOfCurrentSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "currentSpeedKmh");
      final int _cursorIndexOfTotalDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDistanceKm");
      final int _cursorIndexOfNearestGhatName = CursorUtil.getColumnIndexOrThrow(_cursor, "nearestGhatName");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
      final ActiveJourneyEntity _result;
      if(_cursor.moveToFirst()) {
        final String _tmpJourneyCode;
        if (_cursor.isNull(_cursorIndexOfJourneyCode)) {
          _tmpJourneyCode = null;
        } else {
          _tmpJourneyCode = _cursor.getString(_cursorIndexOfJourneyCode);
        }
        final String _tmpPassengerId;
        if (_cursor.isNull(_cursorIndexOfPassengerId)) {
          _tmpPassengerId = null;
        } else {
          _tmpPassengerId = _cursor.getString(_cursorIndexOfPassengerId);
        }
        final String _tmpPassengerName;
        if (_cursor.isNull(_cursorIndexOfPassengerName)) {
          _tmpPassengerName = null;
        } else {
          _tmpPassengerName = _cursor.getString(_cursorIndexOfPassengerName);
        }
        final String _tmpPassengerPhone;
        if (_cursor.isNull(_cursorIndexOfPassengerPhone)) {
          _tmpPassengerPhone = null;
        } else {
          _tmpPassengerPhone = _cursor.getString(_cursorIndexOfPassengerPhone);
        }
        final String _tmpEmergencyContactPhone;
        if (_cursor.isNull(_cursorIndexOfEmergencyContactPhone)) {
          _tmpEmergencyContactPhone = null;
        } else {
          _tmpEmergencyContactPhone = _cursor.getString(_cursorIndexOfEmergencyContactPhone);
        }
        final String _tmpVesselName;
        if (_cursor.isNull(_cursorIndexOfVesselName)) {
          _tmpVesselName = null;
        } else {
          _tmpVesselName = _cursor.getString(_cursorIndexOfVesselName);
        }
        final String _tmpOriginGhatId;
        if (_cursor.isNull(_cursorIndexOfOriginGhatId)) {
          _tmpOriginGhatId = null;
        } else {
          _tmpOriginGhatId = _cursor.getString(_cursorIndexOfOriginGhatId);
        }
        final String _tmpOriginGhatName;
        if (_cursor.isNull(_cursorIndexOfOriginGhatName)) {
          _tmpOriginGhatName = null;
        } else {
          _tmpOriginGhatName = _cursor.getString(_cursorIndexOfOriginGhatName);
        }
        final String _tmpDestGhatId;
        if (_cursor.isNull(_cursorIndexOfDestGhatId)) {
          _tmpDestGhatId = null;
        } else {
          _tmpDestGhatId = _cursor.getString(_cursorIndexOfDestGhatId);
        }
        final String _tmpDestGhatName;
        if (_cursor.isNull(_cursorIndexOfDestGhatName)) {
          _tmpDestGhatName = null;
        } else {
          _tmpDestGhatName = _cursor.getString(_cursorIndexOfDestGhatName);
        }
        final long _tmpStartedAt;
        _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
        final long _tmpLastUpdatedAt;
        _tmpLastUpdatedAt = _cursor.getLong(_cursorIndexOfLastUpdatedAt);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        final double _tmpCurrentLatitude;
        _tmpCurrentLatitude = _cursor.getDouble(_cursorIndexOfCurrentLatitude);
        final double _tmpCurrentLongitude;
        _tmpCurrentLongitude = _cursor.getDouble(_cursorIndexOfCurrentLongitude);
        final double _tmpCurrentSpeedKmh;
        _tmpCurrentSpeedKmh = _cursor.getDouble(_cursorIndexOfCurrentSpeedKmh);
        final double _tmpTotalDistanceKm;
        _tmpTotalDistanceKm = _cursor.getDouble(_cursorIndexOfTotalDistanceKm);
        final String _tmpNearestGhatName;
        if (_cursor.isNull(_cursorIndexOfNearestGhatName)) {
          _tmpNearestGhatName = null;
        } else {
          _tmpNearestGhatName = _cursor.getString(_cursorIndexOfNearestGhatName);
        }
        final boolean _tmpIsSynced;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp != 0;
        _result = new ActiveJourneyEntity(_tmpJourneyCode,_tmpPassengerId,_tmpPassengerName,_tmpPassengerPhone,_tmpEmergencyContactPhone,_tmpVesselName,_tmpOriginGhatId,_tmpOriginGhatName,_tmpDestGhatId,_tmpDestGhatName,_tmpStartedAt,_tmpLastUpdatedAt,_tmpStatus,_tmpCurrentLatitude,_tmpCurrentLongitude,_tmpCurrentSpeedKmh,_tmpTotalDistanceKm,_tmpNearestGhatName,_tmpIsSynced);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public ActiveJourneyEntity getActiveJourney() {
    final String _sql = "SELECT * FROM active_journeys WHERE status = 'ACTIVE' LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfJourneyCode = CursorUtil.getColumnIndexOrThrow(_cursor, "journeyCode");
      final int _cursorIndexOfPassengerId = CursorUtil.getColumnIndexOrThrow(_cursor, "passengerId");
      final int _cursorIndexOfPassengerName = CursorUtil.getColumnIndexOrThrow(_cursor, "passengerName");
      final int _cursorIndexOfPassengerPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "passengerPhone");
      final int _cursorIndexOfEmergencyContactPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "emergencyContactPhone");
      final int _cursorIndexOfVesselName = CursorUtil.getColumnIndexOrThrow(_cursor, "vesselName");
      final int _cursorIndexOfOriginGhatId = CursorUtil.getColumnIndexOrThrow(_cursor, "originGhatId");
      final int _cursorIndexOfOriginGhatName = CursorUtil.getColumnIndexOrThrow(_cursor, "originGhatName");
      final int _cursorIndexOfDestGhatId = CursorUtil.getColumnIndexOrThrow(_cursor, "destGhatId");
      final int _cursorIndexOfDestGhatName = CursorUtil.getColumnIndexOrThrow(_cursor, "destGhatName");
      final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
      final int _cursorIndexOfLastUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedAt");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfCurrentLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "currentLatitude");
      final int _cursorIndexOfCurrentLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "currentLongitude");
      final int _cursorIndexOfCurrentSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "currentSpeedKmh");
      final int _cursorIndexOfTotalDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDistanceKm");
      final int _cursorIndexOfNearestGhatName = CursorUtil.getColumnIndexOrThrow(_cursor, "nearestGhatName");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
      final ActiveJourneyEntity _result;
      if(_cursor.moveToFirst()) {
        final String _tmpJourneyCode;
        if (_cursor.isNull(_cursorIndexOfJourneyCode)) {
          _tmpJourneyCode = null;
        } else {
          _tmpJourneyCode = _cursor.getString(_cursorIndexOfJourneyCode);
        }
        final String _tmpPassengerId;
        if (_cursor.isNull(_cursorIndexOfPassengerId)) {
          _tmpPassengerId = null;
        } else {
          _tmpPassengerId = _cursor.getString(_cursorIndexOfPassengerId);
        }
        final String _tmpPassengerName;
        if (_cursor.isNull(_cursorIndexOfPassengerName)) {
          _tmpPassengerName = null;
        } else {
          _tmpPassengerName = _cursor.getString(_cursorIndexOfPassengerName);
        }
        final String _tmpPassengerPhone;
        if (_cursor.isNull(_cursorIndexOfPassengerPhone)) {
          _tmpPassengerPhone = null;
        } else {
          _tmpPassengerPhone = _cursor.getString(_cursorIndexOfPassengerPhone);
        }
        final String _tmpEmergencyContactPhone;
        if (_cursor.isNull(_cursorIndexOfEmergencyContactPhone)) {
          _tmpEmergencyContactPhone = null;
        } else {
          _tmpEmergencyContactPhone = _cursor.getString(_cursorIndexOfEmergencyContactPhone);
        }
        final String _tmpVesselName;
        if (_cursor.isNull(_cursorIndexOfVesselName)) {
          _tmpVesselName = null;
        } else {
          _tmpVesselName = _cursor.getString(_cursorIndexOfVesselName);
        }
        final String _tmpOriginGhatId;
        if (_cursor.isNull(_cursorIndexOfOriginGhatId)) {
          _tmpOriginGhatId = null;
        } else {
          _tmpOriginGhatId = _cursor.getString(_cursorIndexOfOriginGhatId);
        }
        final String _tmpOriginGhatName;
        if (_cursor.isNull(_cursorIndexOfOriginGhatName)) {
          _tmpOriginGhatName = null;
        } else {
          _tmpOriginGhatName = _cursor.getString(_cursorIndexOfOriginGhatName);
        }
        final String _tmpDestGhatId;
        if (_cursor.isNull(_cursorIndexOfDestGhatId)) {
          _tmpDestGhatId = null;
        } else {
          _tmpDestGhatId = _cursor.getString(_cursorIndexOfDestGhatId);
        }
        final String _tmpDestGhatName;
        if (_cursor.isNull(_cursorIndexOfDestGhatName)) {
          _tmpDestGhatName = null;
        } else {
          _tmpDestGhatName = _cursor.getString(_cursorIndexOfDestGhatName);
        }
        final long _tmpStartedAt;
        _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
        final long _tmpLastUpdatedAt;
        _tmpLastUpdatedAt = _cursor.getLong(_cursorIndexOfLastUpdatedAt);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        final double _tmpCurrentLatitude;
        _tmpCurrentLatitude = _cursor.getDouble(_cursorIndexOfCurrentLatitude);
        final double _tmpCurrentLongitude;
        _tmpCurrentLongitude = _cursor.getDouble(_cursorIndexOfCurrentLongitude);
        final double _tmpCurrentSpeedKmh;
        _tmpCurrentSpeedKmh = _cursor.getDouble(_cursorIndexOfCurrentSpeedKmh);
        final double _tmpTotalDistanceKm;
        _tmpTotalDistanceKm = _cursor.getDouble(_cursorIndexOfTotalDistanceKm);
        final String _tmpNearestGhatName;
        if (_cursor.isNull(_cursorIndexOfNearestGhatName)) {
          _tmpNearestGhatName = null;
        } else {
          _tmpNearestGhatName = _cursor.getString(_cursorIndexOfNearestGhatName);
        }
        final boolean _tmpIsSynced;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp != 0;
        _result = new ActiveJourneyEntity(_tmpJourneyCode,_tmpPassengerId,_tmpPassengerName,_tmpPassengerPhone,_tmpEmergencyContactPhone,_tmpVesselName,_tmpOriginGhatId,_tmpOriginGhatName,_tmpDestGhatId,_tmpDestGhatName,_tmpStartedAt,_tmpLastUpdatedAt,_tmpStatus,_tmpCurrentLatitude,_tmpCurrentLongitude,_tmpCurrentSpeedKmh,_tmpTotalDistanceKm,_tmpNearestGhatName,_tmpIsSynced);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ActiveJourneyEntity> getAllJourneys() {
    final String _sql = "SELECT * FROM active_journeys ORDER BY startedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfJourneyCode = CursorUtil.getColumnIndexOrThrow(_cursor, "journeyCode");
      final int _cursorIndexOfPassengerId = CursorUtil.getColumnIndexOrThrow(_cursor, "passengerId");
      final int _cursorIndexOfPassengerName = CursorUtil.getColumnIndexOrThrow(_cursor, "passengerName");
      final int _cursorIndexOfPassengerPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "passengerPhone");
      final int _cursorIndexOfEmergencyContactPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "emergencyContactPhone");
      final int _cursorIndexOfVesselName = CursorUtil.getColumnIndexOrThrow(_cursor, "vesselName");
      final int _cursorIndexOfOriginGhatId = CursorUtil.getColumnIndexOrThrow(_cursor, "originGhatId");
      final int _cursorIndexOfOriginGhatName = CursorUtil.getColumnIndexOrThrow(_cursor, "originGhatName");
      final int _cursorIndexOfDestGhatId = CursorUtil.getColumnIndexOrThrow(_cursor, "destGhatId");
      final int _cursorIndexOfDestGhatName = CursorUtil.getColumnIndexOrThrow(_cursor, "destGhatName");
      final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
      final int _cursorIndexOfLastUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedAt");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfCurrentLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "currentLatitude");
      final int _cursorIndexOfCurrentLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "currentLongitude");
      final int _cursorIndexOfCurrentSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "currentSpeedKmh");
      final int _cursorIndexOfTotalDistanceKm = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDistanceKm");
      final int _cursorIndexOfNearestGhatName = CursorUtil.getColumnIndexOrThrow(_cursor, "nearestGhatName");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
      final List<ActiveJourneyEntity> _result = new ArrayList<ActiveJourneyEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final ActiveJourneyEntity _item;
        final String _tmpJourneyCode;
        if (_cursor.isNull(_cursorIndexOfJourneyCode)) {
          _tmpJourneyCode = null;
        } else {
          _tmpJourneyCode = _cursor.getString(_cursorIndexOfJourneyCode);
        }
        final String _tmpPassengerId;
        if (_cursor.isNull(_cursorIndexOfPassengerId)) {
          _tmpPassengerId = null;
        } else {
          _tmpPassengerId = _cursor.getString(_cursorIndexOfPassengerId);
        }
        final String _tmpPassengerName;
        if (_cursor.isNull(_cursorIndexOfPassengerName)) {
          _tmpPassengerName = null;
        } else {
          _tmpPassengerName = _cursor.getString(_cursorIndexOfPassengerName);
        }
        final String _tmpPassengerPhone;
        if (_cursor.isNull(_cursorIndexOfPassengerPhone)) {
          _tmpPassengerPhone = null;
        } else {
          _tmpPassengerPhone = _cursor.getString(_cursorIndexOfPassengerPhone);
        }
        final String _tmpEmergencyContactPhone;
        if (_cursor.isNull(_cursorIndexOfEmergencyContactPhone)) {
          _tmpEmergencyContactPhone = null;
        } else {
          _tmpEmergencyContactPhone = _cursor.getString(_cursorIndexOfEmergencyContactPhone);
        }
        final String _tmpVesselName;
        if (_cursor.isNull(_cursorIndexOfVesselName)) {
          _tmpVesselName = null;
        } else {
          _tmpVesselName = _cursor.getString(_cursorIndexOfVesselName);
        }
        final String _tmpOriginGhatId;
        if (_cursor.isNull(_cursorIndexOfOriginGhatId)) {
          _tmpOriginGhatId = null;
        } else {
          _tmpOriginGhatId = _cursor.getString(_cursorIndexOfOriginGhatId);
        }
        final String _tmpOriginGhatName;
        if (_cursor.isNull(_cursorIndexOfOriginGhatName)) {
          _tmpOriginGhatName = null;
        } else {
          _tmpOriginGhatName = _cursor.getString(_cursorIndexOfOriginGhatName);
        }
        final String _tmpDestGhatId;
        if (_cursor.isNull(_cursorIndexOfDestGhatId)) {
          _tmpDestGhatId = null;
        } else {
          _tmpDestGhatId = _cursor.getString(_cursorIndexOfDestGhatId);
        }
        final String _tmpDestGhatName;
        if (_cursor.isNull(_cursorIndexOfDestGhatName)) {
          _tmpDestGhatName = null;
        } else {
          _tmpDestGhatName = _cursor.getString(_cursorIndexOfDestGhatName);
        }
        final long _tmpStartedAt;
        _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
        final long _tmpLastUpdatedAt;
        _tmpLastUpdatedAt = _cursor.getLong(_cursorIndexOfLastUpdatedAt);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        final double _tmpCurrentLatitude;
        _tmpCurrentLatitude = _cursor.getDouble(_cursorIndexOfCurrentLatitude);
        final double _tmpCurrentLongitude;
        _tmpCurrentLongitude = _cursor.getDouble(_cursorIndexOfCurrentLongitude);
        final double _tmpCurrentSpeedKmh;
        _tmpCurrentSpeedKmh = _cursor.getDouble(_cursorIndexOfCurrentSpeedKmh);
        final double _tmpTotalDistanceKm;
        _tmpTotalDistanceKm = _cursor.getDouble(_cursorIndexOfTotalDistanceKm);
        final String _tmpNearestGhatName;
        if (_cursor.isNull(_cursorIndexOfNearestGhatName)) {
          _tmpNearestGhatName = null;
        } else {
          _tmpNearestGhatName = _cursor.getString(_cursorIndexOfNearestGhatName);
        }
        final boolean _tmpIsSynced;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp != 0;
        _item = new ActiveJourneyEntity(_tmpJourneyCode,_tmpPassengerId,_tmpPassengerName,_tmpPassengerPhone,_tmpEmergencyContactPhone,_tmpVesselName,_tmpOriginGhatId,_tmpOriginGhatName,_tmpDestGhatId,_tmpDestGhatName,_tmpStartedAt,_tmpLastUpdatedAt,_tmpStatus,_tmpCurrentLatitude,_tmpCurrentLongitude,_tmpCurrentSpeedKmh,_tmpTotalDistanceKm,_tmpNearestGhatName,_tmpIsSynced);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<JourneyBreadcrumbEntity> getBreadcrumbsForJourney(final String code) {
    final String _sql = "SELECT * FROM journey_breadcrumbs WHERE journeyCode = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (code == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, code);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfJourneyCode = CursorUtil.getColumnIndexOrThrow(_cursor, "journeyCode");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "speedKmh");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
      final List<JourneyBreadcrumbEntity> _result = new ArrayList<JourneyBreadcrumbEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final JourneyBreadcrumbEntity _item;
        final String _tmpJourneyCode;
        if (_cursor.isNull(_cursorIndexOfJourneyCode)) {
          _tmpJourneyCode = null;
        } else {
          _tmpJourneyCode = _cursor.getString(_cursorIndexOfJourneyCode);
        }
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        final double _tmpSpeedKmh;
        _tmpSpeedKmh = _cursor.getDouble(_cursorIndexOfSpeedKmh);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        final boolean _tmpIsSynced;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp != 0;
        _item = new JourneyBreadcrumbEntity(_tmpJourneyCode,_tmpLatitude,_tmpLongitude,_tmpSpeedKmh,_tmpTimestamp,_tmpIsSynced);
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _item.setId(_tmpId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<JourneyBreadcrumbEntity> getUnsyncedBreadcrumbs() {
    final String _sql = "SELECT * FROM journey_breadcrumbs WHERE isSynced = 0 ORDER BY timestamp ASC LIMIT 50";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfJourneyCode = CursorUtil.getColumnIndexOrThrow(_cursor, "journeyCode");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfSpeedKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "speedKmh");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
      final List<JourneyBreadcrumbEntity> _result = new ArrayList<JourneyBreadcrumbEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final JourneyBreadcrumbEntity _item;
        final String _tmpJourneyCode;
        if (_cursor.isNull(_cursorIndexOfJourneyCode)) {
          _tmpJourneyCode = null;
        } else {
          _tmpJourneyCode = _cursor.getString(_cursorIndexOfJourneyCode);
        }
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        final double _tmpSpeedKmh;
        _tmpSpeedKmh = _cursor.getDouble(_cursorIndexOfSpeedKmh);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        final boolean _tmpIsSynced;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp != 0;
        _item = new JourneyBreadcrumbEntity(_tmpJourneyCode,_tmpLatitude,_tmpLongitude,_tmpSpeedKmh,_tmpTimestamp,_tmpIsSynced);
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _item.setId(_tmpId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public void markBreadcrumbsSynced(final List<Long> ids) {
    __db.assertNotSuspendingTransaction();
    StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("UPDATE journey_breadcrumbs SET isSynced = 1 WHERE id IN (");
    final int _inputSize = ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
    int _argIndex = 1;
    for (Long _item : ids) {
      if (_item == null) {
        _stmt.bindNull(_argIndex);
      } else {
        _stmt.bindLong(_argIndex, _item);
      }
      _argIndex ++;
    }
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
