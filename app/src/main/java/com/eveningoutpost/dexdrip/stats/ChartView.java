package com.eveningoutpost.dexdrip.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Range chart view displaying in/above/below range percentages as a stacked bar.
 */
public class ChartView extends View {

    private int inRange = 0;
    private int aboveRange = 0;
    private int belowRange = 0;
    private boolean calculating = false;

    public ChartView(Context context) {
        super(context);
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRangeData(int inRange, int aboveRange, int belowRange) {
        this.inRange = inRange;
        this.aboveRange = aboveRange;
        this.belowRange = belowRange;
        calculating = false;
        postInvalidate();
    }

    public void setCalculating() {
        calculating = true;
        postInvalidate();
    }

    private int dp2px(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) (dp * (metrics.densityDpi / 160f));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (calculating) {
            Paint myPaint = new Paint();
            myPaint.setColor(Color.WHITE);
            myPaint.setAntiAlias(true);
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setTextSize(dp2px(15));
            canvas.drawText("Calculating...", dp2px(30), canvas.getHeight() / 2, myPaint);
            return;
        }

        int total = aboveRange + belowRange + inRange;
        if (total == 0) {
            Paint myPaint = new Paint();
            myPaint.setColor(Color.WHITE);
            myPaint.setAntiAlias(true);
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setTextSize(dp2px(15));
            canvas.drawText("Not enough data!", dp2px(30), canvas.getHeight() / 2, myPaint);
            return;
        }

        // Calculate bar dimensions
        int barWidth = dp2px(160);
        int barHeight = Math.min(canvas.getHeight() - dp2px(40), (canvas.getWidth() / 3) * 2);
        int left = (canvas.getWidth() - barWidth) / 2;
        int top = (canvas.getHeight() - barHeight) / 2;
        int right = left + barWidth;
        int bottom = top + barHeight;

        // Calculate percentages and heights
        float belowPct = (float) belowRange / total;
        float inPct = (float) inRange / total;
        float abovePct = (float) aboveRange / total;
        int belowHeight = Math.round(barHeight * belowPct);
        int inHeight = Math.round(barHeight * inPct);
        int aboveHeight = barHeight - belowHeight - inHeight;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        float radius = 5f;

        // Draw below range (red, bottom, rounded bottom)
        RectF belowRect = new RectF(left, bottom - belowHeight, right, bottom);
        if (belowHeight > 0) {
            paint.setColor(Color.RED);
            if (belowHeight < radius) {
                canvas.drawRoundRect(belowRect, radius, radius, paint);
            } else {
                canvas.save();
                canvas.clipRect(left, bottom - belowHeight, right, bottom - radius);
                canvas.drawRect(belowRect, paint);
                canvas.restore();
                RectF roundRect = new RectF(left, bottom - radius * 2, right, bottom);
                canvas.drawRoundRect(roundRect, radius, radius, paint);
            }
        }

        // Draw in range (green, middle)
        if (inHeight > 0) {
            paint.setColor(Color.GREEN);
            canvas.drawRect(left, bottom - belowHeight - inHeight, right, bottom - belowHeight, paint);
        }

        // Draw above range (yellow, top, rounded top)
        RectF aboveRect = new RectF(left, top, right, top + aboveHeight);
        if (aboveHeight > 0) {
            paint.setColor(Color.YELLOW);
            if (aboveHeight < radius) {
                canvas.drawRoundRect(aboveRect, radius, radius, paint);
            } else {
                canvas.save();
                canvas.clipRect(left, top + radius, right, top + aboveHeight);
                canvas.drawRect(aboveRect, paint);
                canvas.restore();
                RectF roundRect = new RectF(left, top, right, top + radius * 2);
                canvas.drawRoundRect(roundRect, radius, radius, paint);
            }
        }

        // Draw percentage labels
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(dp2px(22));
        textPaint.setTextAlign(Paint.Align.LEFT);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float labelX = right + dp2px(12);

        if (belowHeight > 0) {
            String pct = String.format("%.0f%%", belowPct * 100);
            float y = bottom - belowHeight / 2f - (fm.ascent + fm.descent) / 2;
            canvas.drawText(pct, labelX, y, textPaint);
        }
        if (inHeight > 0) {
            String pct = String.format("%.0f%%", inPct * 100);
            float y = bottom - belowHeight - inHeight / 2f - (fm.ascent + fm.descent) / 2;
            canvas.drawText(pct, labelX, y, textPaint);
        }
        if (aboveHeight > 0) {
            String pct = String.format("%.0f%%", abovePct * 100);
            float y = top + aboveHeight / 2f - (fm.ascent + fm.descent) / 2;
            canvas.drawText(pct, labelX, y, textPaint);
        }
    }
}

