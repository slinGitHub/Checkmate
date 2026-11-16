package com.example.a20210207_checkmate2;

import android.content.Context;
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
import com.google.android.material.color.MaterialColors;

import java.util.List;

public class RectangleTextLineChartRenderer extends LineChartRenderer {

    private final Context context;

    public RectangleTextLineChartRenderer(Context context, LineDataProvider chart,
                                          ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
        this.context = context;
    }

    @Override
    public void drawValues(Canvas c) {
        if (isDrawingValuesAllowed(mChart)) {
            List<ILineDataSet> dataSets = mChart.getLineData().getDataSets();

            // Themefarbe für die "Nx"-Anzeige über der Blase (einmal pro Zeichnen auflösen)
            int eventLabelColor = MaterialColors.getColor(context, R.attr.colorXAxisText, Color.BLACK);

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
                float cornerRadius = Utils.convertDpToPixel(6f);          // Außenradius
                float innerOffset = Utils.convertDpToPixel(3f);           // Rahmenstärke (3f)
                float innerRadius = Math.max(0f, cornerRadius - innerOffset);

                Paint rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                rectPaint.setStyle(Paint.Style.FILL);

                Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                labelPaint.setTextSize(Utils.convertDpToPixel(15f));
                labelPaint.setColor(eventLabelColor);
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
                    int valueTextColor = dataSet.getValueTextColor(dataIndex);
                    Entry entry = dataSet.getEntryForIndex(dataIndex);
                    if (entry == null) continue;
                    String valText = formatter.getPointLabel(entry);

                    // Box-Größe
                    float textWidth = mValuePaint.measureText(valText);
                    float boxWidth = Math.max(textWidth + boxPadding * 3, Utils.convertDpToPixel(38f));
                    float boxHeight = textHeight + boxPadding * 1.8f;

                    RectF rect = new RectF(x - boxWidth / 2, y - boxHeight / 2, x + boxWidth / 2, y + boxHeight / 2);

                    // Daten extrahieren: Events [0] und Farbe [1]
                    Object entryData = entry.getData();
                    int color;
                    int events = 0;

                    if (entryData instanceof int[]) {
                        int[] d = (int[]) entryData;
                        events = d[0];
                        color = d[1];
                    } else {
                        color = dataSet.getValueTextColor(dataIndex);
                    }

                    if ("BubbleSel".equals(dataSetLabel)) {
                        // Rahmen (Außenbox) mit dem dunklen Textfarbton (valueTextColor)
                        rectPaint.setColor(valueTextColor);
                        c.drawRoundRect(rect, cornerRadius, cornerRadius, rectPaint);

                        // Farbige Innenbox
                        rectPaint.setColor(color);
                        RectF innerRect = new RectF(rect.left + innerOffset, rect.top + innerOffset,
                                rect.right - innerOffset, rect.bottom - innerOffset);
                        c.drawRoundRect(innerRect, innerRadius, innerRadius, rectPaint);

                        mValuePaint.setColor(valueTextColor);
                        c.drawText(valText, x, y + textHeight * 0.35f, mValuePaint);

                    } else if ("Text".equals(dataSetLabel) || "TextAvg".equals(dataSetLabel)) {
                        // Normale farbige Box
                        rectPaint.setColor(color);
                        c.drawRoundRect(rect, cornerRadius, cornerRadius, rectPaint);

                        mValuePaint.setColor(valueTextColor);
                        c.drawText(valText, x, y + textHeight * 0.35f, mValuePaint);

                        if (events > 0) {
                            String nText = events + "x";
                            c.drawText(nText, x, y - boxHeight * 3 / 4 - boxPadding / 2, labelPaint);
                        }
                    }
                }
            }
        }
    }
}