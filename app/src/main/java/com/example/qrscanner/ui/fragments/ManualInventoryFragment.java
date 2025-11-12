package com.example.qrscanner.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.qrscanner.R;
import com.example.qrscanner.ui.viewmodels.InventoryViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class ManualInventoryFragment extends Fragment {
    private InventoryViewModel viewModel;
    private TextInputEditText etItemName, etItemCode, etItemQuantity;
    private Button btnAddItem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manual_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);

        etItemName = view.findViewById(R.id.etItemName);
        etItemCode = view.findViewById(R.id.etItemCode);
        etItemQuantity = view.findViewById(R.id.etItemQuantity);
        btnAddItem = view.findViewById(R.id.btnAddItem);

        btnAddItem.setOnClickListener(v -> {
            String name = etItemName.getText() != null ? etItemName.getText().toString() : "";
            String code = etItemCode.getText() != null ? etItemCode.getText().toString() : "";
            String quantity = etItemQuantity.getText() != null ? etItemQuantity.getText().toString() : "";

            if (name.isEmpty() || code.isEmpty()) {
                Toast.makeText(getContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> item = new HashMap<>();
            item.put("name", name);
            item.put("code", code);
            item.put("quantity", quantity);

            viewModel.addManualInventoryItem(item);
            Toast.makeText(getContext(), "Элемент добавлен", Toast.LENGTH_SHORT).show();

            // Clear fields
            etItemName.setText("");
            etItemCode.setText("");
            etItemQuantity.setText("");
        });
    }
}


