package com.example.a20210207_checkmate2;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class ValueFormatterDayPlotYAxis extends ValueFormatter {

    @Override
    public String getFormattedValue(float value) {

        //int index = Math.round(value/60);
        double index = Math.round((double) value*0.0555);

        return (String.valueOf(index));
    }

}
