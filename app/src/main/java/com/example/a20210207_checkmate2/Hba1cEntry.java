package com.example.a20210207_checkmate2;

import java.util.ArrayList;
import java.util.Date;

public class Hba1cEntry {
    Date date;
    double hba1c;
    double inRange;
    double HighRange;
    double LowRange;
    double nValues;
    ArrayList<GlucoseEntry> glucoseData;

    Hba1cEntry(Date date, double hba1c, double inRange, double HighRange, double LowRange, double nValues, ArrayList<GlucoseEntry> glucoseData) {
        this.date = date;
        this.hba1c = hba1c;
        this.inRange=inRange;
        this.HighRange=HighRange;
        this.LowRange=LowRange;
        this.nValues=nValues;
        this.glucoseData=glucoseData;
    }

}
