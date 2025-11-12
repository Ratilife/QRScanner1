package com.example.qrscanner.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.qrscanner.data.local.entities.QRCodeEntity;
import com.example.qrscanner.data.repositories.QRCodeRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private QRCodeRepository repository;
    private LiveData<List<QRCodeEntity>> allCodes;

    public MainViewModel(Application application) {
        super(application);
        repository = new QRCodeRepository(application);
        allCodes = repository.getAllCodes();
    }

    public LiveData<List<QRCodeEntity>> getAllCodes() {
        return allCodes;
    }
}



