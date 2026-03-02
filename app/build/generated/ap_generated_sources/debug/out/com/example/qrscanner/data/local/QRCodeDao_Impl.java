package com.example.qrscanner.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.qrscanner.data.local.entities.QRCodeEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class QRCodeDao_Impl implements QRCodeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<QRCodeEntity> __insertionAdapterOfQRCodeEntity;

  private final EntityDeletionOrUpdateAdapter<QRCodeEntity> __deletionAdapterOfQRCodeEntity;

  private final EntityDeletionOrUpdateAdapter<QRCodeEntity> __updateAdapterOfQRCodeEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public QRCodeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfQRCodeEntity = new EntityInsertionAdapter<QRCodeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `qr_codes` (`id`,`value`,`scanDate`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final QRCodeEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getValue() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getValue());
        }
        final Long _tmp = DateConverter.dateToTimestamp(entity.getScanDate());
        if (_tmp == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, _tmp);
        }
      }
    };
    this.__deletionAdapterOfQRCodeEntity = new EntityDeletionOrUpdateAdapter<QRCodeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `qr_codes` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final QRCodeEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfQRCodeEntity = new EntityDeletionOrUpdateAdapter<QRCodeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `qr_codes` SET `id` = ?,`value` = ?,`scanDate` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final QRCodeEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getValue() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getValue());
        }
        final Long _tmp = DateConverter.dateToTimestamp(entity.getScanDate());
        if (_tmp == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, _tmp);
        }
        statement.bindLong(4, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM qr_codes WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM qr_codes";
        return _query;
      }
    };
  }

  @Override
  public long insert(final QRCodeEntity qrCode) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfQRCodeEntity.insertAndReturnId(qrCode);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final QRCodeEntity qrCode) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfQRCodeEntity.handle(qrCode);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final QRCodeEntity qrCode) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfQRCodeEntity.handle(qrCode);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteById(final long id) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, id);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteById.release(_stmt);
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public LiveData<List<QRCodeEntity>> getAllCodes() {
    final String _sql = "SELECT * FROM qr_codes ORDER BY scanDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"qr_codes"}, false, new Callable<List<QRCodeEntity>>() {
      @Override
      @Nullable
      public List<QRCodeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfScanDate = CursorUtil.getColumnIndexOrThrow(_cursor, "scanDate");
          final List<QRCodeEntity> _result = new ArrayList<QRCodeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final QRCodeEntity _item;
            _item = new QRCodeEntity();
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            _item.setId(_tmpId);
            final String _tmpValue;
            if (_cursor.isNull(_cursorIndexOfValue)) {
              _tmpValue = null;
            } else {
              _tmpValue = _cursor.getString(_cursorIndexOfValue);
            }
            _item.setValue(_tmpValue);
            final Date _tmpScanDate;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfScanDate)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfScanDate);
            }
            _tmpScanDate = DateConverter.fromTimestamp(_tmp);
            _item.setScanDate(_tmpScanDate);
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
  public LiveData<QRCodeEntity> getCodeById(final long id) {
    final String _sql = "SELECT * FROM qr_codes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return __db.getInvalidationTracker().createLiveData(new String[] {"qr_codes"}, false, new Callable<QRCodeEntity>() {
      @Override
      @Nullable
      public QRCodeEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfScanDate = CursorUtil.getColumnIndexOrThrow(_cursor, "scanDate");
          final QRCodeEntity _result;
          if (_cursor.moveToFirst()) {
            _result = new QRCodeEntity();
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            _result.setId(_tmpId);
            final String _tmpValue;
            if (_cursor.isNull(_cursorIndexOfValue)) {
              _tmpValue = null;
            } else {
              _tmpValue = _cursor.getString(_cursorIndexOfValue);
            }
            _result.setValue(_tmpValue);
            final Date _tmpScanDate;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfScanDate)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfScanDate);
            }
            _tmpScanDate = DateConverter.fromTimestamp(_tmp);
            _result.setScanDate(_tmpScanDate);
          } else {
            _result = null;
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
