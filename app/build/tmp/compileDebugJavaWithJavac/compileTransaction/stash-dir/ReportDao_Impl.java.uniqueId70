package com.shoreline.app.data.local.dao;

import android.database.Cursor;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shoreline.app.data.local.entity.OverloadReportEntity;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class ReportDao_Impl implements ReportDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<OverloadReportEntity> __insertionAdapterOfOverloadReportEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsSynced;

  public ReportDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfOverloadReportEntity = new EntityInsertionAdapter<OverloadReportEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `overload_reports` (`report_id`,`vessel_id`,`vessel_number`,`crowd_level`,`passenger_count_estimate`,`latitude`,`longitude`,`timestamp`,`photo_base64`,`reporter_user_id`,`is_anonymous`,`is_synced`,`sync_timestamp`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, OverloadReportEntity value) {
        if (value.getReportId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getReportId());
        }
        if (value.getVesselId() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getVesselId());
        }
        if (value.getVesselNumber() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getVesselNumber());
        }
        if (value.getCrowdLevel() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getCrowdLevel());
        }
        stmt.bindLong(5, value.getEstimatedPassengerCount());
        stmt.bindDouble(6, value.getLatitude());
        stmt.bindDouble(7, value.getLongitude());
        stmt.bindLong(8, value.getTimestamp());
        if (value.getPhotoBase64() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getPhotoBase64());
        }
        if (value.getReporterUserId() == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindString(10, value.getReporterUserId());
        }
        final int _tmp = value.isAnonymous() ? 1 : 0;
        stmt.bindLong(11, _tmp);
        final int _tmp_1 = value.isSynced() ? 1 : 0;
        stmt.bindLong(12, _tmp_1);
        stmt.bindLong(13, value.getSyncTimestamp());
      }
    };
    this.__preparedStmtOfMarkAsSynced = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "UPDATE overload_reports SET is_synced = 1, sync_timestamp = ? WHERE report_id = ?";
        return _query;
      }
    };
  }

  @Override
  public void insertReport(final OverloadReportEntity report) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfOverloadReportEntity.insert(report);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void markAsSynced(final String reportId, final long syncTime) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsSynced.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, syncTime);
    _argIndex = 2;
    if (reportId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, reportId);
    }
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfMarkAsSynced.release(_stmt);
    }
  }

  @Override
  public List<OverloadReportEntity> getPendingSyncReports() {
    final String _sql = "SELECT * FROM overload_reports WHERE is_synced = 0 ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfReportId = CursorUtil.getColumnIndexOrThrow(_cursor, "report_id");
      final int _cursorIndexOfVesselId = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_id");
      final int _cursorIndexOfVesselNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_number");
      final int _cursorIndexOfCrowdLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "crowd_level");
      final int _cursorIndexOfEstimatedPassengerCount = CursorUtil.getColumnIndexOrThrow(_cursor, "passenger_count_estimate");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfPhotoBase64 = CursorUtil.getColumnIndexOrThrow(_cursor, "photo_base64");
      final int _cursorIndexOfReporterUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "reporter_user_id");
      final int _cursorIndexOfIsAnonymous = CursorUtil.getColumnIndexOrThrow(_cursor, "is_anonymous");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "is_synced");
      final int _cursorIndexOfSyncTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_timestamp");
      final List<OverloadReportEntity> _result = new ArrayList<OverloadReportEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final OverloadReportEntity _item;
        final String _tmpReportId;
        if (_cursor.isNull(_cursorIndexOfReportId)) {
          _tmpReportId = null;
        } else {
          _tmpReportId = _cursor.getString(_cursorIndexOfReportId);
        }
        final String _tmpVesselId;
        if (_cursor.isNull(_cursorIndexOfVesselId)) {
          _tmpVesselId = null;
        } else {
          _tmpVesselId = _cursor.getString(_cursorIndexOfVesselId);
        }
        final String _tmpVesselNumber;
        if (_cursor.isNull(_cursorIndexOfVesselNumber)) {
          _tmpVesselNumber = null;
        } else {
          _tmpVesselNumber = _cursor.getString(_cursorIndexOfVesselNumber);
        }
        final String _tmpCrowdLevel;
        if (_cursor.isNull(_cursorIndexOfCrowdLevel)) {
          _tmpCrowdLevel = null;
        } else {
          _tmpCrowdLevel = _cursor.getString(_cursorIndexOfCrowdLevel);
        }
        final int _tmpEstimatedPassengerCount;
        _tmpEstimatedPassengerCount = _cursor.getInt(_cursorIndexOfEstimatedPassengerCount);
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        final String _tmpPhotoBase64;
        if (_cursor.isNull(_cursorIndexOfPhotoBase64)) {
          _tmpPhotoBase64 = null;
        } else {
          _tmpPhotoBase64 = _cursor.getString(_cursorIndexOfPhotoBase64);
        }
        final String _tmpReporterUserId;
        if (_cursor.isNull(_cursorIndexOfReporterUserId)) {
          _tmpReporterUserId = null;
        } else {
          _tmpReporterUserId = _cursor.getString(_cursorIndexOfReporterUserId);
        }
        final boolean _tmpIsAnonymous;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAnonymous);
        _tmpIsAnonymous = _tmp != 0;
        final boolean _tmpIsSynced;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp_1 != 0;
        final long _tmpSyncTimestamp;
        _tmpSyncTimestamp = _cursor.getLong(_cursorIndexOfSyncTimestamp);
        _item = new OverloadReportEntity(_tmpReportId,_tmpVesselId,_tmpVesselNumber,_tmpCrowdLevel,_tmpEstimatedPassengerCount,_tmpLatitude,_tmpLongitude,_tmpTimestamp,_tmpPhotoBase64,_tmpReporterUserId,_tmpIsAnonymous,_tmpIsSynced,_tmpSyncTimestamp);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<OverloadReportEntity> getAllReports() {
    final String _sql = "SELECT * FROM overload_reports ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfReportId = CursorUtil.getColumnIndexOrThrow(_cursor, "report_id");
      final int _cursorIndexOfVesselId = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_id");
      final int _cursorIndexOfVesselNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_number");
      final int _cursorIndexOfCrowdLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "crowd_level");
      final int _cursorIndexOfEstimatedPassengerCount = CursorUtil.getColumnIndexOrThrow(_cursor, "passenger_count_estimate");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfPhotoBase64 = CursorUtil.getColumnIndexOrThrow(_cursor, "photo_base64");
      final int _cursorIndexOfReporterUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "reporter_user_id");
      final int _cursorIndexOfIsAnonymous = CursorUtil.getColumnIndexOrThrow(_cursor, "is_anonymous");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "is_synced");
      final int _cursorIndexOfSyncTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_timestamp");
      final List<OverloadReportEntity> _result = new ArrayList<OverloadReportEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final OverloadReportEntity _item;
        final String _tmpReportId;
        if (_cursor.isNull(_cursorIndexOfReportId)) {
          _tmpReportId = null;
        } else {
          _tmpReportId = _cursor.getString(_cursorIndexOfReportId);
        }
        final String _tmpVesselId;
        if (_cursor.isNull(_cursorIndexOfVesselId)) {
          _tmpVesselId = null;
        } else {
          _tmpVesselId = _cursor.getString(_cursorIndexOfVesselId);
        }
        final String _tmpVesselNumber;
        if (_cursor.isNull(_cursorIndexOfVesselNumber)) {
          _tmpVesselNumber = null;
        } else {
          _tmpVesselNumber = _cursor.getString(_cursorIndexOfVesselNumber);
        }
        final String _tmpCrowdLevel;
        if (_cursor.isNull(_cursorIndexOfCrowdLevel)) {
          _tmpCrowdLevel = null;
        } else {
          _tmpCrowdLevel = _cursor.getString(_cursorIndexOfCrowdLevel);
        }
        final int _tmpEstimatedPassengerCount;
        _tmpEstimatedPassengerCount = _cursor.getInt(_cursorIndexOfEstimatedPassengerCount);
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        final String _tmpPhotoBase64;
        if (_cursor.isNull(_cursorIndexOfPhotoBase64)) {
          _tmpPhotoBase64 = null;
        } else {
          _tmpPhotoBase64 = _cursor.getString(_cursorIndexOfPhotoBase64);
        }
        final String _tmpReporterUserId;
        if (_cursor.isNull(_cursorIndexOfReporterUserId)) {
          _tmpReporterUserId = null;
        } else {
          _tmpReporterUserId = _cursor.getString(_cursorIndexOfReporterUserId);
        }
        final boolean _tmpIsAnonymous;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAnonymous);
        _tmpIsAnonymous = _tmp != 0;
        final boolean _tmpIsSynced;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp_1 != 0;
        final long _tmpSyncTimestamp;
        _tmpSyncTimestamp = _cursor.getLong(_cursorIndexOfSyncTimestamp);
        _item = new OverloadReportEntity(_tmpReportId,_tmpVesselId,_tmpVesselNumber,_tmpCrowdLevel,_tmpEstimatedPassengerCount,_tmpLatitude,_tmpLongitude,_tmpTimestamp,_tmpPhotoBase64,_tmpReporterUserId,_tmpIsAnonymous,_tmpIsSynced,_tmpSyncTimestamp);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<OverloadReportEntity> getReportsForVessel(final String vesselId) {
    final String _sql = "SELECT * FROM overload_reports WHERE vessel_id = ? ORDER BY timestamp DESC";
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
      final int _cursorIndexOfReportId = CursorUtil.getColumnIndexOrThrow(_cursor, "report_id");
      final int _cursorIndexOfVesselId = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_id");
      final int _cursorIndexOfVesselNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "vessel_number");
      final int _cursorIndexOfCrowdLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "crowd_level");
      final int _cursorIndexOfEstimatedPassengerCount = CursorUtil.getColumnIndexOrThrow(_cursor, "passenger_count_estimate");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfPhotoBase64 = CursorUtil.getColumnIndexOrThrow(_cursor, "photo_base64");
      final int _cursorIndexOfReporterUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "reporter_user_id");
      final int _cursorIndexOfIsAnonymous = CursorUtil.getColumnIndexOrThrow(_cursor, "is_anonymous");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "is_synced");
      final int _cursorIndexOfSyncTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_timestamp");
      final List<OverloadReportEntity> _result = new ArrayList<OverloadReportEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final OverloadReportEntity _item;
        final String _tmpReportId;
        if (_cursor.isNull(_cursorIndexOfReportId)) {
          _tmpReportId = null;
        } else {
          _tmpReportId = _cursor.getString(_cursorIndexOfReportId);
        }
        final String _tmpVesselId;
        if (_cursor.isNull(_cursorIndexOfVesselId)) {
          _tmpVesselId = null;
        } else {
          _tmpVesselId = _cursor.getString(_cursorIndexOfVesselId);
        }
        final String _tmpVesselNumber;
        if (_cursor.isNull(_cursorIndexOfVesselNumber)) {
          _tmpVesselNumber = null;
        } else {
          _tmpVesselNumber = _cursor.getString(_cursorIndexOfVesselNumber);
        }
        final String _tmpCrowdLevel;
        if (_cursor.isNull(_cursorIndexOfCrowdLevel)) {
          _tmpCrowdLevel = null;
        } else {
          _tmpCrowdLevel = _cursor.getString(_cursorIndexOfCrowdLevel);
        }
        final int _tmpEstimatedPassengerCount;
        _tmpEstimatedPassengerCount = _cursor.getInt(_cursorIndexOfEstimatedPassengerCount);
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        final String _tmpPhotoBase64;
        if (_cursor.isNull(_cursorIndexOfPhotoBase64)) {
          _tmpPhotoBase64 = null;
        } else {
          _tmpPhotoBase64 = _cursor.getString(_cursorIndexOfPhotoBase64);
        }
        final String _tmpReporterUserId;
        if (_cursor.isNull(_cursorIndexOfReporterUserId)) {
          _tmpReporterUserId = null;
        } else {
          _tmpReporterUserId = _cursor.getString(_cursorIndexOfReporterUserId);
        }
        final boolean _tmpIsAnonymous;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAnonymous);
        _tmpIsAnonymous = _tmp != 0;
        final boolean _tmpIsSynced;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp_1 != 0;
        final long _tmpSyncTimestamp;
        _tmpSyncTimestamp = _cursor.getLong(_cursorIndexOfSyncTimestamp);
        _item = new OverloadReportEntity(_tmpReportId,_tmpVesselId,_tmpVesselNumber,_tmpCrowdLevel,_tmpEstimatedPassengerCount,_tmpLatitude,_tmpLongitude,_tmpTimestamp,_tmpPhotoBase64,_tmpReporterUserId,_tmpIsAnonymous,_tmpIsSynced,_tmpSyncTimestamp);
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
