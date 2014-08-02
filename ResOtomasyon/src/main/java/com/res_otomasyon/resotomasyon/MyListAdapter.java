package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.hesap_urun_gorunumu, null);
        }

        TextView textYemekAdi = (TextView) convertView.findViewById(R.id.textViewYemekAdi);
        TextView textAdet = (TextView) convertView.findViewById(R.id.textViewAdet);
        TextView textFiyat = (TextView) convertView.findViewById(R.id.textViewFiyat);

        textYemekAdi.setText(siparisListesi.get(position).yemekAdi);
        textAdet.setText(siparisListesi.get(position).miktar);
        textFiyat.setText(siparisListesi.get(position).porsiyonFiyati);


        return convertView;
    }
}
