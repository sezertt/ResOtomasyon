package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import Entity.Siparis;

/**
 * Created by sezer on 01.08.2014.
 */

public class MyListAdapter extends BaseAdapter {

    public LayoutInflater inflater;
    public Activity activity;
    ArrayList<Siparis> siparisListesi = new ArrayList<Siparis>();

    public MyListAdapter(ArrayList<Siparis> siparisListesi,Activity act) {
        activity = act;
        inflater = act.getLayoutInflater();
        this.siparisListesi = siparisListesi;
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

    Double toplamFiyat = 0d;
    Double toplamFiyatM = 0d;

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
            textFiyat.setText("ikram");
        }

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        String result = df.format(doubleMiktar);

        textYemekAdi.setText(siparisListesi.get(position).yemekAdi);
        textAdet.setText("x"+result);

        return convertView;
    }
}
