package com.example.qrscanner.data.local;  // Указывает пакет, к которому принадлежит класс.
                                           // Это помогает организовать код в проекте.
import android.content.Context;             // Импорт для получения контекста приложения.

import androidx.room.Database;            // Аннотация @Database из библиотеки Room.
import androidx.room.Room;                // Класс для создания билдера базы данных Room.
import androidx.room.RoomDatabase;        // Абстрактный базовый класс для баз данных Room.
import androidx.room.TypeConverters;      // Аннотация @TypeConverters для преобразования типов.

import com.example.qrscanner.data.local.entities.QRCodeEntity; // Импорт сущности, которая будет таблицей в БД.

// Аннотация @Database:
// - entities = {QRCodeEntity.class}: Указывает, какие сущности (Entity) будут представлены
//   в виде таблиц в этой базе данных. В данном случае, таблица будет создана для QRCodeEntity.
// - version = 1: Версия схемы базы данных. При изменении структуры БД (например, добавление столбца)
//   нужно увеличивать эту версию, чтобы Room мог корректно обработать миграцию.
// - exportSchema = false: Если true, Room экспортирует схему БД в файл JSON. Это полезно для
//   проверки миграции, но часто отключается в начале разработки.
@Database(entities = {QRCodeEntity.class}, version = 1, exportSchema = false)
// Аннотация @TypeConverters:
// - Указывает классы-конвертеры, которые Room должен использовать для преобраз нестандартных типов данных (например, Date, Instant) в типы, поддерживаемые SQLite,
//   и обратно. Здесь используется DateConverter.
@TypeConverters({DateConverter.class})
// Наследование от RoomDatabase:
// - Класс должен быть абстрактным и наследоваться от RoomDatabase, чтобы Room мог
//   сгенерировать реализацию для доступа к БД
public abstract class QRCodeDatabase extends RoomDatabase {
    // Переменная instance:
    // - Статическая переменная для хранения единственного экземпляра базы данных.
    // - Используется для реализации паттерна Singleton, чтобы всегда работать
    //   с одним и тем же экземпляром БД в приложении.
    private static QRCodeDatabase instance;
    // Абстрактный метод qrCodeDao():
    // - Должен возвращать DAO (Data Access Object), который определен в другом месте.
    // - Room автоматически генерирует реализацию этого DAO.
    // - DAO содержит методы для выполнения операций CRUD (Create, Read, Update, Delete)
    //   над сущностью QRCodeEntity.
    public abstract QRCodeDao qrCodeDao();

    // Статический синхронизированный метод getInstance():
    // - Реализует паттерн Singleton.
    // - synchronized гарантирует, что только один поток может выполнить этот метод одновременно,
    //   предотвращая создание нескольких экземпляров базы данных.
    // - Принимает Context, который необходим для создания базы данных.

    // Метод getInstance внутри QRCodeDatabase проверяет, существует ли уже экземпляр базы данных (instance).
    // Если нет (instance == null), он создает новый экземпляр с помощью Room.databaseBuilder,
    // инициализируя базу данных SQLite с именем "qr_code_database". Затем этот экземпляр сохраняется в переменной instance
    // Наконец, метод возвращает текущий экземпляр базы данных (либо только что созданный, либо уже существующий).
    public static synchronized QRCodeDatabase getInstance(Context context) {
        // Проверка, был ли уже создан экземпляр базы данных.
        if (instance == null) {
            // Если instance равен null, значит, база данных еще не создана.
            // Создаем новый экземпляр с помощью Room.databaseBuilder.
            instance = Room.databaseBuilder(
                    // Контекст приложения. getApplicationContext() предпочтительно,
                    // чтобы избежать утечки памяти.
                    context.getApplicationContext(),
                    // Класс текущ (для внутреннего использования Room).
                    QRCodeDatabase.class,
                    "qr_code_database"      // Имя файла базы данных SQLite на устройстве.
            ).build();       // Метод build() завершает настройку и создает экземпляр базы данных.
        }
        // Возвращаем существующий или только что созданный экземпляр.
        return instance;
    }
}




