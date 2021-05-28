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
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.google.android.material.color.MaterialColors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class DayLineChartClass {
    LineChart mDayChart;
    MainActivity mainActivity;
    ArrayList<Hba1cEntry> hba1c_data;
    ArrayList<GlucoseEntry> glucose_data;

    CalcDayLineChartStats calcDayLineChartStats;
    int xAxisTextColor;

    //Constructor
    DayLineChartClass(MainActivity mainActivity, ArrayList<Hba1cEntry> hba1c_data, ArrayList<GlucoseEntry> glucose_data, CalcDayLineChartStats calcDayLineChartStats) {
        mDayChart = (LineChart) mainActivity.findViewById(R.id.DayLineChart);
        this.mainActivity = mainActivity;

        this.hba1c_data = hba1c_data;
        this.glucose_data = glucose_data;
        this.calcDayLineChartStats = calcDayLineChartStats;

        //Initialize Colors
        xAxisTextColor = mainActivity.getResources().getColor(R.color.line);

    }

    public void createChart(int daySelected, boolean useDaySelected, int yMin, int yMax) {
        //Format Axes
        XAxis xAxis = mDayChart.getXAxis();
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(15); //Leave space on right side
        xAxis.setTextColor(xAxisTextColor);
        xAxis.setValueFormatter(new ValueFormatterDayPlotXAxis());
        //String testLabel = xAxis.getValueFormatter().getFormattedValue(300f);

        YAxis left = mDayChart.getAxisLeft();
        left.setDrawLabels(true); // no axis labels
        left.setDrawAxisLine(true); // no axis line
        left.setDrawGridLines(false); // no grid lines
        left.setTextSize(15);
        left.setTextColor(xAxisTextColor);

        //Check if yMax/yMin is sufficient
        if (yMax > 200)
            left.setAxisMaximum((float) yMax);
        else
            left.setAxisMaximum(200f);

        if (yMin < 50)
            left.setAxisMinimum((float) yMin);
        else
            left.setAxisMinimum(50f);

        YAxis right = mDayChart.getAxisRight();
        right.setDrawLabels(false); // no axis labels
        right.setDrawAxisLine(false); // no axis line
        right.setDrawGridLines(false); // no grid lines

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        ArrayList<Entry> oneDayEntry = new ArrayList<>();
        ArrayList<Entry> circleEntry = new ArrayList<>();

        //-------------------------------------------------------------------------------
        //Draw In Range Lines
        //-------------------------------------------------------------------------------
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        double minRange = Double.valueOf(sharedPref.getString(SettingsActivity.KEY_PREF_LOWER_RANGE,"70"));
        double maxRange = Double.valueOf(sharedPref.getString(SettingsActivity.KEY_PREF_UPPER_RANGE,"140"));

        //Max
        oneDayEntry = new ArrayList<>();
        oneDayEntry.add(new Entry(0f, (float) maxRange));
        oneDayEntry.add(new Entry(24*60, (float) maxRange));
        LineDataSet maxRangeDataSet = new LineDataSet(oneDayEntry,"MaxRange");
        maxRangeDataSet.setColor(mainActivity.getResources().getColor(R.color.OutOfRange));
        maxRangeDataSet.enableDashedLine(0f,1f,0f);
        maxRangeDataSet.setDrawCircles(false);
        maxRangeDataSet.setLineWidth(3);
        maxRangeDataSet.setDrawFilled(true);
        maxRangeDataSet.setHighlightEnabled(false);
        maxRangeDataSet.setDrawValues(false);
        maxRangeDataSet.setFillAlpha(MaterialColors.getColor(mainActivity, R.attr.alphaDayLineInRange, 80));
        maxRangeDataSet.setFillColor(MaterialColors.getColor(mainActivity, R.attr.colorDayLineInRange, Color.BLACK));
        maxRangeDataSet.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return (float) minRange; //mDayChart.getAxisLeft().getAxisMinimum();
            }
        });

        dataSets.add(maxRangeDataSet);

        //-------------------------------------------------------------------------------
        //Draw Graph
        //-------------------------------------------------------------------------------

        //for (Hba1cEntry element:hba1c_data){
        float sgv_value = 0;
        int count = hba1c_data.size()-1;
        int count_svg=0;
        ArrayList<String> xVals = new ArrayList<String>();

        SimpleDateFormat dfHour = new SimpleDateFormat("HH", Locale.getDefault()); // Quoted "Z" to indicate UTC, no timezone offset
        SimpleDateFormat dfMin = new SimpleDateFormat("mm", Locale.getDefault()); // Quoted "Z" to indicate UTC, no timezone offset
        SimpleDateFormat dfSec = new SimpleDateFormat("ss", Locale.getDefault()); // Quoted "Z" to indicate UTC, no timezone offset
        dfHour.setTimeZone(TimeZone.getDefault());

        double hours;
        double minutes;
        double Time;

        int i = 0;
        if (useDaySelected == true)
            i = daySelected;

        //for (int i = 0; i <= count; i++) {

            oneDayEntry = new ArrayList<>();
            circleEntry = new ArrayList<>();
            count_svg = hba1c_data.get(count - i).glucoseData.size()-1;

            for (int j = 0; j <= count_svg; j++)
            {
                //Calulate X-Axis Position based on time at day in minutes
                hours = Double.valueOf(dfHour.format(hba1c_data.get(count - i).glucoseData.get(count_svg - j).date));
                minutes = Double.valueOf(dfMin.format(hba1c_data.get(count - i).glucoseData.get(count_svg - j).date));
                Time = hours*60 + minutes;
                //Date l = Instant.ofEpochMilli(dateTime).truncatedTo(ChronoUnit.DAYS).toEpochMilli();
                //xAxisLabel.add(dateString);
                sgv_value = hba1c_data.get(count - i).glucoseData.get(count_svg - j).sgv;

                oneDayEntry.add(new Entry((float) Time, sgv_value));

            }



            LineDataSet d = new LineDataSet(oneDayEntry,"Day" + i);
            d.setDrawCircles(false);
            d.setColor(MaterialColors.getColor(mainActivity, R.attr.colorDayLine, Color.WHITE));

            d.setCircleColor(mainActivity.getResources().getColor(R.color.line));
            d.setLineWidth(3);
            d.setHighlightEnabled(false);
            d.setDrawValues(false);
            dataSets.add(d);

            //Draw a circle at the end of the line only if present day is activ
            if (i==hba1c_data.size()-1) {
                //Get last Element from oneDayEntry
                circleEntry.add(oneDayEntry.get(count_svg));
                LineDataSet circle = new LineDataSet(circleEntry, "Day" + i);
                circle.setCircleColor(mainActivity.getResources().getColor(R.color.line));
                circle.setHighlightEnabled(false);
                circle.setDrawValues(false);
                circle.setCircleRadius(5);
                dataSets.add(circle);
            }

        //}

        //-------------------------------------------------------------------------------
        //Draw Mean Line Graph
        //-------------------------------------------------------------------------------
