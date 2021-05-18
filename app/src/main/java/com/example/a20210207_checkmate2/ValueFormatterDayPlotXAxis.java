package com.example.a20210207_checkmate2;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Collection;

public class ValueFormatterDayPlotXAxis extends ValueFormatter {

    @Override
    public String getFormattedValue(float value) {

        //int index = Math.round(value/60);
        int index = Math.round(value/60);

        return (index + "h");
    }


}
