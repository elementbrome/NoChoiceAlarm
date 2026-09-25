package com.qralarm.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public final class Ui {
    public static final int BG = Color.rgb(246,247,251);
    public static final int SURFACE = Color.WHITE;
    public static final int TEXT = Color.rgb(21,24,33);
    public static final int MUTED = Color.rgb(111,116,128);
    public static final int PRIMARY = Color.rgb(91,76,240);
    public static final int PRIMARY_DARK = Color.rgb(70,56,206);
    public static final int DANGER = Color.rgb(211,58,74);

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

    public static void margin(View v, int l, int t, int r, int b) {
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        p.setMargins(l,t,r,b);
        v.setLayoutParams(p);
    }
}
