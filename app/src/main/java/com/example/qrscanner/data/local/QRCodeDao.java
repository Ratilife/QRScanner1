package com.example.qrscanner.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.qrscanner.data.local.entities.QRCodeEntity;

import java.util.List;

@Dao
public interface QRCodeDao {
    @Query("SELECT * FROM qr_codes ORDER BY scanDate DESC")
    LiveData<List<QRCodeEntity>> getAllCodes();

    @Query("SELECT * FROM qr_codes WHERE id = :id")
    LiveData<QRCodeEntity> getCodeById(long id);

    @Insert
    long insert(QRCodeEntity qrCode);

    @Update
    void update(QRCodeEntity qrCode);

    @Delete
    void delete(QRCodeEntity qrCode);

    @Query("DELETE FROM qr_codes WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM qr_codes")
    void deleteAll();
}




