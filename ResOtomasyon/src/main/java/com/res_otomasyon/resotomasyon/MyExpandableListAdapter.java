package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by sezer on 08.07.2014.
 */

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private final SparseArray<listGroup> groups;
    public LayoutInflater inflater;
    public Activity activity;

    public MyExpandableListAdapter(Activity act, SparseArray<listGroup> groups) {
        activity = act;
        this.groups = groups;
        inflater = act.getLayoutInflater();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).productName.get(childPosition);
    }

    public Object getChild2(int groupPosition, int childPosition) {
        return groups.get(groupPosition).productPrice.get(childPosition);
    }

    public Object getChild3(int groupPosition, int childPosition) {
        return groups.get(groupPosition).productInfo.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String productName = (String) getChild(groupPosition, childPosition);
        final String productPrice = ((String) getChild2(groupPosition, childPosition)).replaceAll(",", "\\.");
        final String productInfo = (String) getChild3(groupPosition, childPosition);

        TextView text;
        ImageView image;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_details, null);
        }

        text = (TextView) convertView.findViewById(R.id.textViewChildPrice);
        text.setText(productPrice + "TL");

        text = (TextView) convertView.findViewById(R.id.textViewChildInfo);
        text.setText(productInfo);

        image = (ImageView) convertView.findViewById(R.id.imageView);
        File file = new File("/mnt/sdcard/shared/Lenovo/Resimler/" + productName + ".png");
        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        image.setImageBitmap(bmp);
        text = (TextView) convertView.findViewById(R.id.textViewChildHeader);
        text.setText(productName);
        final TextView textAdet = ((TextView) convertView.findViewById(R.id.textViewAdet));
        convertView.findViewById(R.id.buttonPlus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textAdet.setText(String.valueOf(Integer.parseInt(textAdet.getText().toString()) + 1));

            }
        });
        convertView.findViewById(R.id.buttonMinus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Integer.parseInt(textAdet.getText().toString()) > 0)
                    textAdet.setText(String.valueOf(Integer.parseInt(textAdet.getText().toString()) - 1));

            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).productName.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_group, null);
        }
        listGroup group = (listGroup) getGroup(groupPosition);

        CheckedTextView textGroupProductName;
        textGroupProductName = (CheckedTextView) convertView.findViewById(R.id.textViewProductGroupHeader);
        textGroupProductName.setText(group.productGroupName);
        textGroupProductName.setChecked(isExpanded);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}