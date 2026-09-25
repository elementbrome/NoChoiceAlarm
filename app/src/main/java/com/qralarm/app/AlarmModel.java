package com.qralarm.app;

import org.json.JSONException;
import org.json.JSONObject;

public class AlarmModel {
    public long id;
    public int hour;
    public int minute;
    public String label = "";
    public int repeatMask = 0; // bit 0 = lundi ... bit 6 = dimanche
    public String soundUri = "";
    public int snoozeMinutes = 0;
    public String qrToken = "";
    public boolean enabled = true;

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("hour", hour);
        o.put("minute", minute);
        o.put("label", label == null ? "" : label);
        o.put("repeatMask", repeatMask);
        o.put("soundUri", soundUri == null ? "" : soundUri);
        o.put("snoozeMinutes", snoozeMinutes);
        o.put("qrToken", qrToken == null ? "" : qrToken);
        o.put("enabled", enabled);
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
        a.snoozeMinutes = o.optInt("snoozeMinutes", 0);
        a.qrToken = o.optString("qrToken", "");
        a.enabled = o.optBoolean("enabled", true);
        return a;
    }

    public String timeText() {
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    public String repeatText() {
        if (repeatMask == 0) return "Une fois";
        if (repeatMask == 0b1111111) return "Tous les jours";
        String[] names = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if ((repeatMask & (1 << i)) != 0) {
                if (sb.length() > 0) sb.append(" · ");
                sb.append(names[i]);
            }
        }
        return sb.toString();
    }
}
