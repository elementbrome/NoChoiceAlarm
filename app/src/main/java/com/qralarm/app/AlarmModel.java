package com.qralarm.app;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class AlarmModel {
    public static final int MODE_QR = 1;
    public static final int MODE_MATH = 2;

    public static final String MATH_EASY = "easy";
    public static final String MATH_HARD = "hard";
    public static final String MATH_BRUTAL = "brutal";

    public long id;
    public int hour;
    public int minute;
    public String label = "";
    public int repeatMask = 0;
    public String soundUri = "";
    public String soundName = "System alarm";
    public int snoozeMinutes = 0;
    public boolean enabled = true;

    public int wakeModeMask = MODE_QR;
    public String mathDifficulty = MATH_EASY;
    public boolean useGlobalQr = true;
    public String customQrToken = "";
    public int volumePercent = 100;
    public int volumeRampSeconds = 0;
    public boolean vibrationEnabled = false;

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("hour", hour);
        o.put("minute", minute);
        o.put("label", label == null ? "" : label);
        o.put("repeatMask", repeatMask);
        o.put("soundUri", soundUri == null ? "" : soundUri);
        o.put("soundName", soundName == null || soundName.trim().isEmpty() ? "System alarm" : soundName);
        o.put("snoozeMinutes", snoozeMinutes);
        o.put("enabled", enabled);
        o.put("wakeModeMask", wakeModeMask);
        o.put("mathDifficulty", mathDifficulty == null ? MATH_EASY : mathDifficulty);
        o.put("useGlobalQr", useGlobalQr);
        o.put("customQrToken", customQrToken == null ? "" : customQrToken);
        o.put("volumePercent", volumePercent);
        o.put("volumeRampSeconds", volumeRampSeconds);
        o.put("vibrationEnabled", vibrationEnabled);
        return o;
    }

    public static AlarmModel fromJson(JSONObject o) {
        AlarmModel a = new AlarmModel();
        a.id = o.optLong("id", System.currentTimeMillis());
        a.hour = o.optInt("hour", 7);
        a.minute = o.optInt("minute", 0);
        a.label = o.optString("label", "");
        a.repeatMask = o.optInt("repeatMask", 0);
        a.soundUri = o.optString("soundUri", "");
        a.soundName = o.optString("soundName", a.soundUri.isEmpty() ? "System alarm" : "Custom sound");
        a.snoozeMinutes = o.optInt("snoozeMinutes", 0);
        a.enabled = o.optBoolean("enabled", true);
        a.wakeModeMask = o.optInt("wakeModeMask", MODE_QR);
        if ((a.wakeModeMask & (MODE_QR | MODE_MATH)) == 0) a.wakeModeMask = MODE_QR;
        a.mathDifficulty = o.optString("mathDifficulty", MATH_EASY);
        a.useGlobalQr = o.optBoolean("useGlobalQr", true);
        a.customQrToken = o.optString("customQrToken", o.optString("qrToken", ""));
        a.volumePercent = clamp(o.optInt("volumePercent", 100), 0, 100);
        a.volumeRampSeconds = normalizeRamp(o.optInt("volumeRampSeconds", 0));
        a.vibrationEnabled = o.optBoolean("vibrationEnabled", false);
        return a;
    }

    public static AlarmModel fromJsonString(String raw) {
        try { return fromJson(new JSONObject(raw)); }
        catch (Exception e) { return null; }
    }

    public String toJsonString() {
        try { return toJson().toString(); }
        catch (Exception e) { return ""; }
    }

    public String timeText() {
        return String.format(Locale.ROOT, "%02d:%02d", hour, minute);
    }

    public String repeatText(Context context) {
        if (repeatMask == 0) return context.getString(R.string.repeat_once);
        if (repeatMask == 0b1111111) return context.getString(R.string.repeat_every_day);
        String[] names = context.getResources().getStringArray(R.array.repeat_day_short);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if ((repeatMask & (1 << i)) != 0) {
                if (sb.length() > 0) sb.append(" · ");
                sb.append(names[i]);
            }
        }
        return sb.toString();
    }

    public boolean usesQr() { return (wakeModeMask & MODE_QR) != 0; }
    public boolean usesMath() { return (wakeModeMask & MODE_MATH) != 0; }

    public String modeChipText(Context context) {
        if (usesQr() && usesMath()) return context.getString(R.string.mode_qr_math);
        if (usesMath()) return context.getString(R.string.mode_math);
        return context.getString(R.string.mode_qr);
    }

    public String snoozeChipText(Context context) {
        return snoozeMinutes <= 0
                ? context.getString(R.string.snooze_off_chip)
                : context.getString(R.string.snooze_minutes_chip, snoozeMinutes);
    }

    public String soundChipText(Context context) {
        if (soundUri == null || soundUri.isEmpty()) return context.getString(R.string.system_alarm);
        if (soundName == null || soundName.trim().isEmpty() || "Custom sound".equals(soundName)) {
            return context.getString(R.string.custom_sound);
        }
        return soundName.trim();
    }

    public String mathDifficultyLabel(Context context) {
        if (MATH_HARD.equals(mathDifficulty)) return context.getString(R.string.difficulty_hard);
        if (MATH_BRUTAL.equals(mathDifficulty)) return context.getString(R.string.difficulty_brutal);
        return context.getString(R.string.difficulty_easy);
    }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private static int normalizeRamp(int seconds) {
        return seconds == 30 || seconds == 60 || seconds == 90 || seconds == 120 ? seconds : 0;
    }
}
