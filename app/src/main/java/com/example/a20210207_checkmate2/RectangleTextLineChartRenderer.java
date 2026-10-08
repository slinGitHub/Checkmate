package com.example.a20210207_checkmate2;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

public class RectangleTextLineChartRenderer extends LineChartRenderer {

    public RectangleTextLineChartRenderer(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    @Override
    public void drawValues(Canvas c) {
        if (isDrawingValuesAllowed(mChart)) {
            List<ILineDataSet> dataSets = mChart.getLineData().getDataSets();

            for (int i = 0; i < dataSets.size(); i++) {
                ILineDataSet dataSet = dataSets.get(i);
                if (!shouldDrawValues(dataSet) || dataSet.getEntryCount() < 1)
                    continue;

                applyValueTextStyle(dataSet);
                Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
                mXBounds.set(mChart, dataSet);

                float[] positions = trans.generateTransformedValuesLine(dataSet, mAnimator.getPhaseX(), mAnimator.getPhaseY(), mXBounds.min, mXBounds.max);
                ValueFormatter formatter = dataSet.getValueFormatter();

                float textHeight = dataSet.getValueTextSize();
                float boxPadding = Utils.convertDpToPixel(4f);
                
                Paint rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                rectPaint.setStyle(Paint.Style.FILL);
                
                Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setStrokeWidth(Utils.convertDpToPixel(2f));
                borderPaint.setColor(Color.WHITE);

                Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                labelPaint.setTextSize(textHeight * 0.7f);
                labelPaint.setColor(Color.WHITE);
                labelPaint.setTextAlign(Paint.Align.CENTER);
                labelPaint.setTypeface(dataSet.getValueTypeface());

                for (int j = 0; j < positions.length; j += 2) {
                    float x = positions[j];
                    float y = positions[j + 1];

                    if (!mViewPortHandler.isInBoundsRight(x))
                        break;
                    if (!mViewPortHandler.isInBoundsLeft(x) || !mViewPortHandler.isInBoundsY(y))
                        continue;

                    Entry entry = dataSet.getEntryForIndex(j / 2 + mXBounds.min);
                    String valText = formatter.getPointLabel(entry);
                    
                    // Draw Box
                    float textWidth = mValuePaint.measureText(valText);
                    float boxWidth = Math.max(textWidth + boxPadding * 4, Utils.convertDpToPixel(40f));
                    float boxHeight = textHeight + boxPadding * 2;
                    
                    RectF rect = new RectF(x - boxWidth / 2, y - boxHeight / 2, x + boxWidth / 2, y + boxHeight / 2);
                    
                    // Get color for this point if possible, otherwise use text color
                    int color = dataSet.getValueTextColor(j / 2);
                    rectPaint.setColor(color);
                    
                    c.drawRect(rect, rectPaint);
                    
                    // Draw white border if selected (color is white)
                    if (color == Color.WHITE) {
                        c.drawRect(rect, borderPaint);
                    }
                    
                    // Draw Main Value Text
                    int originalColor = mValuePaint.getColor();
                    mValuePaint.setColor(Color.BLACK); // Text inside box is usually black
                    c.drawText(valText, x, y + textHeight * 0.35f, mValuePaint);
                    mValuePaint.setColor(originalColor);

                    // Draw "Nx" Label above if present in entry data
                    Object data = entry.getData();
                    if (data != null) {
                        try {
                            String label = Math.round(Double.parseDouble(data.toString())) + "x";
                            c.drawText(label, x, y - boxHeight / 2 - boxPadding, labelPaint);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
    }
}
