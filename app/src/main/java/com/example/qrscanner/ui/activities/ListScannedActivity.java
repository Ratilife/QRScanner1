package com.example.qrscanner.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrscanner.R;
import com.example.qrscanner.models.QRCode;
import com.example.qrscanner.ui.adapters.ScannedCodesAdapter;
import com.example.qrscanner.ui.viewmodels.ListScannedViewModel;

import java.util.List;

public class ListScannedActivity extends AppCompatActivity {
    private ListScannedViewModel viewModel;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private Button btnRescan, btnBackToList;
    private ScannedCodesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_scanned);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(ListScannedViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewScanned);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnRescan = findViewById(R.id.btnRescan);
        btnBackToList = findViewById(R.id.btnBackToList);

        adapter = new ScannedCodesAdapter(this, position -> {
            viewModel.deleteCode(position);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnRescan.setOnClickListener(v -> {
            Intent intent = new Intent(ListScannedActivity.this, ScanActivity.class);
            startActivity(intent);
        });

        btnBackToList.setOnClickListener(v -> finish());

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getSessionCodes().observe(this, codes -> {
            if (codes != null && !codes.isEmpty()) {
                adapter.setCodes(codes);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadSessionCodes();
    }
}

