package com.qralarm.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public final class Ui {
    public static final int BG = Color.rgb(10,12,17);
    public static final int SURFACE = Color.rgb(22,25,33);
    public static final int SURFACE_2 = Color.rgb(31,35,45);
    public static final int TEXT = Color.rgb(244,246,251);
    public static final int MUTED = Color.rgb(158,164,178);
    public static final int PRIMARY = Color.rgb(124,105,255);
    public static final int PRIMARY_DARK = Color.rgb(94,75,224);
    public static final int SUCCESS = Color.rgb(63,196,126);
    public static final int DANGER = Color.rgb(241,91,105);
    public static final int STROKE = Color.rgb(52,57,70);
    public static final int WARNING_BG = Color.rgb(61,47,20);
    public static final int WARNING_TEXT = Color.rgb(255,205,105);

    private Ui() {}

    public static int dp(Context c, int dp) {
        return Math.round(dp * c.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable rounded(int color, float radiusDp, Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(c, (int) radiusDp));
        return d;
    }

    public static GradientDrawable outlined(int fill, int stroke, Context c) {
        GradientDrawable d = rounded(fill, 14, c);
        d.setStroke(dp(c, 1), stroke);
        return d;
    }

    public static TextView text(Context c, String value, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        return t;
    }

    public static TextView chip(Context c, String value) {
        TextView t = text(c, value, 12, TEXT, true);
        t.setGravity(android.view.Gravity.CENTER);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.END);
        t.setPadding(dp(c,10), dp(c,7), dp(c,10), dp(c,7));
        t.setBackground(rounded(SURFACE_2, 14, c));
        return t;
    }

    public static void margin(View v, int l, int t, int r, int b) {
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        p.setMargins(l,t,r,b);
        v.setLayoutParams(p);
    }
}
