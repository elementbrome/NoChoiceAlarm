package com.qralarm.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

public class RingActivity extends Activity {
    private static final int REQ_SCAN = 88;
    private static final String STATE_QR_DONE = "qr_done";
    private static final String STATE_MATH_EXPR = "math_expr";
    private static final String STATE_MATH_ANSWER = "math_answer";

    private long alarmId;
    private AlarmModel alarm;
    private boolean testMode;
    private boolean qrDone;
    private String mathExpression;
    private int mathAnswer;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        Ui.enableImmersive(this);

        alarmId = getIntent().getLongExtra(AlarmScheduler.EXTRA_ALARM_ID,-1);
        testMode = getIntent().getBooleanExtra(AlarmService.EXTRA_TEST,false);
        String json = getIntent().getStringExtra(AlarmService.EXTRA_ALARM_JSON);
        if (testMode && json != null && !json.isEmpty()) alarm = AlarmModel.fromJsonString(json);
        if (alarm == null) alarm = AlarmStore.get(this,alarmId);
        if (alarm == null) { finish(); return; }

        if (b != null) {
            qrDone=b.getBoolean(STATE_QR_DONE,false);
            mathExpression=b.getString(STATE_MATH_EXPR,null);
            mathAnswer=b.getInt(STATE_MATH_ANSWER,Integer.MIN_VALUE);
        }

