package com.shoreline.app.data.local.dao;

import android.database.Cursor;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shoreline.app.data.local.entity.NodeWeatherEntity;
import java.lang.Class;
import java.lang.Double;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class NodeWeatherDao_Impl implements NodeWeatherDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<NodeWeatherEntity> __insertionAdapterOfNodeWeatherEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldWeather;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public NodeWeatherDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfNodeWeatherEntity = new EntityInsertionAdapter<NodeWeatherEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `node_weather` (`nodeId`,`nodeName`,`forecastTime`,`tempC`,`windKmh`,`gustKmh`,`rainMm`,`weatherCode`,`waveHeightM`,`fetchedAt`,`riskLevel`,`riskMultiplier`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, NodeWeatherEntity value) {
        if (value.getNodeId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getNodeId());
        }
        if (value.getNodeName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getNodeName());
        }
        if (value.getForecastTime() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getForecastTime());
        }
        stmt.bindDouble(4, value.getTempC());
        stmt.bindDouble(5, value.getWindKmh());
        stmt.bindDouble(6, value.getGustKmh());
        stmt.bindDouble(7, value.getRainMm());
        stmt.bindLong(8, value.getWeatherCode());
        if (value.getWaveHeightM() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindDouble(9, value.getWaveHeightM());
        }
        stmt.bindLong(10, value.getFetchedAt());
        stmt.bindLong(11, value.getRiskLevel());
        stmt.bindDouble(12, value.getRiskMultiplier());
      }
    };
    this.__preparedStmtOfDeleteOldWeather = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM node_weather WHERE fetchedAt < ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM node_weather";
        return _query;
      }
    };
  }

  @Override
  public void insertAll(final List<NodeWeatherEntity> entities) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfNodeWeatherEntity.insert(entities);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteOldWeather(final long olderThanTimestamp) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldWeather.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, olderThanTimestamp);
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteOldWeather.release(_stmt);
    }
  }

  @Override
  public void clearAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfClearAll.release(_stmt);
    }
  }

  @Override
  public NodeWeatherEntity getNodeWeather(final String nodeId, final String forecastTime) {
    final String _sql = "SELECT * FROM node_weather WHERE nodeId = ? AND forecastTime = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (nodeId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, nodeId);
    }
    _argIndex = 2;
    if (forecastTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, forecastTime);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfNodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "nodeId");
      final int _cursorIndexOfNodeName = CursorUtil.getColumnIndexOrThrow(_cursor, "nodeName");
      final int _cursorIndexOfForecastTime = CursorUtil.getColumnIndexOrThrow(_cursor, "forecastTime");
      final int _cursorIndexOfTempC = CursorUtil.getColumnIndexOrThrow(_cursor, "tempC");
      final int _cursorIndexOfWindKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "windKmh");
      final int _cursorIndexOfGustKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "gustKmh");
      final int _cursorIndexOfRainMm = CursorUtil.getColumnIndexOrThrow(_cursor, "rainMm");
      final int _cursorIndexOfWeatherCode = CursorUtil.getColumnIndexOrThrow(_cursor, "weatherCode");
      final int _cursorIndexOfWaveHeightM = CursorUtil.getColumnIndexOrThrow(_cursor, "waveHeightM");
      final int _cursorIndexOfFetchedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "fetchedAt");
      final int _cursorIndexOfRiskLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "riskLevel");
      final int _cursorIndexOfRiskMultiplier = CursorUtil.getColumnIndexOrThrow(_cursor, "riskMultiplier");
      final NodeWeatherEntity _result;
      if(_cursor.moveToFirst()) {
        final String _tmpNodeId;
        if (_cursor.isNull(_cursorIndexOfNodeId)) {
          _tmpNodeId = null;
        } else {
          _tmpNodeId = _cursor.getString(_cursorIndexOfNodeId);
        }
        final String _tmpNodeName;
        if (_cursor.isNull(_cursorIndexOfNodeName)) {
          _tmpNodeName = null;
        } else {
          _tmpNodeName = _cursor.getString(_cursorIndexOfNodeName);
        }
        final String _tmpForecastTime;
        if (_cursor.isNull(_cursorIndexOfForecastTime)) {
          _tmpForecastTime = null;
        } else {
          _tmpForecastTime = _cursor.getString(_cursorIndexOfForecastTime);
        }
        final double _tmpTempC;
        _tmpTempC = _cursor.getDouble(_cursorIndexOfTempC);
        final double _tmpWindKmh;
        _tmpWindKmh = _cursor.getDouble(_cursorIndexOfWindKmh);
        final double _tmpGustKmh;
        _tmpGustKmh = _cursor.getDouble(_cursorIndexOfGustKmh);
        final double _tmpRainMm;
        _tmpRainMm = _cursor.getDouble(_cursorIndexOfRainMm);
        final int _tmpWeatherCode;
        _tmpWeatherCode = _cursor.getInt(_cursorIndexOfWeatherCode);
        final Double _tmpWaveHeightM;
        if (_cursor.isNull(_cursorIndexOfWaveHeightM)) {
          _tmpWaveHeightM = null;
        } else {
          _tmpWaveHeightM = _cursor.getDouble(_cursorIndexOfWaveHeightM);
        }
        final long _tmpFetchedAt;
        _tmpFetchedAt = _cursor.getLong(_cursorIndexOfFetchedAt);
        final int _tmpRiskLevel;
        _tmpRiskLevel = _cursor.getInt(_cursorIndexOfRiskLevel);
        final double _tmpRiskMultiplier;
        _tmpRiskMultiplier = _cursor.getDouble(_cursorIndexOfRiskMultiplier);
        _result = new NodeWeatherEntity(_tmpNodeId,_tmpNodeName,_tmpForecastTime,_tmpTempC,_tmpWindKmh,_tmpGustKmh,_tmpRainMm,_tmpWeatherCode,_tmpWaveHeightM,_tmpFetchedAt,_tmpRiskLevel,_tmpRiskMultiplier);
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
  public NodeWeatherEntity getNodeWeatherNearest(final String nodeId, final String targetTime) {
    final String _sql = "SELECT * FROM node_weather WHERE nodeId = ? ORDER BY ABS(strftime('%s', forecastTime) - strftime('%s', ?)) ASC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (nodeId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, nodeId);
    }
    _argIndex = 2;
    if (targetTime == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, targetTime);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfNodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "nodeId");
      final int _cursorIndexOfNodeName = CursorUtil.getColumnIndexOrThrow(_cursor, "nodeName");
      final int _cursorIndexOfForecastTime = CursorUtil.getColumnIndexOrThrow(_cursor, "forecastTime");
      final int _cursorIndexOfTempC = CursorUtil.getColumnIndexOrThrow(_cursor, "tempC");
      final int _cursorIndexOfWindKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "windKmh");
      final int _cursorIndexOfGustKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "gustKmh");
      final int _cursorIndexOfRainMm = CursorUtil.getColumnIndexOrThrow(_cursor, "rainMm");
      final int _cursorIndexOfWeatherCode = CursorUtil.getColumnIndexOrThrow(_cursor, "weatherCode");
      final int _cursorIndexOfWaveHeightM = CursorUtil.getColumnIndexOrThrow(_cursor, "waveHeightM");
      final int _cursorIndexOfFetchedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "fetchedAt");
      final int _cursorIndexOfRiskLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "riskLevel");
      final int _cursorIndexOfRiskMultiplier = CursorUtil.getColumnIndexOrThrow(_cursor, "riskMultiplier");
      final NodeWeatherEntity _result;
      if(_cursor.moveToFirst()) {
        final String _tmpNodeId;
        if (_cursor.isNull(_cursorIndexOfNodeId)) {
          _tmpNodeId = null;
        } else {
          _tmpNodeId = _cursor.getString(_cursorIndexOfNodeId);
        }
        final String _tmpNodeName;
        if (_cursor.isNull(_cursorIndexOfNodeName)) {
          _tmpNodeName = null;
        } else {
          _tmpNodeName = _cursor.getString(_cursorIndexOfNodeName);
        }
        final String _tmpForecastTime;
        if (_cursor.isNull(_cursorIndexOfForecastTime)) {
          _tmpForecastTime = null;
        } else {
          _tmpForecastTime = _cursor.getString(_cursorIndexOfForecastTime);
        }
        final double _tmpTempC;
        _tmpTempC = _cursor.getDouble(_cursorIndexOfTempC);
        final double _tmpWindKmh;
        _tmpWindKmh = _cursor.getDouble(_cursorIndexOfWindKmh);
        final double _tmpGustKmh;
        _tmpGustKmh = _cursor.getDouble(_cursorIndexOfGustKmh);
        final double _tmpRainMm;
        _tmpRainMm = _cursor.getDouble(_cursorIndexOfRainMm);
        final int _tmpWeatherCode;
        _tmpWeatherCode = _cursor.getInt(_cursorIndexOfWeatherCode);
        final Double _tmpWaveHeightM;
        if (_cursor.isNull(_cursorIndexOfWaveHeightM)) {
          _tmpWaveHeightM = null;
        } else {
          _tmpWaveHeightM = _cursor.getDouble(_cursorIndexOfWaveHeightM);
        }
        final long _tmpFetchedAt;
        _tmpFetchedAt = _cursor.getLong(_cursorIndexOfFetchedAt);
        final int _tmpRiskLevel;
        _tmpRiskLevel = _cursor.getInt(_cursorIndexOfRiskLevel);
        final double _tmpRiskMultiplier;
        _tmpRiskMultiplier = _cursor.getDouble(_cursorIndexOfRiskMultiplier);
        _result = new NodeWeatherEntity(_tmpNodeId,_tmpNodeName,_tmpForecastTime,_tmpTempC,_tmpWindKmh,_tmpGustKmh,_tmpRainMm,_tmpWeatherCode,_tmpWaveHeightM,_tmpFetchedAt,_tmpRiskLevel,_tmpRiskMultiplier);
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
  public List<NodeWeatherEntity> getWeatherForNodes(final List<String> nodeIds) {
    StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM node_weather WHERE nodeId IN (");
    final int _inputSize = nodeIds.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : nodeIds) {
      if (_item == null) {
        _statement.bindNull(_argIndex);
      } else {
        _statement.bindString(_argIndex, _item);
      }
      _argIndex ++;
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfNodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "nodeId");
      final int _cursorIndexOfNodeName = CursorUtil.getColumnIndexOrThrow(_cursor, "nodeName");
      final int _cursorIndexOfForecastTime = CursorUtil.getColumnIndexOrThrow(_cursor, "forecastTime");
      final int _cursorIndexOfTempC = CursorUtil.getColumnIndexOrThrow(_cursor, "tempC");
      final int _cursorIndexOfWindKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "windKmh");
      final int _cursorIndexOfGustKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "gustKmh");
      final int _cursorIndexOfRainMm = CursorUtil.getColumnIndexOrThrow(_cursor, "rainMm");
      final int _cursorIndexOfWeatherCode = CursorUtil.getColumnIndexOrThrow(_cursor, "weatherCode");
      final int _cursorIndexOfWaveHeightM = CursorUtil.getColumnIndexOrThrow(_cursor, "waveHeightM");
      final int _cursorIndexOfFetchedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "fetchedAt");
      final int _cursorIndexOfRiskLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "riskLevel");
      final int _cursorIndexOfRiskMultiplier = CursorUtil.getColumnIndexOrThrow(_cursor, "riskMultiplier");
      final List<NodeWeatherEntity> _result = new ArrayList<NodeWeatherEntity>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final NodeWeatherEntity _item_1;
        final String _tmpNodeId;
        if (_cursor.isNull(_cursorIndexOfNodeId)) {
          _tmpNodeId = null;
        } else {
          _tmpNodeId = _cursor.getString(_cursorIndexOfNodeId);
        }
        final String _tmpNodeName;
        if (_cursor.isNull(_cursorIndexOfNodeName)) {
          _tmpNodeName = null;
        } else {
          _tmpNodeName = _cursor.getString(_cursorIndexOfNodeName);
        }
        final String _tmpForecastTime;
        if (_cursor.isNull(_cursorIndexOfForecastTime)) {
          _tmpForecastTime = null;
        } else {
          _tmpForecastTime = _cursor.getString(_cursorIndexOfForecastTime);
        }
        final double _tmpTempC;
        _tmpTempC = _cursor.getDouble(_cursorIndexOfTempC);
        final double _tmpWindKmh;
        _tmpWindKmh = _cursor.getDouble(_cursorIndexOfWindKmh);
        final double _tmpGustKmh;
        _tmpGustKmh = _cursor.getDouble(_cursorIndexOfGustKmh);
        final double _tmpRainMm;
        _tmpRainMm = _cursor.getDouble(_cursorIndexOfRainMm);
        final int _tmpWeatherCode;
        _tmpWeatherCode = _cursor.getInt(_cursorIndexOfWeatherCode);
        final Double _tmpWaveHeightM;
        if (_cursor.isNull(_cursorIndexOfWaveHeightM)) {
          _tmpWaveHeightM = null;
        } else {
          _tmpWaveHeightM = _cursor.getDouble(_cursorIndexOfWaveHeightM);
        }
        final long _tmpFetchedAt;
        _tmpFetchedAt = _cursor.getLong(_cursorIndexOfFetchedAt);
        final int _tmpRiskLevel;
        _tmpRiskLevel = _cursor.getInt(_cursorIndexOfRiskLevel);
        final double _tmpRiskMultiplier;
        _tmpRiskMultiplier = _cursor.getDouble(_cursorIndexOfRiskMultiplier);
        _item_1 = new NodeWeatherEntity(_tmpNodeId,_tmpNodeName,_tmpForecastTime,_tmpTempC,_tmpWindKmh,_tmpGustKmh,_tmpRainMm,_tmpWeatherCode,_tmpWaveHeightM,_tmpFetchedAt,_tmpRiskLevel,_tmpRiskMultiplier);
        _result.add(_item_1);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public NodeWeatherEntity getLatestForNode(final String nodeId) {
    final String _sql = "SELECT * FROM node_weather WHERE nodeId = ? ORDER BY fetchedAt DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (nodeId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, nodeId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfNodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "nodeId");
      final int _cursorIndexOfNodeName = CursorUtil.getColumnIndexOrThrow(_cursor, "nodeName");
      final int _cursorIndexOfForecastTime = CursorUtil.getColumnIndexOrThrow(_cursor, "forecastTime");
      final int _cursorIndexOfTempC = CursorUtil.getColumnIndexOrThrow(_cursor, "tempC");
      final int _cursorIndexOfWindKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "windKmh");
      final int _cursorIndexOfGustKmh = CursorUtil.getColumnIndexOrThrow(_cursor, "gustKmh");
      final int _cursorIndexOfRainMm = CursorUtil.getColumnIndexOrThrow(_cursor, "rainMm");
      final int _cursorIndexOfWeatherCode = CursorUtil.getColumnIndexOrThrow(_cursor, "weatherCode");
      final int _cursorIndexOfWaveHeightM = CursorUtil.getColumnIndexOrThrow(_cursor, "waveHeightM");
      final int _cursorIndexOfFetchedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "fetchedAt");
      final int _cursorIndexOfRiskLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "riskLevel");
      final int _cursorIndexOfRiskMultiplier = CursorUtil.getColumnIndexOrThrow(_cursor, "riskMultiplier");
      final NodeWeatherEntity _result;
      if(_cursor.moveToFirst()) {
        final String _tmpNodeId;
        if (_cursor.isNull(_cursorIndexOfNodeId)) {
          _tmpNodeId = null;
        } else {
          _tmpNodeId = _cursor.getString(_cursorIndexOfNodeId);
        }
        final String _tmpNodeName;
        if (_cursor.isNull(_cursorIndexOfNodeName)) {
          _tmpNodeName = null;
        } else {
          _tmpNodeName = _cursor.getString(_cursorIndexOfNodeName);
        }
        final String _tmpForecastTime;
        if (_cursor.isNull(_cursorIndexOfForecastTime)) {
          _tmpForecastTime = null;
        } else {
          _tmpForecastTime = _cursor.getString(_cursorIndexOfForecastTime);
        }
        final double _tmpTempC;
        _tmpTempC = _cursor.getDouble(_cursorIndexOfTempC);
        final double _tmpWindKmh;
        _tmpWindKmh = _cursor.getDouble(_cursorIndexOfWindKmh);
        final double _tmpGustKmh;
        _tmpGustKmh = _cursor.getDouble(_cursorIndexOfGustKmh);
        final double _tmpRainMm;
        _tmpRainMm = _cursor.getDouble(_cursorIndexOfRainMm);
        final int _tmpWeatherCode;
        _tmpWeatherCode = _cursor.getInt(_cursorIndexOfWeatherCode);
        final Double _tmpWaveHeightM;
        if (_cursor.isNull(_cursorIndexOfWaveHeightM)) {
          _tmpWaveHeightM = null;
        } else {
          _tmpWaveHeightM = _cursor.getDouble(_cursorIndexOfWaveHeightM);
        }
        final long _tmpFetchedAt;
        _tmpFetchedAt = _cursor.getLong(_cursorIndexOfFetchedAt);
        final int _tmpRiskLevel;
        _tmpRiskLevel = _cursor.getInt(_cursorIndexOfRiskLevel);
        final double _tmpRiskMultiplier;
        _tmpRiskMultiplier = _cursor.getDouble(_cursorIndexOfRiskMultiplier);
        _result = new NodeWeatherEntity(_tmpNodeId,_tmpNodeName,_tmpForecastTime,_tmpTempC,_tmpWindKmh,_tmpGustKmh,_tmpRainMm,_tmpWeatherCode,_tmpWaveHeightM,_tmpFetchedAt,_tmpRiskLevel,_tmpRiskMultiplier);
      } else {
        _result = null;
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
