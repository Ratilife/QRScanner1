package com.example.qrscanner.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.qrscanner.R;
import com.example.qrscanner.ui.viewmodels.MainViewModel;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private Button btnRead, btnInventory, btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(MainViewModel.class);

        btnRead = findViewById(R.id.btnRead);
        btnInventory = findViewById(R.id.btnInventory);
        btnSend = findViewById(R.id.btnSend);

        btnRead.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScanActivity.class);
            startActivity(intent);
        });

        btnInventory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
            startActivity(intent);
        });

        btnSend.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SendActivity.class);
            startActivity(intent);
        });
    }
}

