package com.qralarm.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class EditAlarmActivity extends Activity {
    private static final int REQ_AUDIO = 701;

    private AlarmModel alarm;
    private boolean isNew;
    private boolean qrSavedThisSession;
    private boolean qrSaveRequired;

    private TimePicker timePicker;
    private EditText label;
    private final TextView[] dayViews = new TextView[7];

    private String soundUri = "";
    private String soundName = "System alarm";
    private Button soundButton;

    private Spinner snoozeSpinner;
    private SeekBar volumeSeek;
    private TextView volumeValue;
    private Spinner rampSpinner;

    private TextView qrModeButton;
    private TextView mathModeButton;
    private LinearLayout qrSection;
    private LinearLayout mathSection;
    private TextView qrGlobalButton;
    private TextView qrIndividualButton;
    private final TextView[] difficultyButtons = new TextView[3];
    private ImageView qrImage;
    private Button qrSaveButton;
    private Bitmap qrBitmap;
    private String shownQrToken = "";

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        long id = getIntent().getLongExtra(AlarmScheduler.EXTRA_ALARM_ID,-1);
        AlarmModel existing = id < 0 ? null : AlarmStore.get(this,id);
        isNew = existing == null;

        if (existing != null) {
            alarm = existing;
        } else {
            alarm = new AlarmModel();
            alarm.id = System.currentTimeMillis();
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MINUTE,5);
            alarm.hour=c.get(Calendar.HOUR_OF_DAY);
            alarm.minute=c.get(Calendar.MINUTE);
            alarm.enabled=true;
            alarm.useGlobalQr=true;
            alarm.wakeModeMask=AlarmModel.MODE_QR;
        }

        qrSaveRequired = isNew && alarm.usesQr();
        soundUri = alarm.soundUri == null ? "" : alarm.soundUri;
        soundName = alarm.soundName == null || alarm.soundName.trim().isEmpty()
                ? (soundUri.isEmpty() ? "System alarm" : "Custom sound") : alarm.soundName;
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Ui.BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this,18),Ui.dp(this,18),Ui.dp(this,18),Ui.dp(this,40));
        scroll.addView(root,new ScrollView.LayoutParams(-1,-2));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = Ui.text(this,"‹",40,Ui.TEXT,false);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v->finish());
        top.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));
        TextView title = Ui.text(this,isNew?"Nouvelle alarme":"Modifier l’alarme",24,Ui.TEXT,true);
        top.addView(title,new LinearLayout.LayoutParams(0,-2,1f));
        TextView save = Ui.text(this,"Enregistrer",15,Ui.PRIMARY,true);
        save.setGravity(Gravity.CENTER);
        save.setPadding(Ui.dp(this,10),0,Ui.dp(this,4),0);
        save.setOnClickListener(v->saveAlarm());
        top.addView(save,new LinearLayout.LayoutParams(-2,Ui.dp(this,48)));
        root.addView(top,new LinearLayout.LayoutParams(-1,-2));

        timePicker = new TimePicker(this);
        timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(this));
        timePicker.setHour(alarm.hour);
        timePicker.setMinute(alarm.minute);
        LinearLayout timeCard = card();
        timeCard.setGravity(Gravity.CENTER);
        timeCard.addView(timePicker,new LinearLayout.LayoutParams(-1,-2));
        addCard(root,timeCard,12);

        section(root,"Nom de l’alarme");
        label = new EditText(this);
        label.setText(alarm.label);
        label.setHint("Facultatif");
        label.setTextSize(17);
        label.setTextColor(Ui.TEXT);
        label.setHintTextColor(0xFF777C89);
        label.setSingleLine(true);
        label.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        label.setPadding(Ui.dp(this,16),0,Ui.dp(this,16),0);
        label.setBackground(Ui.rounded(Ui.SURFACE,16,this));
        root.addView(label,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));

        section(root,"Répétition");
        LinearLayout days = new LinearLayout(this);
        days.setOrientation(LinearLayout.HORIZONTAL);
        days.setGravity(Gravity.CENTER);
        String[] names={"L","M","M","J","V","S","D"};
        for (int i=0;i<7;i++) {
            final int bit=i;
            TextView d=Ui.text(this,names[i],14,Ui.TEXT,true);
            d.setGravity(Gravity.CENTER);
            d.setOnClickListener(v->{ alarm.repeatMask ^= (1<<bit); updateDayStyle(bit); });
            dayViews[i]=d;
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,Ui.dp(this,44),1f);
            if(i>0)p.leftMargin=Ui.dp(this,6);
            days.addView(d,p);
            updateDayStyle(i);
        }
        root.addView(days,new LinearLayout.LayoutParams(-1,-2));

        section(root,"Son");
        soundButton = button("", Ui.TEXT, Ui.SURFACE);
        soundButton.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        soundButton.setOnClickListener(v->chooseSound());
        updateSoundText();
        root.addView(soundButton,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));

        section(root,"Volume");
        LinearLayout volumeCard = card();
        LinearLayout volumeTop = new LinearLayout(this);
        volumeTop.setGravity(Gravity.CENTER_VERTICAL);
        volumeTop.addView(Ui.text(this,"Volume de l’alarme",16,Ui.TEXT,true),new LinearLayout.LayoutParams(0,-2,1f));
        volumeValue = Ui.text(this,alarm.volumePercent+"%",14,Ui.PRIMARY,true);
        volumeTop.addView(volumeValue);
        volumeCard.addView(volumeTop,new LinearLayout.LayoutParams(-1,-2));
        volumeSeek = new SeekBar(this);
        volumeSeek.setMax(100);
        volumeSeek.setProgress(alarm.volumePercent);
        volumeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser) { volumeValue.setText(progress+"%"); }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(-1,-2);
        vp.topMargin=Ui.dp(this,8);
        volumeCard.addView(volumeSeek,vp);
        TextView volumeHint=Ui.text(this,"Ce réglage ajuste le volume de lecture dans l’app, sans modifier le volume système du téléphone.",12,Ui.MUTED,false);
        LinearLayout.LayoutParams vhp=new LinearLayout.LayoutParams(-1,-2); vhp.topMargin=Ui.dp(this,6);
        volumeCard.addView(volumeHint,vhp);
        addCard(root,volumeCard,0);

        section(root,"Augmentation progressive");
        rampSpinner = spinner(new String[]{"Off","30 secondes","60 secondes","90 secondes","120 secondes"});
        rampSpinner.setSelection(rampIndex(alarm.volumeRampSeconds));
        root.addView(rampSpinner,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));

        section(root,"Mode de réveil");
        TextView modeHelp = Ui.text(this,"Vous pouvez activer QR Code, Math, ou les deux. Si les deux sont actifs, le QR est demandé en premier.",13,Ui.MUTED,false);
        root.addView(modeHelp,new LinearLayout.LayoutParams(-1,-2));

        LinearLayout modeRow = new LinearLayout(this);
        modeRow.setOrientation(LinearLayout.HORIZONTAL);
        modeRow.setGravity(Gravity.CENTER);
        qrModeButton = selectable("QR Code", () -> toggleMode(AlarmModel.MODE_QR));
        mathModeButton = selectable("Math", () -> toggleMode(AlarmModel.MODE_MATH));
        LinearLayout.LayoutParams m1 = new LinearLayout.LayoutParams(0,Ui.dp(this,50),1f);
        LinearLayout.LayoutParams m2 = new LinearLayout.LayoutParams(0,Ui.dp(this,50),1f); m2.leftMargin=Ui.dp(this,8);
        modeRow.addView(qrModeButton,m1); modeRow.addView(mathModeButton,m2);
        LinearLayout.LayoutParams mrp=new LinearLayout.LayoutParams(-1,-2); mrp.topMargin=Ui.dp(this,10);
        root.addView(modeRow,mrp);

        mathSection = new LinearLayout(this);
        mathSection.setOrientation(LinearLayout.VERTICAL);
        buildMathSection(mathSection);
        root.addView(mathSection,new LinearLayout.LayoutParams(-1,-2));

        qrSection = new LinearLayout(this);
        qrSection.setOrientation(LinearLayout.VERTICAL);
        buildQrSection(qrSection);
        root.addView(qrSection,new LinearLayout.LayoutParams(-1,-2));

        section(root,"Snooze");
        snoozeSpinner = spinner(new String[]{"Off","2 min","5 min","10 min","15 min"});
        snoozeSpinner.setSelection(snoozeIndex(alarm.snoozeMinutes));
        root.addView(snoozeSpinner,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));

        Button test = button("Tester l’alarme",Color.WHITE,Ui.PRIMARY);
        test.setOnClickListener(v->testAlarm());
        LinearLayout.LayoutParams testP=new LinearLayout.LayoutParams(-1,Ui.dp(this,58));
        testP.topMargin=Ui.dp(this,28);
        root.addView(test,testP);

        TextView testHint=Ui.text(this,"En mode test, un bouton « Arrêter le test » reste disponible même sans QR imprimé.",13,Ui.MUTED,false);
        LinearLayout.LayoutParams thp=new LinearLayout.LayoutParams(-1,-2); thp.topMargin=Ui.dp(this,8);
        root.addView(testHint,thp);

        TextView security=Ui.text(this,"Sécurité : les QR contiennent uniquement un jeton aléatoire local. Les alarmes et réglages restent sur l’appareil.",13,Ui.MUTED,false);
        security.setLineSpacing(0,1.2f);
        LinearLayout.LayoutParams secp=new LinearLayout.LayoutParams(-1,-2); secp.topMargin=Ui.dp(this,24);
        root.addView(security,secp);

        refreshModeUi();
        setContentView(scroll);
    }

    private void buildMathSection(LinearLayout parent) {
        section(parent,"Difficulté du problème de math");
        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        String[] labels={"Easy","Hard","Brutal"};
        String[] values={AlarmModel.MATH_EASY,AlarmModel.MATH_HARD,AlarmModel.MATH_BRUTAL};
        for(int i=0;i<3;i++) {
            final String difficulty=values[i];
            TextView b=selectable(labels[i],()->{ alarm.mathDifficulty=difficulty; updateDifficultyStyles(); });
            difficultyButtons[i]=b;
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,Ui.dp(this,46),1f);
            if(i>0)p.leftMargin=Ui.dp(this,7);
            row.addView(b,p);
        }
        parent.addView(row,new LinearLayout.LayoutParams(-1,-2));
        TextView hint=Ui.text(this,"Easy : 2 nombres à 2 chiffres. Hard : 2 nombres à 4 chiffres. Brutal : 5 nombres avec +, −, ×, ÷ et priorité des opérations ; résultat entre 1 et 200.",12,Ui.MUTED,false);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2); hp.topMargin=Ui.dp(this,8);
        parent.addView(hint,hp);
    }

    private void buildQrSection(LinearLayout parent) {
        section(parent,"QR code");
        TextView scopeHint=Ui.text(this,"Par défaut, toutes les alarmes utilisent le même QR. Vous pouvez choisir un QR unique pour cette alarme.",13,Ui.MUTED,false);
        parent.addView(scopeHint,new LinearLayout.LayoutParams(-1,-2));

        LinearLayout scopeRow=new LinearLayout(this);
        scopeRow.setOrientation(LinearLayout.HORIZONTAL);
        qrGlobalButton=selectable("QR commun",()->setQrScope(true));
        qrIndividualButton=selectable("QR individuel",()->setQrScope(false));
        LinearLayout.LayoutParams gp=new LinearLayout.LayoutParams(0,Ui.dp(this,48),1f);
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(0,Ui.dp(this,48),1f); ip.leftMargin=Ui.dp(this,8);
        scopeRow.addView(qrGlobalButton,gp); scopeRow.addView(qrIndividualButton,ip);
        LinearLayout.LayoutParams srp=new LinearLayout.LayoutParams(-1,-2); srp.topMargin=Ui.dp(this,10);
        parent.addView(scopeRow,srp);

        qrImage = new ImageView(this);
        qrImage.setAdjustViewBounds(true);
        qrImage.setBackgroundColor(Color.WHITE);
        qrImage.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12));
        LinearLayout.LayoutParams qrp=new LinearLayout.LayoutParams(-1,-2); qrp.topMargin=Ui.dp(this,12);
        parent.addView(qrImage,qrp);

        qrSaveButton=button("Enregistrer le QR dans la galerie",Ui.PRIMARY,Ui.SURFACE_2);
        qrSaveButton.setBackground(Ui.outlined(Ui.SURFACE_2,Ui.STROKE,this));
        qrSaveButton.setOnClickListener(v->downloadQr());
        LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-1,Ui.dp(this,52)); dp.topMargin=Ui.dp(this,10);
        parent.addView(qrSaveButton,dp);

        TextView print=Ui.text(this,"Imprimez-le et placez-le loin du lit. Pour une nouvelle alarme utilisant QR Code, l’enregistrement du QR dans la galerie est obligatoire avant de pouvoir enregistrer l’alarme.",12,Ui.MUTED,false);
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2); pp.topMargin=Ui.dp(this,8);
        parent.addView(print,pp);
    }

    private LinearLayout card() {
        LinearLayout c=new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(Ui.dp(this,16),Ui.dp(this,14),Ui.dp(this,16),Ui.dp(this,14));
        c.setBackground(Ui.rounded(Ui.SURFACE,18,this));
        return c;
    }

    private void addCard(LinearLayout root,View v,int top) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.topMargin=Ui.dp(this,top);
        root.addView(v,p);
    }

    private void section(LinearLayout root,String s) {
        TextView t=Ui.text(this,s,14,Ui.MUTED,true);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.topMargin=Ui.dp(this,22); p.bottomMargin=Ui.dp(this,8);
        root.addView(t,p);
    }

    private Button button(String text,int textColor,int background) {
        Button b=new Button(this);
        b.setAllCaps(false);
        b.setText(text);
        b.setTextColor(textColor);
        b.setTextSize(15);
        b.setGravity(Gravity.CENTER);
        b.setPadding(Ui.dp(this,16),0,Ui.dp(this,16),0);
        b.setBackground(Ui.rounded(background,16,this));
        return b;
    }

    private TextView selectable(String text,Runnable click) {
        TextView v=Ui.text(this,text,14,Ui.TEXT,true);
        v.setGravity(Gravity.CENTER);
        v.setBackground(Ui.rounded(Ui.SURFACE,15,this));
        v.setOnClickListener(x->click.run());
        return v;
    }

    private Spinner spinner(String[] values) {
        Spinner s=new Spinner(this);
        ArrayAdapter<String> ad=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,values) {
            @Override public View getView(int position,View convertView,ViewGroup parent) {
                View v=super.getView(position,convertView,parent);
                if(v instanceof TextView){ ((TextView)v).setTextColor(Ui.TEXT); ((TextView)v).setTextSize(15); }
                return v;
            }
            @Override public View getDropDownView(int position,View convertView,ViewGroup parent) {
                View v=super.getDropDownView(position,convertView,parent);
                if(v instanceof TextView){ ((TextView)v).setTextColor(Ui.TEXT); ((TextView)v).setBackgroundColor(Ui.SURFACE_2); ((TextView)v).setPadding(Ui.dp(EditAlarmActivity.this,14),Ui.dp(EditAlarmActivity.this,14),Ui.dp(EditAlarmActivity.this,14),Ui.dp(EditAlarmActivity.this,14)); }
                return v;
            }
        };
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(ad);
        s.setBackground(Ui.rounded(Ui.SURFACE,16,this));
        s.setPadding(Ui.dp(this,12),0,Ui.dp(this,12),0);
        return s;
    }

    private void updateDayStyle(int i) {
        boolean on=(alarm.repeatMask&(1<<i))!=0;
        dayViews[i].setTextColor(on?Color.WHITE:Ui.TEXT);
        dayViews[i].setBackground(on?Ui.rounded(Ui.PRIMARY,22,this):Ui.rounded(Ui.SURFACE,22,this));
    }

    private void toggleMode(int bit) {
        boolean on=(alarm.wakeModeMask&bit)!=0;
        int next= on ? (alarm.wakeModeMask & ~bit) : (alarm.wakeModeMask | bit);
        if((next&(AlarmModel.MODE_QR|AlarmModel.MODE_MATH))==0) {
            Toast.makeText(this,"Sélectionnez au moins un mode de réveil.",Toast.LENGTH_SHORT).show();
            return;
        }
        alarm.wakeModeMask=next;
        if (bit == AlarmModel.MODE_QR && !on && !qrSavedThisSession) qrSaveRequired=true;
        refreshModeUi();
    }

    private void setQrScope(boolean global) {
        if(alarm.useGlobalQr==global) return;
        alarm.useGlobalQr=global;
        if(!global && (alarm.customQrToken==null || alarm.customQrToken.isEmpty())) alarm.customQrToken=QrUtils.newToken();
        qrSavedThisSession=false;
        qrSaveRequired=true;
        qrSaveButton.setText("Enregistrer le QR dans la galerie");
        refreshQr();
        updateQrScopeStyles();
    }

    private void refreshModeUi() {
        styleSelectable(qrModeButton,alarm.usesQr());
        styleSelectable(mathModeButton,alarm.usesMath());
        qrSection.setVisibility(alarm.usesQr()?View.VISIBLE:View.GONE);
        mathSection.setVisibility(alarm.usesMath()?View.VISIBLE:View.GONE);
        updateDifficultyStyles();
        updateQrScopeStyles();
        if(alarm.usesQr()) refreshQr();
    }

    private void styleSelectable(TextView v,boolean selected) {
        v.setTextColor(selected?Color.WHITE:Ui.TEXT);
        v.setBackground(selected?Ui.rounded(Ui.PRIMARY,15,this):Ui.rounded(Ui.SURFACE,15,this));
    }

    private void updateDifficultyStyles() {
        if(difficultyButtons[0]==null) return;
        styleSelectable(difficultyButtons[0],AlarmModel.MATH_EASY.equals(alarm.mathDifficulty));
        styleSelectable(difficultyButtons[1],AlarmModel.MATH_HARD.equals(alarm.mathDifficulty));
        styleSelectable(difficultyButtons[2],AlarmModel.MATH_BRUTAL.equals(alarm.mathDifficulty));
    }

    private void updateQrScopeStyles() {
        if(qrGlobalButton==null) return;
        styleSelectable(qrGlobalButton,alarm.useGlobalQr);
        styleSelectable(qrIndividualButton,!alarm.useGlobalQr);
    }

    private void refreshQr() {
        try {
            shownQrToken=QrUtils.tokenFor(this,alarm);
            qrBitmap=QrUtils.bitmap(shownQrToken,800);
            qrImage.setImageBitmap(qrBitmap);
        } catch(Exception e) {
            Toast.makeText(this,"Impossible de générer le QR code.",Toast.LENGTH_LONG).show();
        }
    }

    private void chooseSound() {
        String[] choices={"Par défaut (alarme du téléphone)","Choisir un son dans mes fichiers"};
        new AlertDialog.Builder(this).setTitle("Son de l’alarme").setItems(choices,(d,which)-> {
            if(which==0){
                soundUri="";
                soundName="System alarm";
                updateSoundText();
            } else {
                Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("audio/*");
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(i,REQ_AUDIO);
            }
        }).show();
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQ_AUDIO && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            Uri uri=data.getData();
            try { getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION); }
            catch(Exception ignored) {}
            soundUri=uri.toString();
            soundName=displayName(uri);
            updateSoundText();
        }
    }

    private String displayName(Uri uri) {
        try(Cursor c=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)) {
            if(c!=null&&c.moveToFirst()) return c.getString(0);
        } catch(Exception ignored) {}
        return "Custom sound";
    }

    private void updateSoundText() {
        soundButton.setText(soundUri==null||soundUri.isEmpty()?"Par défaut · System alarm":soundName);
    }

    private void downloadQr() {
        try {
            collectForm(false);
            shownQrToken=QrUtils.tokenFor(this,alarm);
            qrBitmap=QrUtils.bitmap(shownQrToken,1000);
            String file=alarm.useGlobalQr?"NoChoiceAlarm-QR-commun.png":"NoChoiceAlarm-QR-"+alarm.id+".png";
            QrUtils.saveToGallery(this,qrBitmap,file);
            qrSavedThisSession=true;
            qrSaveRequired=false;
            qrSaveButton.setText("QR enregistré ✓");
            Toast.makeText(this,"QR code enregistré dans la galerie.",Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            Toast.makeText(this,"Impossible d’enregistrer l’image : "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private int snoozeIndex(int m){ if(m==2)return 1;if(m==5)return 2;if(m==10)return 3;if(m==15)return 4;return 0; }
    private int snoozeValue(){ int p=snoozeSpinner.getSelectedItemPosition(); return p==1?2:p==2?5:p==3?10:p==4?15:0; }
    private int rampIndex(int s){ if(s==30)return 1;if(s==60)return 2;if(s==90)return 3;if(s==120)return 4;return 0; }
    private int rampValue(){ int p=rampSpinner.getSelectedItemPosition(); return p==1?30:p==2?60:p==3?90:p==4?120:0; }

    private void collectForm(boolean forceEnabled) {
        alarm.hour=timePicker.getHour();
        alarm.minute=timePicker.getMinute();
        alarm.label=label.getText().toString().trim();
        alarm.soundUri=soundUri==null?"":soundUri;
        alarm.soundName=soundName==null||soundName.trim().isEmpty()?"System alarm":soundName;
        alarm.snoozeMinutes=snoozeValue();
        alarm.volumePercent=volumeSeek.getProgress();
        alarm.volumeRampSeconds=rampValue();
        if(!alarm.useGlobalQr && (alarm.customQrToken==null||alarm.customQrToken.isEmpty())) alarm.customQrToken=QrUtils.newToken();
        if(forceEnabled) alarm.enabled=true;
    }

    private void testAlarm() {
        collectForm(false);
        if(AlarmService.getActiveAlarmId(this)>=0) {
            Toast.makeText(this,"Impossible de lancer un test pendant qu’une vraie alarme sonne.",Toast.LENGTH_LONG).show();
            return;
        }
        String json=alarm.toJsonString();
        if(json.isEmpty()) {
            Toast.makeText(this,"Impossible de préparer le test.",Toast.LENGTH_LONG).show();
            return;
        }
        Intent service=new Intent(this,AlarmService.class)
                .setAction(AlarmService.ACTION_START)
                .putExtra(AlarmScheduler.EXTRA_ALARM_ID,alarm.id)
                .putExtra(AlarmService.EXTRA_TEST,true)
                .putExtra(AlarmService.EXTRA_ALARM_JSON,json);
        if(Build.VERSION.SDK_INT>=26) startForegroundService(service); else startService(service);

        Intent ring=new Intent(this,RingActivity.class)
                .putExtra(AlarmScheduler.EXTRA_ALARM_ID,alarm.id)
                .putExtra(AlarmService.EXTRA_TEST,true)
                .putExtra(AlarmService.EXTRA_ALARM_JSON,json);
        startActivity(ring);
    }

    private void saveAlarm() {
        collectForm(true);
        if(alarm.usesQr() && qrSaveRequired) {
            new AlertDialog.Builder(this)
                    .setTitle("Enregistrez d’abord le QR code")
                    .setMessage("Ce QR vient d’être créé ou modifié. Enregistrez-le dans votre galerie avant d’enregistrer l’alarme, afin de pouvoir l’imprimer et éviter de vous retrouver bloqué au réveil.")
                    .setPositiveButton("OK",null)
                    .show();
            return;
        }
        AlarmStore.save(this,alarm);
        AlarmScheduler.schedule(this,alarm);
        Toast.makeText(this,"Alarme enregistrée pour "+alarm.timeText(),Toast.LENGTH_SHORT).show();
        finish();
    }
}
