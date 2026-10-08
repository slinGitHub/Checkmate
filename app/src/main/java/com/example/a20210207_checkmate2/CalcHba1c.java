package com.example.a20210207_checkmate2;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import androidx.preference.PreferenceManager;

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

    public void CalcAverageHba1c (ArrayList<Hba1cEntry> hba1cDataList, int nDays){
        this.nDays = nDays;
        hba1cAverageData = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -nDays);
        dayStartHba1cCount = cal.getTime();

        double hba1cSum = 0;
        double inRangeSum = 0;
        double highRangeSum = 0;
        double lowRangeSum = 0;
        double minGlucoseSum = 0;
        double lowEventsSum = 0;

        int count = 0;
        for(int i=0; i < hba1cDataList.size(); i++){
            count++;
            Hba1cEntry entry = hba1cDataList.get(i);
            hba1cSum += entry.hba1c;
            inRangeSum += entry.inRange;
            highRangeSum += entry.HighRange;
            lowRangeSum += entry.LowRange;
            minGlucoseSum += entry.minGlucose;
            lowEventsSum += entry.lowEvents;

            if (entry.date.before(dayStartHba1cCount) || count == hba1cDataList.size()) {
                dayStartHba1cCount = entry.date;
                daysGoingBackCount = count;

                double hba1cAvg = hba1cSum / count;
                double inRange = inRangeSum / count;
                double highRange = highRangeSum / count;
                double lowRange = lowRangeSum / count;
                double minGlucoseAvg = minGlucoseSum / count;
                double lowEventsAvg = lowEventsSum / count;

                ArrayList<GlucoseEntry> dummy = new ArrayList<>();
                dummy.add(new GlucoseEntry("", null, 0, "", ""));
                hba1cAverageData.add(new Hba1cEntry(dayStartHba1cCount, hba1cAvg, inRange, highRange, lowRange, 0, dummy, minGlucoseAvg, (int)Math.round(lowEventsAvg)));
                break;
            }
        }
    }

    public void CalcHba1c (Context context){
        hba1cData = new ArrayList<>();
        ArrayList<GlucoseEntry> currentDayEntries = new ArrayList<>();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        double glucoseRangeLow = Double.parseDouble(sharedPref.getString(SettingsActivity.KEY_PREF_LOWER_RANGE, "70"));
        double glucoseRangeHigh = Double.parseDouble(sharedPref.getString(SettingsActivity.KEY_PREF_UPPER_RANGE, "140"));

        double daySgvSum = 0;
        double dayGlucoseHigh = 0;
        double dayGlucoseLow = 0;
        double dayGlucoseIn = 0;
        double dayMinGlucose = 1000;
        int lowEvents = 0;
        boolean isLow = false;
        
        Calendar cal = Calendar.getInstance();
        long lastDayStart = -1;

        for (int i = 0; i < glucoseData.size(); i++) {
            GlucoseEntry entry = glucoseData.get(i);
            int sgv = entry.sgv;

            if (sgv > sgvMax || sgvMax == 0) sgvMax = sgv;
            if (sgv < sgvMin || sgvMin == 0) sgvMin = sgv;

            // Use UTC/System-Midnight to group days without expensive Joda parsing
            cal.setTime(entry.date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long currentDayStart = cal.getTimeInMillis();

            if (lastDayStart == -1) {
                lastDayStart = currentDayStart;
            }

            if (currentDayStart == lastDayStart) {
                daySgvSum += sgv;
                currentDayEntries.add(entry);
                if (sgv < dayMinGlucose) dayMinGlucose = sgv;
                
                if (sgv > glucoseRangeHigh) {
                    dayGlucoseHigh++;
                    isLow = false;
                }
                else if (sgv > glucoseRangeLow) {
                    dayGlucoseIn++;
                    isLow = false;
                }
                else {
                    dayGlucoseLow++;
                    if (!isLow) {
                        lowEvents++;
                        isLow = true;
                    }
                }
            } else {
                // Finish previous day
                double count = currentDayEntries.size();
                if (count > 0) {
                    double hba1c = ((daySgvSum / count) + 46.7) / 28.7;
                    double total = dayGlucoseIn + dayGlucoseHigh + dayGlucoseLow;
                    if (dayMinGlucose == 1000) dayMinGlucose = 0;
                    hba1cData.add(new Hba1cEntry(new Date(lastDayStart), hba1c, dayGlucoseIn/total, dayGlucoseHigh/total, -(dayGlucoseLow/total), count, currentDayEntries, dayMinGlucose, lowEvents));
                }

                // Start new day
                lastDayStart = currentDayStart;
                daySgvSum = sgv;
                currentDayEntries = new ArrayList<>();
                currentDayEntries.add(entry);
                dayMinGlucose = sgv;
                dayGlucoseHigh = (sgv > glucoseRangeHigh) ? 1 : 0;
                dayGlucoseIn = (sgv <= glucoseRangeHigh && sgv > glucoseRangeLow) ? 1 : 0;
                dayGlucoseLow = (sgv <= glucoseRangeLow) ? 1 : 0;
                lowEvents = (sgv <= glucoseRangeLow) ? 1 : 0;
                isLow = (sgv <= glucoseRangeLow);
            }
        }
        // Original code skips the last (oldest) day if incomplete, keeping that logic
    }
}
