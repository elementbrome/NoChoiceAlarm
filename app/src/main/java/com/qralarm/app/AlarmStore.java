package com.qralarm.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AlarmStore {
    private static final String PREFS = "qr_alarm_store";
    private static final String KEY = "alarms";

    private AlarmStore() {}

    public static synchronized List<AlarmModel> all(Context context) {
        List<AlarmModel> out = new ArrayList<>();
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) out.add(AlarmModel.fromJson(arr.getJSONObject(i)));
        } catch (Exception ignored) {}
        out.sort(Comparator.comparingInt((AlarmModel a) -> a.hour).thenComparingInt(a -> a.minute));
        return out;
    }

    public static synchronized AlarmModel get(Context c, long id) {
        for (AlarmModel a : all(c)) if (a.id == id) return a;
        return null;
    }

    public static synchronized void save(Context c, AlarmModel alarm) {
        List<AlarmModel> list = all(c);
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == alarm.id) {
                list.set(i, alarm);
                found = true;
                break;
            }
        }
        if (!found) list.add(alarm);
        write(c, list);
    }

    public static synchronized void delete(Context c, long id) {
        List<AlarmModel> list = all(c);
        list.removeIf(a -> a.id == id);
        write(c, list);
    }

    private static void write(Context c, List<AlarmModel> list) {
        JSONArray arr = new JSONArray();
        for (AlarmModel a : list) {
            try { arr.put(a.toJson()); } catch (Exception ignored) {}
        }
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply();
    }
}
