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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class LowLineChartClass {
    LineChart mChart;

    MainActivity mainActivity;
    ArrayList<Hba1cEntry> hba1c_data;
    ArrayList<ILineDataSet> dataSets;
    int daysGoingBackCount;

    int xAxisTextColor;
    int bubbleColorInRange;
    int bubbleColorAboveRange;
    int bubbleColorHighAbRange;
    int textColor;

    LowLineChartClass(MainActivity mainActivity, ArrayList<Hba1cEntry> hba1c_data, int daysGoingBackCount) {
        mChart = (LineChart) mainActivity.findViewById(R.id.LowLineChart);
        this.mainActivity = mainActivity;
        this.hba1c_data = hba1c_data;
        this.daysGoingBackCount = daysGoingBackCount;

        xAxisTextColor = mainActivity.getResources().getColor(R.color.line);
        bubbleColorInRange = MaterialColors.getColor(mainActivity, R.attr.colorInRange, Color.BLACK);
        bubbleColorAboveRange = MaterialColors.getColor(mainActivity, R.attr.colorOutOfRange, Color.BLACK);
        bubbleColorHighAbRange = MaterialColors.getColor(mainActivity, R.attr.colorVeryOutOfRange, Color.BLACK);
        textColor = MaterialColors.getColor(mainActivity, R.attr.colorTextBubbleBar, Color.BLACK);
    }

    public void createChart(int daySelected, boolean useDaySelected) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        Boolean switchToMol = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_GLUCOSE_MOL, false);
        
        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceMax(0.8f);
        xAxis.setSpaceMin(0.8f);
        xAxis.setTextSize(15);
        xAxis.setTextColor(xAxisTextColor);

        mChart.getAxisLeft().setDrawLabels(false);
        mChart.getAxisLeft().setDrawAxisLine(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getAxisRight().setDrawLabels(false);
        mChart.getAxisRight().setDrawAxisLine(false);
        mChart.getAxisRight().setDrawGridLines(false);

        ArrayList<Entry> dataText = new ArrayList<>();
        ArrayList<Entry> dataBubbleSel = new ArrayList<>();

        final ArrayList<String> xAxisLabel = new ArrayList<>();
        int count = hba1c_data.size() - 1;

        for (int i = 0; i <= count; i++) {
            Hba1cEntry entry = hba1c_data.get(count - i);
            float minGlucose = (float) entry.minGlucose;
            if (switchToMol) {
                minGlucose = (float) (minGlucose / 18.0182);
            }

            SimpleDateFormat df = new SimpleDateFormat("E\ndd/MM", Locale.getDefault());
            xAxisLabel.add(df.format(entry.date));

            // Farblogik fixieren: < 50 Rot, 50-70 Gelb, > 70 Grün
            int color;
            if (entry.minGlucose < 50) {
                color = bubbleColorHighAbRange; // Rot
            } else if (entry.minGlucose <= 70) {
                color = bubbleColorAboveRange;  // Gelb
            } else {
                color = bubbleColorInRange;     // Grün
            }

            int[] entryData = new int[]{entry.lowEvents, color};
            Entry e = new Entry((float) i, minGlucose, entryData);
            dataText.add(e);

            if (useDaySelected && i == daySelected) {
                dataBubbleSel.add(new Entry((float) i, minGlucose, entryData));
            }
        }

        LineDataSet dataTextSet = new LineDataSet(dataText, "Text");
        LineDataSet dataBubbleSelSet = new LineDataSet(dataBubbleSel, "BubbleSel");

        mChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));
        mChart.setXAxisRenderer(new ValueFormatterDateXAxis(mChart.getViewPortHandler(), mChart.getXAxis(), mChart.getTransformer(YAxis.AxisDependency.LEFT)));

        ValueFormatter formatter = switchToMol ? new ValueFormatterOneDecimal() : new ValueFormatterZeroDecimal();

        // Linien komplett ausschalten
        dataTextSet.setDrawCircles(false);
        dataTextSet.setLineWidth(0f);
        dataTextSet.setColor(Color.TRANSPARENT);
        dataTextSet.setDrawValues(true); 
        dataTextSet.setValueTextSize(20f); 
        dataTextSet.setHighlightEnabled(true);
        dataTextSet.setDrawHighlightIndicators(false);
        dataTextSet.setValueFormatter(formatter);
        dataTextSet.setValueTextColor(textColor);

        dataBubbleSelSet.setDrawCircles(false);
        dataBubbleSelSet.setLineWidth(0f);
        dataBubbleSelSet.setColor(Color.TRANSPARENT);
        dataBubbleSelSet.setDrawValues(true);
        dataBubbleSelSet.setValueTextSize(20f);
        dataBubbleSelSet.setValueFormatter(formatter);
        dataBubbleSelSet.setValueTextColor(textColor);

        dataSets = new ArrayList<>();
        dataSets.add(dataTextSet);
        dataSets.add(dataBubbleSelSet);

        LineData data = new LineData(dataSets);
        mChart.setData(data);
        mChart.setRenderer(new RectangleTextLineChartRenderer(mChart, mChart.getAnimator(), mChart.getViewPortHandler()));

        float numberOfVisibleBubbleBars = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_SCREEN_VISIBLE_BUBBLEBARS, "6"));
        mChart.setVisibleXRangeMaximum(numberOfVisibleBubbleBars);

        mChart.getDescription().setEnabled(false);
        mChart.getLegend().setEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setExtraOffsets(0, 30, 0, 0);
        mChart.invalidate();
    }
}
