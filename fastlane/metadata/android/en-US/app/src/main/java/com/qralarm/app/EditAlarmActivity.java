package com.qralarm.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class EditAlarmActivity extends Activity {
    private static final int REQ_AUDIO = 701;

    private AlarmModel alarm;
    private boolean isNew;

    private int selectedHour;
    private int selectedMinute;
    private TextView hourDisplay;
    private TextView minuteDisplay;
    private EditText label;
    private final TextView[] dayViews = new TextView[7];

    private String soundUri = "";
    private String soundName = "System alarm";
    private Button soundButton;

    private Spinner snoozeSpinner;
    private SeekBar volumeSeek;
    private TextView volumeValue;
    private Spinner rampSpinner;
    private Switch vibrationSwitch;

    private LinearLayout qrModeButton;
    private LinearLayout mathModeButton;
    private LinearLayout qrSection;
    private LinearLayout mathSection;
    private TextView qrGlobalButton;
    private TextView qrIndividualButton;
    private final TextView[] difficultyButtons = new TextView[3];
    private TextView difficultyDescription;
    private ImageView qrImage;
    private Button qrSaveButton;
    private Button qrPrintButton;
    private Bitmap qrBitmap;
    private String shownQrToken = "";

    private SwipeSheetLayout sheet;
    private Button saveButton;
    private AlertDialog timeDialog;

    private MediaPlayer previewPlayer;
    private final Handler previewHandler = new Handler(Looper.getMainLooper());
    private final Runnable stopPreviewRunnable = this::stopVolumePreview;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.showStatusBar(this);
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            getWindow().setAttributes(lp);
        }

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
            alarm.vibrationEnabled=false;
        }

        selectedHour = alarm.hour;
        selectedMinute = alarm.minute;

        soundUri = alarm.soundUri == null ? "" : alarm.soundUri;
        soundName = alarm.soundName == null || alarm.soundName.trim().isEmpty()
                ? (soundUri.isEmpty() ? "System alarm" : "Custom sound") : alarm.soundName;
        buildUi();
    }

    private void buildUi() {
        FrameLayout screen = new FrameLayout(this);
        screen.setBackgroundColor(Color.TRANSPARENT);

        View scrim = new View(this);
        scrim.setBackgroundColor(0x99000000);
        screen.addView(scrim,new FrameLayout.LayoutParams(-1,-1));

        sheet = new SwipeSheetLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setBackground(Ui.rounded(Ui.BG,26,this));
        sheet.setElevation(Ui.dp(this,18));
        sheet.setPadding(0,Ui.dp(this,8),0,0);

        FrameLayout.LayoutParams sheetParams = new FrameLayout.LayoutParams(-1,-1,Gravity.BOTTOM);
        sheetParams.topMargin = Ui.dp(this,14);
        screen.addView(sheet,sheetParams);

        LinearLayout handleBox = new LinearLayout(this);
        handleBox.setGravity(Gravity.CENTER);
        View handle = new View(this);
        handle.setBackground(Ui.rounded(0xFF666B78,3,this));
        handleBox.addView(handle,new LinearLayout.LayoutParams(Ui.dp(this,44),Ui.dp(this,4)));
        sheet.addView(handleBox,new LinearLayout.LayoutParams(-1,Ui.dp(this,28)));

        FrameLayout contentFrame = new FrameLayout(this);
        sheet.addView(contentFrame,new LinearLayout.LayoutParams(-1,0,1f));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setBackgroundColor(Ui.BG);
        contentFrame.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        sheet.setScrollChild(scroll);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Ui.dp(this,20),Ui.dp(this,4),Ui.dp(this,20),Ui.dp(this,32));
        scroll.addView(root,new ScrollView.LayoutParams(-1,-2));

        TextView title = Ui.displayText(this,isNew?getString(R.string.new_alarm):getString(R.string.edit_alarm),25,Ui.TEXT);
        root.addView(title,new LinearLayout.LayoutParams(-1,-2));

        buildTimePicker(root);

        section(root,getString(R.string.alarm_name));
        label = new EditText(this);
        label.setText(alarm.label);
        label.setHint(R.string.alarm_name_hint);
        label.setTextSize(17);
        label.setTextColor(Ui.TEXT);
        label.setHintTextColor(0xFF777C89);
        label.setSingleLine(true);
        label.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        label.setPadding(Ui.dp(this,16),0,Ui.dp(this,16),0);
        label.setBackground(Ui.rounded(Ui.SURFACE,16,this));
        root.addView(label,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));

        section(root,getString(R.string.repetition));
        LinearLayout days = new LinearLayout(this);
        days.setOrientation(LinearLayout.HORIZONTAL);
        days.setGravity(Gravity.CENTER);
        String[] names=getResources().getStringArray(R.array.repeat_day_initials);
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

        section(root,getString(R.string.sound));
        soundButton = button("", Ui.TEXT, Ui.SURFACE);
        soundButton.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        soundButton.setOnClickListener(v->chooseSound());
        updateSoundText();
        root.addView(soundButton,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));

        section(root,getString(R.string.volume));
        LinearLayout volumeCard = card();
        LinearLayout volumeTop = new LinearLayout(this);
        volumeTop.setGravity(Gravity.CENTER_VERTICAL);
        volumeTop.addView(Ui.text(this,getString(R.string.alarm_volume),16,Ui.TEXT,true),new LinearLayout.LayoutParams(0,-2,1f));
        volumeValue = Ui.text(this,alarm.volumePercent+"%",14,Ui.ACCENT_START,true);
        volumeTop.addView(volumeValue);
        volumeCard.addView(volumeTop,new LinearLayout.LayoutParams(-1,-2));

        volumeSeek = new SeekBar(this);
        volumeSeek.setMax(100);
        volumeSeek.setProgress(alarm.volumePercent);
        volumeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser) {
                volumeValue.setText(progress+"%");
                if (fromUser) {
                    startVolumePreview();
                    updatePreviewVolume(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {
                startVolumePreview();
                updatePreviewVolume(seekBar.getProgress());
            }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                previewHandler.removeCallbacks(stopPreviewRunnable);
                previewHandler.postDelayed(stopPreviewRunnable,900);
            }
        });
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(-1,-2);
        vp.topMargin=Ui.dp(this,8);
        volumeCard.addView(volumeSeek,vp);
        TextView volumeHint=Ui.text(this,getString(R.string.volume_hint),12,Ui.MUTED,false);
        LinearLayout.LayoutParams vhp=new LinearLayout.LayoutParams(-1,-2); vhp.topMargin=Ui.dp(this,6);
        volumeCard.addView(volumeHint,vhp);
        addCard(root,volumeCard,0);

        section(root,getString(R.string.gradual_volume));
        rampSpinner = spinner(getResources().getStringArray(R.array.ramp_options));
        rampSpinner.setSelection(rampIndex(alarm.volumeRampSeconds));
        root.addView(rampSpinner,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));

        section(root,getString(R.string.vibration));
        LinearLayout vibrationCard = new LinearLayout(this);
        vibrationCard.setGravity(Gravity.CENTER_VERTICAL);
        vibrationCard.setPadding(Ui.dp(this,16),Ui.dp(this,10),Ui.dp(this,10),Ui.dp(this,10));
        vibrationCard.setBackground(Ui.rounded(Ui.SURFACE,16,this));
        vibrationCard.addView(Ui.text(this,getString(R.string.vibrate_during_alarm),16,Ui.TEXT,true),new LinearLayout.LayoutParams(0,-2,1f));
        vibrationSwitch = new Switch(this);
        vibrationSwitch.setChecked(alarm.vibrationEnabled);
        vibrationCard.addView(vibrationSwitch,new LinearLayout.LayoutParams(-2,-2));
        root.addView(vibrationCard,new LinearLayout.LayoutParams(-1,Ui.dp(this,62)));

        section(root,getString(R.string.wake_up_mode));
        TextView modeHelp = Ui.text(this,getString(R.string.wake_mode_help),13,Ui.MUTED,false);
        root.addView(modeHelp,new LinearLayout.LayoutParams(-1,-2));

        LinearLayout modeRow = new LinearLayout(this);
        modeRow.setOrientation(LinearLayout.HORIZONTAL);
        modeRow.setGravity(Gravity.CENTER);
        qrModeButton = selectableWithIcon(getString(R.string.mode_qr), R.drawable.ic_qr_mode, () -> toggleMode(AlarmModel.MODE_QR));
        mathModeButton = selectableWithIcon(getString(R.string.mode_math), R.drawable.ic_math_mode, () -> toggleMode(AlarmModel.MODE_MATH));
        LinearLayout.LayoutParams m1 = new LinearLayout.LayoutParams(0,Ui.dp(this,52),1f);
        LinearLayout.LayoutParams m2 = new LinearLayout.LayoutParams(0,Ui.dp(this,52),1f); m2.leftMargin=Ui.dp(this,8);
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

        section(root,getString(R.string.snooze));
        snoozeSpinner = spinner(getResources().getStringArray(R.array.snooze_options));
        snoozeSpinner.setSelection(snoozeIndex(alarm.snoozeMinutes));
        root.addView(snoozeSpinner,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));

        Button test = outlinedButton(getString(R.string.test_alarm));
        test.setOnClickListener(v->testAlarm());
        LinearLayout.LayoutParams testP=new LinearLayout.LayoutParams(-1,Ui.dp(this,58));
        testP.topMargin=Ui.dp(this,32);
        root.addView(test,testP);

        TextView security=Ui.text(this,getString(R.string.security_note),13,Ui.MUTED,false);
        security.setLineSpacing(0,1.2f);
        LinearLayout.LayoutParams secp=new LinearLayout.LayoutParams(-1,-2); secp.topMargin=Ui.dp(this,24);
        root.addView(security,secp);

        LinearLayout saveBar = new LinearLayout(this);
        saveBar.setPadding(Ui.dp(this,20),Ui.dp(this,10),Ui.dp(this,20),Ui.dp(this,14));
        saveBar.setBackgroundColor(Ui.BG);
        Button save = button(getString(R.string.save),Color.WHITE,Ui.PRIMARY);
        save.setBackground(Ui.gradient(16,this));
        save.setTextSize(17);
        save.setOnClickListener(v->saveAlarm());
        saveButton = save;
        saveBar.addView(save,new LinearLayout.LayoutParams(-1,Ui.dp(this,60)));
        sheet.addView(saveBar,new LinearLayout.LayoutParams(-1,Ui.dp(this,84)));

        sheet.setCallback(new SwipeSheetLayout.Callback() {
            @Override public void onExpandedChanged(boolean expanded) {
                // Editing is allowed whether the sheet is expanded or not.
            }
            @Override public void onDismissed() {
                closeWithoutSaving();
            }
        });

        scrim.setOnClickListener(v -> sheet.dismissSheet());
        refreshModeUi();
        setContentView(screen);
        Ui.applySystemBarInsets(screen,0,0,0,0);
        screen.post(() -> sheet.setPeekHeightPx(Math.max(1,screen.getHeight()/2)));
    }

    private void buildTimePicker(LinearLayout root) {
        LinearLayout display = new LinearLayout(this);
        display.setGravity(Gravity.CENTER);
        display.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams displayP = new LinearLayout.LayoutParams(-1,-2);
        displayP.topMargin=Ui.dp(this,14);
        displayP.bottomMargin=Ui.dp(this,10);
        root.addView(display,displayP);

        hourDisplay = Ui.displayText(this,two(selectedHour),38,Ui.TEXT);
        hourDisplay.setGravity(Gravity.CENTER);
        minuteDisplay = Ui.displayText(this,two(selectedMinute),38,Ui.TEXT);
        minuteDisplay.setGravity(Gravity.CENTER);
        TextView colon = Ui.displayText(this,":",38,Ui.MUTED);
        colon.setGravity(Gravity.CENTER);

        hourDisplay.setBackground(Ui.rounded(Ui.SURFACE_2,16,this));
        minuteDisplay.setBackground(Ui.rounded(Ui.SURFACE_2,16,this));
        hourDisplay.setOnClickListener(v->showTimePickerDialog(MinimalClockView.MODE_HOUR));
        minuteDisplay.setOnClickListener(v->showTimePickerDialog(MinimalClockView.MODE_MINUTE));

        display.addView(hourDisplay,new LinearLayout.LayoutParams(Ui.dp(this,88),Ui.dp(this,66)));
        display.addView(colon,new LinearLayout.LayoutParams(Ui.dp(this,30),Ui.dp(this,66)));
        display.addView(minuteDisplay,new LinearLayout.LayoutParams(Ui.dp(this,88),Ui.dp(this,66)));
    }

    private void showTimePickerDialog(int initialMode) {
        final MinimalClockView picker = new MinimalClockView(this);
        picker.setTime(selectedHour,selectedMinute);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(Ui.dp(this,20),Ui.dp(this,18),Ui.dp(this,20),Ui.dp(this,6));

        TextView title = Ui.displayText(this,getString(R.string.select_time).toUpperCase(Locale.getDefault()),13,Ui.MUTED);
        content.addView(title,new LinearLayout.LayoutParams(-1,-2));

        LinearLayout display = new LinearLayout(this);
        display.setGravity(Gravity.CENTER);
        display.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(-1,-2);
        dp.topMargin=Ui.dp(this,14);
        dp.bottomMargin=Ui.dp(this,12);
        content.addView(display,dp);

        final TextView dialogHour = Ui.displayText(this,two(selectedHour),34,Ui.TEXT);
        dialogHour.setGravity(Gravity.CENTER);
        final TextView dialogMinute = Ui.displayText(this,two(selectedMinute),34,Ui.TEXT);
        dialogMinute.setGravity(Gravity.CENTER);
        TextView colon = Ui.displayText(this,":",34,Ui.MUTED);
        colon.setGravity(Gravity.CENTER);

        display.addView(dialogHour,new LinearLayout.LayoutParams(Ui.dp(this,82),Ui.dp(this,60)));
        display.addView(colon,new LinearLayout.LayoutParams(Ui.dp(this,28),Ui.dp(this,60)));
        display.addView(dialogMinute,new LinearLayout.LayoutParams(Ui.dp(this,82),Ui.dp(this,60)));

        picker.setListener((hour,minute,mode)-> {
            dialogHour.setText(two(hour));
            dialogMinute.setText(two(minute));
            updateDialogTimeStyle(dialogHour,dialogMinute,mode);
        });
        dialogHour.setOnClickListener(v->picker.setMode(MinimalClockView.MODE_HOUR));
        dialogMinute.setOnClickListener(v->picker.setMode(MinimalClockView.MODE_MINUTE));
        picker.setMode(initialMode);

        LinearLayout.LayoutParams pickerParams = new LinearLayout.LayoutParams(-1,Ui.dp(this,302));
        content.addView(picker,pickerParams);

        final Runnable applyPickedTime = () -> {
            selectedHour=picker.getHour();
            selectedMinute=picker.getMinute();
            hourDisplay.setText(two(selectedHour));
            minuteDisplay.setText(two(selectedMinute));
        };

        // If the user taps outside the dialog without pressing OK, the chosen time is kept.
        // If that tap lands on the "Save" button, the alarm is saved right away.
        final AlertDialog dialog = new AlertDialog(this) {
            @Override public boolean dispatchTouchEvent(MotionEvent ev) {
                if (ev.getActionMasked()==MotionEvent.ACTION_DOWN && isOutside(getWindow()!=null?getWindow().getDecorView():null,ev)) {
                    applyPickedTime.run();
                    boolean onSave=isOver(saveButton,ev);
                    dismiss();
                    if (onSave) saveAlarm();
                    return true;
                }
                return super.dispatchTouchEvent(ev);
            }
        };
        dialog.setView(content);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,getString(R.string.cancel),(d,which)->{});
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,getString(R.string.ok),(d,which)->applyPickedTime.run());
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Ui.ACCENT_START);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Ui.ACCENT_START);
        });
        dialog.setOnDismissListener(d -> { if (timeDialog==dialog) timeDialog=null; });
        timeDialog = dialog;
        dialog.show();
        if (dialog.getWindow()!=null) {
            dialog.getWindow().setBackgroundDrawable(Ui.rounded(Ui.SURFACE,24,this));
            int maxWidth=getResources().getDisplayMetrics().widthPixels-Ui.dp(this,32);
            dialog.getWindow().setLayout(Math.min(Ui.dp(this,380),maxWidth),ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setDimAmount(0.68f);
        }
    }

    private static boolean isOutside(View decor,MotionEvent ev) {
        if (decor==null) return false;
        int[] loc=new int[2];
        decor.getLocationOnScreen(loc);
        float x=ev.getRawX(), y=ev.getRawY();
        return x<loc[0] || y<loc[1] || x>loc[0]+decor.getWidth() || y>loc[1]+decor.getHeight();
    }

    private static boolean isOver(View target,MotionEvent ev) {
        if (target==null || !target.isShown()) return false;
        int[] loc=new int[2];
        target.getLocationOnScreen(loc);
        float x=ev.getRawX(), y=ev.getRawY();
        return x>=loc[0] && y>=loc[1] && x<=loc[0]+target.getWidth() && y<=loc[1]+target.getHeight();
    }

    private void updateDialogTimeStyle(TextView hour,TextView minute,int mode) {
        boolean hourActive=mode==MinimalClockView.MODE_HOUR;
        hour.setBackground(hourActive?Ui.gradient(16,this):Ui.rounded(Ui.SURFACE_2,16,this));
        minute.setBackground(!hourActive?Ui.gradient(16,this):Ui.rounded(Ui.SURFACE_2,16,this));
        hour.setTextColor(hourActive?Color.WHITE:Ui.TEXT);
        minute.setTextColor(!hourActive?Color.WHITE:Ui.TEXT);
    }

    private void buildMathSection(LinearLayout parent) {
        section(parent,getString(R.string.math_difficulty));
        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        String[] labels={getString(R.string.difficulty_easy),getString(R.string.difficulty_hard),getString(R.string.difficulty_brutal)};
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
        difficultyDescription=Ui.text(this,"",12,Ui.MUTED,false);
        difficultyDescription.setLineSpacing(0,1.15f);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2); hp.topMargin=Ui.dp(this,8);
        parent.addView(difficultyDescription,hp);
    }

    private void buildQrSection(LinearLayout parent) {
        section(parent,getString(R.string.mode_qr));
        TextView scopeHint=Ui.text(this,getString(R.string.qr_scope_help),13,Ui.MUTED,false);
        parent.addView(scopeHint,new LinearLayout.LayoutParams(-1,-2));

        LinearLayout scopeRow=new LinearLayout(this);
        scopeRow.setOrientation(LinearLayout.HORIZONTAL);
        qrGlobalButton=selectable(getString(R.string.qr_shared),()->setQrScope(true));
        qrIndividualButton=selectable(getString(R.string.qr_individual),()->setQrScope(false));
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

        qrSaveButton=button(getString(R.string.save_qr_gallery),Ui.ACCENT_START,Ui.SURFACE_2);
        qrSaveButton.setBackground(Ui.outlined(Ui.SURFACE_2,Ui.ACCENT_START,16,this));
        qrSaveButton.setOnClickListener(v->downloadQr());
        LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-1,Ui.dp(this,52)); dp.topMargin=Ui.dp(this,10);
        parent.addView(qrSaveButton,dp);

        qrPrintButton=button(getString(R.string.print_qr_a4),Ui.TEXT,Color.TRANSPARENT);
        qrPrintButton.setBackground(Ui.outlined(Color.TRANSPARENT,Ui.ACCENT_START,16,this));
        qrPrintButton.setOnClickListener(v->printQr());
        LinearLayout.LayoutParams printButtonParams=new LinearLayout.LayoutParams(-1,Ui.dp(this,52));
        printButtonParams.topMargin=Ui.dp(this,8);
        parent.addView(qrPrintButton,printButtonParams);

        TextView print=Ui.text(this,getString(R.string.qr_export_help),12,Ui.MUTED,false);
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
        TextView t=Ui.displayText(this,s,13,Ui.MUTED);
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

    private Button outlinedButton(String text) {
        Button b=button(text,Ui.TEXT,Color.TRANSPARENT);
        b.setBackground(Ui.outlined(Color.TRANSPARENT,Ui.ACCENT_START,16,this));
        return b;
    }

    private TextView selectable(String text,Runnable click) {
        TextView v=Ui.text(this,text,14,Ui.TEXT,true);
        v.setGravity(Gravity.CENTER);
        v.setBackground(Ui.rounded(Ui.SURFACE,15,this));
        v.setOnClickListener(x->click.run());
        return v;
    }

    private LinearLayout selectableWithIcon(String text,int icon,Runnable click) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER);
        box.setBackground(Ui.rounded(Ui.SURFACE,15,this));
        box.setClickable(true);
        box.setFocusable(true);

        ImageView image = new ImageView(this);
        image.setImageResource(icon);
        image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        box.addView(image,new LinearLayout.LayoutParams(Ui.dp(this,20),Ui.dp(this,20)));

        TextView label = Ui.text(this,text,14,Ui.TEXT,true);
        label.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2,Ui.dp(this,24));
        lp.leftMargin=Ui.dp(this,8);
        box.addView(label,lp);
        box.setTag(label);
        box.setOnClickListener(v->click.run());
        return box;
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
        dayViews[i].setBackground(on?Ui.gradient(22,this):Ui.rounded(Ui.SURFACE,22,this));
    }

    private void toggleMode(int bit) {
        boolean on=(alarm.wakeModeMask&bit)!=0;
        int next= on ? (alarm.wakeModeMask & ~bit) : (alarm.wakeModeMask | bit);
        if((next&(AlarmModel.MODE_QR|AlarmModel.MODE_MATH))==0) {
            Toast.makeText(this,R.string.select_wake_mode,Toast.LENGTH_SHORT).show();
            return;
        }
        alarm.wakeModeMask=next;
        refreshModeUi();
    }

    private void setQrScope(boolean global) {
        if(alarm.useGlobalQr==global) return;
        alarm.useGlobalQr=global;
        if(!global && (alarm.customQrToken==null || alarm.customQrToken.isEmpty())) alarm.customQrToken=QrUtils.newToken();
        qrSaveButton.setText(R.string.save_qr_gallery);
        qrSaveButton.setTextColor(Ui.ACCENT_START);
        qrSaveButton.setBackground(Ui.outlined(Ui.SURFACE_2,Ui.ACCENT_START,16,this));
        if (qrPrintButton != null) {
            qrPrintButton.setText(R.string.print_qr_a4);
            qrPrintButton.setTextColor(Ui.TEXT);
            qrPrintButton.setBackground(Ui.outlined(Color.TRANSPARENT,Ui.ACCENT_START,16,this));
        }
        refreshQr();
        updateQrScopeStyles();
    }

    private void refreshModeUi() {
        styleModeButton(qrModeButton,alarm.usesQr());
        styleModeButton(mathModeButton,alarm.usesMath());
        qrSection.setVisibility(alarm.usesQr()?View.VISIBLE:View.GONE);
        mathSection.setVisibility(alarm.usesMath()?View.VISIBLE:View.GONE);
        updateDifficultyStyles();
        updateQrScopeStyles();
        if(alarm.usesQr()) refreshQr();
    }

    private void styleModeButton(LinearLayout v,boolean selected) {
        if (v == null) return;
        v.setBackground(selected?Ui.gradient(15,this):Ui.rounded(Ui.SURFACE,15,this));
        Object tag=v.getTag();
        if(tag instanceof TextView) ((TextView)tag).setTextColor(selected?Color.WHITE:Ui.TEXT);
        for(int i=0;i<v.getChildCount();i++) if(v.getChildAt(i) instanceof ImageView) v.getChildAt(i).setAlpha(selected?1f:.76f);
    }

    private void styleSelectable(TextView v,boolean selected) {
        v.setTextColor(selected?Color.WHITE:Ui.TEXT);
        v.setBackground(selected?Ui.gradient(15,this):Ui.rounded(Ui.SURFACE,15,this));
    }

    private void updateDifficultyStyles() {
        if(difficultyButtons[0]==null) return;
        styleSelectable(difficultyButtons[0],AlarmModel.MATH_EASY.equals(alarm.mathDifficulty));
        styleSelectable(difficultyButtons[1],AlarmModel.MATH_HARD.equals(alarm.mathDifficulty));
        styleSelectable(difficultyButtons[2],AlarmModel.MATH_BRUTAL.equals(alarm.mathDifficulty));
        if (difficultyDescription != null) {
            if (AlarmModel.MATH_HARD.equals(alarm.mathDifficulty)) {
                difficultyDescription.setText(R.string.math_hard_description);
            } else if (AlarmModel.MATH_BRUTAL.equals(alarm.mathDifficulty)) {
                difficultyDescription.setText(R.string.math_brutal_description);
            } else {
                difficultyDescription.setText(R.string.math_easy_description);
            }
        }
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
            Toast.makeText(this,R.string.qr_generation_failed,Toast.LENGTH_LONG).show();
        }
    }

    private void chooseSound() {
        stopVolumePreview();
        String[] choices={getString(R.string.default_phone_alarm),getString(R.string.choose_sound_files)};
        new AlertDialog.Builder(this).setTitle(R.string.alarm_sound_title).setItems(choices,(d,which)-> {
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
        if(soundUri==null||soundUri.isEmpty()) soundButton.setText(R.string.default_system_alarm_display);
        else if(soundName==null||soundName.trim().isEmpty()||"Custom sound".equals(soundName)) soundButton.setText(R.string.custom_sound);
        else soundButton.setText(soundName);
    }

    private void startVolumePreview() {
        previewHandler.removeCallbacks(stopPreviewRunnable);
        if (previewPlayer != null) return;
        if (AlarmService.getActiveAlarmId(this) >= 0) return;
        try {
            Uri uri = soundUri == null || soundUri.isEmpty() ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) : Uri.parse(soundUri);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            previewPlayer = new MediaPlayer();
            previewPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            previewPlayer.setDataSource(this,uri);
            previewPlayer.setLooping(true);
            previewPlayer.prepare();
            updatePreviewVolume(volumeSeek.getProgress());
            previewPlayer.start();
        } catch(Exception e) {
            stopVolumePreview();
        }
    }

    private void updatePreviewVolume(int percent) {
        if (previewPlayer == null) return;
        float v=Math.max(0f,Math.min(1f,percent/100f));
        try { previewPlayer.setVolume(v,v); } catch(Exception ignored) {}
    }

    private void stopVolumePreview() {
        previewHandler.removeCallbacks(stopPreviewRunnable);
        if (previewPlayer != null) {
            try { previewPlayer.stop(); } catch(Exception ignored) {}
            try { previewPlayer.release(); } catch(Exception ignored) {}
            previewPlayer=null;
        }
    }

    private void downloadQr() {
        try {
            collectForm(false);
            shownQrToken=QrUtils.tokenFor(this,alarm);
            qrBitmap=QrUtils.bitmap(shownQrToken,1000);
            String file=alarm.useGlobalQr?"NoChoiceAlarm-QR-commun.png":"NoChoiceAlarm-QR-"+alarm.id+".png";
            QrUtils.saveToGallery(this,qrBitmap,file);
            qrSaveButton.setText(R.string.qr_saved);
            qrSaveButton.setTextColor(Color.WHITE);
            qrSaveButton.setBackground(Ui.gradient(16,this));
            Toast.makeText(this,R.string.qr_saved_gallery,Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            Toast.makeText(this,getString(R.string.qr_save_error,e.getMessage()),Toast.LENGTH_LONG).show();
        }
    }

    private void printQr() {
        try {
            collectForm(false);
            shownQrToken=QrUtils.tokenFor(this,alarm);
            qrBitmap=QrUtils.bitmap(shownQrToken,1200);
            QrUtils.printOnA4(this,qrBitmap,getString(R.string.qr_print_job));
            if (qrPrintButton != null) {
                qrPrintButton.setText(R.string.print_action_done);
                qrPrintButton.setTextColor(Color.WHITE);
                qrPrintButton.setBackground(Ui.gradient(16,this));
            }
            Toast.makeText(this,R.string.qr_print_opened,Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            Toast.makeText(this,getString(R.string.qr_print_error,e.getMessage()),Toast.LENGTH_LONG).show();
        }
    }

    private int snoozeIndex(int m){ if(m==2)return 1;if(m==5)return 2;if(m==10)return 3;if(m==15)return 4;return 0; }
    private int snoozeValue(){ int p=snoozeSpinner.getSelectedItemPosition(); return p==1?2:p==2?5:p==3?10:p==4?15:0; }
    private int rampIndex(int s){ if(s==30)return 1;if(s==60)return 2;if(s==90)return 3;if(s==120)return 4;return 0; }
    private int rampValue(){ int p=rampSpinner.getSelectedItemPosition(); return p==1?30:p==2?60:p==3?90:p==4?120:0; }

    private void collectForm(boolean forceEnabled) {
        alarm.hour=selectedHour;
        alarm.minute=selectedMinute;
        alarm.label=label.getText().toString().trim();
        alarm.soundUri=soundUri==null?"":soundUri;
        alarm.soundName=soundName==null||soundName.trim().isEmpty()?"System alarm":soundName;
        alarm.snoozeMinutes=snoozeValue();
        alarm.volumePercent=volumeSeek.getProgress();
        alarm.volumeRampSeconds=rampValue();
        alarm.vibrationEnabled=vibrationSwitch.isChecked();
        if(!alarm.useGlobalQr && (alarm.customQrToken==null||alarm.customQrToken.isEmpty())) alarm.customQrToken=QrUtils.newToken();
        if(forceEnabled) alarm.enabled=true;
    }

    private void testAlarm() {
        stopVolumePreview();
        collectForm(false);
        if(AlarmService.getActiveAlarmId(this)>=0) {
            Toast.makeText(this,R.string.test_real_alarm_active,Toast.LENGTH_LONG).show();
            return;
        }
        String json=alarm.toJsonString();
        if(json.isEmpty()) {
            Toast.makeText(this,R.string.test_prepare_failed,Toast.LENGTH_LONG).show();
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
        stopVolumePreview();
        if (timeDialog!=null) {
            AlertDialog open=timeDialog;
            timeDialog=null;
            try { open.dismiss(); } catch(Exception ignored) {}
        }
        collectForm(true);
        AlarmStore.save(this,alarm);
        AlarmScheduler.schedule(this,alarm);
        AlarmStatusNotifier.update(this);
        Toast.makeText(this,AlarmScheduler.timeUntilText(this,alarm),Toast.LENGTH_LONG).show();
        finish();
        overridePendingTransition(0,0);
    }

    private void closeWithoutSaving() {
        stopVolumePreview();
        finish();
        overridePendingTransition(0,0);
    }

    private String two(int value) {
        return String.format(Locale.ROOT,"%02d",value);
    }

    @Override public void onBackPressed() {
        if (sheet != null) sheet.dismissSheet();
        else closeWithoutSaving();
    }

    @Override protected void onPause() {
        stopVolumePreview();
        super.onPause();
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) Ui.showStatusBar(this);
    }
}
