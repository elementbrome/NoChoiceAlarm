package com.qralarm.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import java.util.Locale;

/**
 * 24-hour radial time picker inspired by Android's classic clock picker.
 * Hours use two rings (00-11 outside, 12-23 inside). Minutes use 00-55 labels.
 */
public class MinimalClockView extends View {
    public static final int MODE_HOUR = 0;
    public static final int MODE_MINUTE = 1;

    public interface Listener {
        void onTimeChanged(int hour, int minute, int mode);
    }

    private final Paint dialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int hour;
    private int minute;
    private int mode = MODE_HOUR;
    private Listener listener;

    public MinimalClockView(Context context) {
        super(context);
        setFocusable(true);
        setClickable(true);

        dialPaint.setColor(Ui.SURFACE_2);

        textPaint.setColor(Ui.MUTED);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        innerTextPaint.setColor(0xFF8C929F);
        innerTextPaint.setTextAlign(Paint.Align.CENTER);
        innerTextPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        selectedTextPaint.setColor(Ui.TEXT);
        selectedTextPaint.setTextAlign(Paint.Align.CENTER);
        selectedTextPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));

        pointerPaint.setColor(Ui.ACCENT_START);
        pointerPaint.setStrokeWidth(Ui.dp(context, 3));
        pointerPaint.setStrokeCap(Paint.Cap.ROUND);

        bubblePaint.setColor(Ui.ACCENT_START);
        centerPaint.setColor(Ui.ACCENT_START);
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
        int desired = Ui.dp(getContext(), 302);
        int width = resolveSize(desired, widthMeasureSpec);
        int height = resolveSize(desired, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f - Ui.dp(getContext(), 10);
        if (radius <= 0) return;

        canvas.drawCircle(cx, cy, radius, dialPaint);
        if (mode == MODE_HOUR) drawHours(canvas, cx, cy, radius);
        else drawMinutes(canvas, cx, cy, radius);
    }

    private void drawHours(Canvas canvas, float cx, float cy, float radius) {
        float outerRadius = radius * 0.76f;
        float innerRadius = radius * 0.50f;
        int index = hour % 12;
        float selectedRadius = hour < 12 ? outerRadius : innerRadius;
        double selectedRad = Math.toRadians(index * 30f - 90f);
        float sx = cx + (float) Math.cos(selectedRad) * selectedRadius;
        float sy = cy + (float) Math.sin(selectedRad) * selectedRadius;

        canvas.drawLine(cx, cy, sx, sy, pointerPaint);
        canvas.drawCircle(sx, sy, Ui.dp(getContext(), hour < 12 ? 22 : 19), bubblePaint);
        canvas.drawCircle(cx, cy, Ui.dp(getContext(), 4), centerPaint);

        textPaint.setTextSize(sp(17f));
        innerTextPaint.setTextSize(sp(13f));
        selectedTextPaint.setTextSize(sp(hour < 12 ? 17f : 13f));

        for (int i = 0; i < 12; i++) {
            double rad = Math.toRadians(i * 30f - 90f);
            float x = cx + (float) Math.cos(rad) * outerRadius;
            float y = cy + (float) Math.sin(rad) * outerRadius - (textPaint.ascent() + textPaint.descent()) / 2f;
            Paint p = hour == i ? selectedTextPaint : textPaint;
            canvas.drawText(String.format(Locale.ROOT, "%02d", i), x, y, p);
        }

        for (int i = 0; i < 12; i++) {
            int value = i + 12;
            double rad = Math.toRadians(i * 30f - 90f);
            float x = cx + (float) Math.cos(rad) * innerRadius;
            float y = cy + (float) Math.sin(rad) * innerRadius - (innerTextPaint.ascent() + innerTextPaint.descent()) / 2f;
            Paint p = hour == value ? selectedTextPaint : innerTextPaint;
            canvas.drawText(String.format(Locale.ROOT, "%02d", value), x, y, p);
        }
    }

    private void drawMinutes(Canvas canvas, float cx, float cy, float radius) {
        float labelRadius = radius * 0.76f;
        double selectedRad = Math.toRadians(minute * 6f - 90f);
        float sx = cx + (float) Math.cos(selectedRad) * labelRadius;
        float sy = cy + (float) Math.sin(selectedRad) * labelRadius;

        canvas.drawLine(cx, cy, sx, sy, pointerPaint);
        canvas.drawCircle(sx, sy, Ui.dp(getContext(), 22), bubblePaint);
        canvas.drawCircle(cx, cy, Ui.dp(getContext(), 4), centerPaint);

        textPaint.setTextSize(sp(16f));
        selectedTextPaint.setTextSize(sp(16f));

        for (int value = 0; value < 60; value += 5) {
            double rad = Math.toRadians(value * 6f - 90f);
            float x = cx + (float) Math.cos(rad) * labelRadius;
            float y = cy + (float) Math.sin(rad) * labelRadius - (textPaint.ascent() + textPaint.descent()) / 2f;
            Paint p = value == minute ? selectedTextPaint : textPaint;
            canvas.drawText(String.format(Locale.ROOT, "%02d", value), x, y, p);
        }

        // If the selected minute is between the visible 5-minute labels, show it on the pointer bubble.
        if (minute % 5 != 0) {
            float y = sy - (selectedTextPaint.ascent() + selectedTextPaint.descent()) / 2f;
            canvas.drawText(String.format(Locale.ROOT, "%02d", minute), sx, y, selectedTextPaint);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP) {
            selectFromPoint(event.getX(), event.getY(), action == MotionEvent.ACTION_UP);
            if (action == MotionEvent.ACTION_UP) performClick();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }

    private void selectFromPoint(float x, float y, boolean release) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float dx = x - cx;
        float dy = y - cy;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        double degrees = Math.toDegrees(Math.atan2(dy, dx)) + 90d;
        if (degrees < 0) degrees += 360d;

        if (mode == MODE_HOUR) {
            int index = normalize((int) Math.round(degrees / 30d), 12);
            float radius = Math.min(getWidth(), getHeight()) / 2f - Ui.dp(getContext(), 10);
            // Inner ring = 12-23. Outer ring = 00-11.
            hour = distance < radius * 0.63f ? index + 12 : index;
            if (release) mode = MODE_MINUTE;
        } else {
            minute = normalize((int) Math.round(degrees / 6d), 60);
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
