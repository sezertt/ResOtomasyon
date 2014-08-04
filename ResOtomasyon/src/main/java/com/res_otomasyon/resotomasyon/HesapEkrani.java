package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

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

        Double kacPorsiyon, yemeginFiyati, toplamHesap = 0d;
        String yemeginAdi, Garson;
        Boolean ikramMi;

        ListView hesapListesi = (ListView) findViewById(R.id.listViewHesap);

        ArrayList<Siparis> urunListesi = new ArrayList<Siparis>();
        ArrayList<Siparis> urunListesiIkram = new ArrayList<Siparis>();
        ArrayList<Siparis> urunListesiToplam = new ArrayList<Siparis>();


        for (int i = 0; i < Siparisler.length; i++) {
            String[] detaylari = Siparisler[i].split("-");

            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            Number number = null;

            //burası virgüllü stringi double a convert etme çevirme kısmı
            try {
                number = format.parse(detaylari[0]);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            yemeginFiyati = number.doubleValue();

            try {
                number = format.parse(detaylari[1]);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            kacPorsiyon =number.doubleValue();

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
                toplamHesap += yemeginFiyati * kacPorsiyon;
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
                    yeniSiparis.porsiyonFiyati = "ikram";
                    urunListesiIkram.add(yeniSiparis);
                }
            }
        }
        urunListesiToplam.addAll(urunListesi);
        urunListesiToplam.addAll(urunListesiIkram);

        Button hesapButton = (Button) findViewById(R.id.buttonHesap);

        hesapButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

            }
        });

                hesapButton.setText("Hesap İste  /  Toplam = " + toplamHesap + " TL");

        MyListAdapter adapter = new MyListAdapter(urunListesiToplam,this);
        hesapListesi.setAdapter(adapter);
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
