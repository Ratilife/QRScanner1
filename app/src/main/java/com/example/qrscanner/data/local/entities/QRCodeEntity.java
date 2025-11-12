package com.example.qrscanner.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.qrscanner.data.local.DateConverter;

import java.util.Date;

@Entity(tableName = "qr_codes")
@TypeConverters(DateConverter.class)
public class QRCodeEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String value;
    private Date scanDate;

    public QRCodeEntity() {
    }

    public QRCodeEntity(String value, Date scanDate) {
        this.value = value;
        this.scanDate = scanDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getScanDate() {
        return scanDate;
    }

    public void setScanDate(Date scanDate) {
        this.scanDate = scanDate;
    }
}


