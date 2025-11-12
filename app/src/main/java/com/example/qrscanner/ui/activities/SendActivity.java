package com.example.qrscanner.ui.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.qrscanner.R;
import com.example.qrscanner.data.remote.ExcelExportService;
import com.example.qrscanner.data.remote.OneCExportService;
import com.example.qrscanner.models.QRCode;
import com.example.qrscanner.ui.fragments.ExcelSendFragment;
import com.example.qrscanner.ui.fragments.OneCFragment;
import com.example.qrscanner.ui.viewmodels.SendViewModel;

import java.util.List;

public class SendActivity extends AppCompatActivity {
    private SendViewModel viewModel;
    private Button btnExcel, btnOneC;
    private ExcelExportService excelExportService;
    private OneCExportService oneCExportService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(SendViewModel.class);
        excelExportService = new ExcelExportService(this);
        oneCExportService = new OneCExportService(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        btnExcel = findViewById(R.id.btnExcel);
        btnOneC = findViewById(R.id.btnOneC);

        btnExcel.setOnClickListener(v -> {
            List<QRCode> codes = viewModel.getSessionCodes();
            if (codes != null && !codes.isEmpty()) {
                excelExportService.exportToExcel(codes);
            } else {
                // Show fragment for more options
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new ExcelSendFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        btnOneC.setOnClickListener(v -> {
            List<QRCode> codes = viewModel.getSessionCodes();
            if (codes != null && !codes.isEmpty()) {
                oneCExportService.exportTo1C(codes);
            } else {
                // Show fragment for more options
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new OneCFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }
}

