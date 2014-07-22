package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import Entity.Employee;
import Entity.Siparis;

public class HesapEkrani extends Activity {

    String departmanAdi, masaAdi;
    ArrayList<Employee> lstEmployee;
    ArrayList<Siparis> lstOrderedProducts = new ArrayList<Siparis>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_hesap_ekrani);
        Bundle extras = getIntent().getExtras();
        departmanAdi = extras.getString("DepartmanAdi");
        masaAdi = extras.getString("MasaAdi");
        lstEmployee = (ArrayList<Employee>) extras.getSerializable("lstEmployees");
        lstOrderedProducts = (ArrayList<Siparis>) extras.getSerializable("lstOrderedProducts");
        String siparisler = extras.getString("siparisler");
        String[] Siparisler = siparisler.split("\\*");

        Double kacPorsiyon, yemeginFiyati;
        String yemeginAdi, Garson;
        Boolean ikramMi;

        ListView list = (ListView) findViewById(R.id.listViewHesap);

        ArrayList<HashMap<String, String>> yazdirilacakListe = new ArrayList<HashMap<String, String>>();

        ArrayList<Siparis> urunListesi = new ArrayList<Siparis>();
        ArrayList<Siparis> urunListesiIkram = new ArrayList<Siparis>();


        for (int i = 0; i < Siparisler.length; i++) {
            String[] detaylari = Siparisler[i].split("-");
            yemeginFiyati = Double.parseDouble(detaylari[0]);
            kacPorsiyon = Double.parseDouble(detaylari[1]);
            yemeginAdi = detaylari[2];
            ikramMi = Boolean.parseBoolean(detaylari[3]);
            Garson = detaylari[4];

            int gruptaYeniGelenSiparisVarMi = -1; // ürün hesapta bulunuyor mu ?

            if(!ikramMi) // ikram değilse
            {
                for (int j = 0; j < urunListesi.size(); j++) {
                    if(yemeginAdi.contentEquals(urunListesi.get(j).toString())) // listede yemek var
                    {
                        // tüm ürünleri unique olcak şekilde 2 listede topla, biri ikram olanlar diğeri ikram olmayanlar. sonra oların hepsini map 'e koy
                        urunListesi.get(j).miktar = (Double.parseDouble(urunListesi.get(j).miktar) + kacPorsiyon) + "";
                    }
                    else // listede yemek yok
                    {
                        Siparis yeniSiparis = new Siparis();
                        yeniSiparis.miktar = kacPorsiyon.toString();
                        yeniSiparis.yemekAdi = yemeginAdi;
                        yeniSiparis.porsiyonFiyati = String.format("%.2f", yemeginFiyati);
                        urunListesi.add(yeniSiparis);
                    }
                }
            }
            else // ikramsa
            {

            }



            HashMap<String, String> map = new HashMap<String, String>();
            map.put("yemek", "101");
            map.put("adet", "6:30 AM");
            map.put("fiyat", "7:40 AM");
            yazdirilacakListe.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, yazdirilacakListe, R.layout.hesap_urun_gorunumu,
                new String[]{"yemek", "adet", "fiyat"}, new int[]{R.id.textViewYemekAdi, R.id.textViewAdet, R.id.textViewFiyat});
        list.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hesap_ekrani, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
