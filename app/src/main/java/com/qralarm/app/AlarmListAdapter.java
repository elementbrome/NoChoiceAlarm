package com.qralarm.app;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class AlarmListAdapter extends BaseAdapter {
    public interface ToggleListener { void onToggle(AlarmModel alarm, boolean enabled); }
    private final Context context;
    private final ToggleListener listener;
    private List<AlarmModel> items = new ArrayList<>();

    public AlarmListAdapter(Context c, ToggleListener l) { context=c; listener=l; }
    public void setItems(List<AlarmModel> a) { items=a; notifyDataSetChanged(); }
    @Override public int getCount() { return items.size(); }
    @Override public AlarmModel getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return items.get(position).id; }

    @Override public View getView(int position, View convert, ViewGroup parent) {
        AlarmModel a = getItem(position);
        LinearLayout card = new LinearLayout(context); card.setOrientation(LinearLayout.HORIZONTAL); card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(Ui.dp(context,18),Ui.dp(context,16),Ui.dp(context,12),Ui.dp(context,16));
        card.setBackground(Ui.rounded(Ui.SURFACE,18,context));
        LinearLayout textWrap = new LinearLayout(context); textWrap.setOrientation(LinearLayout.VERTICAL);
        TextView time = Ui.text(context,a.timeText(),30,a.enabled?Ui.TEXT:0xFF9CA0AA,true); textWrap.addView(time);
        String sub = (a.label==null||a.label.trim().isEmpty()?a.repeatText():a.label.trim()+" · "+a.repeatText());
        TextView detail = Ui.text(context,sub,14,Ui.MUTED,false); LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-2,-2); dp.topMargin=Ui.dp(context,3); textWrap.addView(detail,dp);
        card.addView(textWrap,new LinearLayout.LayoutParams(0,-2,1f));
        Switch sw = new Switch(context); sw.setChecked(a.enabled); sw.setShowText(false);
        sw.setOnCheckedChangeListener((b,checked)->listener.onToggle(a,checked));
        card.addView(sw,new LinearLayout.LayoutParams(-2,-2));
        LinearLayout wrapper = new LinearLayout(context); wrapper.setPadding(Ui.dp(context,16),Ui.dp(context,6),Ui.dp(context,16),Ui.dp(context,6)); wrapper.addView(card,new LinearLayout.LayoutParams(-1,-2));
        return wrapper;
    }
}
