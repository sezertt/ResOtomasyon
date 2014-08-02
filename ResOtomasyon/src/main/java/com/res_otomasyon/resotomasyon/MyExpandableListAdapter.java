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

import java.io.File;

import Entity.Siparis;
import ekclasslar.UrunBilgileri;

/**
 * Created by sezer on 08.07.2014.
 */

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private SparseArray<UrunBilgileri> groups;
    public LayoutInflater inflater;
    public Activity activity;
    MenuEkrani menuEkrani;

    public MyExpandableListAdapter(Activity act, SparseArray<UrunBilgileri> groups, MenuEkrani menuEkrani) {
        activity = act;
        this.groups = groups;
        this.menuEkrani = menuEkrani;
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

    public Object getChild4(int groupPosition, int childPosition) {
        return groups.get(groupPosition).productCount.get(childPosition);
    }

    public Object getChild5(int groupPosition, int childPosition) {
        return groups.get(groupPosition).productPortion.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }
    String productCount;

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String productName = (String) getChild(groupPosition, childPosition);
        final String productPrice = ((String) getChild2(groupPosition, childPosition)).replaceAll(",", "\\.");
        final String productInfo = (String) getChild3(groupPosition, childPosition);
        productCount = (String) getChild4(groupPosition, childPosition);
        final Double productPortion = (Double)getChild5(groupPosition, childPosition);

        TextView text;
        final ImageView image;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.urun_gorunumu, null);
        }

        final TextView textFiyat = (TextView) convertView.findViewById(R.id.textViewChildPrice);
        textFiyat.setText(productPrice + "TL");

        text = (TextView) convertView.findViewById(R.id.textViewChildInfo);
        text.setText(productInfo);

        image = (ImageView) convertView.findViewById(R.id.imageView);
        File file = new File("/mnt/sdcard/shared/Lenovo/Resimler/" + productName + ".png");
        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        image.setImageBitmap(bmp);
        final TextView textName = (TextView) convertView.findViewById(R.id.textViewChildHeader);
        textName.setText(productName);

        final TextView textAdet = (TextView) convertView.findViewById(R.id.textViewAdet);
        textAdet.setText(productCount);

        if (productPortion == 0) // tam porsiyon
        {
            convertView.findViewById(R.id.buttonYarim).setVisibility(View.INVISIBLE);
            convertView.findViewById(R.id.buttonCeyrek).setVisibility(View.INVISIBLE);
        } else if (productPortion == 1) { // yarım porsiyon
            convertView.findViewById(R.id.buttonYarim).setVisibility(View.VISIBLE);
        }
        else // hem yarım hem çeyrek porsiyon
        {
            convertView.findViewById(R.id.buttonYarim).setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.buttonCeyrek).setVisibility(View.VISIBLE);
        }

        convertView.findViewById(R.id.buttonYarim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String miktar = String.valueOf(Double.parseDouble(textAdet.getText().toString()) + 0.5);

                productCount = miktar;
                textAdet.setText(miktar);
                groups.get(groupPosition).productCount.set(childPosition,miktar);
                notifyDataSetChanged();

                for(int i = 0 ; i< menuEkrani.lstOrderedProducts.size();i++) {
                    if (menuEkrani.lstOrderedProducts.get(i).yemekAdi.contentEquals(textName.getText().toString())) {
                        menuEkrani.lstOrderedProducts.get(i).miktar = miktar;
                        return;
                    }
                }

                Siparis siparis = new Siparis();
                siparis.miktar = miktar;
                siparis.porsiyonFiyati = textFiyat.getText().toString();
                siparis.yemekAdi = textName.getText().toString();
                menuEkrani.lstOrderedProducts.add(siparis);
            }
        });

        convertView.findViewById(R.id.buttonCeyrek).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String miktar = String.valueOf(Double.parseDouble(textAdet.getText().toString()) + 0.25);

                productCount = miktar;
                textAdet.setText(miktar);
                groups.get(groupPosition).productCount.set(childPosition,miktar);
                notifyDataSetChanged();

                for(int i = 0 ; i< menuEkrani.lstOrderedProducts.size();i++) {
                    if (menuEkrani.lstOrderedProducts.get(i).yemekAdi.contentEquals(textName.getText().toString())) {
                        menuEkrani.lstOrderedProducts.get(i).miktar = miktar;
                        return;
                    }
                }

                Siparis siparis = new Siparis();
                siparis.miktar = miktar;
                siparis.porsiyonFiyati = textFiyat.getText().toString();
                siparis.yemekAdi = textName.getText().toString();
                menuEkrani.lstOrderedProducts.add(siparis);
            }
        });

        convertView.findViewById(R.id.buttonPlus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String miktar = String.valueOf(Double.parseDouble(textAdet.getText().toString()) + 1);
                productCount = miktar;
                textAdet.setText(miktar);
                groups.get(groupPosition).productCount.set(childPosition,miktar);
                notifyDataSetChanged();

                for(int i = 0 ; i< menuEkrani.lstOrderedProducts.size();i++) {
                    if (menuEkrani.lstOrderedProducts.get(i).yemekAdi.contentEquals(textName.getText().toString())) {
                        menuEkrani.lstOrderedProducts.get(i).miktar = miktar;
                        return;
                    }
                }

                Siparis siparis = new Siparis();
                siparis.miktar = miktar;
                siparis.porsiyonFiyati = textFiyat.getText().toString();
                siparis.yemekAdi = textName.getText().toString();
                menuEkrani.lstOrderedProducts.add(siparis);
            }
        });

        convertView.findViewById(R.id.buttonMinus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            String miktar = String.valueOf(Double.parseDouble(textAdet.getText().toString()) - 1);

            if(Double.parseDouble(textAdet.getText().toString()) - 1 < 0)
                miktar = "0";

            if (!textAdet.getText().equals("0"))
            {
                for(int i = 0 ; i< menuEkrani.lstOrderedProducts.size();i++)
                {
                    if(menuEkrani.lstOrderedProducts.get(i).yemekAdi.contentEquals(textName.getText().toString()))
                    {
                        if(miktar.contentEquals("0"))
                            menuEkrani.lstOrderedProducts.remove(i);
                        else
                            menuEkrani.lstOrderedProducts.get(i).miktar = miktar;

                        textAdet.setText(miktar);

                        break;
                    }
                }
            }
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
            convertView = inflater.inflate(R.layout.kategori_gorunumu, null);
        }
        UrunBilgileri group = (UrunBilgileri) getGroup(groupPosition);

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