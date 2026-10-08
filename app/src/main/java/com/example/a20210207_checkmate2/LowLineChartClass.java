package com.example.a20210207_checkmate2;

import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.core.content.ContextCompat;
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
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.color.MaterialColors;

import java.math.BigDecimal;
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
    int bubbleColorSelected;
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
        bubbleColorSelected = MaterialColors.getColor(mainActivity, R.attr.colorDayLine, Color.BLACK);
        bubbleColorInRange = MaterialColors.getColor(mainActivity, R.attr.colorInRange, Color.BLACK);
        bubbleColorAboveRange = MaterialColors.getColor(mainActivity, R.attr.colorOutOfRange, Color.BLACK);
        bubbleColorHighAbRange = MaterialColors.getColor(mainActivity, R.attr.colorVeryOutOfRange, Color.BLACK);
        textColor = MaterialColors.getColor(mainActivity, R.attr.colorTextBubbleBar, Color.BLACK);
    }

    public void createChart(int daySelected, boolean useDaySelected) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        Boolean switchToMol = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_GLUCOSE_MOL, false);
        double lowerRange = Double.parseDouble(sharedPref.getString(SettingsActivity.KEY_PREF_LOWER_RANGE, "70"));
        
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
        ArrayList<Entry> dataBubble = new ArrayList<>();
        ArrayList<Entry> dataBubbleSel = new ArrayList<>();
        ArrayList<Entry> dataText = new ArrayList<>();

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

            dataBubble.add(new Entry((float) i, minGlucose));
            if (count - daysGoingBackCount < i)
                dataLine.add(new Entry((float) i, minGlucose));

            dataText.add(new Entry((float) i, minGlucose));

            if (entry.minGlucose < 55) {
                colors.add(bubbleColorHighAbRange);
            } else if (entry.minGlucose < lowerRange) {
                colors.add(bubbleColorAboveRange);
            } else {
                colors.add(bubbleColorInRange);
            }

            if (useDaySelected && i == daySelected) {
                colorBubbleSel = colors.get(i);
                dataBubbleSel.add(new Entry((float) i, minGlucose));
                colors.set(i, Color.WHITE);
            }
        }

        LineDataSet dataLineSet = new LineDataSet(dataLine, "Line");
        LineDataSet dataBubbleSet = new LineDataSet(dataBubble, "Bubble");
        LineDataSet dataTextSet = new LineDataSet(dataText, "Text");
        LineDataSet dataBubbleSelSet = new LineDataSet(dataBubbleSel, "BubbleSel");

        mChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));
        mChart.setXAxisRenderer(new ValueFormatterDateXAxis(mChart.getViewPortHandler(), mChart.getXAxis(), mChart.getTransformer(YAxis.AxisDependency.LEFT)));

        dataLineSet.setLineWidth(5);
        dataLineSet.setColor(mainActivity.getResources().getColor(R.color.line));
        dataLineSet.setDrawValues(false);
        dataLineSet.setHighlightEnabled(false);

        dataTextSet.setDrawCircles(false);
        dataTextSet.setValueTextSize(20);
        dataTextSet.setValueTextColor(textColor);
        dataTextSet.enableDashedLine(0f, 1f, 0f);
        
        ValueFormatter formatter = switchToMol ? new ValueFormatterOneDecimal() : new ValueFormatterZeroDecimal();
        dataTextSet.setValueFormatter(formatter);
        dataTextSet.setHighlightEnabled(false);

        dataBubbleSet.setCircleColors(colors);
        dataBubbleSet.setDrawValues(false);
        dataBubbleSet.setCircleRadius(20);
        dataBubbleSet.enableDashedLine(0f,1f,0f);
        dataBubbleSet.setDrawCircleHole(false);
        dataBubbleSet.setHighlightEnabled(true);
        dataBubbleSet.setDrawHighlightIndicators(false);

        dataBubbleSelSet.setFillAlpha(0);
        dataBubbleSelSet.setCircleColor(colorBubbleSel);
        dataBubbleSelSet.setCircleRadius(17);
        dataBubbleSelSet.setDrawValues(false);
        dataBubbleSelSet.setDrawCircleHole(false);

        dataSets = new ArrayList<>();
        dataSets.add(dataLineSet);
        dataSets.add(dataTextSet);
        dataSets.add(dataBubbleSet);
        dataSets.add(dataBubbleSelSet);

        LineData data = new LineData(dataSets);
        mChart.setData(data);
        mChart.setRenderer(new CenteredTextLineChartRenderer(mChart, mChart.getAnimator(), mChart.getViewPortHandler()));

        float numberOfVisibleBubbleBars = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_SCREEN_VISIBLE_BUBBLEBARS, "6"));
        mChart.setVisibleXRangeMaximum(numberOfVisibleBubbleBars);

        mChart.getDescription().setEnabled(false);
        mChart.getLegend().setEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setExtraOffsets(0, 10, 0, 0);
        mChart.invalidate();
    }
}
