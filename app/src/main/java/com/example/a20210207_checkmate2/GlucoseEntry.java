package com.example.a20210207_checkmate2;

import java.io.Serializable;
import java.util.Date;

public class GlucoseEntry implements Serializable {
    String dateString;
    Date date;
    int sgv;
    String direction;
    String sensor;

    GlucoseEntry(String dateString, Date date, int sgv, String direction, String sensor){
        this.dateString = dateString;
        this.date = date;
        this.sgv = sgv;
        this.direction = direction;
        this.sensor = sensor;
    }

}

