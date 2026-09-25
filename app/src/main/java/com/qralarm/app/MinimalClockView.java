package com.qralarm.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

/** A clean clock dial: no circular background, only labels and a purple pointer. */
public class MinimalClockView extends View {
    public static final int MODE_HOUR = 0;
    public static final int MODE_MINUTE = 1;

    public interface Listener {
        void onTimeChanged(int hour, int minute, int mode);
    }

    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int hour;
    private int minute;
    private int mode = MODE_HOUR;
    private Listener listener;

    public MinimalClockView(Context context) {
        super(context);
        setFocusable(true);
        textPaint.setColor(Ui.MUTED);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        selectedPaint.setColor(Ui.TEXT);
        selectedPaint.setTextAlign(Paint.Align.CENTER);
        selectedPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        pointerPaint.setColor(Ui.PRIMARY);
        pointerPaint.setStrokeWidth(Ui.dp(context, 3));
        pointerPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setTime(int hour, int minute) {
        this.hour = normalize(hour, 24);
        this.minute = normalize(minute, 60);
        invalidate();
    }

    public int getHour() { return hour; }
    public int getMinute() { return minute; }

    public void setMode(int mode) {
        this.mode = mode == MODE_MINUTE ? MODE_MINUTE : MODE_HOUR;
        invalidate();
        notifyListener();
    }

    public int getMode() { return mode; }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desired = Ui.dp(getContext(), 300);
        int width = resolveSize(desired, widthMeasureSpec);
        int height = resolveSize(desired, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f - Ui.dp(getContext(), 22);
        if (radius <= 0) return;

        float angle = mode == MODE_HOUR ? (hour / 24f) * 360f : (minute / 60f) * 360f;
        double rad = Math.toRadians(angle - 90f);
        float pointerRadius = radius * 0.72f;
        float px = cx + (float) Math.cos(rad) * pointerRadius;
        float py = cy + (float) Math.sin(rad) * pointerRadius;
        canvas.drawLine(cx, cy, px, py, pointerPaint);

        if (mode == MODE_HOUR) drawHours(canvas, cx, cy, radius);
        else drawMinutes(canvas, cx, cy, radius);
    }

    private void drawHours(Canvas canvas, float cx, float cy, float radius) {
        float textRadius = radius * 0.90f;
        textPaint.setTextSize(sp(12.5f));
        selectedPaint.setTextSize(sp(13.5f));
        for (int i = 0; i < 24; i++) {
            double rad = Math.toRadians((i / 24f) * 360f - 90f);
            float x = cx + (float) Math.cos(rad) * textRadius;
            float y = cy + (float) Math.sin(rad) * textRadius - (textPaint.ascent() + textPaint.descent()) / 2f;
            Paint p = i == hour ? selectedPaint : textPaint;
            canvas.drawText(String.format(java.util.Locale.getDefault(), "%02d", i), x, y, p);
        }
    }

    private void drawMinutes(Canvas canvas, float cx, float cy, float radius) {
        float textRadius = radius * 0.90f;
        textPaint.setTextSize(sp(14f));
        selectedPaint.setTextSize(sp(15f));

        for (int i = 0; i < 60; i += 5) {
            if (i == minute) continue;
            double rad = Math.toRadians((i / 60f) * 360f - 90f);
            float x = cx + (float) Math.cos(rad) * textRadius;
            float y = cy + (float) Math.sin(rad) * textRadius - (textPaint.ascent() + textPaint.descent()) / 2f;
            canvas.drawText(String.format(java.util.Locale.getDefault(), "%02d", i), x, y, textPaint);
        }

        // Always render the selected minute in white, even between the 5-minute labels.
        double selectedRad = Math.toRadians((minute / 60f) * 360f - 90f);
        float sx = cx + (float) Math.cos(selectedRad) * textRadius;
        float sy = cy + (float) Math.sin(selectedRad) * textRadius - (selectedPaint.ascent() + selectedPaint.descent()) / 2f;
        canvas.drawText(String.format(java.util.Locale.getDefault(), "%02d", minute), sx, sy, selectedPaint);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                || event.getActionMasked() == MotionEvent.ACTION_MOVE
                || event.getActionMasked() == MotionEvent.ACTION_UP) {
            selectFromPoint(event.getX(), event.getY(), event.getActionMasked() == MotionEvent.ACTION_UP);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void selectFromPoint(float x, float y, boolean release) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        double degrees = Math.toDegrees(Math.atan2(y - cy, x - cx)) + 90d;
        if (degrees < 0) degrees += 360d;

        if (mode == MODE_HOUR) {
            hour = normalize((int) Math.round((degrees / 360d) * 24d), 24);
            if (release) mode = MODE_MINUTE;
        } else {
            minute = normalize((int) Math.round((degrees / 360d) * 60d), 60);
        }
        invalidate();
        notifyListener();
    }

    private void notifyListener() {
        if (listener != null) listener.onTimeChanged(hour, minute, mode);
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }

    private static int normalize(int value, int mod) {
        int v = value % mod;
        return v < 0 ? v + mod : v;
    }
}
