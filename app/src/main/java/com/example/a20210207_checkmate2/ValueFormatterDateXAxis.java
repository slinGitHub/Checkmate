package com.example.a20210207_checkmate2;

import android.graphics.Canvas;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class ValueFormatterDateXAxis extends XAxisRenderer {
    private int xAxisTextColor = android.graphics.Color.BLACK;
    private int colorControlNormal = android.graphics.Color.BLACK;
    private int daySelected = -1;
    private boolean useDaySelected = false;

    public ValueFormatterDateXAxis(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans) {
        super(viewPortHandler, xAxis, trans);
    }

    public ValueFormatterDateXAxis(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans, int xAxisTextColor, int colorControlNormal, int daySelected, boolean useDaySelected) {
        super(viewPortHandler, xAxis, trans);
        this.xAxisTextColor = xAxisTextColor;
        this.colorControlNormal = colorControlNormal;
        this.daySelected = daySelected;
        this.useDaySelected = useDaySelected;
    }

    @Override
    public void drawLabels(Canvas c, float pos, MPPointF anchor) {
        final float labelRotationAngle = mXAxis.getLabelRotationAngle();

        float[] positions = new float[mXAxis.mEntries.length * 2];

        for (int i = 0; i < positions.length; i += 2) {
            positions[i] = mXAxis.mEntries[i / 2];
        }

        mTrans.pointValuesToPixel(positions);

        for (int i = 0; i < positions.length; i += 2) {
            float x = positions[i];

            if (mViewPortHandler.isInBoundsLeft(x)) {
                if (mViewPortHandler.isInBoundsRight(x)) {
                    int j = i / 2;
                    if (j < mXAxis.mEntries.length) {
                        String label = mXAxis.getValueFormatter().getFormattedValue(mXAxis.mEntries[j], mXAxis);

                        boolean isSelected = useDaySelected && Math.round(mXAxis.mEntries[j]) == daySelected;
                        if (isSelected) {
                            mAxisLabelPaint.setColor(colorControlNormal);
                        } else {
                            mAxisLabelPaint.setColor(xAxisTextColor);
                        }

                        drawLabel(c, label, x, pos, anchor, labelRotationAngle);
                    }
                }
            }
        }
    }

    @Override
    protected void drawLabel(Canvas c, String formattedLabel, float x, float y, MPPointF anchor, float angleDegrees) {
        String line[] = formattedLabel.split("\n");
        if (line.length > 0 && !line[0].isEmpty()) {
            if (mXAxis.getPosition() == XAxis.XAxisPosition.TOP || mXAxis.getPosition() == XAxis.XAxisPosition.TOP_INSIDE) {
                Utils.drawXAxisValue(c, line[0], x, y, mAxisLabelPaint, anchor, angleDegrees);
                if (line.length > 1) {
                    Utils.drawXAxisValue(c, line[1], x, y + mAxisLabelPaint.getTextSize(), mAxisLabelPaint, anchor, angleDegrees);
                }
            } else {
                Utils.drawXAxisValue(c, line[0], x, y - 50, mAxisLabelPaint, anchor, angleDegrees);
                if (line.length > 1) {
                    Utils.drawXAxisValue(c, line[1], x, y - 50 + mAxisLabelPaint.getTextSize(), mAxisLabelPaint, anchor, angleDegrees);
                }
            }
        }
    }
}
