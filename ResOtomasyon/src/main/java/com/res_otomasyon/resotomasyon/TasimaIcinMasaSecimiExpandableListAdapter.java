package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import Entity.GlobalDepartman;
import Entity.GlobalMasalar;

public class TasimaIcinMasaSecimiExpandableListAdapter extends BaseExpandableListAdapter {
    public Activity activity;
    public LayoutInflater inflater;
    public GlobalApplication g;

    public TasimaIcinMasaSecimiExpandableListAdapter(Activity act, GlobalApplication g) {
        this.activity = act;
        this.inflater = act.getLayoutInflater();
        this.g = g;
    }

    @Override
    public int getGroupCount() {
        return g.globalDepartmanlar.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return g.globalDepartmanlar.get(groupPosition);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return g.globalDepartmanlar.get(groupPosition).globalMasalar.size();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return g.globalDepartmanlar.get(groupPosition).globalMasalar.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.kategori_gorunumu, parent, false);
        }
        GlobalDepartman group = (GlobalDepartman) getGroup(groupPosition);

        CheckedTextView textGroupDepartmanAdi = (CheckedTextView) convertView.findViewById(R.id.textViewProductGroupHeader);
        textGroupDepartmanAdi.setText(group.globalDepartmanAdi);
        textGroupDepartmanAdi.setChecked(isExpanded);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        GlobalMasalar MasaAdi = (GlobalMasalar) getChild(groupPosition, childPosition);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.masa_gorunumu, parent, false);
        }
        TextView textMasaAdi = (TextView) convertView.findViewById(R.id.textViewChildMasaAdi);
        textMasaAdi.setText(MasaAdi.globalMasaAdi);

        if (g.globalDepartmanlar.get(groupPosition).globalMasalar.get(childPosition).globalMasaAcikMi)
            convertView.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_red_light));
        else {
            convertView.setBackgroundColor(Color.WHITE);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
