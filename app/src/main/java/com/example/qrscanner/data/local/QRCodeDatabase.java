package com.example.qrscanner.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.qrscanner.data.local.entities.QRCodeEntity;

@Database(entities = {QRCodeEntity.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class QRCodeDatabase extends RoomDatabase {
    private static QRCodeDatabase instance;

    public abstract QRCodeDao qrCodeDao();

    public static synchronized QRCodeDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    QRCodeDatabase.class,
                    "qr_code_database"
            ).build();
        }
        return instance;
    }
}