/*        ArrayList<Double> x_Axis = calcDayLineChartStats.getMeanDailyGlucoseXAxis();
        ArrayList<Double> yGlucoseData = calcDayLineChartStats.getMeanDailyGlucoseYAxis();
        ArrayList<Entry> oneDayEntryMeanGlucose = new ArrayList<>();

        for (int j = 0; j < x_Axis.size()-1; j++) {
             oneDayEntryMeanGlucose.add(new Entry(x_Axis.get(j).floatValue(),yGlucoseData.get(j).floatValue()));
        }
        LineDataSet dMean = new LineDataSet(oneDayEntryMeanGlucose,"Day" + i);
        dMean.setDrawCircles(false);
        dMean.setColor(mainActivity.getResources().getColor(R.color.line));
        dMean.setLineWidth(1);
        dMean.setHighlightEnabled(false);
        dMean.setDrawValues(false);*/

        //Not added -> Fill does not work
        //dataSets.add(dMean);

       //-------------------------------------------------------------------------------
        //Draw 5%-95% Area Graph
        //-------------------------------------------------------------------------------

        //Draw 5perc Area Graph
/*        ArrayList<Double> percentile5yGlucose = calcDayLineChartStats.getPercentile5GlucoseYAxis();
        ArrayList<Entry> oneDayEntry5percentileGlucose = new ArrayList<>();

        for (int j = 0; j < x_Axis.size()-1; j++) {
            oneDayEntry5percentileGlucose.add(new Entry(x_Axis.get(j).floatValue(),percentile5yGlucose.get(j).floatValue()));
        }
        LineDataSet d5perc = new LineDataSet(oneDayEntry5percentileGlucose,"5Percentil");
        d5perc.setDrawCircles(false);
        d5perc.setColor(mainActivity.getResources().getColor(R.color.line));
        //d5perc.enableDashedLine(0f,1f,0f);
        d5perc.setLineWidth(1);
        d5perc.setHighlightEnabled(false);
        d5perc.setDrawValues(false);

        d5perc.setFillAlpha(MaterialColors.getColor(mainActivity, R.attr.alphaDayLineInRange, 80));
        d5perc.setFillColor(MaterialColors.getColor(mainActivity, R.attr.colorDayLineInRange, Color.BLACK));

        //Draw 95perc Area Graph

        ArrayList<Double> percentile95yGlucose = calcDayLineChartStats.getPercentile95GlucoseYAxis();

        ArrayList<Entry> oneDayEntry95percentileGlucose = new ArrayList<>();

        for (int j = 0; j < x_Axis.size()-1; j++) {
            oneDayEntry95percentileGlucose.add(new Entry(x_Axis.get(j).floatValue(),percentile95yGlucose.get(j).floatValue()));
        }
        LineDataSet d95perc = new LineDataSet(oneDayEntry95percentileGlucose,"95Percentil");
        d95perc.setColor(mainActivity.getResources().getColor(R.color.line));
        //d95perc.enableDashedLine(0f,1f,0f);
        d95perc.setDrawCircles(false);
        d95perc.setLineWidth(1);
        d95perc.setHighlightEnabled(false);
        d95perc.setDrawValues(false);

        d95perc.setFillAlpha(MaterialColors.getColor(mainActivity, R.attr.alphaDayLineInRange, 80));
        d95perc.setFillColor(MaterialColors.getColor(mainActivity, R.attr.colorDayLineInRange, Color.BLACK));
        d95perc.setFillFormatter(new FillFormatter(d5perc));*/

        //Fill does not work -> Feature not added
        //dataSets.add(d5perc);
        //dataSets.add(d95perc);

        //-------------------------------------------------------------------------------
        //Render Line Chart
        //-------------------------------------------------------------------------------

        //mChart.setXAxisRenderer(new ValueFormatterDateXAxis(mChart.getViewPortHandler(), mChart.getXAxis(), mChart.getTransformer(YAxis.AxisDependency.LEFT)));
        //mDayChart.setRendererLeftYAxis(new ValueFormatterDayPlotYAxis(mDayChart.getViewPortHandler()));

        LineData lineData = new LineData(dataSets);

        mDayChart.setData(lineData);
        //mDayChart.setRenderer(new MyLineLegendRenderer(mDayChart, mDayChart.getAnimator(), mDayChart.getViewPortHandler()));
        mDayChart.invalidate();
        mDayChart.getLegend().setEnabled(false);
        mDayChart.getDescription().setEnabled(false);
        mDayChart.setExtraOffsets(0, 0, 0, 10); //X-Axis Labels are drawn correctly

    }

    public void createMeanChart() {

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        ArrayList<Double> x_Axis = calcDayLineChartStats.getMeanDailyGlucoseXAxis();

        ArrayList<Double> yGlucoseData5 = calcDayLineChartStats.getPercentile5GlucoseYAxis();
        ArrayList<Entry> oneDayEntry5Glucose = new ArrayList<>();

        for (int j = 0; j < x_Axis.size()-1; j++) {
            oneDayEntry5Glucose.add(new Entry(x_Axis.get(j).floatValue(),yGlucoseData5.get(j).floatValue()));
        }
        LineDataSet d5 = new LineDataSet(oneDayEntry5Glucose,"5");
        d5.setDrawCircles(false);
        d5.setColor(mainActivity.getResources().getColor(R.color.line));
        d5.setLineWidth(1);
        d5.setHighlightEnabled(false);
        d5.setDrawValues(false);

        dataSets.add(d5);



        ArrayList<Double> yGlucoseData95 = calcDayLineChartStats.getPercentile95GlucoseYAxis();
        ArrayList<Entry> oneDayEntry95Glucose = new ArrayList<>();

        for (int j = 0; j < x_Axis.size()-1; j++) {
            oneDayEntry95Glucose.add(new Entry(x_Axis.get(j).floatValue(),yGlucoseData95.get(j).floatValue()));
        }
        LineDataSet d95 = new LineDataSet(oneDayEntry95Glucose,"95");
        d95.setDrawCircles(false);
        d95.setColor(mainActivity.getResources().getColor(R.color.line));
        d95.setLineWidth(1);
        d95.setHighlightEnabled(false);
        d95.setDrawValues(false);

        dataSets.add(d95);

        d95.setDrawFilled(true);
        d95.setFillAlpha(50);
        d95.setFillColor(Color.LTGRAY);
        d95.setFillFormatter(new AreaFillFormatter(d5));


        //-------------------------------------------------------------------------------
        //Draw Mean Line Graph
        //-------------------------------------------------------------------------------
        ArrayList<Double> yGlucoseData = calcDayLineChartStats.getMeanDailyGlucoseYAxis();
        ArrayList<Entry> oneDayEntryMeanGlucose = new ArrayList<>();

        for (int j = 0; j < x_Axis.size()-1; j++) {
             oneDayEntryMeanGlucose.add(new Entry(x_Axis.get(j).floatValue(),yGlucoseData.get(j).floatValue()));
        }
        LineDataSet dMean = new LineDataSet(oneDayEntryMeanGlucose,"Day");
        dMean.setDrawCircles(false);
        dMean.setColor(mainActivity.getResources().getColor(R.color.line));
        dMean.setLineWidth(3);
        dMean.setHighlightEnabled(false);
        dMean.setDrawValues(false);

        dataSets.add(dMean);






        LineData lineData = new LineData(dataSets);
        mDayChart.setRenderer(new AreaLineLegendRenderer(mDayChart, mDayChart.getAnimator(), mDayChart.getViewPortHandler()));
        mDayChart.setData(lineData);
        //mDayChart.setRenderer(new MyLineLegendRenderer(mDayChart, mDayChart.getAnimator(), mDayChart.getViewPortHandler()));
        mDayChart.invalidate();
        mDayChart.getLegend().setEnabled(false);
        mDayChart.getDescription().setEnabled(false);
        mDayChart.setExtraOffsets(0, 0, 0, 10); //X-Axis Labels are drawn correctly


    }
}
