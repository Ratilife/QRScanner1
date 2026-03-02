package com.example.qrscanner.data.remote;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.qrscanner.models.QRCode;
import com.example.qrscanner.utils.DebugLogger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExcelExportService {
    private static final String TAG = "ExcelExportService";
    private Context context;

    public ExcelExportService(Context context) {
        this.context = context;
    }

    public boolean exportToExcel(List<QRCode> qrCodes) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("QR Codes");

            // Header row
            Row headerRow = sheet.createRow(0);
            Cell headerCell1 = headerRow.createCell(0);
            headerCell1.setCellValue("ID");
            Cell headerCell2 = headerRow.createCell(1);
            headerCell2.setCellValue("QR Code Value");
            Cell headerCell3 = headerRow.createCell(2);
            headerCell3.setCellValue("Scan Date");

            // Data rows
            int rowNum = 1;
            for (QRCode qrCode : qrCodes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(qrCode.getId());
                row.createCell(1).setCellValue(qrCode.getValue());
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                row.createCell(2).setCellValue(sdf.format(qrCode.getScanDate()));
            }

            // Auto-size columns
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save file
            String fileName = "QR_Codes_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".xlsx";
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            DebugLogger.i(TAG, "Excel file exported successfully: " + file.getAbsolutePath());
            Toast.makeText(context, "Файл сохранен: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            return true;
        } catch (IOException e) {
            DebugLogger.e(TAG, "Error exporting to Excel", e);
            Toast.makeText(context, "Ошибка при экспорте в Excel", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}




