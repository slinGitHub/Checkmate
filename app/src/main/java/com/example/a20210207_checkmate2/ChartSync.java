package com.example.a20210207_checkmate2;

import android.graphics.Matrix;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.ArrayList;
import java.util.List;

public class ChartSync {
    private final List<BarLineChartBase<?>> charts = new ArrayList<>();
    private final float[] src = new float[9], dst = new float[9];

    public void add(final BarLineChartBase<?> chart) {
        charts.add(chart);
        chart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture g) {
                stopOtherFlings(chart);
            }
            @Override public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture g) {
                chart.postOnAnimation(() -> syncFrom(chart));
            }
            @Override public void onChartTranslate(MotionEvent me, float dX, float dY) {
                chart.postOnAnimation(() -> syncFrom(chart));
            }
            @Override public void onChartScale(MotionEvent me, float sx, float sy) {
                chart.postOnAnimation(() -> syncFrom(chart));
            }
            @Override public void onChartLongPressed(MotionEvent me) {}
            @Override public void onChartDoubleTapped(MotionEvent me) {}
            @Override public void onChartSingleTapped(MotionEvent me) {}
            @Override public void onChartFling(MotionEvent a, MotionEvent b, float vx, float vy) {}
        });
    }

    private void stopOtherFlings(BarLineChartBase<?> leader) {
        for (BarLineChartBase<?> c : charts) {
            if (c != leader && c.getOnTouchListener() instanceof BarLineChartTouchListener) {
                ((BarLineChartTouchListener) c.getOnTouchListener()).stopDeceleration();
            }
        }
    }

    public void syncFrom(BarLineChartBase<?> s) {
        s.getViewPortHandler().getMatrixTouch().getValues(src);
        for (BarLineChartBase<?> c : charts) {
            if (c == s) continue;
            Matrix m = c.getViewPortHandler().getMatrixTouch();
            m.getValues(dst);
            if (dst[Matrix.MSCALE_X] == src[Matrix.MSCALE_X]
                    && dst[Matrix.MTRANS_X] == src[Matrix.MTRANS_X]) continue;
            dst[Matrix.MSCALE_X] = src[Matrix.MSCALE_X];
            dst[Matrix.MTRANS_X] = src[Matrix.MTRANS_X];
            m.setValues(dst);
            c.getViewPortHandler().refresh(m, c, true);
        }
    }
}
