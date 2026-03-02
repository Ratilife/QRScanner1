package com.example.qrscanner.utils;

import android.util.Log;

public class DebugLogger {
    private static final String TAG = "QRScanner";
    private static boolean isDebugEnabled = true;

    public static void setDebugEnabled(boolean enabled) {
        isDebugEnabled = enabled;
    }

    public static void d(String message) {
        if (isDebugEnabled) {
            Log.d(TAG, message);
        }
    }

    public static void d(String tag, String message) {
        if (isDebugEnabled) {
            Log.d(TAG + "/" + tag, message);
        }
    }

    public static void e(String message) {
        Log.e(TAG, message);
    }

    public static void e(String tag, String message) {
        Log.e(TAG + "/" + tag, message);
    }

    public static void e(String tag, String message, Throwable throwable) {
        Log.e(TAG + "/" + tag, message, throwable);
    }

    public static void i(String message) {
        if (isDebugEnabled) {
            Log.i(TAG, message);
        }
    }

    public static void i(String tag, String message) {
        if (isDebugEnabled) {
            Log.i(TAG + "/" + tag, message);
        }
    }

    public static void w(String message) {
        Log.w(TAG, message);
    }

    public static void w(String tag, String message) {
        Log.w(TAG + "/" + tag, message);
    }
}




