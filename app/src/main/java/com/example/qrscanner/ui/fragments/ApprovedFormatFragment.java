package com.example.qrscanner.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrscanner.R;

import java.util.ArrayList;
import java.util.List;

public class ApprovedFormatFragment extends Fragment {
    private RecyclerView recyclerViewFormats;
    private List<String> formats = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_approved_format, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewFormats = view.findViewById(R.id.recyclerViewFormats);

        // Initialize with default formats
        formats.add("Формат 1: Название, Код, Количество");
        formats.add("Формат 2: Артикул, Наименование, Цена");
        formats.add("Формат 3: Штрих-код, Описание, Единица измерения");

        // Simple adapter for formats
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                formats
        );

        recyclerViewFormats.setLayoutManager(new LinearLayoutManager(getContext()));
        // For simplicity, using a simple list view approach
        // In production, you would create a proper RecyclerView adapter

        Toast.makeText(getContext(), "Выберите формат инвентаризации", Toast.LENGTH_SHORT).show();
    }
}



