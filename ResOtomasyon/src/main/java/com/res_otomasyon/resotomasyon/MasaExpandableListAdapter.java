package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;

import Entity.Departman;
import ekclasslar.DepartmanMasalari;

/**
 * Created by Mustafa on 5.8.2014.
 */
public class MasaExpandableListAdapter extends BaseExpandableListAdapter {
    public Activity activity;
    public LayoutInflater inflater;
    private  ArrayList<ArrayList<String>> groups;
    public ArrayList<Departman> lstDepartmantlar;

    public MasaExpandableListAdapter(Activity act, ArrayList<ArrayList<String>> groups,ArrayList<Departman> lstDepartmantlar) {
        this.activity = act;
        this.inflater = act.getLayoutInflater();
        this.groups = groups;
        this.lstDepartmantlar = lstDepartmantlar;
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
        return groups.get(groupPosition).size();
    }
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).get(childPosition);
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
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.kategori_gorunumu, null);
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
        String MasaAdi = (String) getChild(groupPosition,childPosition);
        TextView textMasaAdi;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.masa_gorunumu, null);
        }
        textMasaAdi = (TextView) convertView.findViewById(R.id.textViewChildMasaAdi);
        textMasaAdi.setText(MasaAdi);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
