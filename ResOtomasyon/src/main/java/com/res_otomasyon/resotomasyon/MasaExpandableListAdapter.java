package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import java.util.ArrayList;
import Entity.Departman;
import ekclasslar.DepartmanMasalari;

public class MasaExpandableListAdapter extends BaseExpandableListAdapter {
    public Activity activity;
    public LayoutInflater inflater;
    private ArrayList<DepartmanMasalari> groups;
    public ArrayList<Departman> lstDepartmantlar;
    public int selectedIndex;

    public MasaExpandableListAdapter(Activity act, ArrayList<DepartmanMasalari> groups, ArrayList<Departman> lstDepartmantlar) {
        this.activity = act;
        this.inflater = act.getLayoutInflater();
        this.groups = groups;
        this.lstDepartmantlar = lstDepartmantlar;
        selectedIndex = -1;
    }

    @Override
    public int getGroupCount() {
        return lstDepartmantlar.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return lstDepartmantlar.get(groupPosition);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).Masalar.size();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).Masalar.get(childPosition);
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
        Departman group = (Departman) getGroup(groupPosition);
        CheckedTextView textGroupDepartmanAdi;
        textGroupDepartmanAdi = (CheckedTextView) convertView.findViewById(R.id.textViewProductGroupHeader);
        textGroupDepartmanAdi.setText(group.DepartmanAdi);
        textGroupDepartmanAdi.setChecked(isExpanded);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String MasaAdi = (String) getChild(groupPosition, childPosition);
        TextView textMasaAdi;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.masa_gorunumu, parent, false);
        }
        textMasaAdi = (TextView) convertView.findViewById(R.id.textViewChildMasaAdi);
        textMasaAdi.setText(MasaAdi);
        if (groups.get(groupPosition).mDurumu.get(childPosition))
            convertView.setBackgroundColor(Color.rgb(51, 181, 229));
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
