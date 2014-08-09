package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;

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
    String productCount;
    String[] items;

    ArrayList<Siparis> tamPorsiyon = new ArrayList<Siparis>();
    ArrayList<Siparis> yarimPorsiyon = new ArrayList<Siparis>();
    ArrayList<Siparis> ceyrekPorsiyon = new ArrayList<Siparis>();
    ArrayList<Siparis> ucCeyrekPorsiyon = new ArrayList<Siparis>();
    ArrayList<Siparis> birBucukPorsiyon = new ArrayList<Siparis>();

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

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String productName = (String) getChild(groupPosition, childPosition);
        final String productPrice = ((String) getChild2(groupPosition, childPosition)).replaceAll(",", "\\.");
        final String productInfo = (String) getChild3(groupPosition, childPosition);
        productCount = (String) getChild4(groupPosition, childPosition);
        final Double productPortion = (Double)getChild5(groupPosition, childPosition);

        final TextView text;
        final ImageView image;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.urun_gorunumu, null);
        }

        final TextView textFiyat = (TextView) convertView.findViewById(R.id.textViewChildPrice);
        textFiyat.setText(productPrice + " TL");

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

        convertView.findViewById(R.id.buttonPlus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(productPortion == 0)
                    porsiyonEkle(groupPosition,childPosition,textAdet,textName,textFiyat,productPortion,1d);
                else
                {
                    ArrayList<String> porsiyonlar = new ArrayList<String>();
                    porsiyonlar.add("1.5 Porsiyon");
                    porsiyonlar.add("1 Porsiyon");

                    if(productPortion == 2)
                    {
                        porsiyonlar.add("0.75 Porsiyon");
                    }

                    porsiyonlar.add("0.5 Porsiyon");

                    if(productPortion == 2)
                    {
                        porsiyonlar.add("0.25 Porsiyon");
                    }

                    items = new String[porsiyonlar.size()];

                    for(int i = 0; i < porsiyonlar.size();i++)
                        items[i] = porsiyonlar.get(i);

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1,items);

                    final AlertDialog alert = new AlertDialog.Builder(activity)
                            .setTitle("Porsiyon Seçiniz")
                            .setCancelable(false)
                            .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // porsiyon seçimi tamamlanınca yapılacaklar


                                }
                            })
                            .setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (items[item].charAt(0) == '-') {
                                        Double porsiyon = Double.parseDouble(items[item].toString().substring(0, 4));
                                        porsiyon *= -1;

                                        if (porsiyon == 0.7)
                                            porsiyon = 0.75;
                                        else if (porsiyon == 0.2)
                                            porsiyon = 0.25;
                                        porsiyonCikar(groupPosition, childPosition, textAdet, textName, porsiyon);
                                    } else {
                                        Double porsiyon = Double.parseDouble(items[item].toString().substring(0, 3));
                                        if (porsiyon == 0.7)
                                            porsiyon = 0.75;
                                        else if (porsiyon == 0.2)
                                            porsiyon = 0.25;
                                        porsiyonEkle(groupPosition, childPosition, textAdet, textName, textFiyat, productPortion, porsiyon);
                                    }
                                }
                            })
                            .create();

                    alert.show();
                }
            }
        });

        convertView.findViewById(R.id.buttonMinus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(productPortion == 0)
                    porsiyonCikar(groupPosition,childPosition,textAdet,textName,1d);
                else if(!textAdet.getText().equals("0"))
                {
                    ArrayList<String> porsiyonlar = new ArrayList<String>();

                    if(birBucukPorsiyon.size()>0) // eğer önceden eklenmiş 1.5 porsiyon varsa
                    {
                        for(int i=0;i<birBucukPorsiyon.size();i++)
                        {
                            if(birBucukPorsiyon.get(i).yemekAdi.contentEquals(textName.getText()))
                            {
                                porsiyonlar.add("-1.5 Porsiyon");
                                break;
                            }
                        }
                    }

                    if(ucCeyrekPorsiyon.size()>0) // eğer önceden eklenmiş 0.75 porsiyon varsa
                    {
                        for(int i=0;i<ucCeyrekPorsiyon.size();i++)
                        {
                            if(ucCeyrekPorsiyon.get(i).yemekAdi.contentEquals(textName.getText()))
                            {
                                porsiyonlar.add("-0.75 Porsiyon");
                                break;
                            }
                        }
                    }

                    if(yarimPorsiyon.size()>0) // eğer önceden eklenmiş 0.5 porsiyon varsa
                    {
                        for(int i=0;i<yarimPorsiyon.size();i++)
                        {
                            if(yarimPorsiyon.get(i).yemekAdi.contentEquals(textName.getText()))
                            {
                                porsiyonlar.add("-0.5 Porsiyon");
                                break;
                            }
                        }
                    }

                    if(tamPorsiyon.size()>0)  // eğer önceden eklenmiş 1 porsiyon varsa
                    {
                        for(int i=0;i<tamPorsiyon.size();i++)
                        {
                            if(tamPorsiyon.get(i).yemekAdi.contentEquals(textName.getText()))
                            {
                                porsiyonlar.add("-1 Porsiyon");
                                break;
                            }
                        }
                    }

                    if(ceyrekPorsiyon.size()>0)  // eğer önceden eklenmiş 0.25 porsiyon varsa
                    {
                        for(int i=0;i<ceyrekPorsiyon.size();i++)
                        {
                            if(ceyrekPorsiyon.get(i).yemekAdi.contentEquals(textName.getText()))
                            {
                                porsiyonlar.add("-0.25 Porsiyon");
                                break;
                            }
                        }
                    }

                    items = new String[porsiyonlar.size()];

                    for(int i = 0; i < porsiyonlar.size();i++)
                        items[i] = porsiyonlar.get(i);

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1,items);

                    final AlertDialog alert = new AlertDialog.Builder(activity)
                            .setTitle("Porsiyon Seçiniz")
                            .setCancelable(false)
                            .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // porsiyon seçimi tamamlanınca yapılacaklar


                                }
                            })
                            .setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (items[item].charAt(0) == '-') {
                                        Double porsiyon = Double.parseDouble(items[item].toString().substring(0, 4));
                                        porsiyon *= -1;

                                        if (porsiyon == 0.7)
                                            porsiyon = 0.75;
                                        else if (porsiyon == 0.2)
                                            porsiyon = 0.25;
                                        porsiyonCikar(groupPosition, childPosition, textAdet, textName, porsiyon);
                                    } else {
                                        Double porsiyon = Double.parseDouble(items[item].toString().substring(0, 3));
                                        if (porsiyon == 0.7)
                                            porsiyon = 0.75;
                                        else if (porsiyon == 0.2)
                                            porsiyon = 0.25;
                                        porsiyonEkle(groupPosition, childPosition, textAdet, textName, textFiyat, productPortion, porsiyon);
                                    }
                                }
                            })
                            .create();

                    alert.show();
                }
            }
        });
        return convertView;
    }

    private void porsiyonCikar(int groupPosition, int childPosition, TextView textAdet, TextView textName, Double azaltmaMiktari)
    {
        if (!textAdet.getText().equals("0"))
        {
            if(azaltmaMiktari == 1)
            {
                // eğer varsa azalt yoksa çık
                if(porsiyonAzaltmaliMi(tamPorsiyon,textName.getText().toString()))
                    return;
            }
            else if(azaltmaMiktari == 0.5)
            {
                // eğer varsa azalt
                porsiyonAzaltmaliMi(yarimPorsiyon,textName.getText().toString());
            }
            else if(azaltmaMiktari == 0.25)
            {
                // eğer varsa azalt
                porsiyonAzaltmaliMi(ceyrekPorsiyon,textName.getText().toString());
            }
            else if(azaltmaMiktari == 1.5)
            {
                // eğer varsa azalt
                porsiyonAzaltmaliMi(birBucukPorsiyon,textName.getText().toString());
            }
            else
            {
                // eğer varsa azalt
                porsiyonAzaltmaliMi(ucCeyrekPorsiyon,textName.getText().toString());
            }

            for(int i = 0 ; i< menuEkrani.lstOrderedProducts.size();i++)
            {
                if(menuEkrani.lstOrderedProducts.get(i).yemekAdi.contentEquals(textName.getText().toString()))
                {
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    df.setMinimumFractionDigits(0);
                    df.setGroupingUsed(false);

                    String miktar = df.format(Double.parseDouble(textAdet.getText().toString()) - azaltmaMiktari);

                    if(Double.parseDouble(textAdet.getText().toString()) - azaltmaMiktari < 0)
                        miktar = "0";

                    if(miktar.contentEquals("0"))
                        menuEkrani.lstOrderedProducts.remove(i);
                    else
                        menuEkrani.lstOrderedProducts.get(i).miktar = miktar;
                    textAdet.setText(miktar);
                    groups.get(groupPosition).productCount.set(childPosition,miktar);
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    private Boolean porsiyonAzaltmaliMi(ArrayList<Siparis> porsiyonArrayi, String yemekAdi)
    {
        int siparisVarMi= -1;
        for(int i=0;i<porsiyonArrayi.size();i++)
        {
            if(porsiyonArrayi.get(i).yemekAdi.contentEquals(yemekAdi))
            {
                siparisVarMi = i;
                break;
            }
        }

        if(siparisVarMi == -1)
            return true;
        else
        {
            porsiyonArrayi.get(siparisVarMi).miktar = String.valueOf(Double.parseDouble(porsiyonArrayi.get(siparisVarMi).miktar)-1);
            if(porsiyonArrayi.get(siparisVarMi).miktar.contentEquals("0.0"))
                porsiyonArrayi.remove(siparisVarMi);
            return false;
        }
    }

    private void porsiyonEkle(int groupPosition, int childPosition, TextView textAdet, TextView textName, TextView textFiyat,Double productPortion, Double arttirmaMiktari)
    {
        Siparis siparis = new Siparis();

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        String miktar = df.format(Double.parseDouble(textAdet.getText().toString()) + arttirmaMiktari);

        textAdet.setText(miktar);
        groups.get(groupPosition).productCount.set(childPosition,miktar);
        notifyDataSetChanged();

        if(arttirmaMiktari == 1)
        {
            porsiyonArttir(tamPorsiyon,textName.getText().toString());
        }
        else if(arttirmaMiktari == 0.5)
        {
            porsiyonArttir(yarimPorsiyon,textName.getText().toString());
        }
        else if(arttirmaMiktari == 0.25)
        {
            porsiyonArttir(ceyrekPorsiyon,textName.getText().toString());
        }
        else if(arttirmaMiktari == 1.5)
        {
            porsiyonArttir(birBucukPorsiyon,textName.getText().toString());
        }
        else
        {
            porsiyonArttir(ucCeyrekPorsiyon,textName.getText().toString());
        }

        for(int i = 0 ; i< menuEkrani.lstOrderedProducts.size();i++) {
            if (menuEkrani.lstOrderedProducts.get(i).yemekAdi.contentEquals(textName.getText().toString())) {
                menuEkrani.lstOrderedProducts.get(i).miktar = miktar;
                return;
            }
        }

        siparis.porsiyonSinifi = productPortion;
        siparis.miktar = miktar;
        siparis.porsiyonFiyati = textFiyat.getText().toString().substring(0,textFiyat.getText().length() - 3);
        siparis.yemekAdi = textName.getText().toString();
        menuEkrani.lstOrderedProducts.add(siparis);
    }

    private void porsiyonArttir(ArrayList<Siparis> porsiyonArrayi,String yemekAdi)
    {
        Integer siparisVarMi= -1;
        for(int i=0;i<porsiyonArrayi.size();i++)
        {
            if(porsiyonArrayi.get(i).yemekAdi.contentEquals(yemekAdi))
            {
                siparisVarMi = i;
                break;
            }
        }
        if(siparisVarMi != -1) // siparis listede var
        {
            porsiyonArrayi.get(siparisVarMi).miktar = String.valueOf(Double.parseDouble(porsiyonArrayi.get(siparisVarMi).miktar) + 1);
        }
        else// listede yok
        {
            Siparis siparis = new Siparis();
            siparis.yemekAdi = yemekAdi;
            siparis.miktar = "1";
            porsiyonArrayi.add(siparis);
        }
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