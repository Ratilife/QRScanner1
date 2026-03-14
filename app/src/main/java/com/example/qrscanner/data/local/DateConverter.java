package com.example.qrscanner.data.local;   // Указывает пакет, к которому принадлежит класс.

import androidx.room.TypeConverter;         // Импортирует аннотацию @TypeConverter из библиотеки Room.

import java.util.Date;                      // Импортирует стандартный класс Date из пакета java.util.

// Объявление публичного класса DateConverter.
// Этот класс предназначен для преобразования между типами Java и типами, поддерживаемыми SQLite.
public class DateConverter {
    // Аннотация @TypeConverter указывает Room, что этот метод используется
    // для преобразования значения из базы данных (Long) обратно в объект Java (Dat
    @TypeConverter
    // Объявление статического метода, который принимает Long (временная метка) и возвращает Date.
    // Метод может вернуть null, если входное значение null.
    public static Date fromTimestamp(Long value) {
        // Тернарный оператор: если value равно null, возвращается null.
        // Иначе, создается новый объект Date, используя временную метку value.
        return value == null ? null : new Date(value);
    }
    // Аннотация @TypeConverter указывает Room, что этот метод используется
    // для преобразования объекта Java (Date) в значение, которое можно сохранить в базе данных (Long).
    @TypeConverter
    // Объявление статического метода, который принимает Date и возвращает Long (временную метку).
    // Метод может вернуть null, если входное значение date null.
    public static Long dateToTimestamp(Date date) {
        // Тернарный оператор: если date равно null, возвращается null.
        // Иначе, возвращается временная метка (long) с помощью метода getTime().
        return date == null ? null : date.getTime();
    }
}


/*
Класс DateConverter играет важную роль в интеграции с Room Database. SQLite не имеет собственного типа данных для хранения дат.
Поэтому DateConverter предоставляет два метода:
dateToTimestamp: Преобразует объект java.util.Date в Long (временная метка в миллисекундах с эпохи Unix).
Эта Long-величина затем сохраняется в базе данных.
fromTimestamp: Преобразует Long (временная метка из базы данных) обратно в объект java.util.Date.
Благодаря аннотации @TypeConverters({DateConverter.class}) в QRCodeDatabase, Room знает,
что нужно использовать эти методы при сохранении и чтении объектов Date.
*/

