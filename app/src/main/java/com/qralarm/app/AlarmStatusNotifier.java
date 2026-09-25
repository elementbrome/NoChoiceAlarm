package com.qralarm.app;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Persistent, silent summary of scheduled/active alarms. */
public final class AlarmStatusNotifier {
    private static final String CHANNEL = "scheduled_alarms";
    private static final int ID = 9100;

    private AlarmStatusNotifier() {}

    public static void update(Context context) {
        try {
            updateInternal(context);
        } catch (RuntimeException e) {
            // A status notification is helpful but must never be able to crash the alarm app.
            Log.w("NoChoiceAlarm", "Unable to update scheduled-alarm notification", e);
        }
    }

    private static void updateInternal(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        createChannel(context,nm);

        if (Build.VERSION.SDK_INT >= 33
                && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            nm.cancel(ID);
            return;
        }

        List<AlarmModel> enabled = new ArrayList<>();
        for (AlarmModel a : AlarmStore.all(context)) if (a.enabled) enabled.add(a);
        if (enabled.isEmpty()) {
            nm.cancel(ID);
            return;
        }

        Intent open = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                context, 9100, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.InboxStyle inbox = new Notification.InboxStyle();
        int max = Math.min(enabled.size(), 5);
        for (int i = 0; i < max; i++) {
            AlarmModel a = enabled.get(i);
            String label = a.label == null || a.label.trim().isEmpty() ? context.getString(R.string.default_alarm_label) : a.label.trim();
            long next = AlarmScheduler.nextTrigger(a, System.currentTimeMillis());
            String day = new SimpleDateFormat("EEE", AppLocale.uiLocale(context)).format(new Date(next));
            inbox.addLine(day + " · " + a.timeText() + " · " + label);
        }
        if (enabled.size() > max) inbox.setSummaryText(context.getString(R.string.more_alarms, enabled.size() - max));

        AlarmModel first = enabled.get(0);
        String firstLabel = first.label == null || first.label.trim().isEmpty() ? context.getString(R.string.default_alarm_label) : first.label.trim();
        String title = enabled.size() == 1 ? context.getString(R.string.active_alarm_single) : context.getString(R.string.active_alarm_multiple, enabled.size());
        String text = context.getString(R.string.next_alarm, first.timeText(), firstLabel);

        Notification n = new Notification.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(inbox)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setContentIntent(pi)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .build();
        nm.notify(ID, n);
    }

    private static void createChannel(Context context, NotificationManager nm) {
        NotificationChannel ch = new NotificationChannel(
                CHANNEL, context.getString(R.string.scheduled_channel_name), NotificationManager.IMPORTANCE_LOW);
        ch.setDescription(context.getString(R.string.scheduled_channel_description));
        ch.setSound(null, null);
        ch.enableVibration(false);
        ch.setShowBadge(true);
        nm.createNotificationChannel(ch);
    }
}
