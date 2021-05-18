package com.example.a20210207_checkmate2;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class ValueFormatterPercentage extends ValueFormatter {

    private DecimalFormat mFormat;

    public ValueFormatterPercentage() {
        mFormat = new DecimalFormat("#"); // use no decimal
    }

    @Override
    public String getFormattedValue(float value) {
        // write your logic here
        return mFormat.format(value) + "%"; // e.g. append a dollar-sign
    }
}
