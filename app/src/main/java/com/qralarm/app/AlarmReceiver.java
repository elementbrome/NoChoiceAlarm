package com.qralarm.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID,-1);
        boolean snooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_SNOOZE,false);
        if (id < 0) return;
        AlarmModel a = AlarmStore.get(context,id);
        if (a == null) return;
        if (!snooze) AlarmScheduler.onRegularFired(context,a);
        Intent s = new Intent(context, AlarmService.class).setAction(AlarmService.ACTION_START).putExtra(AlarmScheduler.EXTRA_ALARM_ID,id);
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(s); else context.startService(s);
    }
}
