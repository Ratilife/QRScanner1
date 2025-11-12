package com.example.qrscanner.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.qrscanner.data.repositories.QRCodeRepository;
import com.example.qrscanner.models.QRCode;

public class ScanViewModel extends AndroidViewModel {
    private QRCodeRepository repository;
    private MutableLiveData<QRCode> scannedCode = new MutableLiveData<>();
    private MutableLiveData<Boolean> isBatchMode = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ScanViewModel(Application application) {
        super(application);
        repository = new QRCodeRepository(application);
    }

    public MutableLiveData<QRCode> getScannedCode() {
        return scannedCode;
    }

    public MutableLiveData<Boolean> getIsBatchMode() {
        return isBatchMode;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setBatchMode(boolean batchMode) {
        isBatchMode.setValue(batchMode);
    }

    public void onQRCodeScanned(String qrValue) {
        if (qrValue != null && !qrValue.isEmpty()) {
            QRCode qrCode = new QRCode(qrValue);
            scannedCode.setValue(qrCode);
            
            if (isBatchMode.getValue() != null && isBatchMode.getValue()) {
                repository.addSessionCode(qrCode);
            } else {
                repository.insert(qrCode);
            }
        } else {
            errorMessage.setValue("Не удалось отсканировать QR-код");
        }
    }

    public QRCodeRepository getRepository() {
        return repository;
    }
}



