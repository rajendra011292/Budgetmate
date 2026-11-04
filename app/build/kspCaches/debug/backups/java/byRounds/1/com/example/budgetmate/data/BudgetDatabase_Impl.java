package com.example.budgetmate.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BudgetDatabase_Impl extends BudgetDatabase {
  private volatile BudgetDao _budgetDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `Account` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `openingBalance` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `Envelope` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `accountId` INTEGER NOT NULL, `parentId` INTEGER, `name` TEXT NOT NULL, `rolloverToSavings` INTEGER NOT NULL, `isSavings` INTEGER NOT NULL, FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Envelope_accountId` ON `Envelope` (`accountId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Envelope_parentId` ON `Envelope` (`parentId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `LedgerEntry` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `accountId` INTEGER NOT NULL, `envelopeId` INTEGER, `type` TEXT NOT NULL, `amount` INTEGER NOT NULL, `date` INTEGER NOT NULL, `note` TEXT NOT NULL, `transferAccountId` INTEGER, FOREIGN KEY(`accountId`) REFERENCES `Account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`envelopeId`) REFERENCES `Envelope`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_LedgerEntry_accountId` ON `LedgerEntry` (`accountId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_LedgerEntry_envelopeId` ON `LedgerEntry` (`envelopeId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `SavingsGoal` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmount` INTEGER NOT NULL, `savedAmount` INTEGER NOT NULL, `dueDate` INTEGER, `note` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `RecurringTransaction` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `amount` INTEGER NOT NULL, `type` TEXT NOT NULL, `accountId` INTEGER NOT NULL, `envelopeId` INTEGER, `interval` TEXT NOT NULL, `nextDue` INTEGER NOT NULL, `active` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ad241f6fadfa92b87730dacfe8145b24')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `Account`");
        db.execSQL("DROP TABLE IF EXISTS `Envelope`");
        db.execSQL("DROP TABLE IF EXISTS `LedgerEntry`");
        db.execSQL("DROP TABLE IF EXISTS `SavingsGoal`");
        db.execSQL("DROP TABLE IF EXISTS `RecurringTransaction`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsAccount = new HashMap<String, TableInfo.Column>(3);
        _columnsAccount.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccount.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccount.put("openingBalance", new TableInfo.Column("openingBalance", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAccount = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAccount = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAccount = new TableInfo("Account", _columnsAccount, _foreignKeysAccount, _indicesAccount);
        final TableInfo _existingAccount = TableInfo.read(db, "Account");
        if (!_infoAccount.equals(_existingAccount)) {
          return new RoomOpenHelper.ValidationResult(false, "Account(com.example.budgetmate.data.Account).\n"
                  + " Expected:\n" + _infoAccount + "\n"
                  + " Found:\n" + _existingAccount);
        }
        final HashMap<String, TableInfo.Column> _columnsEnvelope = new HashMap<String, TableInfo.Column>(6);
        _columnsEnvelope.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnvelope.put("accountId", new TableInfo.Column("accountId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnvelope.put("parentId", new TableInfo.Column("parentId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnvelope.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnvelope.put("rolloverToSavings", new TableInfo.Column("rolloverToSavings", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEnvelope.put("isSavings", new TableInfo.Column("isSavings", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEnvelope = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysEnvelope.add(new TableInfo.ForeignKey("Account", "CASCADE", "NO ACTION", Arrays.asList("accountId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesEnvelope = new HashSet<TableInfo.Index>(2);
        _indicesEnvelope.add(new TableInfo.Index("index_Envelope_accountId", false, Arrays.asList("accountId"), Arrays.asList("ASC")));
        _indicesEnvelope.add(new TableInfo.Index("index_Envelope_parentId", false, Arrays.asList("parentId"), Arrays.asList("ASC")));
        final TableInfo _infoEnvelope = new TableInfo("Envelope", _columnsEnvelope, _foreignKeysEnvelope, _indicesEnvelope);
        final TableInfo _existingEnvelope = TableInfo.read(db, "Envelope");
        if (!_infoEnvelope.equals(_existingEnvelope)) {
          return new RoomOpenHelper.ValidationResult(false, "Envelope(com.example.budgetmate.data.Envelope).\n"
                  + " Expected:\n" + _infoEnvelope + "\n"
                  + " Found:\n" + _existingEnvelope);
        }
        final HashMap<String, TableInfo.Column> _columnsLedgerEntry = new HashMap<String, TableInfo.Column>(8);
        _columnsLedgerEntry.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLedgerEntry.put("accountId", new TableInfo.Column("accountId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLedgerEntry.put("envelopeId", new TableInfo.Column("envelopeId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLedgerEntry.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLedgerEntry.put("amount", new TableInfo.Column("amount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLedgerEntry.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLedgerEntry.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLedgerEntry.put("transferAccountId", new TableInfo.Column("transferAccountId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLedgerEntry = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysLedgerEntry.add(new TableInfo.ForeignKey("Account", "CASCADE", "NO ACTION", Arrays.asList("accountId"), Arrays.asList("id")));
        _foreignKeysLedgerEntry.add(new TableInfo.ForeignKey("Envelope", "SET NULL", "NO ACTION", Arrays.asList("envelopeId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesLedgerEntry = new HashSet<TableInfo.Index>(2);
        _indicesLedgerEntry.add(new TableInfo.Index("index_LedgerEntry_accountId", false, Arrays.asList("accountId"), Arrays.asList("ASC")));
        _indicesLedgerEntry.add(new TableInfo.Index("index_LedgerEntry_envelopeId", false, Arrays.asList("envelopeId"), Arrays.asList("ASC")));
        final TableInfo _infoLedgerEntry = new TableInfo("LedgerEntry", _columnsLedgerEntry, _foreignKeysLedgerEntry, _indicesLedgerEntry);
        final TableInfo _existingLedgerEntry = TableInfo.read(db, "LedgerEntry");
        if (!_infoLedgerEntry.equals(_existingLedgerEntry)) {
          return new RoomOpenHelper.ValidationResult(false, "LedgerEntry(com.example.budgetmate.data.LedgerEntry).\n"
                  + " Expected:\n" + _infoLedgerEntry + "\n"
                  + " Found:\n" + _existingLedgerEntry);
        }
        final HashMap<String, TableInfo.Column> _columnsSavingsGoal = new HashMap<String, TableInfo.Column>(6);
        _columnsSavingsGoal.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavingsGoal.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavingsGoal.put("targetAmount", new TableInfo.Column("targetAmount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavingsGoal.put("savedAmount", new TableInfo.Column("savedAmount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavingsGoal.put("dueDate", new TableInfo.Column("dueDate", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavingsGoal.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSavingsGoal = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSavingsGoal = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSavingsGoal = new TableInfo("SavingsGoal", _columnsSavingsGoal, _foreignKeysSavingsGoal, _indicesSavingsGoal);
        final TableInfo _existingSavingsGoal = TableInfo.read(db, "SavingsGoal");
        if (!_infoSavingsGoal.equals(_existingSavingsGoal)) {
          return new RoomOpenHelper.ValidationResult(false, "SavingsGoal(com.example.budgetmate.data.SavingsGoal).\n"
                  + " Expected:\n" + _infoSavingsGoal + "\n"
                  + " Found:\n" + _existingSavingsGoal);
        }
        final HashMap<String, TableInfo.Column> _columnsRecurringTransaction = new HashMap<String, TableInfo.Column>(9);
        _columnsRecurringTransaction.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringTransaction.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringTransaction.put("amount", new TableInfo.Column("amount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringTransaction.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringTransaction.put("accountId", new TableInfo.Column("accountId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringTransaction.put("envelopeId", new TableInfo.Column("envelopeId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringTransaction.put("interval", new TableInfo.Column("interval", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringTransaction.put("nextDue", new TableInfo.Column("nextDue", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecurringTransaction.put("active", new TableInfo.Column("active", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRecurringTransaction = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRecurringTransaction = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRecurringTransaction = new TableInfo("RecurringTransaction", _columnsRecurringTransaction, _foreignKeysRecurringTransaction, _indicesRecurringTransaction);
        final TableInfo _existingRecurringTransaction = TableInfo.read(db, "RecurringTransaction");
        if (!_infoRecurringTransaction.equals(_existingRecurringTransaction)) {
          return new RoomOpenHelper.ValidationResult(false, "RecurringTransaction(com.example.budgetmate.data.RecurringTransaction).\n"
                  + " Expected:\n" + _infoRecurringTransaction + "\n"
                  + " Found:\n" + _existingRecurringTransaction);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "ad241f6fadfa92b87730dacfe8145b24", "963efda2718e17b4a58c3d47e4b71617");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "Account","Envelope","LedgerEntry","SavingsGoal","RecurringTransaction");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `Account`");
      _db.execSQL("DELETE FROM `Envelope`");
      _db.execSQL("DELETE FROM `LedgerEntry`");
      _db.execSQL("DELETE FROM `SavingsGoal`");
      _db.execSQL("DELETE FROM `RecurringTransaction`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(BudgetDao.class, BudgetDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public BudgetDao dao() {
    if (_budgetDao != null) {
      return _budgetDao;
    } else {
      synchronized(this) {
        if(_budgetDao == null) {
          _budgetDao = new BudgetDao_Impl(this);
        }
        return _budgetDao;
      }
    }
  }
}
