package com.example.a20210207_checkmate2;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class CustomBarChartRenderer extends BarChartRenderer {

    public CustomBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    @Override
    public void drawValue(Canvas c, String valueText, float x, float y, int color) {
        if (valueText == null || !valueText.contains("%")) {
            super.drawValue(c, valueText, x, y, color);
            return;
        }

        float textHeight = mValuePaint.getTextSize();

        Paint numberPaint = new Paint(mValuePaint);
        numberPaint.setColor(color);

        Paint percentPaint = new Paint(mValuePaint);
        percentPaint.setColor(color);
        percentPaint.setTextSize(textHeight * 0.5f); // Half font size for %

        String numberPart = valueText.replace("%", "").trim();
        String percentPart = "%";

        float numWidth = numberPaint.measureText(numberPart);
        float percentWidth = percentPaint.measureText(percentPart);
        float totalWidth = numWidth + percentWidth;

        c.drawText(numberPart, x - totalWidth / 2 + numWidth / 2, y, numberPaint);
        c.drawText(percentPart, x - totalWidth / 2 + numWidth + percentWidth / 2, y, percentPaint);
    }
}
