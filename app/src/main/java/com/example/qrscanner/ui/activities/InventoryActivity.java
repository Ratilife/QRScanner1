package com.example.qrscanner.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.qrscanner.R;
import com.example.qrscanner.ui.fragments.ApprovedFormatFragment;
import com.example.qrscanner.ui.fragments.ManualInventoryFragment;
import com.example.qrscanner.ui.viewmodels.InventoryViewModel;

public class InventoryActivity extends AppCompatActivity {
    private InventoryViewModel viewModel;
    private Button btnBasedOnJson, btnCreateManually, btnApprovedFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(InventoryViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        btnBasedOnJson = findViewById(R.id.btnBasedOnJson);
        btnCreateManually = findViewById(R.id.btnCreateManually);
        btnApprovedFormat = findViewById(R.id.btnApprovedFormat);

        btnBasedOnJson.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, JsonInventoryActivity.class);
            startActivity(intent);
        });

        btnCreateManually.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new ManualInventoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnApprovedFormat.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new ApprovedFormatFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}

