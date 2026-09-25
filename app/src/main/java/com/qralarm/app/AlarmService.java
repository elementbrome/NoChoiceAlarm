package com.qralarm.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class AlarmService extends Service {
    public static final String ACTION_START = "com.qralarm.app.START_ALARM";
    public static final String ACTION_STOP = "com.qralarm.app.STOP_ALARM";
    private static final String CHANNEL = "ringing_alarm";
    private static final int NOTIFICATION_ID = 9001;
    static final String ACTIVE_PREF = "active_alarm";
    private MediaPlayer player;
    private Vibrator vibrator;
    private long activeId = -1;

    public static long getActiveAlarmId(Context c) {
        return c.getSharedPreferences(ACTIVE_PREF,Context.MODE_PRIVATE).getLong("id",-1);
    }

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopRinging();
            stopSelf();
            return START_NOT_STICKY;
        }
        long id = intent == null ? getSharedPreferences(ACTIVE_PREF,MODE_PRIVATE).getLong("id",-1)
                : intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID,-1);
        if (id < 0) { stopSelf(); return START_NOT_STICKY; }
        AlarmModel alarm = AlarmStore.get(this,id);
        if (alarm == null) { stopSelf(); return START_NOT_STICKY; }
        activeId = id;
        getSharedPreferences(ACTIVE_PREF,MODE_PRIVATE).edit().putLong("id",id).apply();
        startForeground(NOTIFICATION_ID, buildNotification(alarm));
        startSound(alarm);
        startVibration();
        return START_STICKY;
    }

    private void createChannel() {
        NotificationChannel ch = new NotificationChannel(CHANNEL,"Alarme en cours",NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Notification affichée pendant qu’une alarme sonne");
        ch.setSound(null,null);
        ch.enableVibration(false);
        ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
    }

    private Notification buildNotification(AlarmModel a) {
        Intent open = new Intent(this, RingActivity.class).putExtra(AlarmScheduler.EXTRA_ALARM_ID,a.id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 77, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        String title = a.label == null || a.label.trim().isEmpty() ? "Alarme" : a.label.trim();
        return new Notification.Builder(this,CHANNEL)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(title + " · " + a.timeText())
                .setContentText("Scannez le QR code pour arrêter l’alarme")
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(pi)
                .setFullScreenIntent(pi,true)
                .build();
    }

    private void startSound(AlarmModel a) {
        stopPlayer();
        Uri uri = null;
        try {
            if (a.soundUri != null && !a.soundUri.isEmpty()) uri = Uri.parse(a.soundUri);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
            player.setDataSource(this,uri);
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (Exception e) {
            Log.e("QRAlarm","Impossible de lire le son choisi",e);
            try {
                Uri fallback = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                player = new MediaPlayer();
                player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
                player.setDataSource(this,fallback);
                player.setLooping(true); player.prepare(); player.start();
            } catch (Exception ignored) {}
        }
    }

    private void startVibration() {
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0,700,350,700,350};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern,0));
        }
    }

    private void stopPlayer() {
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) {}
            try { player.release(); } catch (Exception ignored) {}
            player = null;
        }
    }

    private void stopRinging() {
        stopPlayer();
        if (vibrator != null) vibrator.cancel();
        vibrator = null;
        activeId = -1;
        getSharedPreferences(ACTIVE_PREF,MODE_PRIVATE).edit().remove("id").apply();
        stopForeground(STOP_FOREGROUND_REMOVE);
    }

    @Override public void onDestroy() {
        stopRinging();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
