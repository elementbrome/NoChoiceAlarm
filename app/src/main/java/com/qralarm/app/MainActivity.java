package com.qralarm.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final int REQ_PERMS = 300;
    private AlarmListAdapter adapter;
    private TextView permissionWarning;
    private boolean permissionsFlowStarted;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
        if (!getPreferences(MODE_PRIVATE).getBoolean("intro_shown",false)) {
            getPreferences(MODE_PRIVATE).edit().putBoolean("intro_shown",true).apply();
            new AlertDialog.Builder(this)
                    .setTitle("Autorisations nécessaires")
                    .setMessage("NoChoice Alarm a besoin de la caméra pour les réveils QR, des notifications pour afficher le réveil et de l’autorisation plein écran pour apparaître sur l’écran verrouillé. Aucune donnée n’est envoyée sur Internet.")
                    .setCancelable(false)
                    .setPositiveButton("Continuer",(d,w)->requestRuntimePermissions())
                    .show();
        }
    }

    private void buildUi() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Ui.BG);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0,Ui.dp(this,24),0,Ui.dp(this,96));

        TextView title = Ui.text(this,"Alarmes",34,Ui.TEXT,true);
        LinearLayout.LayoutParams titleP = new LinearLayout.LayoutParams(-1,-2);
        titleP.leftMargin=Ui.dp(this,22); titleP.rightMargin=Ui.dp(this,22);
        content.addView(title,titleP);

        TextView subtitle = Ui.text(this,"Cliquez sur une alarme pour la modifier.",14,Ui.MUTED,false);
        LinearLayout.LayoutParams subP = new LinearLayout.LayoutParams(-1,-2);
        subP.leftMargin=Ui.dp(this,22); subP.rightMargin=Ui.dp(this,22); subP.topMargin=Ui.dp(this,4);
        content.addView(subtitle,subP);

        permissionWarning = Ui.text(this,"",14,Ui.WARNING_TEXT,true);
        permissionWarning.setPadding(Ui.dp(this,14),Ui.dp(this,10),Ui.dp(this,14),Ui.dp(this,10));
        permissionWarning.setBackground(Ui.rounded(Ui.WARNING_BG,12,this));
        permissionWarning.setVisibility(View.GONE);
        permissionWarning.setOnClickListener(v -> {
            if (!runtimePermissionsGranted()) requestRuntimePermissions();
            else openFullScreenSettings();
        });
        LinearLayout.LayoutParams warnP = new LinearLayout.LayoutParams(-1,-2);
        warnP.leftMargin=Ui.dp(this,16); warnP.rightMargin=Ui.dp(this,16); warnP.topMargin=Ui.dp(this,14);
        content.addView(permissionWarning,warnP);

        ListView list = new ListView(this);
        list.setDivider(null);
        list.setSelector(android.R.color.transparent);
        list.setClipToPadding(false);
        list.setPadding(0,Ui.dp(this,10),0,Ui.dp(this,10));
        list.setCacheColorHint(Color.TRANSPARENT);

        adapter = new AlarmListAdapter(this,new AlarmListAdapter.Listener() {
            @Override public void onToggle(AlarmModel alarm, boolean enabled) {
                alarm.enabled=enabled;
                AlarmStore.save(MainActivity.this,alarm);
                if (enabled) AlarmScheduler.schedule(MainActivity.this,alarm);
                else {
                    AlarmScheduler.cancelRegular(MainActivity.this,alarm.id);
                    AlarmScheduler.cancelSnooze(MainActivity.this,alarm.id);
                }
                refresh();
            }

            @Override public void onDelete(AlarmModel alarm) {
                confirmDelete(alarm);
            }
        });
        list.setAdapter(adapter);
        list.setOnItemClickListener((p,v,pos,id)-> openEdit(adapter.getItem(pos).id));

        TextView empty = Ui.text(this,"Aucune alarme\nTouchez + pour créer votre premier réveil.",16,Ui.MUTED,false);
        empty.setGravity(Gravity.CENTER);
        empty.setLineSpacing(0,1.3f);

        content.addView(list,new LinearLayout.LayoutParams(-1,0,1f));
        root.addView(content,new FrameLayout.LayoutParams(-1,-1));
        root.addView(empty,new FrameLayout.LayoutParams(0,0));
        list.setEmptyView(empty);
        FrameLayout.LayoutParams ep = new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER);
        ep.leftMargin=Ui.dp(this,35); ep.rightMargin=Ui.dp(this,35);
        empty.setLayoutParams(ep);

        TextView fab = Ui.text(this,"+",34,Color.WHITE,false);
        fab.setGravity(Gravity.CENTER);
        fab.setBackground(Ui.rounded(Ui.PRIMARY,29,this));
        fab.setElevation(Ui.dp(this,10));
        fab.setOnClickListener(v -> openEdit(-1));
        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(Ui.dp(this,60),Ui.dp(this,60),Gravity.BOTTOM|Gravity.END);
        fp.rightMargin=Ui.dp(this,22); fp.bottomMargin=Ui.dp(this,24);
        root.addView(fab,fp);
        setContentView(root);
    }

    @Override protected void onResume() {
        super.onResume();
        recoverActiveAlarm();
        refresh();
        updatePermissionWarning();
        if (getPreferences(MODE_PRIVATE).getBoolean("intro_shown",false) && !permissionsFlowStarted) {
            if (runtimePermissionsGranted()) maybeRequestFullScreenAccess();
        }
    }

    private void recoverActiveAlarm() {
        long id = AlarmService.getActiveAlarmId(this);
        if (id < 0 || AlarmStore.get(this,id) == null) return;
        Intent service = new Intent(this,AlarmService.class).setAction(AlarmService.ACTION_START).putExtra(AlarmScheduler.EXTRA_ALARM_ID,id);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(service); else startService(service);
        Intent ring = new Intent(this,RingActivity.class).putExtra(AlarmScheduler.EXTRA_ALARM_ID,id);
        startActivity(ring);
    }

    private void refresh() {
        if (adapter != null) adapter.setItems(AlarmStore.all(this));
    }

    private void openEdit(long id) {
        if (!runtimePermissionsGranted()) {
            Toast.makeText(this,"Accordez d’abord les autorisations indispensables.",Toast.LENGTH_LONG).show();
            requestRuntimePermissions();
            return;
        }
        Intent i = new Intent(this,EditAlarmActivity.class);
        if (id >= 0) i.putExtra(AlarmScheduler.EXTRA_ALARM_ID,id);
        startActivity(i);
    }

    private void confirmDelete(AlarmModel a) {
        String lbl = a.label == null ? "" : a.label.trim();
        new AlertDialog.Builder(this)
                .setTitle("Supprimer l’alarme ?")
                .setMessage(a.timeText() + (lbl.isEmpty()?"":" · "+lbl))
                .setNegativeButton("Annuler",null)
                .setPositiveButton("Supprimer",(d,w)-> {
                    AlarmScheduler.cancelRegular(this,a.id);
                    AlarmScheduler.cancelSnooze(this,a.id);
                    AlarmStore.delete(this,a.id);
                    refresh();
                }).show();
    }

    private boolean runtimePermissionsGranted() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return false;
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false;
        if (Build.VERSION.SDK_INT <= 28 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) return false;
        return true;
    }

    private void requestRuntimePermissions() {
        List<String> missing = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) missing.add(Manifest.permission.CAMERA);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) missing.add(Manifest.permission.POST_NOTIFICATIONS);
        if (Build.VERSION.SDK_INT <= 28 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) missing.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (missing.isEmpty()) {
            maybeRequestFullScreenAccess();
            return;
        }
        permissionsFlowStarted=true;
        requestPermissions(missing.toArray(new String[0]),REQ_PERMS);
    }

    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grants) {
        super.onRequestPermissionsResult(requestCode,permissions,grants);
        permissionsFlowStarted=false;
        updatePermissionWarning();
        if (requestCode==REQ_PERMS) {
            if (runtimePermissionsGranted()) maybeRequestFullScreenAccess();
            else new AlertDialog.Builder(this)
                    .setTitle("Autorisation requise")
                    .setMessage("Sans caméra et notifications, NoChoice Alarm ne peut pas garantir le fonctionnement prévu. Vous pouvez réactiver ces autorisations dans les réglages de l’application.")
                    .setNegativeButton("Fermer",null)
                    .setPositiveButton("Réglages",(d,w)-> {
                        try { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()))); }
                        catch(Exception ignored) {}
                    }).show();
        }
    }

    private void maybeRequestFullScreenAccess() {
        if (Build.VERSION.SDK_INT < 34) return;
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (nm.canUseFullScreenIntent()) return;
        if (getPreferences(MODE_PRIVATE).getBoolean("fsi_prompted",false)) return;
        getPreferences(MODE_PRIVATE).edit().putBoolean("fsi_prompted",true).apply();
        new AlertDialog.Builder(this)
                .setTitle("Affichage plein écran")
                .setMessage("Autorisez NoChoice Alarm à afficher un réveil en plein écran, même lorsque le téléphone est verrouillé.")
                .setNegativeButton("Plus tard",null)
                .setPositiveButton("Ouvrir les réglages",(d,w)-> openFullScreenSettings()).show();
    }

    private void openFullScreenSettings() {
        if (Build.VERSION.SDK_INT < 34) return;
        try { startActivity(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:"+getPackageName()))); }
        catch (Exception ignored) {}
    }

    private void updatePermissionWarning() {
        StringBuilder sb = new StringBuilder();
        if (!runtimePermissionsGranted()) sb.append("Autorisations manquantes — touchez ici pour corriger.");
        if (Build.VERSION.SDK_INT >= 34) {
            NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if (!nm.canUseFullScreenIntent()) {
                if (sb.length()>0) sb.append("\n");
                sb.append("Affichage plein écran non autorisé.");
            }
        }
        permissionWarning.setText(sb.toString());
        permissionWarning.setVisibility(sb.length()==0?View.GONE:View.VISIBLE);
    }
}
