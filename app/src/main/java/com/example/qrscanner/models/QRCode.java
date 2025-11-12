package com.example.qrscanner.models;

import java.util.Date;

public class QRCode {
    private String value;
    private Date scanDate;
    private long id;

    public QRCode() {
        this.scanDate = new Date();
    }

    public QRCode(String value) {
        this.value = value;
        this.scanDate = new Date();
    }

    public QRCode(String value, Date scanDate) {
        this.value = value;
        this.scanDate = scanDate;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}



