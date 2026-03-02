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

public class QRCodeScanner {
    private static final String TAG = "QRCodeScanner";
    private QRCodeReader qrCodeReader;

    public QRCodeScanner() {
        qrCodeReader = new QRCodeReader();
    }

    public String scanQRCode(byte[] imageData) {
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            return scanQRCode(bitmap);
        } catch (Exception e) {
            DebugLogger.e(TAG, "Error scanning QR code from byte array", e);
            return null;
        }
    }

    public String scanQRCode(Bitmap bitmap) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = qrCodeReader.decode(binaryBitmap);
            String qrValue = result.getText();
            DebugLogger.d(TAG, "QR code scanned successfully: " + qrValue);
            return qrValue;
        } catch (NotFoundException e) {
            DebugLogger.d(TAG, "QR code not found in image");
            return null;
        } catch (ChecksumException | FormatException e) {
            DebugLogger.e(TAG, "Error decoding QR code", e);
            return null;
        } catch (Exception e) {
            DebugLogger.e(TAG, "Unexpected error scanning QR code", e);
            return null;
        }
    }

    public String scanQRCodeFromFile(File file) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap == null) {
                DebugLogger.e(TAG, "Failed to decode bitmap from file");
                return null;
            }
            return scanQRCode(bitmap);
        } catch (Exception e) {
            DebugLogger.e(TAG, "Error scanning QR code from file", e);
            return null;
        }
    }
}




