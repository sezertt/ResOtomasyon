package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import Entity.Siparis;

/**
 * Created by sezer on 01.08.2014.
 */

public class MyListAdapter extends BaseAdapter {

    public LayoutInflater inflater;
    public Activity activity;
    ArrayList<Siparis> siparisListesi = new ArrayList<Siparis>();
    Boolean hesapMi;
    private int selectedIndex;
    private int selectedColor = Color.WHITE;

    public MyListAdapter(ArrayList<Siparis> siparisListesi,Activity act, Boolean hesapMi) {
        activity = act;
        inflater = act.getLayoutInflater();
        this.siparisListesi = siparisListesi;
        this.hesapMi = hesapMi;
        selectedIndex = -1;
    }
    public void setSelectedIndex(int ind)
    {
        selectedIndex = ind;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return siparisListesi.size();
    }

    @Override
    public Object getItem(int position) {
        return siparisListesi.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.hesap_urun_gorunumu, null);
        }

        TextView textYemekAdi = (TextView) convertView.findViewById(R.id.textViewYemekAdi);
        TextView textAdet = (TextView) convertView.findViewById(R.id.textViewAdet);
        TextView textFiyat = (TextView) convertView.findViewById(R.id.textViewFiyat);

        Double doubleMiktar = Double.valueOf(siparisListesi.get(position).miktar);

        Double doubleFiyat;
        try
        {
            doubleFiyat = Double.parseDouble(siparisListesi.get(position).porsiyonFiyati);
            textFiyat.setText(String.format("%.2f", doubleFiyat)+" TL"); // 2 hane virgülden sonra virgül den
        }
        catch (Exception ex)
        {
            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            Number number = null;
            //burası virgüllü stringi double a convert etme çevirme kısmı
            try {
                number = format.parse(siparisListesi.get(position).porsiyonFiyati);
                doubleFiyat = number.doubleValue();
                textFiyat.setText(String.format("%.2f", doubleFiyat)+" TL"); // 2 hane virgülden sonra virgül den

            } catch (ParseException e) {
                e.printStackTrace();
                textFiyat.setText("ikram");
            }
        }

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        String result = df.format(doubleMiktar);

        textYemekAdi.setText(siparisListesi.get(position).yemekAdi);
        textAdet.setText("x"+result);

        if(selectedIndex!= -1 && position == selectedIndex)
        {
            convertView.setBackgroundColor(Color.rgb(51,181,229));
        }
        else
        {
            convertView.setBackgroundColor(selectedColor);
        }

        return convertView;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }
}
