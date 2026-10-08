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
    ArrayList<Integer> colors;
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

        YAxis left = mChart.getAxisLeft();
        left.setDrawLabels(false);
        left.setDrawAxisLine(false);
        left.setDrawGridLines(false);

        YAxis right = mChart.getAxisRight();
        right.setDrawLabels(false);
        right.setDrawAxisLine(false);
        right.setDrawGridLines(false);

        ArrayList<Entry> dataLine = new ArrayList<>();
        ArrayList<Entry> dataText = new ArrayList<>();
        ArrayList<Entry> dataBubbleSel = new ArrayList<>();

        final ArrayList<String> xAxisLabel = new ArrayList<>();
        int count = hba1c_data.size() - 1;

        colors = new ArrayList<Integer>();
        int colorBubbleSel = 0;

        for (int i = 0; i <= count; i++) {
            Hba1cEntry entry = hba1c_data.get(count - i);
            float minGlucose = (float) entry.minGlucose;
            if (switchToMol) {
                minGlucose = (float) (minGlucose / 18.0182);
            }

            SimpleDateFormat df = new SimpleDateFormat("E\ndd/MM", Locale.getDefault());
            xAxisLabel.add(df.format(entry.date));

            Entry e = new Entry((float) i, minGlucose, entry.lowEvents);
            
            if (count - daysGoingBackCount < i)
                dataLine.add(new Entry((float) i, minGlucose));

            dataText.add(e);

            // Farblogik: < 50 Rot, 50-70 Gelb, > 70 Grün
            if (entry.minGlucose < 50) {
                colors.add(bubbleColorHighAbRange);
            } else if (entry.minGlucose <= 70) {
                colors.add(bubbleColorAboveRange);
            } else {
                colors.add(bubbleColorInRange);
            }

            if (useDaySelected && i == daySelected) {
                colorBubbleSel = colors.get(i);
                dataBubbleSel.add(new Entry((float) i, minGlucose, entry.lowEvents));
                colors.set(i, Color.WHITE);
            }
        }

        LineDataSet dataLineSet = new LineDataSet(dataLine, "Line");
        LineDataSet dataTextSet = new LineDataSet(dataText, "Text");
        LineDataSet dataBubbleSelSet = new LineDataSet(dataBubbleSel, "BubbleSel");

        mChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));
        mChart.setXAxisRenderer(new ValueFormatterDateXAxis(mChart.getViewPortHandler(), mChart.getXAxis(), mChart.getTransformer(YAxis.AxisDependency.LEFT)));

        dataLineSet.setLineWidth(5);
        dataLineSet.setColor(mainActivity.getResources().getColor(R.color.line));
        dataLineSet.setDrawValues(false);
        dataLineSet.setHighlightEnabled(false);

        dataTextSet.setDrawCircles(false);
        dataTextSet.setValueTextSize(18);
        dataTextSet.setValueTextColors(colors);
        
        ValueFormatter formatter = switchToMol ? new ValueFormatterOneDecimal() : new ValueFormatterZeroDecimal();
        dataTextSet.setValueFormatter(formatter);
        dataTextSet.setHighlightEnabled(true);
        dataTextSet.setDrawHighlightIndicators(false);

        dataBubbleSelSet.setDrawCircles(false);
        dataBubbleSelSet.setDrawValues(true);
        dataBubbleSelSet.setValueTextSize(18);
        dataBubbleSelSet.setValueTextColor(Color.BLACK);
        dataBubbleSelSet.setValueTextColors(java.util.Collections.singletonList(colorBubbleSel));
        dataBubbleSelSet.setValueFormatter(formatter);

        dataSets = new ArrayList<>();
        dataSets.add(dataLineSet);
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
