package com.qralarm.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class EditAlarmActivity extends Activity {
    private static final int REQ_AUDIO = 701;
    private AlarmModel alarm;
    private TimePicker timePicker;
    private EditText label;
    private final TextView[] dayViews = new TextView[7];
    private String soundUri = "";
    private Button soundButton;
    private Spinner snoozeSpinner;
    private ImageView qrImage;
    private Bitmap qrBitmap;
    private boolean isNew;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        long id = getIntent().getLongExtra(AlarmScheduler.EXTRA_ALARM_ID,-1);
        AlarmModel existing = id < 0 ? null : AlarmStore.get(this,id);
        isNew = existing == null;
        if (existing != null) alarm = existing; else {
            alarm = new AlarmModel(); alarm.id = System.currentTimeMillis();
            Calendar c = Calendar.getInstance(); c.add(Calendar.MINUTE,5); alarm.hour=c.get(Calendar.HOUR_OF_DAY); alarm.minute=c.get(Calendar.MINUTE);
            alarm.qrToken = QrUtils.newToken(); alarm.enabled=true;
        }
        if (alarm.qrToken == null || alarm.qrToken.isEmpty()) alarm.qrToken=QrUtils.newToken();
        soundUri = alarm.soundUri == null ? "" : alarm.soundUri;
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(Ui.BG);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(Ui.dp(this,18),Ui.dp(this,18),Ui.dp(this,18),Ui.dp(this,32));
        scroll.addView(root,new ScrollView.LayoutParams(-1,-2));

        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = Ui.text(this,"‹",40,Ui.TEXT,false); back.setGravity(Gravity.CENTER); back.setOnClickListener(v->finish()); top.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));
        TextView title = Ui.text(this,isNew?"Nouvelle alarme":"Modifier l’alarme",24,Ui.TEXT,true); top.addView(title,new LinearLayout.LayoutParams(0,-2,1f));
        TextView save = Ui.text(this,"Enregistrer",15,Ui.PRIMARY,true); save.setGravity(Gravity.CENTER); save.setPadding(Ui.dp(this,10),0,Ui.dp(this,4),0); save.setOnClickListener(v->saveAlarm()); top.addView(save,new LinearLayout.LayoutParams(-2,Ui.dp(this,48)));
        root.addView(top,new LinearLayout.LayoutParams(-1,-2));

        timePicker = new TimePicker(this); timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(this)); timePicker.setHour(alarm.hour); timePicker.setMinute(alarm.minute);
        LinearLayout timeCard = card(); timeCard.setGravity(Gravity.CENTER); timeCard.addView(timePicker,new LinearLayout.LayoutParams(-1,-2)); addCard(root,timeCard,12);

        section(root,"Nom de l’alarme");
        label = new EditText(this); label.setText(alarm.label); label.setHint("Facultatif"); label.setTextSize(17); label.setTextColor(Ui.TEXT); label.setHintTextColor(0xFF9CA0AA); label.setSingleLine(true); label.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); label.setPadding(Ui.dp(this,16),0,Ui.dp(this,16),0); label.setBackground(Ui.rounded(Ui.SURFACE,16,this));
        root.addView(label,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));

        section(root,"Répétition");
        LinearLayout days = new LinearLayout(this); days.setOrientation(LinearLayout.HORIZONTAL); days.setGravity(Gravity.CENTER);
        String[] names={"L","M","M","J","V","S","D"};
        for (int i=0;i<7;i++) {
            final int bit=i; TextView d=Ui.text(this,names[i],14,Ui.TEXT,true); d.setGravity(Gravity.CENTER); d.setOnClickListener(v->{ alarm.repeatMask ^= (1<<bit); updateDayStyle(bit); }); dayViews[i]=d;
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,Ui.dp(this,44),1f); if(i>0)p.leftMargin=Ui.dp(this,6); days.addView(d,p); updateDayStyle(i);
        }
        root.addView(days,new LinearLayout.LayoutParams(-1,-2));

        section(root,"Son");
        soundButton = new Button(this); soundButton.setAllCaps(false); soundButton.setTextSize(16); soundButton.setTextColor(Ui.TEXT); soundButton.setGravity(Gravity.START|Gravity.CENTER_VERTICAL); soundButton.setPadding(Ui.dp(this,16),0,Ui.dp(this,16),0); soundButton.setBackground(Ui.rounded(Ui.SURFACE,16,this)); soundButton.setOnClickListener(v->chooseSound()); updateSoundText(null);
        root.addView(soundButton,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));

        section(root,"Mode de réveil");
        LinearLayout mode = card();
        LinearLayout mt = new LinearLayout(this); mt.setOrientation(LinearLayout.VERTICAL);
        mt.addView(Ui.text(this,"QR code",17,Ui.TEXT,true));
        TextView md=Ui.text(this,"Le bon QR devra être scanné pour arrêter définitivement l’alarme.",14,Ui.MUTED,false); LinearLayout.LayoutParams mdp=new LinearLayout.LayoutParams(-1,-2); mdp.topMargin=Ui.dp(this,4); mt.addView(md,mdp);
        mode.addView(mt,new LinearLayout.LayoutParams(-1,-2)); addCard(root,mode,0);

        qrImage = new ImageView(this); qrImage.setAdjustViewBounds(true); qrImage.setBackgroundColor(Color.WHITE); qrImage.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12));
        try { qrBitmap=QrUtils.bitmap(alarm.qrToken,800); qrImage.setImageBitmap(qrBitmap); } catch(Exception e) { Toast.makeText(this,"Impossible de générer le QR code.",Toast.LENGTH_LONG).show(); }
        LinearLayout.LayoutParams qrp=new LinearLayout.LayoutParams(-1,-2); qrp.topMargin=Ui.dp(this,12); root.addView(qrImage,qrp);
        Button download=new Button(this); download.setAllCaps(false); download.setText("Enregistrer le QR dans la galerie"); download.setTextColor(Ui.PRIMARY); download.setTextSize(15); download.setBackground(Ui.outlined(Ui.SURFACE,0xFFE1E3EA,this)); download.setOnClickListener(v->downloadQr());
        LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-1,Ui.dp(this,52)); dp.topMargin=Ui.dp(this,10); root.addView(download,dp);
        TextView print=Ui.text(this,"Conseil : imprimez ce QR et placez-le loin du lit (salle de bain, cuisine…).",13,Ui.MUTED,false); LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2); pp.topMargin=Ui.dp(this,8); root.addView(print,pp);

        section(root,"Snooze");
        snoozeSpinner = new Spinner(this); String[] options={"Off","2 min","5 min","10 min","15 min"}; ArrayAdapter<String> ad=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,options); snoozeSpinner.setAdapter(ad); snoozeSpinner.setBackground(Ui.rounded(Ui.SURFACE,16,this)); snoozeSpinner.setPadding(Ui.dp(this,12),0,Ui.dp(this,12),0); int idx=snoozeIndex(alarm.snoozeMinutes); snoozeSpinner.setSelection(idx);
        root.addView(snoozeSpinner,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));

        TextView security=Ui.text(this,"Sécurité : le QR code contient uniquement un jeton aléatoire local. Il ne contient ni nom, ni heure, ni donnée personnelle.",13,Ui.MUTED,false); security.setLineSpacing(0,1.2f); LinearLayout.LayoutParams secp=new LinearLayout.LayoutParams(-1,-2); secp.topMargin=Ui.dp(this,24); root.addView(security,secp);
        setContentView(scroll);
    }

    private LinearLayout card() { LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(Ui.dp(this,16),Ui.dp(this,14),Ui.dp(this,16),Ui.dp(this,14)); c.setBackground(Ui.rounded(Ui.SURFACE,18,this)); return c; }
    private void addCard(LinearLayout root,View v,int top) { LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.topMargin=Ui.dp(this,top); root.addView(v,p); }
    private void section(LinearLayout root,String s) { TextView t=Ui.text(this,s,14,Ui.MUTED,true); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.topMargin=Ui.dp(this,22); p.bottomMargin=Ui.dp(this,8); root.addView(t,p); }

    private void updateDayStyle(int i) { boolean on=(alarm.repeatMask&(1<<i))!=0; dayViews[i].setTextColor(on?Color.WHITE:Ui.TEXT); dayViews[i].setBackground(on?Ui.rounded(Ui.PRIMARY,22,this):Ui.rounded(Ui.SURFACE,22,this)); }

    private void chooseSound() {
        String[] choices={"Par défaut (alarme du téléphone)","Choisir un son dans mes fichiers"};
        new AlertDialog.Builder(this).setTitle("Son de l’alarme").setItems(choices,(d,which)-> {
            if(which==0){ soundUri=""; updateSoundText("Par défaut"); }
            else {
                Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("audio/*"); i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); startActivityForResult(i,REQ_AUDIO);
            }
        }).show();
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQ_AUDIO && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            Uri uri=data.getData();
            try { getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch(Exception ignored) {}
            soundUri=uri.toString(); updateSoundText(displayName(uri));
        }
    }

    private String displayName(Uri uri) {
        try(Cursor c=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)) { if(c!=null&&c.moveToFirst()) return c.getString(0); } catch(Exception ignored) {}
        return "Son personnalisé";
    }

    private void updateSoundText(String known) {
        if(soundUri==null||soundUri.isEmpty()) soundButton.setText("Par défaut (alarme du téléphone)");
        else soundButton.setText(known==null?"Son personnalisé":known);
    }

    private void downloadQr() {
        try {
            if(qrBitmap==null) qrBitmap=QrUtils.bitmap(alarm.qrToken,1000);
            QrUtils.saveToGallery(this,qrBitmap,"QR-Alarm-"+alarm.id+".png");
            Toast.makeText(this,"QR code enregistré dans la galerie.",Toast.LENGTH_LONG).show();
        } catch(Exception e) { Toast.makeText(this,"Impossible d’enregistrer l’image : "+e.getMessage(),Toast.LENGTH_LONG).show(); }
    }

    private int snoozeIndex(int m){ if(m==2)return 1;if(m==5)return 2;if(m==10)return 3;if(m==15)return 4;return 0; }
    private int snoozeValue(){ int p=snoozeSpinner.getSelectedItemPosition(); return p==1?2:p==2?5:p==3?10:p==4?15:0; }

    private void saveAlarm() {
        alarm.hour=timePicker.getHour(); alarm.minute=timePicker.getMinute(); alarm.label=label.getText().toString().trim(); alarm.soundUri=soundUri==null?"":soundUri; alarm.snoozeMinutes=snoozeValue(); alarm.enabled=true;
        AlarmStore.save(this,alarm);
        AlarmScheduler.schedule(this,alarm);
        Toast.makeText(this,"Alarme enregistrée pour "+alarm.timeText(),Toast.LENGTH_SHORT).show();
        finish();
    }
}
