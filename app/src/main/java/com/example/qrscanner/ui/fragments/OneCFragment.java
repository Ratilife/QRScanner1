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
import com.example.qrscanner.data.remote.OneCExportService;
import com.example.qrscanner.models.QRCode;
import com.example.qrscanner.ui.viewmodels.SendViewModel;

import java.util.List;

public class OneCFragment extends Fragment {
    private SendViewModel viewModel;
    private TextView tvExportInfo;
    private Button btnExport1C;
    private OneCExportService oneCExportService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_one_c, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SendViewModel.class);
        oneCExportService = new OneCExportService(requireContext());

        tvExportInfo = view.findViewById(R.id.tvExportInfo);
        btnExport1C = view.findViewById(R.id.btnExport1C);

        List<QRCode> codes = viewModel.getSessionCodes();
        if (codes != null && !codes.isEmpty()) {
            tvExportInfo.setText("Готово к экспорту: " + codes.size() + " элементов");
        } else {
            tvExportInfo.setText("Нет данных для экспорта");
            btnExport1C.setEnabled(false);
        }

        btnExport1C.setOnClickListener(v -> {
            List<QRCode> codesToExport = viewModel.getSessionCodes();
            if (codesToExport != null && !codesToExport.isEmpty()) {
                boolean success = oneCExportService.exportTo1C(codesToExport);
                if (success) {
                    Toast.makeText(getContext(), "Экспорт выполнен успешно", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Нет данных для экспорта", Toast.LENGTH_SHORT).show();
            }
        });
    }
}



