package com.qralarm.app;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AlarmListAdapter extends BaseAdapter {
    public interface Listener {
        void onToggle(AlarmModel alarm, boolean enabled);
        void onDelete(AlarmModel alarm);
    }

    private final Context context;
    private final Listener listener;
    private List<AlarmModel> items = new ArrayList<>();

    public AlarmListAdapter(Context c, Listener l) {
        context = c;
        listener = l;
    }

    public void setItems(List<AlarmModel> a) {
        items = a;
        notifyDataSetChanged();
    }

    @Override public int getCount() { return items.size(); }
    @Override public AlarmModel getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return items.get(position).id; }

    @Override public View getView(int position, View convert, ViewGroup parent) {
        AlarmModel a = getItem(position);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(context,18), Ui.dp(context,16), Ui.dp(context,14), Ui.dp(context,14));
        card.setBackground(a.enabled
                ? Ui.outlined(Ui.SURFACE_ACTIVE, Ui.PRIMARY, 20, context)
                : Ui.rounded(Ui.SURFACE,20,context));
        card.setElevation(Ui.dp(context,a.enabled ? 5 : 2));

        LinearLayout top = new LinearLayout(context);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout identity = new LinearLayout(context);
        identity.setOrientation(LinearLayout.VERTICAL);

        if (a.enabled) {
            TextView active = Ui.text(context,"ACTIVE",11,Ui.PRIMARY,true);
            active.setLetterSpacing(.12f);
            identity.addView(active,new LinearLayout.LayoutParams(-2,-2));
        }

        TextView time = Ui.text(context, a.timeText(), 34, a.enabled ? Ui.TEXT : 0xFF777C89, true);
        LinearLayout.LayoutParams timeP = new LinearLayout.LayoutParams(-2,-2);
        timeP.topMargin = a.enabled ? Ui.dp(context,2) : 0;
        identity.addView(time,timeP);

        String labelText = a.label == null || a.label.trim().isEmpty() ? "Alarme" : a.label.trim();
        TextView label = Ui.text(context, labelText, 16, a.enabled ? Ui.TEXT : 0xFF777C89, true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2,-2);
        lp.topMargin = Ui.dp(context,2);
        identity.addView(label, lp);

        TextView repeat = Ui.text(context, a.repeatText(), 13, a.enabled ? 0xFFB6B0F5 : Ui.MUTED, false);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-2,-2);
        rp.topMargin = Ui.dp(context,3);
        identity.addView(repeat, rp);

        top.addView(identity, new LinearLayout.LayoutParams(0,-2,1f));

        ImageButton trash = new ImageButton(context);
        trash.setImageResource(R.drawable.ic_delete);
        trash.setBackground(Ui.rounded(Ui.SURFACE_2,16,context));
        trash.setPadding(Ui.dp(context,10),Ui.dp(context,10),Ui.dp(context,10),Ui.dp(context,10));
        trash.setContentDescription("Supprimer l’alarme");
        trash.setFocusable(false);
        trash.setOnClickListener(v -> listener.onDelete(a));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(Ui.dp(context,44),Ui.dp(context,44));
        tp.rightMargin = Ui.dp(context,8);
        top.addView(trash,tp);

        Switch sw = new Switch(context);
        sw.setChecked(a.enabled);
        sw.setShowText(false);
        sw.setFocusable(false);
        sw.setOnCheckedChangeListener((b,checked)->listener.onToggle(a,checked));
        top.addView(sw,new LinearLayout.LayoutParams(-2,-2));

        card.addView(top,new LinearLayout.LayoutParams(-1,-2));

        LinearLayout chips = new LinearLayout(context);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setGravity(Gravity.CENTER_VERTICAL);
        String[] texts = {a.modeChipText(), a.snoozeChipText(), a.soundChipText()};
        for (int i=0;i<texts.length;i++) {
            TextView chip = Ui.chip(context,texts[i]);
            if (!a.enabled) chip.setTextColor(Ui.MUTED);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0,Ui.dp(context,34),1f);
            if (i>0) cp.leftMargin = Ui.dp(context,7);
            chips.addView(chip,cp);
        }
        LinearLayout.LayoutParams chipsP = new LinearLayout.LayoutParams(-1,-2);
        chipsP.topMargin=Ui.dp(context,14);
        card.addView(chips,chipsP);

        LinearLayout wrapper = new LinearLayout(context);
        wrapper.setPadding(Ui.dp(context,16),Ui.dp(context,7),Ui.dp(context,16),Ui.dp(context,7));
        wrapper.addView(card,new LinearLayout.LayoutParams(-1,-2));
        return wrapper;
    }
}
