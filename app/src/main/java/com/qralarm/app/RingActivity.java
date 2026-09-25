package com.qralarm.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

public class RingActivity extends Activity {
    private static final int REQ_SCAN = 88;
    private long alarmId;
    private AlarmModel alarm;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        alarmId = getIntent().getLongExtra(AlarmScheduler.EXTRA_ALARM_ID,-1);
        alarm = AlarmStore.get(this,alarmId);
        if (alarm == null) { finish(); return; }
        buildUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(Ui.dp(this,28),Ui.dp(this,60),Ui.dp(this,28),Ui.dp(this,32));
        root.setBackgroundColor(Ui.BG);

        TextView kicker = Ui.text(this,"RÉVEIL",14,Ui.PRIMARY,true);
        kicker.setLetterSpacing(.15f); root.addView(kicker);
        TextView time = Ui.text(this,alarm.timeText(),64,Ui.TEXT,true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-2,-2); tp.topMargin=Ui.dp(this,12); root.addView(time,tp);
        String lbl = alarm.label == null || alarm.label.trim().isEmpty() ? "Il est l’heure" : alarm.label.trim();
        TextView label = Ui.text(this,lbl,22,Ui.TEXT,true); label.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2); lp.topMargin=Ui.dp(this,8); root.addView(label,lp);
        TextView info = Ui.text(this,"Pour arrêter définitivement l’alarme, scannez le QR code imprimé.",16,Ui.MUTED,false);
        info.setGravity(Gravity.CENTER); info.setLineSpacing(0,1.15f);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1,-2); ip.topMargin=Ui.dp(this,22); root.addView(info,ip);

        Space sp = new Space(this); root.addView(sp,new LinearLayout.LayoutParams(1,0,1f));
        Button scan = new Button(this); scan.setText("Scanner le QR code"); scan.setTextColor(Color.WHITE); scan.setTextSize(17); scan.setAllCaps(false);
        scan.setBackground(Ui.rounded(Ui.PRIMARY,16,this)); scan.setOnClickListener(v -> startScanner());
        root.addView(scan,new LinearLayout.LayoutParams(-1,Ui.dp(this,58)));

        if (alarm.snoozeMinutes > 0) {
            Button snooze = new Button(this); snooze.setText("Snooze · " + alarm.snoozeMinutes + " min"); snooze.setTextColor(Ui.TEXT); snooze.setTextSize(16); snooze.setAllCaps(false);
            snooze.setBackground(Ui.outlined(Ui.SURFACE,0xFFE1E3EA,this)); snooze.setOnClickListener(v -> snooze());
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,Ui.dp(this,54)); p.topMargin=Ui.dp(this,12); root.addView(snooze,p);
        }
        setContentView(root);
    }

    private void startScanner() {
        Intent i = new Intent(this,QrScannerActivity.class).putExtra(AlarmScheduler.EXTRA_ALARM_ID,alarmId);
        startActivityForResult(i,REQ_SCAN);
    }

    private void snooze() {
        AlarmScheduler.cancelSnooze(this,alarmId);
        AlarmScheduler.scheduleSnooze(this,alarmId,alarm.snoozeMinutes);
        stopService(new Intent(this,AlarmService.class));
        finishAndRemoveTask();
    }

    @Override protected void onActivityResult(int req, int result, Intent data) {
        super.onActivityResult(req,result,data);
        if (req == REQ_SCAN && result == RESULT_OK) {
            finishAndRemoveTask();
        }
    }

    @Override public void onBackPressed() {
        // Intentionnel : le bouton retour ne permet pas d’échapper à l’écran de réveil.
    }
}