        if (alarm.usesQr() && !qrDone) buildQrStage();
        else if (alarm.usesMath()) buildMathStage();
        else buildQrStage();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean(STATE_QR_DONE,qrDone);
        if (mathExpression != null) out.putString(STATE_MATH_EXPR,mathExpression);
        if (mathAnswer != Integer.MIN_VALUE) out.putInt(STATE_MATH_ANSWER,mathAnswer);
    }

    private LinearLayout baseLayout(String instruction) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(Ui.dp(this,28),Ui.dp(this,54),Ui.dp(this,28),Ui.dp(this,30));
        root.setBackgroundColor(Ui.BG);

        TextView kicker = Ui.text(this,testMode?"TEST DU RÉVEIL":"RÉVEIL",13,Ui.PRIMARY,true);
        kicker.setLetterSpacing(.14f);
        root.addView(kicker);

        TextView time = Ui.displayText(this,alarm.timeText(),62,Ui.TEXT);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-2,-2);
        tp.topMargin=Ui.dp(this,10);
        root.addView(time,tp);

        String lbl = alarm.label == null || alarm.label.trim().isEmpty() ? "Il est l’heure" : alarm.label.trim();
        TextView label = Ui.displayText(this,lbl,22,Ui.TEXT);
        label.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=Ui.dp(this,6);
        root.addView(label,lp);

        TextView info = Ui.text(this,instruction,15,Ui.MUTED,false);
        info.setGravity(Gravity.CENTER);
        info.setLineSpacing(0,1.15f);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1,-2);
        ip.topMargin=Ui.dp(this,18);
        root.addView(info,ip);
        return root;
    }

    private void buildQrStage() {
        String instruction = alarm.usesMath()
                ? "Étape 1 sur 2 · Scannez le QR code. Le problème de math apparaîtra ensuite."
                : "Scannez le QR code imprimé pour arrêter l’alarme.";
        LinearLayout root=baseLayout(instruction);
        root.addView(new Space(this),new LinearLayout.LayoutParams(1,0,1f));

        Button scan = primaryButton("Scanner le QR code");
        scan.setOnClickListener(v -> startScanner());
        root.addView(scan,new LinearLayout.LayoutParams(-1,Ui.dp(this,58)));
        addSecondaryActions(root);
        setContentView(root);
    }

    private void buildMathStage() {
        if (mathExpression == null || mathAnswer == Integer.MIN_VALUE) {
            MathChallenge challenge=MathChallenge.generate(alarm.mathDifficulty);
            mathExpression=challenge.expression;
            mathAnswer=challenge.answer;
        }

        String prefix=alarm.usesQr()?"Étape 2 sur 2 · ":"";
        LinearLayout root=baseLayout(prefix+"Résolvez le calcul pour arrêter l’alarme.");

        TextView difficulty=Ui.text(this,alarm.mathDifficultyLabel().toUpperCase(),12,Ui.PRIMARY,true);
        difficulty.setLetterSpacing(.12f);
        LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-2,-2); dp.topMargin=Ui.dp(this,28);
        root.addView(difficulty,dp);

        TextView expression=Ui.displayText(this,mathExpression,32,Ui.TEXT);
        expression.setGravity(Gravity.CENTER);
        expression.setPadding(Ui.dp(this,16),Ui.dp(this,18),Ui.dp(this,16),Ui.dp(this,18));
        expression.setBackground(Ui.rounded(Ui.SURFACE,18,this));
        LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,-2); ep.topMargin=Ui.dp(this,10);
        root.addView(expression,ep);

        EditText answer=new EditText(this);
        answer.setTextColor(Ui.TEXT);
        answer.setHintTextColor(0xFF777C89);
        answer.setHint("Votre réponse");
        answer.setTextSize(22);
        answer.setGravity(Gravity.CENTER);
        answer.setSingleLine(true);
        answer.setInputType(InputType.TYPE_CLASS_NUMBER);
        answer.setBackground(Ui.rounded(Ui.SURFACE_2,16,this));
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,Ui.dp(this,58)); ap.topMargin=Ui.dp(this,14);
        root.addView(answer,ap);

        Button validate=primaryButton("Valider");
        validate.setOnClickListener(v->{
            String raw=answer.getText().toString().trim();
            if(raw.isEmpty()) return;
            try {
                if(Integer.parseInt(raw)==mathAnswer) completeAlarm();
                else {
                    answer.setText("");
                    Toast.makeText(this,"Mauvaise réponse. Réessayez.",Toast.LENGTH_SHORT).show();
                }
            } catch(Exception e) {
                answer.setText("");
            }
        });
        LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,Ui.dp(this,56)); vp.topMargin=Ui.dp(this,12);
        root.addView(validate,vp);

        root.addView(new Space(this),new LinearLayout.LayoutParams(1,0,1f));
        addSecondaryActions(root);
        setContentView(root);
        answer.requestFocus();
    }

    private Button primaryButton(String text) {
        Button b=new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(17);
        b.setAllCaps(false);
        b.setBackground(Ui.gradient(16,this));
        return b;
    }

    private Button secondaryButton(String text,int color) {
        Button b=new Button(this);
        b.setText(text);
        b.setTextColor(color);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setBackground(Ui.outlined(Ui.SURFACE,Ui.STROKE,this));
        return b;
    }

    private void addSecondaryActions(LinearLayout root) {
        if (!testMode && alarm.snoozeMinutes > 0) {
            Button snooze = secondaryButton("Snooze · " + alarm.snoozeMinutes + " min",Ui.TEXT);
            snooze.setOnClickListener(v -> snooze());
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,Ui.dp(this,54));
            p.topMargin=Ui.dp(this,12);
            root.addView(snooze,p);
        }

        if (testMode) {
            Button stop = secondaryButton("Arrêter le test",Ui.DANGER);
            stop.setOnClickListener(v -> stopTest());
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,Ui.dp(this,54));
            p.topMargin=Ui.dp(this,12);
            root.addView(stop,p);
        }
    }

    private void startScanner() {
        String token=QrUtils.tokenFor(this,alarm);
        Intent i = new Intent(this,QrScannerActivity.class)
                .putExtra(AlarmScheduler.EXTRA_ALARM_ID,alarmId)
                .putExtra(QrScannerActivity.EXTRA_QR_TOKEN,token);
        startActivityForResult(i,REQ_SCAN);
    }

    private void snooze() {
        AlarmScheduler.cancelSnooze(this,alarmId);
        AlarmScheduler.scheduleSnooze(this,alarmId,alarm.snoozeMinutes);
        stopAlarmService();
        finishAndRemoveTask();
    }

    private void stopTest() {
        stopAlarmService();
        finish();
    }

    private void completeAlarm() {
        stopAlarmService();
        if (testMode) finish(); else finishAndRemoveTask();
    }

    private void stopAlarmService() {
        Intent stop=new Intent(this,AlarmService.class).setAction(AlarmService.ACTION_STOP);
        startService(stop);
    }

    @Override protected void onActivityResult(int req, int result, Intent data) {
        super.onActivityResult(req,result,data);
        if (req == REQ_SCAN && result == RESULT_OK) {
            qrDone=true;
            if (alarm.usesMath()) buildMathStage();
            else completeAlarm();
        }
    }

    @Override public void onBackPressed() {
        // Intentionnel : retour ne contourne ni un vrai réveil ni le test. Le test possède son propre bouton d’arrêt.
    }
    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) Ui.enableImmersive(this);
    }

}
