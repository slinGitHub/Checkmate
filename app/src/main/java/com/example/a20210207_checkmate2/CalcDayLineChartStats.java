package com.example.a20210207_checkmate2;

import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;

public class CalcDayLineChartStats {

    ArrayList<GlucoseEntry> glucoseData;
    ArrayList<Double> meanDailyGlucose;
    ArrayList<Double> percentile95yGlucose;
    ArrayList<Double> percentile5yGlucose;

    ArrayList<Double> x_Axis;
    ArrayList<ArrayList<Double>> yGlucoseData;


    CalcDayLineChartStats(ArrayList<GlucoseEntry> glucoseData){
        this.glucoseData = glucoseData;
    }

    public ArrayList<Double> getMeanDailyGlucoseXAxis() {
        return x_Axis;
    }

    public ArrayList<Double> getMeanDailyGlucoseYAxis() {
        return meanDailyGlucose;
    }

    public ArrayList<Double> getPercentile95GlucoseYAxis() { return percentile95yGlucose;}

    public ArrayList<Double> getPercentile5GlucoseYAxis() { return percentile5yGlucose;}

    public void CalcMedian (int nDays){

        //Calculate X-Axis
        //Time in float and minute = 1
        int interval = 5; // 5 Min Intervall Distance
        x_Axis = new ArrayList<>();
        yGlucoseData = new ArrayList<ArrayList<Double>>();

        int day_interval = 1440/5;
        for (int i = 0; i <= day_interval; i++)
        {
            x_Axis.add(0,(double) (i*interval));
            yGlucoseData.add(new ArrayList<Double>());
        }
        Collections.reverse(x_Axis);

        //Calculate Y-Array

        SimpleDateFormat dfHour = new SimpleDateFormat("HH", Locale.getDefault()); // Quoted "Z" to indicate UTC, no timezone offset
        SimpleDateFormat dfMin = new SimpleDateFormat("mm", Locale.getDefault()); // Quoted "Z" to indicate UTC, no timezone offset
        SimpleDateFormat dfSec = new SimpleDateFormat("ss", Locale.getDefault()); // Quoted "Z" to indicate UTC, no timezone offset
        dfHour.setTimeZone(TimeZone.getTimeZone("UTC"));

        int hours;
        int minutes;

        int count_svg= glucoseData.size()-1;
        int glucoseTime;
        int time;

        //for loop through every SVG element
        for (int i = 0; i < count_svg; i++)
        {
            hours = Integer.valueOf(dfHour.format(glucoseData.get(i).date));
            minutes = Integer.valueOf(dfMin.format(glucoseData.get(i).date));
            glucoseTime = hours*60 + minutes;


            //put SVG element in the right time interval
            for (int j = 0; j < day_interval; j++) {

                time = x_Axis.get(j).intValue();
                if (glucoseTime < (time + interval) && glucoseTime > time)
                    yGlucoseData.get(j).add((double) glucoseData.get(i).sgv);
            }
        }

        meanDailyGlucose = new ArrayList<>();
        percentile95yGlucose = new ArrayList<>();
        percentile5yGlucose = new ArrayList<>();

        ArrayList<Double> interval_array = new ArrayList<Double>();
        int sum;
        //Calculate mean of every time interval
        for (int i = 0; i < day_interval; i++) {
            interval_array =  yGlucoseData.get(i);

            if (interval_array.size() > 0) {
                //mean
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    meanDailyGlucose.add(i, (interval_array.stream().mapToDouble(f -> f.doubleValue()).sum()) / interval_array.size());
                }

                Collections.sort(interval_array);

                //95Percentile
                percentile95yGlucose.add(i, percentile(interval_array, 95));
                //5Percentile
                percentile5yGlucose.add(i, percentile(interval_array, 5));
            } else {
                meanDailyGlucose.add(i, (double) 0);
                percentile95yGlucose.add(i, (double) 0);
                percentile5yGlucose.add(i, (double) 0);
            }
        }

        int b = 0;
    }

    public static double percentile(ArrayList<Double> interval, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * interval.size());
        return interval.get(index-1);
    }


}
