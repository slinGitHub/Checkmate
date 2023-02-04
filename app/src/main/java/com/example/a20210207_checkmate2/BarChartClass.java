package com.example.a20210207_checkmate2;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.material.color.MaterialColors;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class BarChartClass {
    BarChart bChart;
    MainActivity mainActivity;
    ArrayList<Hba1cEntry> hba1c_data;

    int bxAxisTextColor;

    //Constructor
    BarChartClass(MainActivity mainActivity,ArrayList<Hba1cEntry> hba1c_data) {
        bChart = (BarChart) mainActivity.findViewById(R.id.BarChart);
        this.mainActivity = mainActivity;
        this.hba1c_data = hba1c_data;

        //Initialize Colors
        bxAxisTextColor = mainActivity.getResources().getColor(R.color.line);
    }

    public void createChart(int daySelected, boolean useDaySelected) {

        //Get Values from Preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        float inRangeGoal = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_IN_RANGE_GOAL,"50"));
        float inRangeTooLow = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_IN_RANGE_TOO_LOW,"30"));

        //Load Data in Chart
        ArrayList<BarEntry> dataBar = new ArrayList<BarEntry>();
        ArrayList<BarEntry> dataBar2 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> dataBarSel = new ArrayList<BarEntry>();

        XAxis bxAxis = bChart.getXAxis();
        bxAxis.setDrawAxisLine(false);
        bxAxis.setDrawLabels(true);
        bxAxis.setDrawGridLines(false);
        bxAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        bxAxis.setSpaceMax(0.8f);
        bxAxis.setSpaceMin(0.8f);
        bxAxis.setTextSize(15); //Leave space on right side
        bxAxis.setTextColor(bxAxisTextColor);
        bxAxis.setGranularityEnabled(true);
        bxAxis.setGranularity(1f);

        YAxis bLeft = bChart.getAxisLeft();
        bLeft.setDrawLabels(false); // no axis labels
        bLeft.setDrawAxisLine(false); // no axis line
        bLeft.setDrawGridLines(false); // no grid lines

        YAxis bRight = bChart.getAxisRight();
        bRight.setDrawLabels(false); // no axis labels
        bRight.setDrawAxisLine(false); // no axis line
        bRight.setDrawGridLines(false); // no grid lines

        //Get maximum number of measurements a day -> detect days without enough data
        double maxValues = 0;
        for(Hba1cEntry entry : hba1c_data){
            if (entry.nValues > maxValues)
                maxValues = entry.nValues;
        }

        //Set marker Color depending on RangeValue
        ArrayList<Integer> bcolors = new ArrayList<Integer>();

        int count = hba1c_data.size() - 1;
        ArrayList<String> xAxisLabel = new ArrayList<>();
        int selectedBarColor = 0;

        for (int i = 0; i <= count; i++) {

            float val1 = BigDecimal.valueOf(hba1c_data.get(count - i).LowRange * 100).setScale(0, BigDecimal.ROUND_HALF_DOWN).floatValue(); //Low
            float val2 = BigDecimal.valueOf(hba1c_data.get(count - i).inRange * 100).setScale(0, BigDecimal.ROUND_HALF_DOWN).floatValue(); //InRange
            float val3 = BigDecimal.valueOf(hba1c_data.get(count - i).HighRange * 100).setScale(0, BigDecimal.ROUND_HALF_DOWN).floatValue(); //High

            //Set X-Axis Label
            SimpleDateFormat df = new SimpleDateFormat("E\ndd/MM", Locale.ENGLISH); // Quoted "Z" to indicate UTC, no timezone offset
            String dateString = df.format(hba1c_data.get(count - i).date);
            xAxisLabel.add(dateString);

            dataBar.add(new BarEntry(i, new float[]{val1, val2, val3}));
            dataBar2.add(new BarEntry(i, val2));


            //if ((hba1c_data.get(count - i).nValues > (0.5 * maxValues)) || i == count) {
                if (val2 >= inRangeGoal) {
                    bcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorInRange, Color.BLACK));
                    //bBackgroundcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorInRange, Color.BLACK));
                } else if (val2 >= inRangeTooLow) {
                    bcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorOutOfRange, Color.BLACK));
                    //bBackgroundcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorOutOfRange, Color.BLACK));
                } else {
                    bcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorVeryOutOfRange, Color.BLACK));
                    //bBackgroundcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorVeryOutOfRange, Color.BLACK));
                }
            //} else {
            //    bcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorNotEnoughData, Color.BLACK));
            //    //bBackgroundcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorNotEnoughData, Color.BLACK));
            //}

            //Color day selected brighter
            if (useDaySelected == true && i == daySelected){
                dataBarSel.add(new BarEntry(i, val2));
                selectedBarColor = bcolors.get(i);
            }
        }

        //Draw x-Axis labels
        bChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));
        bChart.setXAxisRenderer(new ValueFormatterDateXAxis(bChart.getViewPortHandler(), bChart.getXAxis(), bChart.getTransformer(YAxis.AxisDependency.LEFT)));

        //Bar in background with out and in Range Values
        BarDataSet dataBarSet = new BarDataSet(dataBar, "BarBackground");
        dataBarSet.setDrawIcons(false);
        dataBarSet.setDrawValues(false);
        dataBarSet.setHighlightEnabled(false);

        int[] colorsInt = new int[]{MaterialColors.getColor(mainActivity, R.attr.colorBarInRangeBackground, Color.BLACK), MaterialColors.getColor(mainActivity, R.attr.colorBarInRangeBackground, Color.BLACK), MaterialColors.getColor(mainActivity, R.attr.colorBarInRangeBackground, Color.BLACK)};
        dataBarSet.setColors(colorsInt,MaterialColors.getColor(mainActivity, R.attr.alphaBarInRangeBackground, 80));

        //Bar with in Range Values
        BarDataSet dataBarSet2 = new BarDataSet(dataBar2, "BarInRange");
        dataBarSet2.setColors(bcolors);
        dataBarSet2.setValueTextSize(20);
        dataBarSet2.setHighLightAlpha(0);
        dataBarSet2.setHighLightColor(mainActivity.getResources().getColor(R.color.white));
        dataBarSet2.setHighlightEnabled(true);

        //Bar Selected with border around bar
        BarDataSet dataBarSetSel = new BarDataSet(dataBarSel, "BarSelected");
        dataBarSetSel.setDrawValues(false);
        dataBarSetSel.setColor(selectedBarColor);
        dataBarSetSel.setBarBorderWidth(3f);
        dataBarSetSel.setBarBorderColor(Color.WHITE);

        ArrayList<IBarDataSet> dataSetsBar = new ArrayList<>();
        dataSetsBar.add(dataBarSet);
        dataSetsBar.add(dataBarSet2);
        dataSetsBar.add(dataBarSetSel);

        BarData dataBarChart = new BarData(dataSetsBar);
        //dataBarChart.setBarWidth(0.8f);
        dataBarChart.setValueFormatter(new ValueFormatterPercentage());
        dataBarChart.setValueTextColor(MaterialColors.getColor(mainActivity, R.attr.colorTextBubbleBar, Color.BLACK));

        bChart.setData(dataBarChart);
        //bChart.setFitBars(true); //Makes an error so that the bar chart is jumping to the left at reload -> do not use
        bChart.invalidate();
        float numberOfVisibleBubbleBars = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_SCREEN_VISIBLE_BUBBLEBARS,"6"));
        bChart.setVisibleXRangeMaximum(numberOfVisibleBubbleBars);

        bChart.getDescription().setEnabled(false);
        bChart.getLegend().setEnabled(false);
        bChart.setScaleEnabled(false);
        bChart.setDrawValueAboveBar(false);
        bChart.setExtraOffsets(0, 0, 0, 30); //X-Axis Labels are drawn correctly


        //bChart.setViewPortOffsets(0f,0f,40f,130f);
        //bChart.setMinOffset(0f);
        //float yMax = bChart.getData().getDataSetByIndex(0).getYMax();
        //float yMin = bChart.getData().getDataSetByIndex(0).getYMin();
        //bLeft.setAxisMaximum(yMax);
        //bLeft.setAxisMinimum(yMin);

        //Set the description for the bar chart
        double glucoseRangeLow = Double.valueOf(sharedPref.getString(SettingsActivity.KEY_PREF_LOWER_RANGE,"70"));
        double glucoseRangeHigh = Double.valueOf(sharedPref.getString(SettingsActivity.KEY_PREF_UPPER_RANGE,"140"));

        //Boolean switchToMol = Boolean.getBoolean(SettingsActivity.KEY_PREF_SWITCH_GLUCOSE_MOL);
        Boolean switchToMol = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_GLUCOSE_MOL,false);
        TextView barChartTitle = (TextView) mainActivity.findViewById(R.id.barText);
        if (switchToMol) {
            barChartTitle.setText("In Range (" + String.format("%1.1f",(glucoseRangeLow*0.0555)) + "-" + String.format("%1.1f",(glucoseRangeHigh*0.0555)) + "mmol/l)");
        } else {
            barChartTitle.setText("In Range (" + String.format("%1.0f",glucoseRangeLow) + "-" + String.format("%1.0f",glucoseRangeHigh) + "mg/dl)");
        }


    }

}

