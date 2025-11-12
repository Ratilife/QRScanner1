package com.example.qrscanner.data.repositories;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.qrscanner.data.local.QRCodeDatabase;
import com.example.qrscanner.data.local.QRCodeDao;
import com.example.qrscanner.data.local.entities.QRCodeEntity;
import com.example.qrscanner.models.QRCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QRCodeRepository {
    private QRCodeDao qrCodeDao;
    private LiveData<List<QRCodeEntity>> allCodes;
    private List<QRCode> sessionCodes = new ArrayList<>();

    public QRCodeRepository(Application application) {
        QRCodeDatabase database = QRCodeDatabase.getInstance(application);
        qrCodeDao = database.qrCodeDao();
        allCodes = qrCodeDao.getAllCodes();
    }

    public LiveData<List<QRCodeEntity>> getAllCodes() {
        return allCodes;
    }

    public void insert(QRCode qrCode) {
        QRCodeEntity entity = new QRCodeEntity(qrCode.getValue(), qrCode.getScanDate());
        new InsertQRCodeAsyncTask(qrCodeDao).execute(entity);
    }

    public void delete(QRCodeEntity qrCode) {
        new DeleteQRCodeAsyncTask(qrCodeDao).execute(qrCode);
    }

    public void deleteById(long id) {
        new DeleteByIdAsyncTask(qrCodeDao).execute(id);
    }

    // Session management
    public void addSessionCode(QRCode qrCode) {
        sessionCodes.add(qrCode);
    }

    public List<QRCode> getSessionCodes() {
        return sessionCodes;
    }

    public void clearSessionCodes() {
        sessionCodes.clear();
    }

    public void removeSessionCode(int position) {
        if (position >= 0 && position < sessionCodes.size()) {
            sessionCodes.remove(position);
        }
    }

    private static class InsertQRCodeAsyncTask extends AsyncTask<QRCodeEntity, Void, Void> {
        private QRCodeDao qrCodeDao;

        private InsertQRCodeAsyncTask(QRCodeDao qrCodeDao) {
            this.qrCodeDao = qrCodeDao;
        }

        @Override
        protected Void doInBackground(QRCodeEntity... qrCodeEntities) {
            qrCodeDao.insert(qrCodeEntities[0]);
            return null;
        }
    }

    private static class DeleteQRCodeAsyncTask extends AsyncTask<QRCodeEntity, Void, Void> {
        private QRCodeDao qrCodeDao;

        private DeleteQRCodeAsyncTask(QRCodeDao qrCodeDao) {
            this.qrCodeDao = qrCodeDao;
        }

        @Override
        protected Void doInBackground(QRCodeEntity... qrCodeEntities) {
            qrCodeDao.delete(qrCodeEntities[0]);
            return null;
        }
    }

    private static class DeleteByIdAsyncTask extends AsyncTask<Long, Void, Void> {
        private QRCodeDao qrCodeDao;

        private DeleteByIdAsyncTask(QRCodeDao qrCodeDao) {
            this.qrCodeDao = qrCodeDao;
        }

        @Override
        protected Void doInBackground(Long... ids) {
            qrCodeDao.deleteById(ids[0]);
            return null;
        }
    }
}



