package com.example.qrscanner.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class CameraHelper {
    private static final String TAG = "CameraHelper";
    private Camera camera;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;

    public CameraHelper(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        } else {
            camera = Camera.open();
        }
    }

    public boolean hasFlash(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void toggleFlash(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            toggleFlashMarshmallow(context);
        } else {
            toggleFlashLegacy();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void toggleFlashMarshmallow(Context context) {
        try {
            if (cameraManager == null) {
                cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            }
            if (cameraId == null) {
                cameraId = cameraManager.getCameraIdList()[0];
            }
            cameraManager.setTorchMode(cameraId, !isFlashOn);
            isFlashOn = !isFlashOn;
            DebugLogger.d(TAG, "Flash toggled: " + isFlashOn);
        } catch (CameraAccessException e) {
            DebugLogger.e(TAG, "Error toggling flash", e);
        }
    }

    private void toggleFlashLegacy() {
        if (camera == null) {
            return;
        }
        Camera.Parameters params = camera.getParameters();
        if (isFlashOn) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            isFlashOn = false;
        } else {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            isFlashOn = true;
        }
        camera.setParameters(params);
        DebugLogger.d(TAG, "Flash toggled (legacy): " + isFlashOn);
    }

    public void release() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraManager != null && isFlashOn) {
            try {
                if (cameraId != null) {
                    cameraManager.setTorchMode(cameraId, false);
                }
            } catch (CameraAccessException e) {
                DebugLogger.e(TAG, "Error releasing flash", e);
            }
        }
    }

    public boolean isFlashOn() {
        return isFlashOn;
    }
}




