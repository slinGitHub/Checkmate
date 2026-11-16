package com.example.a20210207_checkmate2;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

import java.util.ArrayList;
import java.util.List;

public class CenteredTextLineChartRenderer extends LineChartRenderer {

    private ArrayList<Integer> mColors;

    public CenteredTextLineChartRenderer(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    public CenteredTextLineChartRenderer(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler, ArrayList<Integer> colors) {
        super(chart, animator, viewPortHandler);
        this.mColors = colors;
    }

    @Override
    protected void drawDataSet(Canvas c, ILineDataSet dataSet) {
        if (dataSet.getLabel() != null && dataSet.getLabel().equals("Line") && mColors != null && !mColors.isEmpty()) {
            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
            mXBounds.set(mChart, dataSet);

            float[] positions = trans.generateTransformedValuesLine(dataSet, mAnimator.getPhaseX(), mAnimator.getPhaseY(), mXBounds.min, mXBounds.max);

            mRenderPaint.setStyle(Paint.Style.STROKE);
            mRenderPaint.setStrokeWidth(dataSet.getLineWidth());
            mRenderPaint.setPathEffect(dataSet.getDashPathEffect());

            for (int j = 0; j < positions.length - 2; j += 2) {
                float x1 = positions[j];
                float y1 = positions[j + 1];
                float x2 = positions[j + 2];
                float y2 = positions[j + 3];

                if (!mViewPortHandler.isInBoundsLeft(x2) && !mViewPortHandler.isInBoundsRight(x1))
                    continue;

                int entryIndex1 = j / 2 + mXBounds.min;
                int entryIndex2 = entryIndex1 + 1;

                if (entryIndex1 < 0 || entryIndex2 >= mColors.size())
                    continue;

                int color1 = mColors.get(entryIndex1);
                int color2 = mColors.get(entryIndex2);

                float xMid = (x1 + x2) / 2f;
                float yMid = (y1 + y2) / 2f;

                // Draw first half with color1
                mRenderPaint.setColor(color1);
                c.drawLine(x1, y1, xMid, yMid, mRenderPaint);

                // Draw second half with color2
                mRenderPaint.setColor(color2);
                c.drawLine(xMid, yMid, x2, y2, mRenderPaint);
            }

            if (dataSet.isDrawCirclesEnabled()) {
                // drawCircles doesn't need dataSet parameter or handled by bubble dataset
            }
        } else {
            super.drawDataSet(c, dataSet);
        }
    }

    //Modified drawValues Method
    // Center label on coordinate instead of applying a valOffset

    @Override
    public void drawValues(Canvas c) {

        if (isDrawingValuesAllowed(mChart)) {

            List<ILineDataSet> dataSets = mChart.getLineData().getDataSets();

            for (int i = 0; i < dataSets.size(); i++) {

                ILineDataSet dataSet = dataSets.get(i);

                float textHeight = dataSet.getValueTextSize();

                if (!shouldDrawValues(dataSet) || dataSet.getEntryCount() < 1)
                    continue;

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet);

                Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

                // make sure the values do not interfear with the circles
                int valOffset = (int) (dataSet.getCircleRadius() * 1.75f);

                if (!dataSet.isDrawCirclesEnabled())
                    valOffset = valOffset / 2;

                mXBounds.set(mChart, dataSet);

                float[] positions = trans.generateTransformedValuesLine(dataSet, mAnimator.getPhaseX(), mAnimator
                        .getPhaseY(), mXBounds.min, mXBounds.max);
                ValueFormatter formatter = dataSet.getValueFormatter();

                MPPointF iconsOffset = MPPointF.getInstance(dataSet.getIconsOffset());
                iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x);
                iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y);

                for (int j = 0; j < positions.length; j += 2) {

                    float x = positions[j];
                    float y = positions[j + 1];

                    if (!mViewPortHandler.isInBoundsRight(x))
                        break;

                    if (!mViewPortHandler.isInBoundsLeft(x) || !mViewPortHandler.isInBoundsY(y))
                        continue;

                    Entry entry = dataSet.getEntryForIndex(j / 2 + mXBounds.min);

                    if (dataSet.isDrawValuesEnabled()) {
                        //drawValue(c, formatter.getPointLabel(entry), x, y - valOffset, dataSet.getValueTextColor(j / 2));
                        drawValue(c, formatter.getPointLabel(entry), x, y+textHeight*0.35f, dataSet.getValueTextColor(j / 2 + mXBounds.min));
                    }

                    if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {

                        Drawable icon = entry.getIcon();

                        Utils.drawImage(
                                c,
                                icon,
                                (int)(x + iconsOffset.x),
                                (int)(y + iconsOffset.y),
                                icon.getIntrinsicWidth(),
                                icon.getIntrinsicHeight());
                    }
                }

                MPPointF.recycleInstance(iconsOffset);
            }
        }
    }
}
