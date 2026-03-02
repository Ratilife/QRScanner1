package com.example.qrscanner;

import android.app.Application;

import com.example.qrscanner.data.local.QRCodeDatabase;

public class QRScannerApplication extends Application {
    //тот метод вызывается один раз при первом запуске приложения, до создания любых активностей (Activity)
    // или сервисов (Service). Это идеальное место для инициализации компонентов,
    // которые должны быть доступны в течение всего жизненного цикла приложения.
    @Override
    public void onCreate() {
        super.onCreate();  // Вызов родительской реализации
        //Эта строка инициализирует базу данных QR-кодов. Предполагается, что QRCodeDatabase реализует паттерн Singleton (одиночка).
        // Метод getInstance(Context context) получает Context (в данном случае this, так как Application является Context),
        // чтобы получить доступ к ресурсам и файловой системе устройства для создания или получения экземпляра базы данных.
        // Вызов этого метода здесь гарантирует, что база данных будет готова к использованию в других частях приложения сразу после запуска.
        // Initialize database
        QRCodeDatabase.getInstance(this);
        // Здесь this означает текущий экземпляр QRScannerApplication, который передается в качестве аргумента Context в метод getInstance.
    }
}




