package com.example.a20210207_checkmate2;

import android.content.res.Configuration;
import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.color.MaterialColors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

public class LineChartAvgClass {
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
    int bubbleNotEnoughDataColor;
    int textColor;

    //Constructor
    LineChartAvgClass(MainActivity mainActivity, ArrayList<Hba1cEntry> hba1c_data,ArrayList<Hba1cEntry> hba1cAverageData) {
        mAChart = (LineChart) mainActivity.findViewById(R.id.LineChartAvg);
        this.mainActivity = mainActivity;

        xAxis = mAChart.getXAxis();
        left = mAChart.getAxisLeft();
        right = mAChart.getAxisRight();

        this.hba1c_data = hba1c_data;
        this.hba1cAverageData = hba1cAverageData;

        dataSets = new ArrayList<>();

        //Initialize Colors
        xAxisTextColor = mainActivity.getResources().getColor(R.color.white);
        bubbleColorInRange = MaterialColors.getColor(mainActivity, R.attr.colorInRange, Color.BLACK);
        bubbleColorAboveRange = MaterialColors.getColor(mainActivity, R.attr.colorOutOfRange, Color.BLACK);
        bubbleColorHighAbRange = MaterialColors.getColor(mainActivity, R.attr.colorVeryOutOfRange, Color.BLACK);
        bubbleNotEnoughDataColor = MaterialColors.getColor(mainActivity, R.attr.colorNotEnoughData, Color.BLACK);
        textColor = MaterialColors.getColor(mainActivity, R.attr.colorTextBubbleBar, Color.BLACK);
    }

    public void createChart(boolean selected) {

        //Format Axes
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceMax(0.8f);
        xAxis.setSpaceMin(0.8f);
        xAxis.setTextSize(15); //Leave space on right side
        xAxis.setTextColor(xAxisTextColor);
        xAxis.setCenterAxisLabels(false);

        left.setDrawLabels(false); // no axis labels
        left.setDrawAxisLine(false); // no axis line
        left.setDrawGridLines(false); // no grid lines

        right.setDrawLabels(false); // no axis labels
        right.setDrawAxisLine(false); // no axis line
        right.setDrawGridLines(false); // no grid lines

        //-------------------------------------------------------------------------------
        //Add average Hba1c values and format line
        //-------------------------------------------------------------------------------
        ArrayList<Entry> dataLineAvg = new ArrayList<>();
        ArrayList<Entry> dataTextAvg = new ArrayList<>();
        ArrayList<Entry> dataBubbleSel = new ArrayList<>();

        Integer colorAvg;

        float hba1cAvgValue = BigDecimal.valueOf(hba1cAverageData.get(0).hba1c).setScale(1, BigDecimal.ROUND_HALF_DOWN).floatValue();
        if (hba1cAvgValue <= 6.3f) {
            colorAvg = bubbleColorInRange;
        } else if (hba1cAvgValue > 6.3f && hba1cAvgValue < 7.0f) {
            colorAvg = bubbleColorAboveRange;
        } else {
            colorAvg = bubbleColorHighAbRange;
        }

        ArrayList<String> xAxisLabel = new ArrayList<>();
        xAxisLabel.add("30 Day\nAvg");
        dataLineAvg.add(new Entry(0, hba1cAvgValue, hba1cAvgValue));
        dataTextAvg.add(new Entry(0, hba1cAvgValue, hba1cAvgValue));

        int colorBubbleSel = 0;
        if (selected == true) {
            colorBubbleSel = colorAvg;
            dataBubbleSel.add(new Entry(0, hba1cAvgValue, hba1cAvgValue));
            colorAvg = Color.WHITE;
        }

        //Set Custom Label
        mAChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));
        mAChart.setXAxisRenderer(new ValueFormatterDateXAxis(mAChart.getViewPortHandler(), mAChart.getXAxis(), mAChart.getTransformer(YAxis.AxisDependency.LEFT)));

        LineDataSet dataLineAvgSet = new LineDataSet(dataLineAvg, "LineAvg");
        LineDataSet dataTextAvgSet = new LineDataSet(dataTextAvg, "TextAvg");
        LineDataSet dataBubbleSelSet = new LineDataSet(dataBubbleSel, "BubbleSel");

        //Format Text Data
        dataTextAvgSet.setDrawCircles(false);
        dataTextAvgSet.setValueTextSize(20);
        dataTextAvgSet.setValueFormatter(new ValueFormatterOneDecimal());
        dataTextAvgSet.enableDashedLine(0f,1f,0f);
        dataTextAvgSet.setValueTextColor(textColor);

        dataLineAvgSet.setCircleColors(colorAvg);
        //dataLineAvgSet.setColor(mainActivity.getResources().getColor(R.color.blue));
        dataLineAvgSet.setLineWidth(5);
        dataLineAvgSet.setDrawValues(false);
        dataLineAvgSet.setValueTextColors(Collections.singletonList(mainActivity.getResources().getColor(R.color.white)));
        dataLineAvgSet.setCircleRadius(20);
        dataLineAvgSet.setDrawCircleHole(false);
        dataLineAvgSet.setCircleHoleRadius(22);
        dataLineAvgSet.setCircleHoleColor(colorAvg);
        //dataLineAvgSet.setValueFormatter(new ValueFormatterOneDecimal());
        dataLineAvgSet.enableDashedLine(0f,1f,0f);
        dataLineAvgSet.setDrawHighlightIndicators(false);

        //Format Bubble Data Selected
        dataBubbleSelSet.setFillAlpha(0);
        dataBubbleSelSet.setCircleColor(colorBubbleSel);
        dataBubbleSelSet.setCircleRadius(17);
        dataBubbleSelSet.setDrawValues(false);
        dataBubbleSelSet.setDrawCircleHole(false);

        dataSets.add(dataLineAvgSet);
        dataSets.add(dataTextAvgSet);
        dataSets.add(dataBubbleSelSet);

        //-------------------------------------------------------------------------------
        //Render Line Chart
        //-------------------------------------------------------------------------------
        LineData data = new LineData(dataSets);

        mAChart.setRenderer(new CenteredTextLineChartRenderer(mAChart,mAChart.getAnimator(),mAChart.getViewPortHandler()));

        mAChart.setData(data);
        mAChart.invalidate();
        //Set number of displayed values to 6
        //mAChart.setVisibleXRangeMaximum(6);
        xAxis.setGranularity(1f);
        //mAChart.moveViewToX(hba1c_data.size());
        mAChart.getDescription().setEnabled(false);
        mAChart.getLegend().setEnabled(false);
        mAChart.setScaleEnabled(false);
        mAChart.setExtraOffsets(0, 10, 0, 0); //Bottom 30 : X-Axis Labels are drawn correctly
    }
}
