package com.example.qrscanner;

import android.app.Application;

import com.example.qrscanner.data.local.QRCodeDatabase;

public class QRScannerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize database
        QRCodeDatabase.getInstance(this);
    }
}



