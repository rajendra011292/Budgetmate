package com.example.budgetmate.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BudgetDao_Impl implements BudgetDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Account> __insertionAdapterOfAccount;

  private final EntityInsertionAdapter<Envelope> __insertionAdapterOfEnvelope;

  private final EntityInsertionAdapter<LedgerEntry> __insertionAdapterOfLedgerEntry;

  private final EntityInsertionAdapter<SavingsGoal> __insertionAdapterOfSavingsGoal;

  private final EntityInsertionAdapter<RecurringTransaction> __insertionAdapterOfRecurringTransaction;

  private final EntityDeletionOrUpdateAdapter<Account> __deletionAdapterOfAccount;

  private final EntityDeletionOrUpdateAdapter<Envelope> __deletionAdapterOfEnvelope;

  private final EntityDeletionOrUpdateAdapter<SavingsGoal> __deletionAdapterOfSavingsGoal;

  private final EntityDeletionOrUpdateAdapter<RecurringTransaction> __deletionAdapterOfRecurringTransaction;

  private final EntityDeletionOrUpdateAdapter<LedgerEntry> __deletionAdapterOfLedgerEntry;

  private final EntityDeletionOrUpdateAdapter<Account> __updateAdapterOfAccount;

  private final EntityDeletionOrUpdateAdapter<Envelope> __updateAdapterOfEnvelope;

  private final EntityDeletionOrUpdateAdapter<LedgerEntry> __updateAdapterOfLedgerEntry;

  private final EntityDeletionOrUpdateAdapter<SavingsGoal> __updateAdapterOfSavingsGoal;

  private final EntityDeletionOrUpdateAdapter<RecurringTransaction> __updateAdapterOfRecurringTransaction;

  public BudgetDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAccount = new EntityInsertionAdapter<Account>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `Account` (`id`,`name`,`openingBalance`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Account entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getOpeningBalance());
      }
    };
    this.__insertionAdapterOfEnvelope = new EntityInsertionAdapter<Envelope>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `Envelope` (`id`,`accountId`,`parentId`,`name`,`rolloverToSavings`,`isSavings`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Envelope entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getAccountId());
        if (entity.getParentId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getParentId());
        }
        statement.bindString(4, entity.getName());
        final int _tmp = entity.getRolloverToSavings() ? 1 : 0;
        statement.bindLong(5, _tmp);
        final int _tmp_1 = entity.isSavings() ? 1 : 0;
        statement.bindLong(6, _tmp_1);
      }
    };
    this.__insertionAdapterOfLedgerEntry = new EntityInsertionAdapter<LedgerEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `LedgerEntry` (`id`,`accountId`,`envelopeId`,`type`,`amount`,`date`,`note`,`transferAccountId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LedgerEntry entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getAccountId());
        if (entity.getEnvelopeId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getEnvelopeId());
        }
        statement.bindString(4, entity.getType());
        statement.bindLong(5, entity.getAmount());
        statement.bindLong(6, entity.getDate());
        statement.bindString(7, entity.getNote());
        if (entity.getTransferAccountId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getTransferAccountId());
        }
      }
    };
    this.__insertionAdapterOfSavingsGoal = new EntityInsertionAdapter<SavingsGoal>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `SavingsGoal` (`id`,`name`,`targetAmount`,`savedAmount`,`dueDate`,`note`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SavingsGoal entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getTargetAmount());
        statement.bindLong(4, entity.getSavedAmount());
        if (entity.getDueDate() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getDueDate());
        }
        statement.bindString(6, entity.getNote());
      }
    };
    this.__insertionAdapterOfRecurringTransaction = new EntityInsertionAdapter<RecurringTransaction>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `RecurringTransaction` (`id`,`name`,`amount`,`type`,`accountId`,`envelopeId`,`interval`,`nextDue`,`active`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecurringTransaction entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getAmount());
        statement.bindString(4, entity.getType());
        statement.bindLong(5, entity.getAccountId());
        if (entity.getEnvelopeId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getEnvelopeId());
        }
        statement.bindString(7, entity.getInterval());
        statement.bindLong(8, entity.getNextDue());
        final int _tmp = entity.getActive() ? 1 : 0;
        statement.bindLong(9, _tmp);
      }
    };
    this.__deletionAdapterOfAccount = new EntityDeletionOrUpdateAdapter<Account>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `Account` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Account entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfEnvelope = new EntityDeletionOrUpdateAdapter<Envelope>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `Envelope` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Envelope entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfSavingsGoal = new EntityDeletionOrUpdateAdapter<SavingsGoal>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `SavingsGoal` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SavingsGoal entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfRecurringTransaction = new EntityDeletionOrUpdateAdapter<RecurringTransaction>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `RecurringTransaction` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecurringTransaction entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfLedgerEntry = new EntityDeletionOrUpdateAdapter<LedgerEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `LedgerEntry` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LedgerEntry entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfAccount = new EntityDeletionOrUpdateAdapter<Account>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `Account` SET `id` = ?,`name` = ?,`openingBalance` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Account entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getOpeningBalance());
        statement.bindLong(4, entity.getId());
      }
    };
    this.__updateAdapterOfEnvelope = new EntityDeletionOrUpdateAdapter<Envelope>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `Envelope` SET `id` = ?,`accountId` = ?,`parentId` = ?,`name` = ?,`rolloverToSavings` = ?,`isSavings` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Envelope entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getAccountId());
        if (entity.getParentId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getParentId());
        }
        statement.bindString(4, entity.getName());
        final int _tmp = entity.getRolloverToSavings() ? 1 : 0;
        statement.bindLong(5, _tmp);
        final int _tmp_1 = entity.isSavings() ? 1 : 0;
        statement.bindLong(6, _tmp_1);
        statement.bindLong(7, entity.getId());
      }
    };
    this.__updateAdapterOfLedgerEntry = new EntityDeletionOrUpdateAdapter<LedgerEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `LedgerEntry` SET `id` = ?,`accountId` = ?,`envelopeId` = ?,`type` = ?,`amount` = ?,`date` = ?,`note` = ?,`transferAccountId` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LedgerEntry entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getAccountId());
        if (entity.getEnvelopeId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getEnvelopeId());
        }
        statement.bindString(4, entity.getType());
        statement.bindLong(5, entity.getAmount());
        statement.bindLong(6, entity.getDate());
        statement.bindString(7, entity.getNote());
        if (entity.getTransferAccountId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getTransferAccountId());
        }
        statement.bindLong(9, entity.getId());
      }
    };
    this.__updateAdapterOfSavingsGoal = new EntityDeletionOrUpdateAdapter<SavingsGoal>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `SavingsGoal` SET `id` = ?,`name` = ?,`targetAmount` = ?,`savedAmount` = ?,`dueDate` = ?,`note` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SavingsGoal entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getTargetAmount());
        statement.bindLong(4, entity.getSavedAmount());
        if (entity.getDueDate() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getDueDate());
        }
        statement.bindString(6, entity.getNote());
        statement.bindLong(7, entity.getId());
      }
    };
    this.__updateAdapterOfRecurringTransaction = new EntityDeletionOrUpdateAdapter<RecurringTransaction>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `RecurringTransaction` SET `id` = ?,`name` = ?,`amount` = ?,`type` = ?,`accountId` = ?,`envelopeId` = ?,`interval` = ?,`nextDue` = ?,`active` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecurringTransaction entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getAmount());
        statement.bindString(4, entity.getType());
        statement.bindLong(5, entity.getAccountId());
        if (entity.getEnvelopeId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getEnvelopeId());
        }
        statement.bindString(7, entity.getInterval());
        statement.bindLong(8, entity.getNextDue());
        final int _tmp = entity.getActive() ? 1 : 0;
        statement.bindLong(9, _tmp);
        statement.bindLong(10, entity.getId());
      }
    };
  }

  @Override
  public Object addAccount(final Account account, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfAccount.insertAndReturnId(account);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object addEnvelope(final Envelope envelope, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfEnvelope.insertAndReturnId(envelope);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object addEntry(final LedgerEntry entry, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfLedgerEntry.insertAndReturnId(entry);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object addGoal(final SavingsGoal goal, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSavingsGoal.insertAndReturnId(goal);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object addRecurring(final RecurringTransaction item,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRecurringTransaction.insertAndReturnId(item);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAccount(final Account account, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAccount.handle(account);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteEnvelope(final Envelope envelope,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfEnvelope.handle(envelope);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteGoal(final SavingsGoal goal, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSavingsGoal.handle(goal);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRecurring(final RecurringTransaction item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfRecurringTransaction.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteEntry(final LedgerEntry entry, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfLedgerEntry.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateAccount(final Account account, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAccount.handle(account);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateEnvelope(final Envelope envelope,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfEnvelope.handle(envelope);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateEntry(final LedgerEntry entry, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfLedgerEntry.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateGoal(final SavingsGoal goal, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSavingsGoal.handle(goal);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRecurring(final RecurringTransaction item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfRecurringTransaction.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Account>> accounts() {
    final String _sql = "SELECT * FROM Account ORDER BY id";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Account"}, new Callable<List<Account>>() {
      @Override
      @NonNull
      public List<Account> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfOpeningBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "openingBalance");
          final List<Account> _result = new ArrayList<Account>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Account _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpOpeningBalance;
            _tmpOpeningBalance = _cursor.getLong(_cursorIndexOfOpeningBalance);
            _item = new Account(_tmpId,_tmpName,_tmpOpeningBalance);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Envelope>> envelopes() {
    final String _sql = "SELECT * FROM Envelope ORDER BY id";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"Envelope"}, new Callable<List<Envelope>>() {
      @Override
      @NonNull
      public List<Envelope> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfParentId = CursorUtil.getColumnIndexOrThrow(_cursor, "parentId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRolloverToSavings = CursorUtil.getColumnIndexOrThrow(_cursor, "rolloverToSavings");
          final int _cursorIndexOfIsSavings = CursorUtil.getColumnIndexOrThrow(_cursor, "isSavings");
          final List<Envelope> _result = new ArrayList<Envelope>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Envelope _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpAccountId;
            _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            final Long _tmpParentId;
            if (_cursor.isNull(_cursorIndexOfParentId)) {
              _tmpParentId = null;
            } else {
              _tmpParentId = _cursor.getLong(_cursorIndexOfParentId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final boolean _tmpRolloverToSavings;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfRolloverToSavings);
            _tmpRolloverToSavings = _tmp != 0;
            final boolean _tmpIsSavings;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSavings);
            _tmpIsSavings = _tmp_1 != 0;
            _item = new Envelope(_tmpId,_tmpAccountId,_tmpParentId,_tmpName,_tmpRolloverToSavings,_tmpIsSavings);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<LedgerEntry>> entries() {
    final String _sql = "SELECT * FROM LedgerEntry ORDER BY date DESC, id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"LedgerEntry"}, new Callable<List<LedgerEntry>>() {
      @Override
      @NonNull
      public List<LedgerEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfEnvelopeId = CursorUtil.getColumnIndexOrThrow(_cursor, "envelopeId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfTransferAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "transferAccountId");
          final List<LedgerEntry> _result = new ArrayList<LedgerEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LedgerEntry _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpAccountId;
            _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            final Long _tmpEnvelopeId;
            if (_cursor.isNull(_cursorIndexOfEnvelopeId)) {
              _tmpEnvelopeId = null;
            } else {
              _tmpEnvelopeId = _cursor.getLong(_cursorIndexOfEnvelopeId);
            }
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpAmount;
            _tmpAmount = _cursor.getLong(_cursorIndexOfAmount);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final Long _tmpTransferAccountId;
            if (_cursor.isNull(_cursorIndexOfTransferAccountId)) {
              _tmpTransferAccountId = null;
            } else {
              _tmpTransferAccountId = _cursor.getLong(_cursorIndexOfTransferAccountId);
            }
            _item = new LedgerEntry(_tmpId,_tmpAccountId,_tmpEnvelopeId,_tmpType,_tmpAmount,_tmpDate,_tmpNote,_tmpTransferAccountId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SavingsGoal>> goals() {
    final String _sql = "SELECT * FROM SavingsGoal ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"SavingsGoal"}, new Callable<List<SavingsGoal>>() {
      @Override
      @NonNull
      public List<SavingsGoal> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfTargetAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "targetAmount");
          final int _cursorIndexOfSavedAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "savedAmount");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final List<SavingsGoal> _result = new ArrayList<SavingsGoal>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SavingsGoal _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpTargetAmount;
            _tmpTargetAmount = _cursor.getLong(_cursorIndexOfTargetAmount);
            final long _tmpSavedAmount;
            _tmpSavedAmount = _cursor.getLong(_cursorIndexOfSavedAmount);
            final Long _tmpDueDate;
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null;
            } else {
              _tmpDueDate = _cursor.getLong(_cursorIndexOfDueDate);
            }
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            _item = new SavingsGoal(_tmpId,_tmpName,_tmpTargetAmount,_tmpSavedAmount,_tmpDueDate,_tmpNote);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<RecurringTransaction>> recurring() {
    final String _sql = "SELECT * FROM RecurringTransaction ORDER BY nextDue";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"RecurringTransaction"}, new Callable<List<RecurringTransaction>>() {
      @Override
      @NonNull
      public List<RecurringTransaction> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfEnvelopeId = CursorUtil.getColumnIndexOrThrow(_cursor, "envelopeId");
          final int _cursorIndexOfInterval = CursorUtil.getColumnIndexOrThrow(_cursor, "interval");
          final int _cursorIndexOfNextDue = CursorUtil.getColumnIndexOrThrow(_cursor, "nextDue");
          final int _cursorIndexOfActive = CursorUtil.getColumnIndexOrThrow(_cursor, "active");
          final List<RecurringTransaction> _result = new ArrayList<RecurringTransaction>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecurringTransaction _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpAmount;
            _tmpAmount = _cursor.getLong(_cursorIndexOfAmount);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpAccountId;
            _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            final Long _tmpEnvelopeId;
            if (_cursor.isNull(_cursorIndexOfEnvelopeId)) {
              _tmpEnvelopeId = null;
            } else {
              _tmpEnvelopeId = _cursor.getLong(_cursorIndexOfEnvelopeId);
            }
            final String _tmpInterval;
            _tmpInterval = _cursor.getString(_cursorIndexOfInterval);
            final long _tmpNextDue;
            _tmpNextDue = _cursor.getLong(_cursorIndexOfNextDue);
            final boolean _tmpActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfActive);
            _tmpActive = _tmp != 0;
            _item = new RecurringTransaction(_tmpId,_tmpName,_tmpAmount,_tmpType,_tmpAccountId,_tmpEnvelopeId,_tmpInterval,_tmpNextDue,_tmpActive);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object account(final long id, final Continuation<? super Account> $completion) {
    final String _sql = "SELECT * FROM Account WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Account>() {
      @Override
      @Nullable
      public Account call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfOpeningBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "openingBalance");
          final Account _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpOpeningBalance;
            _tmpOpeningBalance = _cursor.getLong(_cursorIndexOfOpeningBalance);
            _result = new Account(_tmpId,_tmpName,_tmpOpeningBalance);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object envelope(final long id, final Continuation<? super Envelope> $completion) {
    final String _sql = "SELECT * FROM Envelope WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Envelope>() {
      @Override
      @Nullable
      public Envelope call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfParentId = CursorUtil.getColumnIndexOrThrow(_cursor, "parentId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRolloverToSavings = CursorUtil.getColumnIndexOrThrow(_cursor, "rolloverToSavings");
          final int _cursorIndexOfIsSavings = CursorUtil.getColumnIndexOrThrow(_cursor, "isSavings");
          final Envelope _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpAccountId;
            _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            final Long _tmpParentId;
            if (_cursor.isNull(_cursorIndexOfParentId)) {
              _tmpParentId = null;
            } else {
              _tmpParentId = _cursor.getLong(_cursorIndexOfParentId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final boolean _tmpRolloverToSavings;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfRolloverToSavings);
            _tmpRolloverToSavings = _tmp != 0;
            final boolean _tmpIsSavings;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSavings);
            _tmpIsSavings = _tmp_1 != 0;
            _result = new Envelope(_tmpId,_tmpAccountId,_tmpParentId,_tmpName,_tmpRolloverToSavings,_tmpIsSavings);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object children(final long id, final Continuation<? super List<Envelope>> $completion) {
    final String _sql = "SELECT * FROM Envelope WHERE parentId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Envelope>>() {
      @Override
      @NonNull
      public List<Envelope> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfParentId = CursorUtil.getColumnIndexOrThrow(_cursor, "parentId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRolloverToSavings = CursorUtil.getColumnIndexOrThrow(_cursor, "rolloverToSavings");
          final int _cursorIndexOfIsSavings = CursorUtil.getColumnIndexOrThrow(_cursor, "isSavings");
          final List<Envelope> _result = new ArrayList<Envelope>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Envelope _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpAccountId;
            _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            final Long _tmpParentId;
            if (_cursor.isNull(_cursorIndexOfParentId)) {
              _tmpParentId = null;
            } else {
              _tmpParentId = _cursor.getLong(_cursorIndexOfParentId);
            }
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final boolean _tmpRolloverToSavings;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfRolloverToSavings);
            _tmpRolloverToSavings = _tmp != 0;
            final boolean _tmpIsSavings;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSavings);
            _tmpIsSavings = _tmp_1 != 0;
            _item = new Envelope(_tmpId,_tmpAccountId,_tmpParentId,_tmpName,_tmpRolloverToSavings,_tmpIsSavings);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object dueRecurring(final long now,
      final Continuation<? super List<RecurringTransaction>> $completion) {
    final String _sql = "SELECT * FROM RecurringTransaction WHERE active = 1 AND nextDue <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, now);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecurringTransaction>>() {
      @Override
      @NonNull
      public List<RecurringTransaction> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfEnvelopeId = CursorUtil.getColumnIndexOrThrow(_cursor, "envelopeId");
          final int _cursorIndexOfInterval = CursorUtil.getColumnIndexOrThrow(_cursor, "interval");
          final int _cursorIndexOfNextDue = CursorUtil.getColumnIndexOrThrow(_cursor, "nextDue");
          final int _cursorIndexOfActive = CursorUtil.getColumnIndexOrThrow(_cursor, "active");
          final List<RecurringTransaction> _result = new ArrayList<RecurringTransaction>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecurringTransaction _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpAmount;
            _tmpAmount = _cursor.getLong(_cursorIndexOfAmount);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpAccountId;
            _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            final Long _tmpEnvelopeId;
            if (_cursor.isNull(_cursorIndexOfEnvelopeId)) {
              _tmpEnvelopeId = null;
            } else {
              _tmpEnvelopeId = _cursor.getLong(_cursorIndexOfEnvelopeId);
            }
            final String _tmpInterval;
            _tmpInterval = _cursor.getString(_cursorIndexOfInterval);
            final long _tmpNextDue;
            _tmpNextDue = _cursor.getLong(_cursorIndexOfNextDue);
            final boolean _tmpActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfActive);
            _tmpActive = _tmp != 0;
            _item = new RecurringTransaction(_tmpId,_tmpName,_tmpAmount,_tmpType,_tmpAccountId,_tmpEnvelopeId,_tmpInterval,_tmpNextDue,_tmpActive);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
