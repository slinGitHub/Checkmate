package com.example.a20210207_checkmate2;
import android.graphics.Canvas;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class EdgeAlignedXAxisRenderer extends XAxisRenderer {

    public EdgeAlignedXAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans) {
        super(viewPortHandler, xAxis, trans);
    }

    @Override
    protected void drawLabel(Canvas c, String formattedLabel, float x, float y,
                             MPPointF anchor, float angleDegrees) {
        float halfWidth = Utils.calcTextWidth(mAxisLabelPaint, formattedLabel) / 2f;
        float left = mViewPortHandler.contentLeft();
        float right = mViewPortHandler.contentRight();

        // Label (zentriert um x) in den Content-Bereich klemmen
        if (x - halfWidth < left) {
            x = left + halfWidth;          // "0h" bündig links
        } else if (x + halfWidth > right) {
            x = right - halfWidth;         // "24h" bündig rechts
        }
        super.drawLabel(c, formattedLabel, x, y, anchor, angleDegrees);
    }
}