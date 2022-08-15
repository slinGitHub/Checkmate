package com.example.a20210207_checkmate2;

import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

public class ValueFormatterZeroDecimal extends ValueFormatter implements IValueFormatter {

    private DecimalFormat mFormat = new DecimalFormat("0"); // use one decimal

    public String getFormattedValue(float value) {
        return mFormat.format(value);
    }
}
