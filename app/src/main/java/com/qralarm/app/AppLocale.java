package com.qralarm.app;

import android.content.Context;
import android.os.Build;

import java.util.Locale;

/** The app intentionally supports French and English only; every other system language falls back to English. */
public final class AppLocale {
    private AppLocale() {}

    public static Locale uiLocale(Context context) {
        Locale system;
        if (Build.VERSION.SDK_INT >= 24) {
            system = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            system = context.getResources().getConfiguration().locale;
        }
        return system != null && "fr".equalsIgnoreCase(system.getLanguage()) ? Locale.FRENCH : Locale.ENGLISH;
    }
}
