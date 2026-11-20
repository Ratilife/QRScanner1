package com.example.qrscanner.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.File;

/**
 * QRCodeScanner — утилита для распознавания QR-кодов из ГОТОВЫХ ИЗОБРАЖЕНИЙ
 * (byte[] / Bitmap / File). Это НЕ сканер с камеры в реальном времени —
 * для камеры используется библиотека journeyapps (DecoratedBarcodeView)
 * в ScanActivity.
 *
 * ЗАЧЕМ НУЖЕН:
 *  - распознать QR-код с картинки, выбранной из галереи;
 *  - распознать QR-код из файла (например, сохранённого скриншота);
 *  - распознать QR-код из байтового массива (например, из сети/БД).
 *
 * ЖИЗНЕННЫЙ ЦИКЛ: это НЕ часть жизненного цикла Activity — статическая
 * утилита, вызывается из любого места (Activity, ViewModel, фоновый поток).
 * Обычно вызывается НЕ в onResume/onPause, а по действию пользователя
 * («выбрать картинку из галереи») — например, в onActivityResult()
 * после выбора файла (аналогично JsonInventoryActivity, где JSON читается
 * в onActivityResult()).
 *
 * ВАЖНО (потоки): decode() из ZXing — синхронная и потенциально
 * длительная операция (особенно для больших Bitmap). Вызывать её
 * в главном потоке (UI) не рекомендуется — иначе возможен ANR.
 * Обычно QRCodeScanner вызывают из фонового потока (Executor, Thread,
 * coroutine), а результат возвращают в UI через Handler/LiveData.
 * В текущем коде этой утилиты фонового потока нет — ответственность
 * за поток лежит на вызывающем коде.
 */
public class QRCodeScanner {
    // Тег для логов Logcat (используется в DebugLogger).
    private static final String TAG = "QRCodeScanner";
    // Объект ZXing для декодирования QR-кодов.
    // Создаётся один раз в конструкторе и переиспользуется.
    // ВНИМАНИЕ: QRCodeReader из ZXing не является потокобезопасным —
    // если QRCodeScanner используется из нескольких потоков одновременно,
    // нужна синхронизация или отдельный экземпляр на поток.
    private QRCodeReader qrCodeReader;

    /**
     * Конструктор: создаём QRCodeReader один раз.
     * Повторное создание reader'а на каждый вызов scanQRCode()
     * было бы расточительным.
     */
    public QRCodeScanner() {
        qrCodeReader = new QRCodeReader();
    }

    /**
     * Распознаёт QR-код из БАЙТОВОГО МАССИВА изображения.
     *
     * Шаги:
     *  1. Декодируем byte[] в Bitmap через BitmapFactory.
     *  2. Передаём Bitmap в scanQRCode(Bitmap) — там основная логика ZXing.
     *
     * @param imageData байты изображения (JPEG/PNG и т.п.)
     * @return текст QR-кода или null, если код не найден / ошибка
     */
    public String scanQRCode(byte[] imageData) {
        try {
            // Декодируем массив байтов в Bitmap.
            // Если данные битые — decodeByteArray вернёт null,
            // и scanQRCode(null) бросит NPE, который поймает catch ниже.
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            return scanQRCode(bitmap);
        } catch (Exception e) {
            // Логируем ошибку и возвращаем null — вызывающий код
            // должен проверить результат на null.
            DebugLogger.e(TAG, "Error scanning QR code from byte array", e);
            return null;
        }
    }

    /**
     * Распознаёт QR-код из готового Bitmap — ОСНОВНАЯ логика ZXing.
     *
     * Шаги:
     *  1. Получаем ширину/высоту и массив пикселей (ARGB int[]).
     *  2. Оборачиваем пиксели в RGBLuminanceSource (ZXing работает
     *     с яркостью, а не с цветом).
     *  3. HybridBinarizer — адаптивная бинаризация: превращает
     *     изображение в чёрно-белое, устойчиво к неравномерному освещению.
     *  4. QRCodeReader.decode() — собственно распознавание.
     *
     * @param bitmap изображение с QR-кодом
     * @return текст QR-кода или null, если код не найден / ошибка
     */
    public String scanQRCode(Bitmap bitmap) {
        try {
            // Шаг 1: извлекаем пиксели Bitmap в массив int[] (ARGB).
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            // Шаг 2: оборачиваем пиксели в источник яркости ZXing.
            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            // Шаг 3: бинаризация (адаптивная, HybridBinarizer).
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            // Шаг 4: распознавание QR-кода.
            Result result = qrCodeReader.decode(binaryBitmap);
            String qrValue = result.getText();
            // Успех — логируем и возвращаем текст.
            DebugLogger.d(TAG, "QR code scanned successfully: " + qrValue);
            return qrValue;
        } catch (NotFoundException e) {
            // QR-код не найден на изображении — это НЕ ошибка,
            // а штатная ситуация (пользователь выбрал картинку без кода).
            // Логируем как DEBUG, возвращаем null.
            DebugLogger.d(TAG, "QR code not found in image");
            return null;
        } catch (ChecksumException | FormatException e) {
            // Код найден, но повреждён (неверная контрольная сумма)
            // или имеет неверный формат. Это уже ошибка — логируем как ERROR.
            DebugLogger.e(TAG, "Error decoding QR code", e);
            return null;
        } catch (Exception e) {
            // Любая другая ошибка (NPE, OOM и т.п.) — логируем как ERROR.
            DebugLogger.e(TAG, "Unexpected error scanning QR code", e);
            return null;
        }
    }

    /**
     * Распознаёт QR-код из ФАЙЛА на диске.
     *
     * Шаги:
     *  1. Декодируем файл в Bitmap через BitmapFactory.
     *  2. Проверяем, что Bitmap не null (файл битый / не изображение).
     *  3. Передаём Bitmap в scanQRCode(Bitmap).
     *
     * @param file файл с изображением
     * @return текст QR-кода или null, если код не найден / ошибка
     */
    public String scanQRCodeFromFile(File file) {
        try {
            // Шаг 1: декодируем файл в Bitmap.
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            // Шаг 2: если файл не изображение или повреждён —
            // decodeFile вернёт null. Логируем и выходим.
            if (bitmap == null) {
                DebugLogger.e(TAG, "Failed to decode bitmap from file");
                return null;
            }
            // Шаг 3: основная логика распознавания.
            return scanQRCode(bitmap);
        } catch (Exception e) {
            DebugLogger.e(TAG, "Error scanning QR code from file", e);
            return null;
        }
    }
}




