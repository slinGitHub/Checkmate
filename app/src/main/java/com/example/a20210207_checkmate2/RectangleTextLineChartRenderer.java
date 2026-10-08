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

                Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                labelPaint.setTextSize(textHeight * 0.65f);
                labelPaint.setColor(Color.WHITE);
                labelPaint.setTextAlign(Paint.Align.CENTER);
                labelPaint.setTypeface(dataSet.getValueTypeface());

                String dataSetLabel = dataSet.getLabel();

                for (int j = 0; j < positions.length; j += 2) {
                    float x = positions[j];
                    float y = positions[j + 1];

                    if (!mViewPortHandler.isInBoundsRight(x))
                        break;
                    if (!mViewPortHandler.isInBoundsLeft(x) || !mViewPortHandler.isInBoundsY(y))
                        continue;

                    int dataIndex = j / 2 + mXBounds.min;
                    Entry entry = dataSet.getEntryForIndex(dataIndex);
                    String valText = formatter.getPointLabel(entry);
                    
                    // Box-Größe basierend auf Text berechnen
                    float textWidth = mValuePaint.measureText(valText);
                    float boxWidth = Math.max(textWidth + boxPadding * 3, Utils.convertDpToPixel(38f));
                    float boxHeight = textHeight + boxPadding * 1.8f;
                    
                    RectF rect = new RectF(x - boxWidth / 2, y - boxHeight / 2, x + boxWidth / 2, y + boxHeight / 2);
                    
                    // WICHTIG: Korrekten Farb-Index verwenden (absolut statt relativ zum Viewport)
                    int color = dataSet.getValueTextColor(dataIndex);
                    
                    if ("BubbleSel".equals(dataSetLabel)) {
                        // Zeichne den inneren farbigen Teil der Selektion
                        rectPaint.setColor(color);
                        float innerOffset = Utils.convertDpToPixel(2f);
                        RectF innerRect = new RectF(rect.left + innerOffset, rect.top + innerOffset, 
                                                   rect.right - innerOffset, rect.bottom - innerOffset);
                        c.drawRect(innerRect, rectPaint);
                        
                        // Text innerhalb der Box
                        int originalColor = mValuePaint.getColor();
                        mValuePaint.setColor(Color.BLACK);
                        c.drawText(valText, x, y + textHeight * 0.35f, mValuePaint);
                        mValuePaint.setColor(originalColor);
                    } else if ("Text".equals(dataSetLabel) || "TextAvg".equals(dataSetLabel)) {
                        // Normale Box zeichnen (farbig oder weiß bei Selektion im Text Dataset)
                        rectPaint.setColor(color);
                        c.drawRect(rect, rectPaint);
                        
                        // Wert-Text innerhalb der Box
                        int originalColor = mValuePaint.getColor();
                        mValuePaint.setColor(Color.BLACK);
                        c.drawText(valText, x, y + textHeight * 0.35f, mValuePaint);
                        mValuePaint.setColor(originalColor);
                        
                        // "Nx" Label (Events) über der Box positionieren
                        Object data = entry.getData();
                        if (data != null) {
                            try {
                                String nText = Math.round(Double.parseDouble(data.toString())) + "x";
                                c.drawText(nText, x, y - boxHeight / 2 - boxPadding / 2, labelPaint);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
        }
    }
}
