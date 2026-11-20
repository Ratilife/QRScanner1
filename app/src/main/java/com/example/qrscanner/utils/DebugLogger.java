package com.example.qrscanner.utils;

import android.util.Log;

/**
 * DebugLogger — утилита для логирования в Logcat.
 *
 * Зачем нужна обёртка над android.util.Log:
 *  1. Единый тег по умолчанию ("QRScanner") + опциональный подтег
 *     (например, "ScanActivity") — в Logcat удобно фильтровать
 *     по "QRScanner" и видеть все логи приложения, либо по
 *     "QRScanner/ScanActivity" — только логи конкретного экрана.
 *  2. Глобальный переключатель isDebugEnabled: отладочные (d) и
 *     информационные (i) логи можно выключить одной строкой
 *     (setDebugEnabled(false)) перед релизом, не удаляя вызовы из кода.
 *     Ошибки (e) и предупреждения (w) пишутся ВСЕГДА — их глушить нельзя.
 *  3. Единый формат тегов — проще искать в Logcat, не нужно в каждом
 *     классе заводить свой TAG и помнить про конкатенацию.
 *
 * ВАЖНО: это НЕ часть жизненного цикла Activity — утилита вызывается
 * из любого места приложения (из Activity, ViewModel, хелперов).
 * Но именно через неё в коде фиксируются события жизненного цикла
 * и ошибочные сценарии (например, отказ в разрешении камеры
 * в ScanActivity, ошибка чтения JSON в JsonInventoryActivity).
 */
public class DebugLogger {
    // Базовый тег для ВСЕХ логов приложения. В Logcat фильтр "QRScanner"
    // покажет все логи. Если передан подтег, итоговый тег будет
    // "QRScanner/<подтег>", например "QRScanner/ScanActivity".
    private static final String TAG = "QRScanner";
    // Глобальный переключатель отладочных логов.
    // true  — d() и i() пишутся в Logcat (режим разработки).
    // false — d() и i() молчат (режим релиза, чтобы не засорять Logcat).
    // e() и w() пишутся ВСЕГДА — ошибки и предупреждения нужны и в релизе.
    private static boolean isDebugEnabled = true;

    /**
     * Включает/выключает отладочные (d) и информационные (i) логи.
     * Вызывать при старте приложения (например, в Application.onCreate())
     * в зависимости от BuildConfig.DEBUG:
     *   DebugLogger.setDebugEnabled(BuildConfig.DEBUG);
     *
     * @param enabled true — писать d()/i(), false — молчать.
     */
    public static void setDebugEnabled(boolean enabled) {
        isDebugEnabled = enabled;
    }

    /**
     * DEBUG-лог с базовым тегом "QRScanner".
     * Пишется только если isDebugEnabled == true.
     * Используется для подробных отладочных сообщений.
     */
    public static void d(String message) {
        if (isDebugEnabled) {
            Log.d(TAG, message);
        }
    }
    /**
     * DEBUG-лог с подтегом: итоговый тег "QRScanner/<tag>".
     * Пишется только если isDebugEnabled == true.
     * Пример: DebugLogger.d("ScanActivity", "QR Code scanned: ...")
     * → тег "QRScanner/ScanActivity".
     */
    public static void d(String tag, String message) {
        if (isDebugEnabled) {
            Log.d(TAG + "/" + tag, message);
        }
    }

    /**
     * ERROR-лог с базовым тегом "QRScanner".
     * Пишется ВСЕГДА (даже при isDebugEnabled == false) — ошибки
     * нужно видеть и в релизе.
     */
    public static void e(String message) {
        Log.e(TAG, message);
    }

    /**
     * ERROR-лог с подтегом и стектрейсом исключения.
     * Пишется ВСЕГДА. Используется, когда нужно зафиксировать
     * исключение (например, ошибка чтения JSON-файла
     * в JsonInventoryActivity):
     *   DebugLogger.e(TAG, "Error reading JSON file", e);
     * В Logcat будет и сообщение, и полный стектрейс.
     */
    public static void e(String tag, String message) {
        Log.e(TAG + "/" + tag, message);
    }

    /**
     * INFO-лог с базовым тегом "QRScanner".
     * Пишется только если isDebugEnabled == true.
     */
    public static void e(String tag, String message, Throwable throwable) {
        Log.e(TAG + "/" + tag, message, throwable);
    }


    public static void i(String message) {
        if (isDebugEnabled) {
            Log.i(TAG, message);
        }
    }
    /**
     * INFO-лог с подтегом: итоговый тег "QRScanner/<tag>".
     * Пишется только если isDebugEnabled == true.
     */
    public static void i(String tag, String message) {
        if (isDebugEnabled) {
            Log.i(TAG + "/" + tag, message);
        }
    }
    /**
     * WARN-лог с базовым тегом "QRScanner".
     * Пишется ВСЕГДА (даже при isDebugEnabled == false).
     * Используется для нештатных, но не критичных ситуаций —
     * например, «фонарик недоступен» или «пользователь отклонил
     * разрешение камеры» (TODO из ScanActivity).
     */
    public static void w(String message) {
        Log.w(TAG, message);
    }

    /**
     * WARN-лог с подтегом: итоговый тег "QRScanner/<tag>".
     * Пишется ВСЕГДА.
     */
    public static void w(String tag, String message) {
        Log.w(TAG + "/" + tag, message);
    }
}




