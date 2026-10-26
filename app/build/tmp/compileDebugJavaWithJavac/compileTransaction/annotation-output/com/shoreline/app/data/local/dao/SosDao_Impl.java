package com.shoreline.app.data.local.dao;

import android.database.Cursor;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shoreline.app.data.local.entity.SosSignalEntity;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class SosDao_Impl implements SosDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SosSignalEntity> __insertionAdapterOfSosSignalEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsCloudSynced;

  public SosDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSosSignalEntity = new EntityInsertionAdapter<SosSignalEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `sos_signals` (`sos_id`,`sender_name`,`sender_phone`,`latitude`,`longitude`,`timestamp`,`hop_count`,`origin_device_id`,`status`,`is_relayed`,`is_synced_to_cloud`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, SosSignalEntity value) {
        if (value.getSosId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getSosId());
        }
        if (value.getSenderName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getSenderName());
        }
        if (value.getSenderPhone() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getSenderPhone());
        }
        stmt.bindDouble(4, value.getLatitude());
        stmt.bindDouble(5, value.getLongitude());
        stmt.bindLong(6, value.getTimestamp());
        stmt.bindLong(7, value.getHopCount());
        if (value.getOriginDeviceId() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getOriginDeviceId());
        }
        if (value.getStatus() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getStatus());
        }
        final int _tmp = value.isRelayed() ? 1 : 0;
        stmt.bindLong(10, _tmp);
        final int _tmp_1 = value.isSyncedToCloud() ? 1 : 0;
        stmt.bindLong(11, _tmp_1);
      }
    };
    this.__preparedStmtOfMarkAsCloudSynced = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "UPDATE sos_signals SET is_synced_to_cloud = 1 WHERE sos_id = ?";
        return _query;
      }
    };
  }

  @Override
  public void insertSos(final SosSignalEntity sos) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfSosSignalEntity.insert(sos);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void markAsCloudSynced(final String sosId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsCloudSynced.acquire();
    int _argIndex = 1;
    if (sosId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, sosId);
    }
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfMarkAsCloudSynced.release(_stmt);
    }
  }

  @Override
  public SosSignalEntity getSosById(final String sosId) {
    final String _sql = "SELECT * FROM sos_signals WHERE sos_id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (sosId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, sosId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfSosId = CursorUtil.getColumnIndexOrThrow(_cursor, "sos_id");
      final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "sender_name");
      final int _cursorIndexOfSenderPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "sender_phone");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hop_count");
      final int _cursorIndexOfOriginDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "origin_device_id");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfIsRelayed = CursorUtil.getColumnIndexOrThrow(_cursor, "is_relayed");
      final int _cursorIndexOfIsSyncedToCloud = CursorUtil.getColumnIndexOrThrow(_cursor, "is_synced_to_cloud");
      final SosSignalEntity _result;
      if(_cursor.moveToFirst()) {
        final String _tmpSosId;
        if (_cursor.isNull(_cursorIndexOfSosId)) {
          _tmpSosId = null;
        } else {
          _tmpSosId = _cursor.getString(_cursorIndexOfSosId);
        }
        final String _tmpSenderName;
        if (_cursor.isNull(_cursorIndexOfSenderName)) {
          _tmpSenderName = null;
        } else {
          _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
        }
        final String _tmpSenderPhone;
        if (_cursor.isNull(_cursorIndexOfSenderPhone)) {
          _tmpSenderPhone = null;
        } else {
          _tmpSenderPhone = _cursor.getString(_cursorIndexOfSenderPhone);
        }
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        final int _tmpHopCount;
        _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
        final String _tmpOriginDeviceId;
        if (_cursor.isNull(_cursorIndexOfOriginDeviceId)) {
          _tmpOriginDeviceId = null;
        } else {
          _tmpOriginDeviceId = _cursor.getString(_cursorIndexOfOriginDeviceId);
        }
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        final boolean _tmpIsRelayed;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsRelayed);
        _tmpIsRelayed = _tmp != 0;
        final boolean _tmpIsSyncedToCloud;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsSyncedToCloud);
        _tmpIsSyncedToCloud = _tmp_1 != 0;
        _result = new SosSignalEntity(_tmpSosId,_tmpSenderName,_tmpSenderPhone,_tmpLatitude,_tmpLongitude,_tmpTimestamp,_tmpHopCount,_tmpOriginDeviceId,_tmpStatus,_tmpIsRelayed,_tmpIsSyncedToCloud);
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
  public List<SosSignalEntity> getPendingCloudSyncSos() {
    final String _sql = "SELECT * FROM sos_signals WHERE is_synced_to_cloud = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfSosId = CursorUtil.getColumnIndexOrThrow(_cursor, "sos_id");
      final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "sender_name");
      final int _cursorIndexOfSenderPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "sender_phone");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hop_count");
      final int _cursorIndexOfOriginDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "origin_device_id");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfIsRelayed = CursorUtil.getColumnIndexOrThrow(_cursor, "is_relayed");
      final int _cursorIndexOfIsSyncedToCloud = CursorUtil.getColumnIndexOrThrow(_cursor, "is_synced_to_cloud");
      final List<SosSignalEntity> _result = new ArrayList<SosSignalEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final SosSignalEntity _item;
        final String _tmpSosId;
        if (_cursor.isNull(_cursorIndexOfSosId)) {
          _tmpSosId = null;
        } else {
          _tmpSosId = _cursor.getString(_cursorIndexOfSosId);
        }
        final String _tmpSenderName;
        if (_cursor.isNull(_cursorIndexOfSenderName)) {
          _tmpSenderName = null;
        } else {
          _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
        }
        final String _tmpSenderPhone;
        if (_cursor.isNull(_cursorIndexOfSenderPhone)) {
          _tmpSenderPhone = null;
        } else {
          _tmpSenderPhone = _cursor.getString(_cursorIndexOfSenderPhone);
        }
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        final int _tmpHopCount;
        _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
        final String _tmpOriginDeviceId;
        if (_cursor.isNull(_cursorIndexOfOriginDeviceId)) {
          _tmpOriginDeviceId = null;
        } else {
          _tmpOriginDeviceId = _cursor.getString(_cursorIndexOfOriginDeviceId);
        }
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        final boolean _tmpIsRelayed;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsRelayed);
        _tmpIsRelayed = _tmp != 0;
        final boolean _tmpIsSyncedToCloud;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsSyncedToCloud);
        _tmpIsSyncedToCloud = _tmp_1 != 0;
        _item = new SosSignalEntity(_tmpSosId,_tmpSenderName,_tmpSenderPhone,_tmpLatitude,_tmpLongitude,_tmpTimestamp,_tmpHopCount,_tmpOriginDeviceId,_tmpStatus,_tmpIsRelayed,_tmpIsSyncedToCloud);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<SosSignalEntity> getAllSosSignals() {
    final String _sql = "SELECT * FROM sos_signals ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfSosId = CursorUtil.getColumnIndexOrThrow(_cursor, "sos_id");
      final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "sender_name");
      final int _cursorIndexOfSenderPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "sender_phone");
      final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
      final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfHopCount = CursorUtil.getColumnIndexOrThrow(_cursor, "hop_count");
      final int _cursorIndexOfOriginDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "origin_device_id");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfIsRelayed = CursorUtil.getColumnIndexOrThrow(_cursor, "is_relayed");
      final int _cursorIndexOfIsSyncedToCloud = CursorUtil.getColumnIndexOrThrow(_cursor, "is_synced_to_cloud");
      final List<SosSignalEntity> _result = new ArrayList<SosSignalEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final SosSignalEntity _item;
        final String _tmpSosId;
        if (_cursor.isNull(_cursorIndexOfSosId)) {
          _tmpSosId = null;
        } else {
          _tmpSosId = _cursor.getString(_cursorIndexOfSosId);
        }
        final String _tmpSenderName;
        if (_cursor.isNull(_cursorIndexOfSenderName)) {
          _tmpSenderName = null;
        } else {
          _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
        }
        final String _tmpSenderPhone;
        if (_cursor.isNull(_cursorIndexOfSenderPhone)) {
          _tmpSenderPhone = null;
        } else {
          _tmpSenderPhone = _cursor.getString(_cursorIndexOfSenderPhone);
        }
        final double _tmpLatitude;
        _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
        final double _tmpLongitude;
        _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        final int _tmpHopCount;
        _tmpHopCount = _cursor.getInt(_cursorIndexOfHopCount);
        final String _tmpOriginDeviceId;
        if (_cursor.isNull(_cursorIndexOfOriginDeviceId)) {
          _tmpOriginDeviceId = null;
        } else {
          _tmpOriginDeviceId = _cursor.getString(_cursorIndexOfOriginDeviceId);
        }
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        final boolean _tmpIsRelayed;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsRelayed);
        _tmpIsRelayed = _tmp != 0;
        final boolean _tmpIsSyncedToCloud;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsSyncedToCloud);
        _tmpIsSyncedToCloud = _tmp_1 != 0;
        _item = new SosSignalEntity(_tmpSosId,_tmpSenderName,_tmpSenderPhone,_tmpLatitude,_tmpLongitude,_tmpTimestamp,_tmpHopCount,_tmpOriginDeviceId,_tmpStatus,_tmpIsRelayed,_tmpIsSyncedToCloud);
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
