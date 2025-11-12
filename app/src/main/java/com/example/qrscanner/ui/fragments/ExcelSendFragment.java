package com.example.qrscanner.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.qrscanner.R;
import com.example.qrscanner.data.remote.ExcelExportService;
import com.example.qrscanner.models.QRCode;
import com.example.qrscanner.ui.viewmodels.SendViewModel;

import java.util.List;

public class ExcelSendFragment extends Fragment {
    private SendViewModel viewModel;
    private TextView tvExportInfo;
    private Button btnExportExcel;
    private ExcelExportService excelExportService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_excel_send, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SendViewModel.class);
        excelExportService = new ExcelExportService(requireContext());

        tvExportInfo = view.findViewById(R.id.tvExportInfo);
        btnExportExcel = view.findViewById(R.id.btnExportExcel);

        List<QRCode> codes = viewModel.getSessionCodes();
        if (codes != null && !codes.isEmpty()) {
            tvExportInfo.setText("Готово к экспорту: " + codes.size() + " элементов");
        } else {
            tvExportInfo.setText("Нет данных для экспорта");
            btnExportExcel.setEnabled(false);
        }

        btnExportExcel.setOnClickListener(v -> {
            List<QRCode> codesToExport = viewModel.getSessionCodes();
            if (codesToExport != null && !codesToExport.isEmpty()) {
                boolean success = excelExportService.exportToExcel(codesToExport);
                if (success) {
                    Toast.makeText(getContext(), "Экспорт выполнен успешно", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Нет данных для экспорта", Toast.LENGTH_SHORT).show();
            }
        });
    }
}



