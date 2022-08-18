package com.example.a20210207_checkmate2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.preference.PreferenceManager;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class CalcHba1c {

int sgvMax;
int sgvMin;
Date dayStartHba1cCount;
int daysGoingBackCount;
int nDays;

ArrayList<GlucoseEntry> glucoseData;
ArrayList<Hba1cEntry> hba1cData;
ArrayList<Hba1cEntry> hba1cAverageData;


    CalcHba1c(ArrayList<GlucoseEntry> glucoseData){
        sgvMax = 0;
        sgvMin = 0;
        this.glucoseData = glucoseData;
    }

    public ArrayList<GlucoseEntry> getGlucoseData(){
        return glucoseData;
    }

    public ArrayList<Hba1cEntry> getHba1cAverageData(){
        return hba1cAverageData;
    }

    public ArrayList<Hba1cEntry> getHba1cData(){
        return hba1cData;
    }

    //Calculate the Average Hba1c per day based on glucoseData
    public void CalcAverageHba1c (ArrayList<Hba1cEntry> glucoseData, int nDays){
        this.nDays = nDays;

        hba1cAverageData = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -nDays);

        //SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Quoted "Z" to indicate UTC, no timezone offset
        //df.setTimeZone(TimeZone.getTimeZone("UTC"));

        dayStartHba1cCount = cal.getTime();

        double hba1cSum = 0;
        double hba1cAvg = 0;

        double inRangeSum = 0;
        double highRangeSum = 0;
        double lowRangeSum = 0;

        double inRange = 0;
        double highRange = 0;
        double lowRange = 0;

        //Check the last date in Dataset before your desired date
        int count=0;
        for(int i=0; i<glucoseData.size();i++){

            count++;
            hba1cSum = hba1cSum + glucoseData.get(i).hba1c;

            inRangeSum = inRangeSum + glucoseData.get(i).inRange;
            highRangeSum = highRangeSum + glucoseData.get(i).HighRange;
            lowRangeSum = lowRangeSum + glucoseData.get(i).LowRange;

            //CHeck the day and continue if last day is not reached
            if ((glucoseData.get(i).date.before(dayStartHba1cCount)) || (count == glucoseData.size())) {
                dayStartHba1cCount = glucoseData.get(i).date;
                daysGoingBackCount = count;

                hba1cAvg = hba1cSum / (count);

                inRange = inRangeSum / (count);
                highRange = highRangeSum / (count);
                lowRange = lowRangeSum / (count);

                break;
            }
        }

        //Initialize a Null Glucose Entry
        ArrayList<GlucoseEntry> glucoseEntryData = new ArrayList<>();
        glucoseEntryData.add(new GlucoseEntry("", null,0,"",""));

        //Write the start day for hba1c calculation and the average hba1c in the last entry
        hba1cAverageData.add(new Hba1cEntry(dayStartHba1cCount,hba1cAvg,inRange,highRange,lowRange,0, glucoseEntryData));
    }

    //Calculate the average hba1c and in range for all days of a dataset
    public void CalcHba1c (Context context){

        hba1cData = new ArrayList<>();
        ArrayList<GlucoseEntry> glucoseEntryData = new ArrayList<>(); //Inside loop -> Otherwise all List elements are the last day in the end

        //Get Day from Data
        DateTime day_data_joda = null;
        DateTime day_joda = null;
        Date day = null;
        double sgv_sum = 0;
        double svg_i = 0;
        double hba1c;

        //Count Glucose above and below range
        //Load saved settings from shared preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        double glucoseRangeLow = Double.valueOf(sharedPref.getString(SettingsActivity.KEY_PREF_LOWER_RANGE,"70"));
        double glucoseRangeHigh = Double.valueOf(sharedPref.getString(SettingsActivity.KEY_PREF_UPPER_RANGE,"140"));

        double glucoseHigh = 0;
        double glucoseLow = 0;
        double glucoseIn = 0;

        int sgv = 0;
        int i;
        for(i=0; i<glucoseData.size();i++)
        {
            sgv = glucoseData.get(i).sgv;

            //get max sgv in Data
            if (sgv > sgvMax || sgvMax == 0)
                sgvMax = sgv;

            //get min sgv in Data
            if (sgv < sgvMin || sgvMin == 0)
                sgvMin = sgv;

            //Calculate in Range values
            if (sgv > glucoseRangeHigh) {
                glucoseHigh++;
            } else if (sgv > glucoseRangeLow) {
                glucoseIn++;
            } else {
                glucoseLow++;
            }

            //Calculate HbA1c
            String dateString = glucoseData.get(i).dateString;

            //Get Day (Joda)
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withLocale(Locale.ROOT).withChronology(ISOChronology.getInstanceUTC());
            day_data_joda = formatter.withZone(DateTimeZone.getDefault()).parseDateTime(dateString);
            Interval today = new Interval( day_data_joda.withTimeAtStartOfDay(), day_data_joda.plusDays(1).withTimeAtStartOfDay() );

            //start value
            if (i==0)
                day_joda = day_data_joda;

            //Same day : add to sgv_sum and count svg_i
            if (today.contains(day_joda)) {
                sgv_sum = sgv_sum + sgv;
                svg_i++;
                glucoseEntryData.add(glucoseData.get(i));

                //New day :
            } else {
                // Calculate Hba1c
                hba1c = ((sgv_sum/svg_i) + 46.7) / 28.7;

                //Write Data to Array
                day = day_joda.toDate();
                hba1cData.add(new Hba1cEntry(day,hba1c,(glucoseIn/(glucoseIn+glucoseHigh+glucoseLow)),(glucoseHigh/(glucoseIn+glucoseHigh+glucoseLow)),-(glucoseLow/(glucoseIn+glucoseHigh+glucoseLow)),svg_i,glucoseEntryData));

                //Reset Data for new day
                sgv_sum = sgv;
                svg_i = 1;
                glucoseEntryData = new ArrayList<>();
                glucoseEntryData.add(glucoseData.get(i));
                day_joda = day_data_joda;
                glucoseHigh = 0;
                glucoseLow = 0;
                glucoseIn = 0;
            }

        }

        // Dont save the last day, it not complete and therefore incorrect, mostly gray
        // Copy last day to hba1c entry after loop is finished
        // Calculate Hba1c
        // hba1c = ((sgv_sum/svg_i) + 46.7) / 28.7;
        // day = day_joda.toDate();
        // Write Data to Array
        // hba1cData.add(new Hba1cEntry(day,hba1c,(glucoseIn/(glucoseIn+glucoseHigh+glucoseLow)),(glucoseHigh/(glucoseIn+glucoseHigh+glucoseLow)),-(glucoseLow/(glucoseIn+glucoseHigh+glucoseLow)),svg_i,glucoseEntryData));
    }
}
