package com.example.qrscanner.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.qrscanner.data.repositories.QRCodeRepository;
import com.example.qrscanner.models.QRCode;

import java.util.List;

public class SendViewModel extends AndroidViewModel {
    private QRCodeRepository repository;
    private MutableLiveData<Boolean> exportSuccess = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public SendViewModel(Application application) {
        super(application);
        repository = new QRCodeRepository(application);
    }

    public MutableLiveData<Boolean> getExportSuccess() {
        return exportSuccess;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public List<QRCode> getSessionCodes() {
        return repository.getSessionCodes();
    }

    public void exportData(List<QRCode> codes) {
        if (codes == null || codes.isEmpty()) {
            errorMessage.setValue("Нет данных для экспорта");
            return;
        }
        exportSuccess.setValue(true);
    }
}



