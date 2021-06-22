package com.example.a20210207_checkmate2;

import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CalcDayLineChartStats {

    ArrayList<GlucoseEntry> glucoseData;
    int day_interval;
    int nDays;
    ArrayList<Double> x_Axis;
    ArrayList<ArrayList<Double>> yGlucoseData;


    CalcDayLineChartStats(ArrayList<GlucoseEntry> glucoseData) {
        this.glucoseData = glucoseData;
    }

    public ArrayList<Double> getMeanDailyGlucoseXAxis() {
        return x_Axis;
    }

    public ArrayList<Double> getPercentileGlucoseYAxis(int percentile) {
        return CalcMeanGlucose(day_interval,yGlucoseData,percentile);
    }

    public void CalcMedian(int nDays) {
        this.nDays = nDays;
        //--------------------------------------------------------
        //Get SVG Count based on Glucose Data size and nDays used for calculation
        //--------------------------------------------------------
        int count_svg = CalcSVGDataSize(glucoseData, nDays);
        //--------------------------------------------------------
        //Calculate and Initialize X-Axis and yGlucsoeData Array Size
        //--------------------------------------------------------
        //Time in float and minute = 1
        int interval = 30; // 15 Min Intervall Distance
        x_Axis = new ArrayList<>();
        yGlucoseData = new ArrayList<ArrayList<Double>>();

        day_interval = 1440 / interval;
        for (int i = 0; i <= day_interval; i++) {
            x_Axis.add(0, (double) (i * interval));
            yGlucoseData.add(new ArrayList<Double>());
        }
        Collections.reverse(x_Axis);

        //--------------------------------------------------------
        //Put all the Glucose Elements in the right yGlucose Data Element
        //--------------------------------------------------------
        SimpleDateFormat dfHour = new SimpleDateFormat("HH", Locale.getDefault()); // Quoted "Z" to indicate UTC, no timezone offset
        SimpleDateFormat dfMin = new SimpleDateFormat("mm", Locale.getDefault()); // Quoted "Z" to indicate UTC, no timezone offset
        SimpleDateFormat dfSec = new SimpleDateFormat("ss", Locale.getDefault()); // Quoted "Z" to indicate UTC, no timezone offset
        dfHour.setTimeZone(TimeZone.getTimeZone("UTC"));
        int hours;
        int minutes;
        int glucoseTime;
        int time;

        //for loop through every SVG element
        for (int i = 0; i < count_svg; i++) {
            hours = Integer.valueOf(dfHour.format(glucoseData.get(i).date));
            minutes = Integer.valueOf(dfMin.format(glucoseData.get(i).date));
            glucoseTime = hours * 60 + minutes;

            //put SVG element in the right time interval
            for (int j = 0; j < day_interval; j++) {
                time = x_Axis.get(j).intValue();
                if (glucoseTime < (time + interval) && glucoseTime > time) {
                    yGlucoseData.get(j).add((double) glucoseData.get(i).sgv);
                    break;
                }
            }
        }
    }

    public ArrayList<Double> CalcMeanGlucose(int day_interval,ArrayList<ArrayList<Double>> yGlucoseData, int percentile){
        ArrayList<Double> percentileYGlucose =new ArrayList<>();

        ArrayList<Double> interval_array = new ArrayList<Double>();
        int sum;

        //Calculate mean of every time interval
        for(int i = 0; i<day_interval;i++) {
            interval_array = yGlucoseData.get(i);

            if (interval_array.size() > 0) {
                //mean
                if (percentile == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        percentileYGlucose.add(i, (interval_array.stream().mapToDouble(f -> f.doubleValue()).sum()) / interval_array.size());
                    }
                } else {
                    Collections.sort(interval_array);
                    percentileYGlucose.add(i, percentile(interval_array, percentile));
                }
            } else {
                percentileYGlucose.add(i, (double) 0);
            }
        }
        return percentileYGlucose;
    }


    public static int CalcSVGDataSize(ArrayList<GlucoseEntry> glucoseData,int nDays){
        //--------------------------------------------------------
        //Reduce Glucose Data Size depending on needed time span
        //--------------------------------------------------------

        Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -nDays);
        Date dayStartCount = cal.getTime();

        int count_svg= glucoseData.size()-1;
            for(int i=0; i<glucoseData.size();i++){

            if (glucoseData.get(i).date.before(dayStartCount)){
                count_svg = i;
                break;
            }
        }

        return count_svg;
    }

    public static double percentile(ArrayList<Double> interval, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * interval.size());
        return interval.get(index-1);
    }


}
