package com.qralarm.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import java.util.List;

public final class AlarmScheduler {
    public static final String EXTRA_ALARM_ID = "alarm_id";
    public static final String EXTRA_SNOOZE = "is_snooze";

    private AlarmScheduler() {}

    public static void schedule(Context c, AlarmModel a) {
        cancelRegular(c, a.id);
        if (!a.enabled) return;
        long trigger = nextTrigger(a, System.currentTimeMillis());
        scheduleAt(c, a.id, trigger, false);
    }

    public static void scheduleSnooze(Context c, long alarmId, int minutes) {
        if (minutes <= 0) return;
        scheduleAt(c, alarmId, System.currentTimeMillis() + minutes * 60_000L, true);
    }

    public static void scheduleRecovery(Context c, long alarmId) {
        scheduleAt(c, alarmId, System.currentTimeMillis() + 2_000L, true);
    }

    private static void scheduleAt(Context c, long id, long when, boolean snooze) {
        AlarmManager am = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(c, AlarmReceiver.class).putExtra(EXTRA_ALARM_ID,id).putExtra(EXTRA_SNOOZE,snooze);
        int req = requestCode(id, snooze);
        PendingIntent fire = PendingIntent.getBroadcast(c, req, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent show = new Intent(c, MainActivity.class);
        PendingIntent showPi = PendingIntent.getActivity(c, req ^ 0x11111111, show, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setAlarmClock(new AlarmManager.AlarmClockInfo(when, showPi), fire);
    }

    public static void cancelRegular(Context c, long id) { cancel(c,id,false); }
    public static void cancelSnooze(Context c, long id) { cancel(c,id,true); }

    private static void cancel(Context c, long id, boolean snooze) {
        AlarmManager am = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(c, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(c, requestCode(id,snooze), i, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pi != null) { am.cancel(pi); pi.cancel(); }
    }

    public static void onRegularFired(Context c, AlarmModel a) {
        if (a.repeatMask == 0) {
            a.enabled = false;
            AlarmStore.save(c,a);
        } else {
            long trigger = nextTrigger(a, System.currentTimeMillis() + 60_000L);
            scheduleAt(c,a.id,trigger,false);
        }
    }

    public static void rescheduleAll(Context c) {
        List<AlarmModel> list = AlarmStore.all(c);
        for (AlarmModel a : list) if (a.enabled) schedule(c,a);
    }

    public static long nextTrigger(AlarmModel a, long after) {
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(after);
        if (a.repeatMask == 0) {
            Calendar t = Calendar.getInstance();
            t.setTimeInMillis(after);
            t.set(Calendar.HOUR_OF_DAY,a.hour); t.set(Calendar.MINUTE,a.minute); t.set(Calendar.SECOND,0); t.set(Calendar.MILLISECOND,0);
            if (t.getTimeInMillis() <= after) t.add(Calendar.DAY_OF_YEAR,1);
            return t.getTimeInMillis();
        }
        for (int add=0; add<=7; add++) {
            Calendar t = Calendar.getInstance();
            t.setTimeInMillis(after);
            t.add(Calendar.DAY_OF_YEAR,add);
            t.set(Calendar.HOUR_OF_DAY,a.hour); t.set(Calendar.MINUTE,a.minute); t.set(Calendar.SECOND,0); t.set(Calendar.MILLISECOND,0);
            int bit = dayBit(t.get(Calendar.DAY_OF_WEEK));
            if ((a.repeatMask & (1 << bit)) != 0 && t.getTimeInMillis() > after) return t.getTimeInMillis();
        }
        return after + 24*60*60_000L;
    }

    private static int dayBit(int calendarDay) {
        switch (calendarDay) {
            case Calendar.MONDAY: return 0;
            case Calendar.TUESDAY: return 1;
            case Calendar.WEDNESDAY: return 2;
            case Calendar.THURSDAY: return 3;
            case Calendar.FRIDAY: return 4;
            case Calendar.SATURDAY: return 5;
            default: return 6;
        }
    }

    private static int requestCode(long id, boolean snooze) {
        int x = (int)(id ^ (id >>> 32));
        x &= 0x3fffffff;
        if (snooze) x |= 0x40000000;
        return x;
    }
}
