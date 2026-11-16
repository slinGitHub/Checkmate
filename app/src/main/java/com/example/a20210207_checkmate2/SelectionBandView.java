package com.example.a20210207_checkmate2;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.graphics.ColorUtils;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.YAxis;
import com.google.android.material.color.MaterialColors;

public class SelectionBandView extends View {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float radius, padX;
    private BarLineChartBase<?> ref;           // Referenz-Chart (GMI)
    private float x = Float.NaN, xStep = 1f;   // Datenraum, Tage = Index im Abstand 1
    private ValueAnimator anim;

    public SelectionBandView(Context c, AttributeSet a) {
        super(c, a);
        float d = getResources().getDisplayMetrics().density;
        radius = 12 * d; padX = 1 * d;
        int base = MaterialColors.getColor(c, R.attr.colorBackgroundCheckmateAvg, Color.DKGRAY);
        fill.setColor(ColorUtils.setAlphaComponent(base, 255));     // ~14 %
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(d);
        stroke.setColor(ColorUtils.setAlphaComponent(base, 80));
    }

    public void attach(BarLineChartBase<?> reference) { ref = reference; }

    public void select(float newX, boolean animate) {
        if (anim != null) anim.cancel();
        if (!animate || Float.isNaN(x)) { x = newX; invalidate(); return; }
        anim = ValueAnimator.ofFloat(x, newX);
        anim.setDuration(250);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.addUpdateListener(a -> { x = (float) a.getAnimatedValue(); invalidate(); });
        anim.start();
    }

    @Override protected void onDraw(Canvas c) {
        if (ref == null || Float.isNaN(x) || ref.getViewPortHandler().contentWidth() <= 0) return;
        float[] p = { x - xStep / 2f, 0f, x + xStep / 2f, 0f };
        ref.getTransformer(YAxis.AxisDependency.LEFT).pointValuesToPixel(p);  // Datenraum -> Chart-Pixel
        float off = ref.getX() - getX();                                       // Chart-Koordinaten -> View
        rect.set(p[0] + off + padX, 0, p[2] + off - padX, getHeight());
        c.drawRoundRect(rect, radius, radius, fill);
//        c.drawRoundRect(rect, radius, radius, stroke);
    }
}
