package com.example.a20210207_checkmate2;

import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;

public class LowLineChartAvgClass {
    LineChart mAChart;
    XAxis xAxis;
    YAxis left;
    YAxis right;

    MainActivity mainActivity;
    ArrayList<Hba1cEntry> hba1c_data;
    ArrayList<Hba1cEntry> hba1cAverageData;
    ArrayList<ILineDataSet> dataSets;

    int xAxisTextColor;
    int bubbleColorInRange;
    int bubbleColorAboveRange;
    int bubbleColorHighAbRange;
    int textColor;

    LowLineChartAvgClass(MainActivity mainActivity, ArrayList<Hba1cEntry> hba1c_data, ArrayList<Hba1cEntry> hba1cAverageData) {
        mAChart = (LineChart) mainActivity.findViewById(R.id.LowLineChartAvg);
        this.mainActivity = mainActivity;

        xAxis = mAChart.getXAxis();
        left = mAChart.getAxisLeft();
        right = mAChart.getAxisRight();

        this.hba1c_data = hba1c_data;
        this.hba1cAverageData = hba1cAverageData;

        dataSets = new ArrayList<>();

        xAxisTextColor = mainActivity.getResources().getColor(R.color.white);
        bubbleColorInRange = MaterialColors.getColor(mainActivity, R.attr.colorInRange, Color.BLACK);
        bubbleColorAboveRange = MaterialColors.getColor(mainActivity, R.attr.colorOutOfRange, Color.BLACK);
        bubbleColorHighAbRange = MaterialColors.getColor(mainActivity, R.attr.colorVeryOutOfRange, Color.BLACK);
        textColor = MaterialColors.getColor(mainActivity, R.attr.colorTextBubbleBar, Color.BLACK);
    }

    public void createChart(boolean selected) {
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceMax(0.8f);
        xAxis.setSpaceMin(0.8f);
        xAxis.setTextSize(15);
        xAxis.setTextColor(xAxisTextColor);

        left.setDrawLabels(false);
        left.setDrawAxisLine(false);
        left.setDrawGridLines(false);

        right.setDrawLabels(false);
        right.setDrawAxisLine(false);
        right.setDrawGridLines(false);

        ArrayList<Entry> dataLineAvg = new ArrayList<>();
        ArrayList<Entry> dataTextAvg = new ArrayList<>();
        ArrayList<Entry> dataBubbleSel = new ArrayList<>();

        Integer colorAvg;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        Boolean switchToMol = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_GLUCOSE_MOL, false);

        double minAvgValue = hba1cAverageData.get(0).minGlucose;
        if (switchToMol) {
            minAvgValue = minAvgValue / 18.0182;
        }

        // Farblogik für Durchschnitt: < 50 Rot, 50-70 Gelb, > 70 Grün
        if (hba1cAverageData.get(0).minGlucose < 50) {
            colorAvg = bubbleColorHighAbRange;
        } else if (hba1cAverageData.get(0).minGlucose <= 70) {
            colorAvg = bubbleColorAboveRange;
        } else {
            colorAvg = bubbleColorInRange;
        }

        ArrayList<String> xAxisLabel = new ArrayList<>();
        xAxisLabel.add(sharedPref.getString(SettingsActivity.KEY_PREF_HBA1C_DAYS, "90") + " Day\nAvg");
        
        // Low Events für den Renderer mitgeben
        Entry avgEntry = new Entry(0, (float) minAvgValue, hba1cAverageData.get(0).lowEvents);
        dataLineAvg.add(avgEntry);
        dataTextAvg.add(avgEntry);

        int colorBubbleSel = 0;
        if (selected) {
            colorBubbleSel = colorAvg;
            dataBubbleSel.add(new Entry(0, (float) minAvgValue, hba1cAverageData.get(0).lowEvents));
            colorAvg = Color.WHITE;
        }

        mAChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));
        mAChart.setXAxisRenderer(new ValueFormatterDateXAxis(mAChart.getViewPortHandler(), mAChart.getXAxis(), mAChart.getTransformer(YAxis.AxisDependency.LEFT)));

        LineDataSet dataLineAvgSet = new LineDataSet(dataLineAvg, "LineAvg");
        LineDataSet dataTextAvgSet = new LineDataSet(dataTextAvg, "TextAvg");
        LineDataSet dataBubbleSelSet = new LineDataSet(dataBubbleSel, "BubbleSel");

        dataTextAvgSet.setDrawCircles(false);
        dataTextAvgSet.setValueTextSize(20);
        ValueFormatter formatter = switchToMol ? new ValueFormatterOneDecimal() : new ValueFormatterZeroDecimal();
        dataTextAvgSet.setValueFormatter(formatter);
        dataTextAvgSet.enableDashedLine(0f, 1f, 0f);
        dataTextAvgSet.setValueTextColor(textColor);
        dataTextAvgSet.setValueTextColors(java.util.Collections.singletonList(colorAvg));

        dataLineAvgSet.setDrawCircles(false); // Kreise deaktivieren
        dataLineAvgSet.setLineWidth(5);
        dataLineAvgSet.setDrawValues(false);
        dataLineAvgSet.enableDashedLine(0f, 1f, 0f);
        dataLineAvgSet.setDrawHighlightIndicators(false);

        dataBubbleSelSet.setDrawCircles(false); // Kreise deaktivieren
        dataBubbleSelSet.setDrawValues(true);
        dataBubbleSelSet.setValueTextSize(20);
        dataBubbleSelSet.setValueTextColor(Color.BLACK);
        dataBubbleSelSet.setValueTextColors(java.util.Collections.singletonList(colorBubbleSel));
        dataBubbleSelSet.setValueFormatter(formatter);

        dataSets = new ArrayList<>();
        dataSets.add(dataLineAvgSet);
        dataSets.add(dataTextAvgSet);
        dataSets.add(dataBubbleSelSet);

        LineData data = new LineData(dataSets);
        // Rechteckigen Renderer setzen
        mAChart.setRenderer(new RectangleTextLineChartRenderer(mAChart, mAChart.getAnimator(), mAChart.getViewPortHandler()));
        mAChart.setData(data);
        mAChart.getDescription().setEnabled(false);
        mAChart.getLegend().setEnabled(false);
        mAChart.setScaleEnabled(false);
        mAChart.setExtraOffsets(0, 30, 0, 0);
        mAChart.invalidate();
    }
}
