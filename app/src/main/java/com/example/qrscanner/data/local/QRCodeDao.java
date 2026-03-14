package com.example.qrscanner.data.local;   // Указывает пакет, к которому принадлежит интерфейс.

// Импортирует LiveData из библиотеки Architecture Components.
// LiveData позволяет UI наблюдать за изменениями данных в базе данных.
import androidx.lifecycle.LiveData;
// Импортирует аннотацию @Dao из библиотеки Room.
// Она указывает, что этот интерфейс является DAO (Data Access Object).
import androidx.room.Dao;
// Импортирует аннотацию @Delete из библиотеки Room.
// Используется для пометки метода, удаляющего сущности.
import androidx.room.Delete;
// Импортирует аннотацию @Insert из библиотеки Room.
// Используется для пометки метода, вставляющего сущности.
import androidx.room.Insert;
// Импортирует аннотацию @Query из библиотеки Room.
// Позволяет писать SQL-запросы для выполнения операций с БД.
import androidx.room.Query;
// Импортирует аннотацию @Update из библиотеки Room.
// Используется для пометки метода, обновляющего сущности.
import androidx.room.Update;
// Импортирует сущность QRCodeEntity, которую DAO будет использовать.
import com.example.qrscanner.data.local.entities.QRCodeEntity;
// Импортирует стандартный класс List для возврата коллекций
import java.util.List;
// Аннотация @Dao указывает Room, что это Data Access Object.
// Интерфейс содержит методы для доступа к данным сущности QRCodeEntity.
@Dao
public interface QRCodeDao { // Объявление интерфейса DAO.
    // Аннотация @Query: определяет SQL-запрос.
    // SELECT * FROM qr_codes ORDER BY scanDate DESC:
    // - Выбирает все столбцы (*) из таблицы 'qr_codes'.
    // - Результаты сортируются по столбцу 'scanDate' в порядке убывания (DESC).
    @Query("SELECT * FROM qr_codes ORDER BY scanDate DESC")
    // Метод возвращает LiveData, содержащий список всех QRCodeEntity.
    // LiveData автоматически уведомляет наблюдателей (например, UI) об изменениях.
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


/*
Интерфейс QRCodeDao определяет контракт (набор методов) для выполнения операций с базой данных,
связанных с сущностью QRCodeEntity. Room автоматически генерирует реализацию этого интерфейса,
преобразуя аннотации (@Query, @Insert, @Update, @Delete) в соответствующие SQL-операции.
Это позволяет удобно и безопасно работать с базой данных, не требуя написания большого объема boilerplate-кода.
Использование LiveData позволяет легко обновлять UI при изменении данных в базе.
 */