package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

import Entity.UrunTasimaListesi;

public class UrunTasimaListAdapter extends BaseAdapter {

    public LayoutInflater inflater;
    public Activity activity;
    ArrayList<UrunTasimaListesi> urunTasimaSiparisListesi = new ArrayList<UrunTasimaListesi>();
    EditText editTextTasinacakMiktar;

    public UrunTasimaListAdapter(ArrayList<UrunTasimaListesi> urunTasimaSiparisListesi,Activity act) {
        activity = act;
        inflater = act.getLayoutInflater();
        this.urunTasimaSiparisListesi = urunTasimaSiparisListesi;
    }

    @Override
    public int getCount() {
        return urunTasimaSiparisListesi.size();
    }

    @Override
    public Object getItem(int position) {
        return urunTasimaSiparisListesi.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.urun_tasima_listesi_gorunumu, parent, false);
        }

        TextView textYemekAdi = (TextView) convertView.findViewById(R.id.textViewYemekAdi);
        TextView textAdet = (TextView) convertView.findViewById(R.id.textViewAdet);
        TextView textFiyat = (TextView) convertView.findViewById(R.id.textViewFiyat);
        editTextTasinacakMiktar = (EditText) convertView.findViewById(R.id.editTextTasinacakMiktar);
        TextView textPorsiyon = (TextView) convertView.findViewById(R.id.textViewPorsiyon);
        TextView textViewKG = (TextView) convertView.findViewById(R.id.textViewKG);

        if(urunTasimaSiparisListesi.get(position).tasinacakUrunKiloSatisiMi)
        {
            textViewKG.setText("(KG)");
        }
        else
        {
            textViewKG.setText("");
        }

        String miktar = "x" + String.valueOf(urunTasimaSiparisListesi.get(position).tasinacakUrunAdedi);

        View.OnFocusChangeListener focusChangeListener =  new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText editText = (EditText)v;
                if(!hasFocus){
                    Integer girilenSayi;
                    try {
                        String x = editText.getText().toString();
                        girilenSayi = Integer.parseInt(x);
                    } catch (Exception ex) {
                        return;
                    }

                    if (girilenSayi > urunTasimaSiparisListesi.get(position).tasinacakUrunAdedi) {
                        girilenSayi = urunTasimaSiparisListesi.get(position).tasinacakUrunAdedi;
                    }
                    urunTasimaSiparisListesi.get(position).tasinacakUrunSecilenAdet = girilenSayi;
                    editText.setText(girilenSayi+"");
                }
            }
        };
        editTextTasinacakMiktar.setText(urunTasimaSiparisListesi.get(position).tasinacakUrunSecilenAdet+"");

        editTextTasinacakMiktar.setOnFocusChangeListener(focusChangeListener);

        Double doubleFiyat;
        try
        {
            doubleFiyat = Double.parseDouble(urunTasimaSiparisListesi.get(position).tasinacakUrunFiyati);
            textFiyat.setText(String.format("%.2f", doubleFiyat)+" TL"); // 2 hane virgülden sonra virgül den
        }
        catch (Exception ex)
        {
            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            Number number;

            //burası virgüllü stringi double a convert etme çevirme kısmı
            try {
                number = format.parse(urunTasimaSiparisListesi.get(position).tasinacakUrunFiyati);
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

        textYemekAdi.setText(urunTasimaSiparisListesi.get(position).tasinacakUrunYemekAdi);

        if(urunTasimaSiparisListesi.get(position).tasinacakUrunPorsiyonu > 0)
            textPorsiyon.setText(String.valueOf(urunTasimaSiparisListesi.get(position).tasinacakUrunPorsiyonu));
        else
        {
            textPorsiyon.setText("");
        }

        textAdet.setText(miktar);

        return convertView;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }
}
