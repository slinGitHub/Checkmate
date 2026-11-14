package com.example.a20210207_checkmate2;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.model.GradientColor;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class CustomBarChartRenderer extends BarChartRenderer {

    private final RectF mBarShadowRectBufferLocal = new RectF();

    public CustomBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    @Override
    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mBarBorderPaint.setColor(dataSet.getBarBorderColor());
        mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));

        final boolean drawBorder = dataSet.getBarBorderWidth() > 0.f;

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        // draw the bar shadow before the values
        if (mChart.isDrawBarShadowEnabled()) {
            mShadowPaint.setColor(dataSet.getBarShadowColor());

            BarData barData = mChart.getBarData();

            final float barWidth = barData.getBarWidth();
            final float barWidthHalf = barWidth / 2.0f;

            for (int i = 0, count = Math.min((int)(Math.ceil((float)(dataSet.getEntryCount()) * phaseX)), dataSet.getEntryCount());
                 i < count;
                 i++) {

                BarEntry e = dataSet.getEntryForIndex(i);

                float x = e.getX();

                mBarShadowRectBufferLocal.left = x - barWidthHalf;
                mBarShadowRectBufferLocal.right = x + barWidthHalf;

                trans.rectValueToPixel(mBarShadowRectBufferLocal);

                if (!mViewPortHandler.isInBoundsLeft(mBarShadowRectBufferLocal.right))
                    continue;

                if (!mViewPortHandler.isInBoundsRight(mBarShadowRectBufferLocal.left))
                    break;

                mBarShadowRectBufferLocal.top = mViewPortHandler.contentTop();
                mBarShadowRectBufferLocal.bottom = mViewPortHandler.contentBottom();

                float cornerRadius = Utils.convertDpToPixel(6f);
                c.drawRoundRect(mBarShadowRectBufferLocal, cornerRadius, cornerRadius, mShadowPaint);
            }
        }

        // initialize the buffer
        BarBuffer buffer = mBarBuffers[index];
        buffer.setPhases(phaseX, phaseY);
        buffer.setDataSet(index);
        buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
        buffer.setBarWidth(mChart.getBarData().getBarWidth());

        buffer.feed(dataSet);

        trans.pointValuesToPixel(buffer.buffer);

        // Gestapelte DataSets (Hintergrund) als ein durchgehender Balken zeichnen
        if (dataSet.isStacked()) {
            drawMergedStacks(c, buffer, dataSet, drawBorder);
            return;
        }

        final boolean isSingleColor = dataSet.getColors().size() == 1;

        if (isSingleColor) {
            mRenderPaint.setColor(dataSet.getColor());
        }

        for (int j = 0; j < buffer.size(); j += 4) {

            if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
                continue;

            if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                break;

            if (!isSingleColor) {
                // Set the color for the currently drawn value. If the index
                // is out of bounds, reuse colors.
                mRenderPaint.setColor(dataSet.getColor(j / 4));
            }

            if (dataSet.getGradientColor() != null) {
                GradientColor gradientColor = dataSet.getGradientColor();
                mRenderPaint.setShader(
                        new LinearGradient(
                                buffer.buffer[j],
                                buffer.buffer[j + 3],
                                buffer.buffer[j],
                                buffer.buffer[j + 1],
                                gradientColor.getStartColor(),
                                gradientColor.getEndColor(),
                                android.graphics.Shader.TileMode.MIRROR));
            }

            if (dataSet.getGradientColors() != null) {
                mRenderPaint.setShader(
                        new LinearGradient(
                                buffer.buffer[j],
                                buffer.buffer[j + 3],
                                buffer.buffer[j],
                                buffer.buffer[j + 1],
                                dataSet.getGradientColor(j / 4).getStartColor(),
                                dataSet.getGradientColor(j / 4).getEndColor(),
                                android.graphics.Shader.TileMode.MIRROR));
            }

            float cornerRadius = Utils.convertDpToPixel(6f);

            c.drawRoundRect(
                    buffer.buffer[j],
                    buffer.buffer[j + 1],
                    buffer.buffer[j + 2],
                    buffer.buffer[j + 3],
                    cornerRadius,
                    cornerRadius,
                    mRenderPaint
            );

            if (drawBorder) {
                c.drawRoundRect(
                        buffer.buffer[j],
                        buffer.buffer[j + 1],
                        buffer.buffer[j + 2],
                        buffer.buffer[j + 3],
                        cornerRadius,
                        cornerRadius,
                        mBarBorderPaint
                );
            }
        }
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

    private void drawMergedStacks(Canvas c, BarBuffer buffer, IBarDataSet dataSet, boolean drawBorder) {
        float cornerRadius = Utils.convertDpToPixel(6f);

        mRenderPaint.setShader(null);
        mRenderPaint.setColor(dataSet.getColor(0));

        int entryCount = Math.min(
                (int) Math.ceil(dataSet.getEntryCount() * mAnimator.getPhaseX()),
                dataSet.getEntryCount());

        int bufferIndex = 0;

        for (int i = 0; i < entryCount && bufferIndex < buffer.size(); i++) {
            BarEntry e = dataSet.getEntryForIndex(i);
            float[] vals = e.getYVals();
            int segments = (vals == null) ? 1 : vals.length;
            int end = bufferIndex + segments * 4;

            // Links/rechts sind für alle Segmente eines Entrys identisch
            float left = buffer.buffer[bufferIndex];
            float right = buffer.buffer[bufferIndex + 2];

            // Oberste und unterste Pixelkante über alle Segmente
            float top = Float.MAX_VALUE;
            float bottom = -Float.MAX_VALUE;
            for (int k = bufferIndex; k < end; k += 4) {
                top = Math.min(top, Math.min(buffer.buffer[k + 1], buffer.buffer[k + 3]));
                bottom = Math.max(bottom, Math.max(buffer.buffer[k + 1], buffer.buffer[k + 3]));
            }
            bufferIndex = end;

            if (!mViewPortHandler.isInBoundsLeft(right))
                continue;
            if (!mViewPortHandler.isInBoundsRight(left))
                break;

            c.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, mRenderPaint);

            if (drawBorder) {
                c.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, mBarBorderPaint);
            }
        }
    }
}
