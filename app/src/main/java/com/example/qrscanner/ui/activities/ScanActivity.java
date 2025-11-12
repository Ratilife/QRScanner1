package com.example.qrscanner.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.qrscanner.R;
import com.example.qrscanner.models.QRCode;
import com.example.qrscanner.ui.viewmodels.ScanViewModel;
import com.example.qrscanner.utils.CameraHelper;
import com.example.qrscanner.utils.DebugLogger;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class ScanActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final String TAG = "ScanActivity";

    private ScanViewModel viewModel;
    private DecoratedBarcodeView barcodeView;
    private Button btnBack, btnListCodes, btnFlashlight, btnCameraSwitch;
    private Button btnNormalMode, btnBatchMode;
    private CameraHelper cameraHelper;
    private boolean isBatchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(ScanViewModel.class);
        cameraHelper = new CameraHelper(this);

        initViews();
        setupObservers();
        checkCameraPermission();
    }

    private void initViews() {
        barcodeView = findViewById(R.id.barcodeView);
        btnBack = findViewById(R.id.btnBack);
        btnListCodes = findViewById(R.id.btnListCodes);
        btnFlashlight = findViewById(R.id.btnFlashlight);
        btnCameraSwitch = findViewById(R.id.btnCameraSwitch);
        btnNormalMode = findViewById(R.id.btnNormalMode);
        btnBatchMode = findViewById(R.id.btnBatchMode);

        btnBack.setOnClickListener(v -> finish());

        btnListCodes.setOnClickListener(v -> {
            Intent intent = new Intent(ScanActivity.this, ListScannedActivity.class);
            startActivity(intent);
        });

        btnFlashlight.setOnClickListener(v -> {
            if (cameraHelper.hasFlash(this)) {
                cameraHelper.toggleFlash(this);
                btnFlashlight.setText(cameraHelper.isFlashOn() ? "Выкл. фонарик" : "Вкл. фонарик");
            } else {
                Toast.makeText(this, "Фонарик недоступен", Toast.LENGTH_SHORT).show();
            }
        });

        btnCameraSwitch.setOnClickListener(v -> {
            // Switch camera functionality would be implemented here
            Toast.makeText(this, "Переключение камеры", Toast.LENGTH_SHORT).show();
        });

        btnNormalMode.setOnClickListener(v -> {
            isBatchMode = false;
            viewModel.setBatchMode(false);
            updateModeButtons();
        });

        btnBatchMode.setOnClickListener(v -> {
            isBatchMode = true;
            viewModel.setBatchMode(true);
            updateModeButtons();
        });

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    handleScanResult(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) {
            }
        });
    }

    private void updateModeButtons() {
        if (isBatchMode) {
            btnBatchMode.setAlpha(1.0f);
            btnNormalMode.setAlpha(0.5f);
        } else {
            btnNormalMode.setAlpha(1.0f);
            btnBatchMode.setAlpha(0.5f);
        }
    }

    private void setupObservers() {
        viewModel.getScannedCode().observe(this, qrCode -> {
            if (qrCode != null) {
                if (!isBatchMode) {
                    // Single scan mode - go to list
                    Intent intent = new Intent(ScanActivity.this, ListScannedActivity.class);
                    startActivity(intent);
                } else {
                    // Batch mode - continue scanning
                    Toast.makeText(this, "QR-код добавлен: " + qrCode.getValue(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleScanResult(String qrValue) {
        DebugLogger.d(TAG, "QR Code scanned: " + qrValue);
        viewModel.onQRCodeScanned(qrValue);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            startScanning();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, "Разрешение на камеру необходимо для сканирования", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startScanning() {
        barcodeView.resume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            barcodeView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraHelper != null) {
            cameraHelper.release();
        }
    }
}

