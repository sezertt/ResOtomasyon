package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import Entity.Siparis;

public class MyListAdapter extends BaseAdapter {

    public LayoutInflater inflater;
    public Activity activity;
    ArrayList<Siparis> siparisListesi = new ArrayList<Siparis>();
    Boolean hesapMi;
    private int selectedIndex;

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
            convertView = inflater.inflate(R.layout.hesap_urun_gorunumu, parent, false);
        }

        TextView textYemekAdi = (TextView) convertView.findViewById(R.id.textViewYemekAdi);
        TextView textAdet = (TextView) convertView.findViewById(R.id.textViewAdet);
        TextView textFiyat = (TextView) convertView.findViewById(R.id.textViewFiyat);
        TextView textPorsiyon = (TextView) convertView.findViewById(R.id.textViewPorsiyon);

        TextView textViewKG = (TextView) convertView.findViewById(R.id.textViewKG);


        String miktar = "";

        if(siparisListesi.get(position).siparisAdedi != 0)
            miktar = "x" + String.valueOf(siparisListesi.get(position).siparisAdedi);

        if(siparisListesi.get(position).siparisKiloSatisiMi)
        {
            textViewKG.setText("(KG)");
        }
        else
        {
            textViewKG.setText("");
        }

        Double doubleFiyat;
        try
        {
            doubleFiyat = Double.parseDouble(siparisListesi.get(position).siparisFiyati);
            textFiyat.setText(String.format("%.2f", doubleFiyat)+" TL"); // 2 hane virgülden sonra virgül den
        }
        catch (Exception ex)
        {
            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            Number number;

            //burası virgüllü stringi double a convert etme çevirme kısmı
            try {
                number = format.parse(siparisListesi.get(position).siparisFiyati);
                doubleFiyat = number.doubleValue();
                textFiyat.setText(String.format("%.2f", doubleFiyat) + " TL"); // 2 hane virgülden sonra virgül den

            } catch (ParseException e) {
                e.printStackTrace();
                textFiyat.setText("ikram");
            }
        }

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        textYemekAdi.setText(siparisListesi.get(position).siparisYemekAdi);

       if(siparisListesi.get(position).siparisPorsiyonu > 0)
            textPorsiyon.setText(String.valueOf(siparisListesi.get(position).siparisPorsiyonu));
        else
        {
            textPorsiyon.setText("");
        }

        textAdet.setText(miktar);

        if(selectedIndex!= -1 && position == selectedIndex)
        {
            convertView.setBackgroundColor(Color.rgb(51,181,229));
        }
        else
        {
            int selectedColor = Color.WHITE;
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
