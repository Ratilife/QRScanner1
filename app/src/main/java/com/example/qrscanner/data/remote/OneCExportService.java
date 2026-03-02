package com.example.qrscanner.data.remote;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.qrscanner.models.QRCode;
import com.example.qrscanner.utils.DebugLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OneCExportService {
    private static final String TAG = "OneCExportService";
    private Context context;

    public OneCExportService(Context context) {
        this.context = context;
    }

    public boolean exportTo1C(List<QRCode> qrCodes) {
        try {
            // Create JSON structure compatible with 1C
            List<Map<String, Object>> dataList = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

            for (QRCode qrCode : qrCodes) {
                Map<String, Object> item = new HashMap<>();
                item.put("Id", qrCode.getId());
                item.put("Value", qrCode.getValue());
                item.put("ScanDate", sdf.format(qrCode.getScanDate()));
                dataList.add(item);
            }

            Map<String, Object> root = new HashMap<>();
            root.put("DocumentType", "QRCodeScan");
            root.put("Data", dataList);
            root.put("ExportDate", sdf.format(new Date()));

            // Convert to JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(root);

            // Save file
            String fileName = "QR_Codes_1C_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".json";
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();

            DebugLogger.i(TAG, "1C file exported successfully: " + file.getAbsolutePath());
            Toast.makeText(context, "Файл сохранен: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            return true;
        } catch (IOException e) {
            DebugLogger.e(TAG, "Error exporting to 1C", e);
            Toast.makeText(context, "Ошибка при экспорте в 1С", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}




