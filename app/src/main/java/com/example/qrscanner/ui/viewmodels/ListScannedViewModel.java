package com.example.qrscanner.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.qrscanner.data.repositories.QRCodeRepository;
import com.example.qrscanner.models.QRCode;

import java.util.List;

public class ListScannedViewModel extends AndroidViewModel {
    private QRCodeRepository repository;
    private MutableLiveData<List<QRCode>> sessionCodes = new MutableLiveData<>();

    public ListScannedViewModel(Application application) {
        super(application);
        repository = new QRCodeRepository(application);
        loadSessionCodes();
    }

    public MutableLiveData<List<QRCode>> getSessionCodes() {
        return sessionCodes;
    }

    public void loadSessionCodes() {
        sessionCodes.setValue(repository.getSessionCodes());
    }

    public void deleteCode(int position) {
        repository.removeSessionCode(position);
        loadSessionCodes();
    }

    public void clearAllCodes() {
        repository.clearSessionCodes();
        loadSessionCodes();
    }

    public List<QRCode> getCodes() {
        return repository.getSessionCodes();
    }
}



