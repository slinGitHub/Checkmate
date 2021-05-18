package com.example.a20210207_checkmate2;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

public class CoupleChartGestureListener implements OnChartGestureListener {

    private Chart srcChart;
    private Chart[] dstCharts;

    public CoupleChartGestureListener(Chart srcChart, Chart[] dstCharts) {
        this.srcChart = srcChart;
        this.dstCharts = dstCharts;
    }

    @Override
    public void onChartGestureStart(MotionEvent E, ChartTouchListener.ChartGesture c){

    }

    @Override
    public void onChartGestureEnd(MotionEvent E, ChartTouchListener.ChartGesture c){

    }

    @Override
    public void onChartLongPressed(MotionEvent E){

    }

    @Override
    public void onChartDoubleTapped(MotionEvent E){

    }

    @Override
    public void onChartSingleTapped(MotionEvent E){

    }

    @Override
    public void onChartFling(MotionEvent E,MotionEvent B, float x, float y){

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        //Log.d(TAG, "onChartScale " + scaleX + "/" + scaleY + " X=" + me.getX() + "Y=" + me.getY());
        syncCharts();
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        //Log.d(TAG, "onChartTranslate " + dX + "/" + dY + " X=" + me.getX() + "Y=" + me.getY());
        syncCharts();
    }

    public void syncCharts() {
        Matrix srcMatrix;
        float[] srcVals = new float[9];
        Matrix dstMatrix;
        float[] dstVals = new float[9];

        // get src chart translation matrix:
        srcMatrix = srcChart.getViewPortHandler().getMatrixTouch();
        srcMatrix.getValues(srcVals);

        // apply X axis scaling and position to dst charts:
        for (Chart dstChart : dstCharts) {
            if (dstChart.getVisibility() == View.VISIBLE) {
                dstMatrix = dstChart.getViewPortHandler().getMatrixTouch();
                dstMatrix.getValues(dstVals);
                dstVals[Matrix.MSCALE_X] = srcVals[Matrix.MSCALE_X];
                dstVals[Matrix.MTRANS_X] = srcVals[Matrix.MTRANS_X];
                dstMatrix.setValues(dstVals);
                dstChart.getViewPortHandler().refresh(dstMatrix, dstChart, true);
            }
        }
    }

}
