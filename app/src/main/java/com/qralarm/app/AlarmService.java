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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class AlarmService extends Service {
    public static final String ACTION_START = "com.qralarm.app.START_ALARM";
    public static final String ACTION_STOP = "com.qralarm.app.STOP_ALARM";
    public static final String EXTRA_TEST = "alarm_test_mode";
    public static final String EXTRA_ALARM_JSON = "alarm_json";

    private static final String CHANNEL = "ringing_alarm";
    private static final int NOTIFICATION_ID = 9001;
    static final String ACTIVE_PREF = "active_alarm";

    private MediaPlayer player;
    private Vibrator vibrator;
    private long activeId = -1;
    private boolean activeTest;
    private final Handler volumeHandler = new Handler(Looper.getMainLooper());
    private Runnable volumeRamp;

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

        boolean test = intent != null && intent.getBooleanExtra(EXTRA_TEST,false);
        String json = intent == null ? "" : intent.getStringExtra(EXTRA_ALARM_JSON);
        long id = intent == null
                ? getSharedPreferences(ACTIVE_PREF,MODE_PRIVATE).getLong("id",-1)
                : intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID,-1);

        AlarmModel alarm = null;
        if (test && json != null && !json.isEmpty()) alarm = AlarmModel.fromJsonString(json);
        if (alarm == null && id >= 0) alarm = AlarmStore.get(this,id);
        if (alarm == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // Si un nouveau réveil remplace un test ou une sonnerie précédente, libérer proprement les ressources.
        stopPlayer();
        stopRamp();
        if (vibrator != null) vibrator.cancel();

        activeId = alarm.id;
        activeTest = test;
        if (!test) {
            getSharedPreferences(ACTIVE_PREF,MODE_PRIVATE).edit().putLong("id",alarm.id).apply();
        }

        startForeground(NOTIFICATION_ID, buildNotification(alarm,test,json));
        startSound(alarm);
        startVibration();
        return test ? START_NOT_STICKY : START_STICKY;
    }

    private void createChannel() {
        NotificationChannel ch = new NotificationChannel(CHANNEL,"Alarme en cours",NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Notification affichée pendant qu’une alarme sonne");
        ch.setSound(null,null);
        ch.enableVibration(false);
        ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
    }

    private Notification buildNotification(AlarmModel a, boolean test, String json) {
        Intent open = new Intent(this, RingActivity.class)
                .putExtra(AlarmScheduler.EXTRA_ALARM_ID,a.id)
                .putExtra(EXTRA_TEST,test)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (test && json != null) open.putExtra(EXTRA_ALARM_JSON,json);

        PendingIntent pi = PendingIntent.getActivity(
                this,
                test ? 78 : 77,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String baseTitle = a.label == null || a.label.trim().isEmpty() ? "Alarme" : a.label.trim();
        String instruction;
        if (a.usesQr() && a.usesMath()) instruction="Scannez le QR puis résolvez le calcul";
        else if (a.usesMath()) instruction="Résolvez le calcul pour arrêter l’alarme";
        else instruction="Scannez le QR code pour arrêter l’alarme";

        Notification.Builder builder = new Notification.Builder(this,CHANNEL)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle((test ? "TEST · " : "") + baseTitle + " · " + a.timeText())
                .setContentText(instruction)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(pi);
        if (!test) builder.setFullScreenIntent(pi,true);
        return builder.build();
    }

    private void startSound(AlarmModel a) {
        stopPlayer();
        Uri uri = null;
        try {
            if (a.soundUri != null && !a.soundUri.isEmpty()) uri = Uri.parse(a.soundUri);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
            player.setDataSource(this,uri);
            player.setLooping(true);
            player.prepare();
            applyVolumeAndStart(a);
        } catch (Exception e) {
            Log.e("NoChoiceAlarm","Impossible de lire le son choisi",e);
            try {
                Uri fallback = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                player = new MediaPlayer();
                player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
                player.setDataSource(this,fallback);
                player.setLooping(true);
                player.prepare();
                applyVolumeAndStart(a);
            } catch (Exception ignored) {}
        }
    }

    private void applyVolumeAndStart(AlarmModel a) {
        float target = Math.max(0f,Math.min(1f,a.volumePercent/100f));
        if (a.volumeRampSeconds <= 0) {
            player.setVolume(target,target);
            player.start();
            return;
        }

        player.setVolume(0f,0f);
        player.start();
        long started = System.currentTimeMillis();
        long duration = a.volumeRampSeconds * 1000L;
        volumeRamp = new Runnable() {
            @Override public void run() {
                if (player == null) return;
                float fraction = Math.min(1f,(System.currentTimeMillis()-started)/(float)duration);
                float now = target * fraction;
                try { player.setVolume(now,now); } catch(Exception ignored) { return; }
                if (fraction < 1f) volumeHandler.postDelayed(this,250);
            }
        };
        volumeHandler.post(volumeRamp);
    }

    private void startVibration() {
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0,700,350,700,350};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern,0));
        }
    }

    private void stopRamp() {
        if (volumeRamp != null) volumeHandler.removeCallbacks(volumeRamp);
        volumeRamp=null;
    }

    private void stopPlayer() {
        stopRamp();
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
        getSharedPreferences(ACTIVE_PREF,MODE_PRIVATE).edit().remove("id").apply();
        activeId = -1;
        activeTest=false;
        stopForeground(STOP_FOREGROUND_REMOVE);
    }

    @Override public void onDestroy() {
        stopRinging();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
