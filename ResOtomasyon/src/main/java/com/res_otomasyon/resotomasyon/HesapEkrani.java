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

        ListView hesapListesi = (ListView) findViewById(R.id.listViewHesap);

        ArrayList<Siparis> urunListesi = new ArrayList<Siparis>();
        ArrayList<Siparis> urunListesiIkram = new ArrayList<Siparis>();
        ArrayList<Siparis> urunListesiToplam = new ArrayList<Siparis>();


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
                        gruptaYeniGelenSiparisVarMi = j;
                        break;
                    }
                }

                if(gruptaYeniGelenSiparisVarMi != -1) // listede yemek var
                {
                    urunListesi.get(gruptaYeniGelenSiparisVarMi).miktar = (Double.parseDouble(urunListesi.get(gruptaYeniGelenSiparisVarMi).miktar) + kacPorsiyon) + "";
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
            else // ikramsa
            {
                for (int j = 0; j < urunListesiIkram.size(); j++) {
                    if(yemeginAdi.contentEquals(urunListesiIkram.get(j).toString())) // listede yemek var
                    {
                        gruptaYeniGelenSiparisVarMi = j;
                        break;
                    }
                }

                if(gruptaYeniGelenSiparisVarMi != -1) // listede yemek var
                {
                    urunListesiIkram.get(gruptaYeniGelenSiparisVarMi).miktar = (Double.parseDouble(urunListesiIkram.get(gruptaYeniGelenSiparisVarMi).miktar) + kacPorsiyon) + "";
                }
                else // listede yemek yok
                {
                    Siparis yeniSiparis = new Siparis();
                    yeniSiparis.miktar = kacPorsiyon.toString();
                    yeniSiparis.yemekAdi = yemeginAdi;
                    yeniSiparis.porsiyonFiyati = String.format("%.2f", yemeginFiyati);
                    urunListesiIkram.add(yeniSiparis);
                }
            }
        }
        urunListesiToplam = concat(urunListesi, urunListesiIkram);

        MyListAdapter adapter = new MyListAdapter(urunListesiToplam,this);
        hesapListesi.setAdapter(adapter);
    }

    ArrayList<Siparis> concat(ArrayList<Siparis> A, ArrayList<Siparis> B) {
        ArrayList<Siparis> C= new ArrayList<Siparis>();
        System.arraycopy(A, 0, C, 0, A.size());
        System.arraycopy(B, 0, C, A.size(), B.size());
        return C;
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
