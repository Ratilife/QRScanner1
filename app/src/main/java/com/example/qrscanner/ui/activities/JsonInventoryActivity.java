package com.example.qrscanner.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.qrscanner.R;
import com.example.qrscanner.ui.viewmodels.InventoryViewModel;
import com.example.qrscanner.utils.DebugLogger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonInventoryActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_JSON = 101;
    private static final String TAG = "JsonInventoryActivity";

    private InventoryViewModel viewModel;
    private Button btnSelectJson;
    private TextView tvJsonContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_inventory);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(InventoryViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        btnSelectJson = findViewById(R.id.btnSelectJson);
        tvJsonContent = findViewById(R.id.tvJsonContent);

        btnSelectJson.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/json");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Выберите JSON файл"), REQUEST_CODE_PICK_JSON);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getInventoryData().observe(this, data -> {
            if (data != null && !data.isEmpty()) {
                tvJsonContent.setText("Загружено элементов: " + data.size());
                tvJsonContent.setVisibility(android.view.View.VISIBLE);
                Toast.makeText(this, "JSON файл успешно загружен", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_JSON && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        reader.close();
                        inputStream.close();

                        String jsonContent = stringBuilder.toString();
                        viewModel.loadInventoryFromJson(jsonContent);
                        DebugLogger.d(TAG, "JSON file loaded successfully");
                    }
                } catch (Exception e) {
                    DebugLogger.e(TAG, "Error reading JSON file", e);
                    Toast.makeText(this, "Ошибка при чтении файла", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

