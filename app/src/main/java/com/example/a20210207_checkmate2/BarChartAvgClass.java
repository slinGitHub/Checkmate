package com.example.a20210207_checkmate2;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.color.MaterialColors;
import androidx.preference.PreferenceManager;

import java.math.BigDecimal;
import java.util.ArrayList;

public class BarChartAvgClass {

BarChart bAvgChart;
MainActivity mainActivity;
ArrayList<Hba1cEntry> hba1c_data;
ArrayList<Hba1cEntry> hba1cAverageData;

XAxis bxAxis;
YAxis bLeft;
YAxis bRight;

int bxAxisTextColor;
int textColorInRange;
int textColorAboveRange;
int textColorHighAbRange;
int barSelectedLineColor;

        //Constructor
        BarChartAvgClass(MainActivity mainActivity,ArrayList<Hba1cEntry> hba1c_data,ArrayList<Hba1cEntry> hba1cAverageData) {
            bAvgChart = (BarChart) mainActivity.findViewById(R.id.BarChartAvg);
            this.mainActivity = mainActivity;
            this.hba1c_data = hba1c_data;
            this.hba1cAverageData = hba1cAverageData;

            //Initialize Colors
            bxAxisTextColor = MaterialColors.getColor(mainActivity, R.attr.colorGMIline, Color.BLACK);
            textColorInRange = MaterialColors.getColor(mainActivity, R.attr.colorTextInRange, Color.BLACK);
            textColorAboveRange = MaterialColors.getColor(mainActivity, R.attr.colorTextOutOfRange, Color.BLACK);
            textColorHighAbRange = MaterialColors.getColor(mainActivity, R.attr.colorVeryOutOfRange, Color.BLACK);
            barSelectedLineColor = MaterialColors.getColor(mainActivity, R.attr.colorSelectedLine, Color.BLACK);
        }

