package com.qralarm.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String a = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(a) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)
                || Intent.ACTION_TIME_CHANGED.equals(a) || Intent.ACTION_TIMEZONE_CHANGED.equals(a)) {
            AlarmScheduler.rescheduleAll(context);
            long activeId = AlarmService.getActiveAlarmId(context);
            if (activeId >= 0 && AlarmStore.get(context, activeId) != null) AlarmScheduler.scheduleRecovery(context, activeId);
        }
    }
}
