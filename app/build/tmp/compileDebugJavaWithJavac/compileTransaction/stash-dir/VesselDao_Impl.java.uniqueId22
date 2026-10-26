package com.shoreline.app.data.local.dao;

import android.database.Cursor;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shoreline.app.data.local.entity.VesselEntity;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class VesselDao_Impl implements VesselDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<VesselEntity> __insertionAdapterOfVesselEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAllVessels;

  public VesselDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfVesselEntity = new EntityInsertionAdapter<VesselEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `vessels` (`vessel_id`,`registration_number`,`vessel_name`,`route_name`,`licensed_capacity`,`fitness_expiry`,`community_rating`,`total_reports`,`is_flagged`,`cached_at`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, VesselEntity value) {
        if (value.getVesselId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getVesselId());
        }
        if (value.getRegistrationNumber() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getRegistrationNumber());
        }
        if (value.getVesselName() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getVesselName());
        }
        if (value.getRouteName() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getRouteName());
        }
        stmt.bindLong(5, value.getLicensedCapacity());
        if (value.getFitnessExpiryDate() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getFitnessExpiryDate());
        }
        stmt.bindDouble(7, value.getCommunityRating());
        stmt.bindLong(8, value.getTotalReports());
        final int _tmp = value.isFlagged() ? 1 : 0;
        stmt.bindLong(9, _tmp);
        stmt.bindLong(10, value.getCachedAt());
      }
    };
    this.__preparedStmtOfClearAllVessels = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM vessels";
        return _query;
      }
    };
  }

  @Override
  public void insertOrUpdateVessel(final VesselEntity vessel) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfVesselEntity.insert(vessel);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAll(final List<VesselEntity> vessels) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfVesselEntity.insert(vessels);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void clearAllVessels() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllVessels.acquire();
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfClearAllVessels.release(_stmt);
    }
  }

  @Override
  public VesselEntity getVesselByRegistrationNumber(final String regNumber) {
    final String _sql = "SELECT * FROM vessels WHERE registration_number = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (regNumber == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, regNumber);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfVesselId = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_id");
      final int _cursorIndexOfRegistrationNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "registration_number");
      final int _cursorIndexOfVesselName = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_name");
      final int _cursorIndexOfRouteName = CursorUtil.getColumnIndexOrThrow(_cursor, "route_name");
      final int _cursorIndexOfLicensedCapacity = CursorUtil.getColumnIndexOrThrow(_cursor, "licensed_capacity");
      final int _cursorIndexOfFitnessExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "fitness_expiry");
      final int _cursorIndexOfCommunityRating = CursorUtil.getColumnIndexOrThrow(_cursor, "community_rating");
      final int _cursorIndexOfTotalReports = CursorUtil.getColumnIndexOrThrow(_cursor, "total_reports");
      final int _cursorIndexOfIsFlagged = CursorUtil.getColumnIndexOrThrow(_cursor, "is_flagged");
      final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
      final VesselEntity _result;
      if(_cursor.moveToFirst()) {
        final String _tmpVesselId;
        if (_cursor.isNull(_cursorIndexOfVesselId)) {
          _tmpVesselId = null;
        } else {
          _tmpVesselId = _cursor.getString(_cursorIndexOfVesselId);
        }
        final String _tmpRegistrationNumber;
        if (_cursor.isNull(_cursorIndexOfRegistrationNumber)) {
          _tmpRegistrationNumber = null;
        } else {
          _tmpRegistrationNumber = _cursor.getString(_cursorIndexOfRegistrationNumber);
        }
        final String _tmpVesselName;
        if (_cursor.isNull(_cursorIndexOfVesselName)) {
          _tmpVesselName = null;
        } else {
          _tmpVesselName = _cursor.getString(_cursorIndexOfVesselName);
        }
        final String _tmpRouteName;
        if (_cursor.isNull(_cursorIndexOfRouteName)) {
          _tmpRouteName = null;
        } else {
          _tmpRouteName = _cursor.getString(_cursorIndexOfRouteName);
        }
        final int _tmpLicensedCapacity;
        _tmpLicensedCapacity = _cursor.getInt(_cursorIndexOfLicensedCapacity);
        final String _tmpFitnessExpiryDate;
        if (_cursor.isNull(_cursorIndexOfFitnessExpiryDate)) {
          _tmpFitnessExpiryDate = null;
        } else {
          _tmpFitnessExpiryDate = _cursor.getString(_cursorIndexOfFitnessExpiryDate);
        }
        final float _tmpCommunityRating;
        _tmpCommunityRating = _cursor.getFloat(_cursorIndexOfCommunityRating);
        final int _tmpTotalReports;
        _tmpTotalReports = _cursor.getInt(_cursorIndexOfTotalReports);
        final boolean _tmpIsFlagged;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFlagged);
        _tmpIsFlagged = _tmp != 0;
        final long _tmpCachedAt;
        _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
        _result = new VesselEntity(_tmpVesselId,_tmpRegistrationNumber,_tmpVesselName,_tmpRouteName,_tmpLicensedCapacity,_tmpFitnessExpiryDate,_tmpCommunityRating,_tmpTotalReports,_tmpIsFlagged,_tmpCachedAt);
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
  public VesselEntity getVesselById(final String vesselId) {
    final String _sql = "SELECT * FROM vessels WHERE vessel_id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (vesselId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, vesselId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfVesselId = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_id");
      final int _cursorIndexOfRegistrationNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "registration_number");
      final int _cursorIndexOfVesselName = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_name");
      final int _cursorIndexOfRouteName = CursorUtil.getColumnIndexOrThrow(_cursor, "route_name");
      final int _cursorIndexOfLicensedCapacity = CursorUtil.getColumnIndexOrThrow(_cursor, "licensed_capacity");
      final int _cursorIndexOfFitnessExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "fitness_expiry");
      final int _cursorIndexOfCommunityRating = CursorUtil.getColumnIndexOrThrow(_cursor, "community_rating");
      final int _cursorIndexOfTotalReports = CursorUtil.getColumnIndexOrThrow(_cursor, "total_reports");
      final int _cursorIndexOfIsFlagged = CursorUtil.getColumnIndexOrThrow(_cursor, "is_flagged");
      final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
      final VesselEntity _result;
      if(_cursor.moveToFirst()) {
        final String _tmpVesselId;
        if (_cursor.isNull(_cursorIndexOfVesselId)) {
          _tmpVesselId = null;
        } else {
          _tmpVesselId = _cursor.getString(_cursorIndexOfVesselId);
        }
        final String _tmpRegistrationNumber;
        if (_cursor.isNull(_cursorIndexOfRegistrationNumber)) {
          _tmpRegistrationNumber = null;
        } else {
          _tmpRegistrationNumber = _cursor.getString(_cursorIndexOfRegistrationNumber);
        }
        final String _tmpVesselName;
        if (_cursor.isNull(_cursorIndexOfVesselName)) {
          _tmpVesselName = null;
        } else {
          _tmpVesselName = _cursor.getString(_cursorIndexOfVesselName);
        }
        final String _tmpRouteName;
        if (_cursor.isNull(_cursorIndexOfRouteName)) {
          _tmpRouteName = null;
        } else {
          _tmpRouteName = _cursor.getString(_cursorIndexOfRouteName);
        }
        final int _tmpLicensedCapacity;
        _tmpLicensedCapacity = _cursor.getInt(_cursorIndexOfLicensedCapacity);
        final String _tmpFitnessExpiryDate;
        if (_cursor.isNull(_cursorIndexOfFitnessExpiryDate)) {
          _tmpFitnessExpiryDate = null;
        } else {
          _tmpFitnessExpiryDate = _cursor.getString(_cursorIndexOfFitnessExpiryDate);
        }
        final float _tmpCommunityRating;
        _tmpCommunityRating = _cursor.getFloat(_cursorIndexOfCommunityRating);
        final int _tmpTotalReports;
        _tmpTotalReports = _cursor.getInt(_cursorIndexOfTotalReports);
        final boolean _tmpIsFlagged;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFlagged);
        _tmpIsFlagged = _tmp != 0;
        final long _tmpCachedAt;
        _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
        _result = new VesselEntity(_tmpVesselId,_tmpRegistrationNumber,_tmpVesselName,_tmpRouteName,_tmpLicensedCapacity,_tmpFitnessExpiryDate,_tmpCommunityRating,_tmpTotalReports,_tmpIsFlagged,_tmpCachedAt);
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
  public List<VesselEntity> getAllVessels() {
    final String _sql = "SELECT * FROM vessels ORDER BY vessel_name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfVesselId = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_id");
      final int _cursorIndexOfRegistrationNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "registration_number");
      final int _cursorIndexOfVesselName = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_name");
      final int _cursorIndexOfRouteName = CursorUtil.getColumnIndexOrThrow(_cursor, "route_name");
      final int _cursorIndexOfLicensedCapacity = CursorUtil.getColumnIndexOrThrow(_cursor, "licensed_capacity");
      final int _cursorIndexOfFitnessExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "fitness_expiry");
      final int _cursorIndexOfCommunityRating = CursorUtil.getColumnIndexOrThrow(_cursor, "community_rating");
      final int _cursorIndexOfTotalReports = CursorUtil.getColumnIndexOrThrow(_cursor, "total_reports");
      final int _cursorIndexOfIsFlagged = CursorUtil.getColumnIndexOrThrow(_cursor, "is_flagged");
      final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
      final List<VesselEntity> _result = new ArrayList<VesselEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final VesselEntity _item;
        final String _tmpVesselId;
        if (_cursor.isNull(_cursorIndexOfVesselId)) {
          _tmpVesselId = null;
        } else {
          _tmpVesselId = _cursor.getString(_cursorIndexOfVesselId);
        }
        final String _tmpRegistrationNumber;
        if (_cursor.isNull(_cursorIndexOfRegistrationNumber)) {
          _tmpRegistrationNumber = null;
        } else {
          _tmpRegistrationNumber = _cursor.getString(_cursorIndexOfRegistrationNumber);
        }
        final String _tmpVesselName;
        if (_cursor.isNull(_cursorIndexOfVesselName)) {
          _tmpVesselName = null;
        } else {
          _tmpVesselName = _cursor.getString(_cursorIndexOfVesselName);
        }
        final String _tmpRouteName;
        if (_cursor.isNull(_cursorIndexOfRouteName)) {
          _tmpRouteName = null;
        } else {
          _tmpRouteName = _cursor.getString(_cursorIndexOfRouteName);
        }
        final int _tmpLicensedCapacity;
        _tmpLicensedCapacity = _cursor.getInt(_cursorIndexOfLicensedCapacity);
        final String _tmpFitnessExpiryDate;
        if (_cursor.isNull(_cursorIndexOfFitnessExpiryDate)) {
          _tmpFitnessExpiryDate = null;
        } else {
          _tmpFitnessExpiryDate = _cursor.getString(_cursorIndexOfFitnessExpiryDate);
        }
        final float _tmpCommunityRating;
        _tmpCommunityRating = _cursor.getFloat(_cursorIndexOfCommunityRating);
        final int _tmpTotalReports;
        _tmpTotalReports = _cursor.getInt(_cursorIndexOfTotalReports);
        final boolean _tmpIsFlagged;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFlagged);
        _tmpIsFlagged = _tmp != 0;
        final long _tmpCachedAt;
        _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
        _item = new VesselEntity(_tmpVesselId,_tmpRegistrationNumber,_tmpVesselName,_tmpRouteName,_tmpLicensedCapacity,_tmpFitnessExpiryDate,_tmpCommunityRating,_tmpTotalReports,_tmpIsFlagged,_tmpCachedAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<VesselEntity> getFlaggedVessels() {
    final String _sql = "SELECT * FROM vessels WHERE is_flagged = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfVesselId = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_id");
      final int _cursorIndexOfRegistrationNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "registration_number");
      final int _cursorIndexOfVesselName = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_name");
      final int _cursorIndexOfRouteName = CursorUtil.getColumnIndexOrThrow(_cursor, "route_name");
      final int _cursorIndexOfLicensedCapacity = CursorUtil.getColumnIndexOrThrow(_cursor, "licensed_capacity");
      final int _cursorIndexOfFitnessExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "fitness_expiry");
      final int _cursorIndexOfCommunityRating = CursorUtil.getColumnIndexOrThrow(_cursor, "community_rating");
      final int _cursorIndexOfTotalReports = CursorUtil.getColumnIndexOrThrow(_cursor, "total_reports");
      final int _cursorIndexOfIsFlagged = CursorUtil.getColumnIndexOrThrow(_cursor, "is_flagged");
      final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
      final List<VesselEntity> _result = new ArrayList<VesselEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final VesselEntity _item;
        final String _tmpVesselId;
        if (_cursor.isNull(_cursorIndexOfVesselId)) {
          _tmpVesselId = null;
        } else {
          _tmpVesselId = _cursor.getString(_cursorIndexOfVesselId);
        }
        final String _tmpRegistrationNumber;
        if (_cursor.isNull(_cursorIndexOfRegistrationNumber)) {
          _tmpRegistrationNumber = null;
        } else {
          _tmpRegistrationNumber = _cursor.getString(_cursorIndexOfRegistrationNumber);
        }
        final String _tmpVesselName;
        if (_cursor.isNull(_cursorIndexOfVesselName)) {
          _tmpVesselName = null;
        } else {
          _tmpVesselName = _cursor.getString(_cursorIndexOfVesselName);
        }
        final String _tmpRouteName;
        if (_cursor.isNull(_cursorIndexOfRouteName)) {
          _tmpRouteName = null;
        } else {
          _tmpRouteName = _cursor.getString(_cursorIndexOfRouteName);
        }
        final int _tmpLicensedCapacity;
        _tmpLicensedCapacity = _cursor.getInt(_cursorIndexOfLicensedCapacity);
        final String _tmpFitnessExpiryDate;
        if (_cursor.isNull(_cursorIndexOfFitnessExpiryDate)) {
          _tmpFitnessExpiryDate = null;
        } else {
          _tmpFitnessExpiryDate = _cursor.getString(_cursorIndexOfFitnessExpiryDate);
        }
        final float _tmpCommunityRating;
        _tmpCommunityRating = _cursor.getFloat(_cursorIndexOfCommunityRating);
        final int _tmpTotalReports;
        _tmpTotalReports = _cursor.getInt(_cursorIndexOfTotalReports);
        final boolean _tmpIsFlagged;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsFlagged);
        _tmpIsFlagged = _tmp != 0;
        final long _tmpCachedAt;
        _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
        _item = new VesselEntity(_tmpVesselId,_tmpRegistrationNumber,_tmpVesselName,_tmpRouteName,_tmpLicensedCapacity,_tmpFitnessExpiryDate,_tmpCommunityRating,_tmpTotalReports,_tmpIsFlagged,_tmpCachedAt);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