    public void createChart(int nDays, boolean selected) {

        View cardAvg = mainActivity.findViewById(R.id.lineText5);
        if (cardAvg != null) {
            android.graphics.drawable.Drawable bg = cardAvg.getBackground();
            if (bg instanceof GradientDrawable) {
                GradientDrawable shape = (GradientDrawable) bg.mutate();
                int color = selected ?
                        MaterialColors.getColor(mainActivity, R.attr.colorBackgroundCheckmateAvg, Color.DKGRAY) :
                        MaterialColors.getColor(mainActivity, R.attr.colorBackgroundCheckmateBright, Color.WHITE);
                shape.setColor(color);
            }
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        float inRangeGoal = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_IN_RANGE_GOAL,"50"));
        float inRangeTooLow = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_IN_RANGE_TOO_LOW,"30"));

        bxAxis = bAvgChart.getXAxis();
        bLeft = bAvgChart.getAxisLeft();
        bRight = bAvgChart.getAxisRight();

        bxAxis.setDrawAxisLine(false);
        bxAxis.setDrawLabels(false);
        bxAxis.setDrawGridLines(false);
        bxAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        bxAxis.setSpaceMax(0.8f);
        bxAxis.setSpaceMin(0.8f);
        bxAxis.setTextColor(bxAxisTextColor);
        bxAxis.setTextSize(15);
        bxAxis.setGranularityEnabled(true);
        bxAxis.setGranularity(1f);

        bLeft.setDrawLabels(false); // no axis labels
        bLeft.setDrawAxisLine(false); // no axis line
        bLeft.setDrawGridLines(false); // no grid lines

        bRight.setDrawLabels(false); // no axis labels
        bRight.setDrawAxisLine(false); // no axis line
        bRight.setDrawGridLines(false); // no grid lines

        //Load Data in Chart
        ArrayList<BarEntry> dataBar = new ArrayList<BarEntry>();
        ArrayList<BarEntry> dataBar2 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> dataBarSel = new ArrayList<BarEntry>();

        //Set marker Color depending on RangeValue
        ArrayList<Integer> bAcolors = new ArrayList<Integer>();
        ArrayList<Integer> bATextColors = new ArrayList<Integer>();
        ArrayList<String> xAAxisLabel = new ArrayList<>();

        xAAxisLabel.add(nDays + " Day\nAvg");
        float val1 = BigDecimal.valueOf(hba1cAverageData.get(0).LowRange * 100).setScale(0, BigDecimal.ROUND_HALF_DOWN).floatValue(); //Low
        float val2 = BigDecimal.valueOf(hba1cAverageData.get(0).inRange * 100).setScale(0, BigDecimal.ROUND_HALF_DOWN).floatValue(); //InRange
        float val3 = BigDecimal.valueOf(hba1cAverageData.get(0).HighRange * 100).setScale(0, BigDecimal.ROUND_HALF_DOWN).floatValue(); //High
        dataBar.add(new BarEntry(0f, new float[]{val1, val2, val3}));
        dataBar2.add(new BarEntry(0f, val2));
        if (val2 >= inRangeGoal) {
            bAcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorInRange, Color.BLACK));
            bATextColors.add(textColorInRange);
        } else if (val2 >= inRangeTooLow) {
            bAcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorOutOfRange, Color.BLACK));
            bATextColors.add(textColorAboveRange);
        } else {
            bAcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorVeryOutOfRange, Color.BLACK));
            bATextColors.add(textColorHighAbRange);
        }
        int selectedBarColor = 0;
        if (selected) {
            dataBarSel.add(new BarEntry(0f, val2));
            selectedBarColor = bAcolors.isEmpty() ? 0 : bAcolors.get(0);
        }

        //Draw x-Axis labels
        bAvgChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAAxisLabel));
        bAvgChart.setXAxisRenderer(new ValueFormatterDateXAxis(bAvgChart.getViewPortHandler(), bAvgChart.getXAxis(), bAvgChart.getTransformer(YAxis.AxisDependency.LEFT)));

        BarDataSet dataBarSet = new BarDataSet(dataBar, "DataBarBackground");
        dataBarSet.setDrawIcons(false);
        dataBarSet.setDrawValues(false);
        dataBarSet.setHighlightEnabled(false);
        int[] colorsInt = new int[]{MaterialColors.getColor(mainActivity, R.attr.colorBarInRangeBackground, Color.BLACK), MaterialColors.getColor(mainActivity, R.attr.colorBarInRangeBackground, Color.BLACK), MaterialColors.getColor(mainActivity, R.attr.colorBarInRangeBackground, Color.BLACK)};
        dataBarSet.setColors(colorsInt,MaterialColors.getColor(mainActivity, R.attr.alphaBarInRangeBackground, 255));

        //dataBarSet.setStackLabels(new String[]{"<70", "InRange", ">140"}); //Hardcoded

        BarDataSet dataBarSet2 = new BarDataSet(dataBar2, "DataBarInRange");
        dataBarSet2.setColors(bAcolors);
        dataBarSet2.setValueTextSize(20);
        dataBarSet2.setValueTextColors(bATextColors);
        dataBarSet2.setHighLightAlpha(0);
        dataBarSet2.setHighLightColor(mainActivity.getResources().getColor(R.color.white));
        dataBarSet2.setHighlightEnabled(true);

        //Bar Selected with border around bar
        BarDataSet dataBarSetSel = new BarDataSet(dataBarSel, "BarSelected");
        dataBarSetSel.setDrawValues(false);
        dataBarSetSel.setColor(selectedBarColor);
        dataBarSetSel.setBarBorderWidth(3f);
        dataBarSetSel.setBarBorderColor(barSelectedLineColor);

        ArrayList<IBarDataSet> dataSetsBar = new ArrayList<>();
        dataSetsBar.add(dataBarSet);
        dataSetsBar.add(dataBarSet2);
        dataSetsBar.add(dataBarSetSel);

        BarData dataBarChart = new BarData(dataSetsBar);
        dataBarChart.setBarWidth(0.9f);
        dataBarChart.setValueFormatter(new ValueFormatterPercentage());

        bAvgChart.setRenderer(new CustomBarChartRenderer(bAvgChart, bAvgChart.getAnimator(), bAvgChart.getViewPortHandler()));
        bAvgChart.setData(dataBarChart);
        bAvgChart.setTouchEnabled(true);
        bAvgChart.setFitBars(false); //Makes an error so that the bar chart is jumping to the left at reload -> do not use
        bAvgChart.setVisibleXRangeMinimum(dataBar.size());

        // Remove the default 15dp chart padding/margin
        // Convert dp to pixels for precise control (left, top, right, bottom)
        float leftOffset = Utils.convertDpToPixel(9f);
        float topOffset = Utils.convertDpToPixel(15f);
        float rightOffset = Utils.convertDpToPixel(9f);
        float bottomOffset = Utils.convertDpToPixel(15f);
        bAvgChart.setViewPortOffsets(leftOffset, topOffset, rightOffset, bottomOffset);

        bAvgChart.invalidate();
        //bAvgChart.setVisibleXRangeMaximum(1);
        bAvgChart.moveViewToX(1);
        bxAxis.setGranularity(1f);
        bAvgChart.getXAxis().setAxisMaximum(0f + 0.5f);
        bAvgChart.getXAxis().setAxisMinimum(0f - 0.5f);

        bAvgChart.getDescription().setEnabled(false);
        bAvgChart.getLegend().setEnabled(false);
        bAvgChart.setScaleEnabled(false);
        bAvgChart.setDrawValueAboveBar(false);
        bAvgChart.setExtraOffsets(0, 0, 0, 0);

    }

}
