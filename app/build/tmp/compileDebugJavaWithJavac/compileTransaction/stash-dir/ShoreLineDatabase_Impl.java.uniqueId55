package com.shoreline.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.RoomOpenHelper.ValidationResult;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import com.shoreline.app.data.local.dao.NodeWeatherDao;
import com.shoreline.app.data.local.dao.NodeWeatherDao_Impl;
import com.shoreline.app.data.local.dao.ReportDao;
import com.shoreline.app.data.local.dao.ReportDao_Impl;
import com.shoreline.app.data.local.dao.SosDao;
import com.shoreline.app.data.local.dao.SosDao_Impl;
import com.shoreline.app.data.local.dao.UserDao;
import com.shoreline.app.data.local.dao.UserDao_Impl;
import com.shoreline.app.data.local.dao.VesselDao;
import com.shoreline.app.data.local.dao.VesselDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class ShoreLineDatabase_Impl extends ShoreLineDatabase {
  private volatile UserDao _userDao;

  private volatile VesselDao _vesselDao;

  private volatile ReportDao _reportDao;

  private volatile SosDao _sosDao;

  private volatile NodeWeatherDao _nodeWeatherDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`user_id` TEXT NOT NULL, `email` TEXT, `full_name` TEXT, `phone_number` TEXT, `role` TEXT, `emergency_contact` TEXT, `created_at` INTEGER NOT NULL, PRIMARY KEY(`user_id`))");
        _db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `vessels` (`vessel_id` TEXT NOT NULL, `registration_number` TEXT, `vessel_name` TEXT, `route_name` TEXT, `licensed_capacity` INTEGER NOT NULL, `fitness_expiry` TEXT, `community_rating` REAL NOT NULL, `total_reports` INTEGER NOT NULL, `is_flagged` INTEGER NOT NULL, `cached_at` INTEGER NOT NULL, PRIMARY KEY(`vessel_id`))");
        _db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_vessels_registration_number` ON `vessels` (`registration_number`)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `overload_reports` (`report_id` TEXT NOT NULL, `vessel_id` TEXT, `vessel_number` TEXT, `crowd_level` TEXT, `passenger_count_estimate` INTEGER NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `timestamp` INTEGER NOT NULL, `photo_base64` TEXT, `reporter_user_id` TEXT, `is_anonymous` INTEGER NOT NULL, `is_synced` INTEGER NOT NULL, `sync_timestamp` INTEGER NOT NULL, PRIMARY KEY(`report_id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `sos_signals` (`sos_id` TEXT NOT NULL, `sender_name` TEXT, `sender_phone` TEXT, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `timestamp` INTEGER NOT NULL, `hop_count` INTEGER NOT NULL, `origin_device_id` TEXT, `status` TEXT, `is_relayed` INTEGER NOT NULL, `is_synced_to_cloud` INTEGER NOT NULL, PRIMARY KEY(`sos_id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `node_weather` (`nodeId` TEXT NOT NULL, `nodeName` TEXT NOT NULL, `forecastTime` TEXT NOT NULL, `tempC` REAL NOT NULL, `windKmh` REAL NOT NULL, `gustKmh` REAL NOT NULL, `rainMm` REAL NOT NULL, `weatherCode` INTEGER NOT NULL, `waveHeightM` REAL, `fetchedAt` INTEGER NOT NULL, `riskLevel` INTEGER NOT NULL, `riskMultiplier` REAL NOT NULL, PRIMARY KEY(`nodeId`, `forecastTime`))");
        _db.execSQL("CREATE INDEX IF NOT EXISTS `index_node_weather_nodeId` ON `node_weather` (`nodeId`)");
        _db.execSQL("CREATE INDEX IF NOT EXISTS `index_node_weather_fetchedAt` ON `node_weather` (`fetchedAt`)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd05eb12b072906980b7f50284fe4aed9')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `users`");
        _db.execSQL("DROP TABLE IF EXISTS `vessels`");
        _db.execSQL("DROP TABLE IF EXISTS `overload_reports`");
        _db.execSQL("DROP TABLE IF EXISTS `sos_signals`");
        _db.execSQL("DROP TABLE IF EXISTS `node_weather`");
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onDestructiveMigration(_db);
          }
        }
      }

      @Override
      public void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      public RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(7);
        _columnsUsers.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("full_name", new TableInfo.Column("full_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("phone_number", new TableInfo.Column("phone_number", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("role", new TableInfo.Column("role", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("emergency_contact", new TableInfo.Column("emergency_contact", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(1);
        _indicesUsers.add(new TableInfo.Index("index_users_email", true, Arrays.asList("email"), Arrays.asList("ASC")));
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(_db, "users");
        if (! _infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.shoreline.app.data.local.entity.UserEntity).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsVessels = new HashMap<String, TableInfo.Column>(10);
        _columnsVessels.put("vessel_id", new TableInfo.Column("vessel_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVessels.put("registration_number", new TableInfo.Column("registration_number", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVessels.put("vessel_name", new TableInfo.Column("vessel_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVessels.put("route_name", new TableInfo.Column("route_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVessels.put("licensed_capacity", new TableInfo.Column("licensed_capacity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVessels.put("fitness_expiry", new TableInfo.Column("fitness_expiry", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVessels.put("community_rating", new TableInfo.Column("community_rating", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVessels.put("total_reports", new TableInfo.Column("total_reports", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVessels.put("is_flagged", new TableInfo.Column("is_flagged", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVessels.put("cached_at", new TableInfo.Column("cached_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysVessels = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesVessels = new HashSet<TableInfo.Index>(1);
        _indicesVessels.add(new TableInfo.Index("index_vessels_registration_number", true, Arrays.asList("registration_number"), Arrays.asList("ASC")));
        final TableInfo _infoVessels = new TableInfo("vessels", _columnsVessels, _foreignKeysVessels, _indicesVessels);
        final TableInfo _existingVessels = TableInfo.read(_db, "vessels");
        if (! _infoVessels.equals(_existingVessels)) {
          return new RoomOpenHelper.ValidationResult(false, "vessels(com.shoreline.app.data.local.entity.VesselEntity).\n"
                  + " Expected:\n" + _infoVessels + "\n"
                  + " Found:\n" + _existingVessels);
        }
        final HashMap<String, TableInfo.Column> _columnsOverloadReports = new HashMap<String, TableInfo.Column>(13);
        _columnsOverloadReports.put("report_id", new TableInfo.Column("report_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOverloadReports.put("vessel_id", new TableInfo.Column("vessel_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOverloadReports.put("vessel_number", new TableInfo.Column("vessel_number", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOverloadReports.put("crowd_level", new TableInfo.Column("crowd_level", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOverloadReports.put("passenger_count_estimate", new TableInfo.Column("passenger_count_estimate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOverloadReports.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOverloadReports.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOverloadReports.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOverloadReports.put("photo_base64", new TableInfo.Column("photo_base64", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOverloadReports.put("reporter_user_id", new TableInfo.Column("reporter_user_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOverloadReports.put("is_anonymous", new TableInfo.Column("is_anonymous", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOverloadReports.put("is_synced", new TableInfo.Column("is_synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOverloadReports.put("sync_timestamp", new TableInfo.Column("sync_timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysOverloadReports = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesOverloadReports = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoOverloadReports = new TableInfo("overload_reports", _columnsOverloadReports, _foreignKeysOverloadReports, _indicesOverloadReports);
        final TableInfo _existingOverloadReports = TableInfo.read(_db, "overload_reports");
        if (! _infoOverloadReports.equals(_existingOverloadReports)) {
          return new RoomOpenHelper.ValidationResult(false, "overload_reports(com.shoreline.app.data.local.entity.OverloadReportEntity).\n"
                  + " Expected:\n" + _infoOverloadReports + "\n"
                  + " Found:\n" + _existingOverloadReports);
        }
        final HashMap<String, TableInfo.Column> _columnsSosSignals = new HashMap<String, TableInfo.Column>(11);
        _columnsSosSignals.put("sos_id", new TableInfo.Column("sos_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSosSignals.put("sender_name", new TableInfo.Column("sender_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSosSignals.put("sender_phone", new TableInfo.Column("sender_phone", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSosSignals.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSosSignals.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSosSignals.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSosSignals.put("hop_count", new TableInfo.Column("hop_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSosSignals.put("origin_device_id", new TableInfo.Column("origin_device_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSosSignals.put("status", new TableInfo.Column("status", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSosSignals.put("is_relayed", new TableInfo.Column("is_relayed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSosSignals.put("is_synced_to_cloud", new TableInfo.Column("is_synced_to_cloud", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSosSignals = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSosSignals = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSosSignals = new TableInfo("sos_signals", _columnsSosSignals, _foreignKeysSosSignals, _indicesSosSignals);
        final TableInfo _existingSosSignals = TableInfo.read(_db, "sos_signals");
        if (! _infoSosSignals.equals(_existingSosSignals)) {
          return new RoomOpenHelper.ValidationResult(false, "sos_signals(com.shoreline.app.data.local.entity.SosSignalEntity).\n"
                  + " Expected:\n" + _infoSosSignals + "\n"
                  + " Found:\n" + _existingSosSignals);
        }
        final HashMap<String, TableInfo.Column> _columnsNodeWeather = new HashMap<String, TableInfo.Column>(12);
        _columnsNodeWeather.put("nodeId", new TableInfo.Column("nodeId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNodeWeather.put("nodeName", new TableInfo.Column("nodeName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNodeWeather.put("forecastTime", new TableInfo.Column("forecastTime", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNodeWeather.put("tempC", new TableInfo.Column("tempC", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNodeWeather.put("windKmh", new TableInfo.Column("windKmh", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNodeWeather.put("gustKmh", new TableInfo.Column("gustKmh", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNodeWeather.put("rainMm", new TableInfo.Column("rainMm", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNodeWeather.put("weatherCode", new TableInfo.Column("weatherCode", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNodeWeather.put("waveHeightM", new TableInfo.Column("waveHeightM", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNodeWeather.put("fetchedAt", new TableInfo.Column("fetchedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNodeWeather.put("riskLevel", new TableInfo.Column("riskLevel", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNodeWeather.put("riskMultiplier", new TableInfo.Column("riskMultiplier", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysNodeWeather = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesNodeWeather = new HashSet<TableInfo.Index>(2);
        _indicesNodeWeather.add(new TableInfo.Index("index_node_weather_nodeId", false, Arrays.asList("nodeId"), Arrays.asList("ASC")));
        _indicesNodeWeather.add(new TableInfo.Index("index_node_weather_fetchedAt", false, Arrays.asList("fetchedAt"), Arrays.asList("ASC")));
        final TableInfo _infoNodeWeather = new TableInfo("node_weather", _columnsNodeWeather, _foreignKeysNodeWeather, _indicesNodeWeather);
        final TableInfo _existingNodeWeather = TableInfo.read(_db, "node_weather");
        if (! _infoNodeWeather.equals(_existingNodeWeather)) {
          return new RoomOpenHelper.ValidationResult(false, "node_weather(com.shoreline.app.data.local.entity.NodeWeatherEntity).\n"
                  + " Expected:\n" + _infoNodeWeather + "\n"
                  + " Found:\n" + _existingNodeWeather);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "d05eb12b072906980b7f50284fe4aed9", "43c88d5dff8d8962e6fb9c6845f5b265");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "users","vessels","overload_reports","sos_signals","node_weather");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `vessels`");
      _db.execSQL("DELETE FROM `overload_reports`");
      _db.execSQL("DELETE FROM `sos_signals`");
      _db.execSQL("DELETE FROM `node_weather`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(VesselDao.class, VesselDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReportDao.class, ReportDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SosDao.class, SosDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(NodeWeatherDao.class, NodeWeatherDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  public List<Migration> getAutoMigrations(
      @NonNull Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecsMap) {
    return Arrays.asList();
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public VesselDao vesselDao() {
    if (_vesselDao != null) {
      return _vesselDao;
    } else {
      synchronized(this) {
        if(_vesselDao == null) {
          _vesselDao = new VesselDao_Impl(this);
        }
        return _vesselDao;
      }
    }
  }

  @Override
  public ReportDao reportDao() {
    if (_reportDao != null) {
      return _reportDao;
    } else {
      synchronized(this) {
        if(_reportDao == null) {
          _reportDao = new ReportDao_Impl(this);
        }
        return _reportDao;
      }
    }
  }

  @Override
  public SosDao sosDao() {
    if (_sosDao != null) {
      return _sosDao;
    } else {
      synchronized(this) {
        if(_sosDao == null) {
          _sosDao = new SosDao_Impl(this);
        }
        return _sosDao;
      }
    }
  }

  @Override
  public NodeWeatherDao nodeWeatherDao() {
    if (_nodeWeatherDao != null) {
      return _nodeWeatherDao;
    } else {
      synchronized(this) {
        if(_nodeWeatherDao == null) {
          _nodeWeatherDao = new NodeWeatherDao_Impl(this);
        }
        return _nodeWeatherDao;
      }
    }
  }
}
