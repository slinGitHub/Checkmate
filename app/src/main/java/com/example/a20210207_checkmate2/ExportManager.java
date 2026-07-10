package com.example.a20210207_checkmate2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.FileProvider;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExportManager {

    // GEÄNDERT: List<Hba1cEntry> statt List<CalcHba1c.Hba1cEntry>
    public static String exportHba1cToCSV(Context context, List<Hba1cEntry> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }

        String fileName = "CheckMate_Export_" + System.currentTimeMillis() + ".csv";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);

        try (CSVWriter writer = new CSVWriter(
                new FileWriter(file),
                ';', // Trennzeichen auf Semikolon setzen
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {

            // Header (Excel erkennt das Semikolon nun als Spaltentrennung)
            String[] header = {
                    "Date", "HbA1c_Percent", "TimeInRange_Percent",
                    "TimeHigh_Percent", "TimeLow_Percent", "ReadingsCount"
            };
            writer.writeNext(header);

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);

            for (Hba1cEntry entry : dataList) {
                String[] row = {
                        sdf.format(entry.date),
                        // Locale.GERMANY erzwingt das Komma als Dezimaltrennzeichen
                        String.format(Locale.GERMANY, "%.2f", entry.hba1c),
                        String.format(Locale.GERMANY, "%.1f", entry.inRange * 100),
                        String.format(Locale.GERMANY, "%.1f", entry.HighRange * 100),
                        String.format(Locale.GERMANY, "%.1f", Math.abs(entry.LowRange) * 100),
                        String.valueOf((int)entry.nValues)
                };
                writer.writeNext(row);
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void shareFile(Context context, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return;

        Uri uri = FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Export Data to Excel"));
    }
}