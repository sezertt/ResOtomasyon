package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

import Entity.Employee;
import Entity.Siparis;

public class HesapEkrani extends Activity {

    String departmanAdi, masaAdi;
    ArrayList<Employee> lstEmployee;
    ArrayList<Siparis> lstOrderedProducts = new ArrayList<Siparis>();
    SharedPreferences preferences = null;
    boolean MasaKilitliMi = false;
    MyListAdapter adapterSecilenSiparisler;
    ArrayList<Siparis> urunListesiToplam = new ArrayList<Siparis>();
    int selectedSiparisItemPosition = -1;
    GlobalApplication g;
    ArrayList<String> porsiyonlarPozitif = new ArrayList<String>();
    ArrayList<String> porsiyonlarNegatif = new ArrayList<String>();

    Double kacPorsiyon, yemeginFiyati, toplamHesap = 0d;
    Button buttonSepet, buttonNot;


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

        g = (GlobalApplication) getApplicationContext();

        preferences = this.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE);
        MasaKilitliMi = preferences.getBoolean("MasaKilitli", false);

        String yemeginAdi, Garson;
        Boolean ikramMi;

        ListView hesapListesi = (ListView) findViewById(R.id.listViewHesap);
        final ListView secilenSiparislerinListesi = (ListView) findViewById(R.id.listViewSecilenSiparisler);

        ArrayList<Siparis> urunListesi = new ArrayList<Siparis>();
        ArrayList<Siparis> urunListesiIkram = new ArrayList<Siparis>();

        final Button hesapButton = (Button) findViewById(R.id.buttonHesap);
        Button buttonAzalt = (Button) findViewById(R.id.buttonAzalt);
        Button buttonArttir = (Button) findViewById(R.id.buttonArttir);
        buttonSepet = (Button) findViewById(R.id.buttonSepetOnayla);
        buttonNot = (Button) findViewById(R.id.buttonNot);

        final String siparisler = extras.getString("siparisler");

        if(siparisler != null)
        {
            String[] Siparisler = siparisler.split("\\*");

            for (String aSiparisler : Siparisler) {
                String[] detaylari = aSiparisler.split("-");

                NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                Number number = null;

                //burası virgüllü stringi double a convert etme çevirme kısmı
                try {
                    number = format.parse(detaylari[0]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (number != null) {
                    yemeginFiyati = number.doubleValue();
                }

                try {
                    number = format.parse(detaylari[1]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (number != null) {
                    kacPorsiyon = number.doubleValue();
                }

                yemeginAdi = detaylari[2];
                ikramMi = Boolean.parseBoolean(detaylari[3]);
                Garson = detaylari[4];

                int gruptaYeniGelenSiparisVarMi = -1; // ürün hesapta bulunuyor mu ?

                if (!ikramMi) // ikram değilse
                {
                    for (int j = 0; j < urunListesi.size(); j++) {
                        if (yemeginAdi.contentEquals(urunListesi.get(j).yemekAdi)) // listede yemek var
                        {
                            gruptaYeniGelenSiparisVarMi = j;
                            break;
                        }
                    }

                    if (gruptaYeniGelenSiparisVarMi != -1) // listede yemek var
                    {
                        urunListesi.get(gruptaYeniGelenSiparisVarMi).miktar = (Double.parseDouble(urunListesi.get(gruptaYeniGelenSiparisVarMi).miktar) + kacPorsiyon) + "";
                    } else // listede yemek yok
                    {
                        Siparis yeniSiparis = new Siparis();
                        yeniSiparis.miktar = kacPorsiyon.toString();
                        yeniSiparis.yemekAdi = yemeginAdi;
                        yeniSiparis.porsiyonFiyati = String.format("%.2f", yemeginFiyati);
                        urunListesi.add(yeniSiparis);
                    }
                    toplamHesap += yemeginFiyati * kacPorsiyon;
                } else // ikramsa
                {
                    for (int j = 0; j < urunListesiIkram.size(); j++) {
                        if (yemeginAdi.contentEquals(urunListesiIkram.get(j).yemekAdi)) // listede yemek var
                        {
                            gruptaYeniGelenSiparisVarMi = j;
                            break;
                        }
                    }

                    if (gruptaYeniGelenSiparisVarMi != -1) // listede yemek var
                    {
                        urunListesiIkram.get(gruptaYeniGelenSiparisVarMi).miktar = (Double.parseDouble(urunListesiIkram.get(gruptaYeniGelenSiparisVarMi).miktar) + kacPorsiyon) + "";
                    } else // listede yemek yok
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

            hesapButton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                // BURASI YAPILACAK HESAP İSTEME BUTONU
                }
            });

            MyListAdapter adapterHesap = new MyListAdapter(urunListesiToplam, this,true);
            hesapListesi.setAdapter(adapterHesap);

            hesapButton.setText("Hesap İste  /  Toplam = " + toplamHesap + " TL");
        }
        else
        {
            hesapButton.setText("Hesap İste  /  Toplam = 0 TL");
            hesapButton.setEnabled(false);
        }

        adapterSecilenSiparisler = new MyListAdapter(lstOrderedProducts,this,false);
        secilenSiparislerinListesi.setAdapter(adapterSecilenSiparisler);

        registerForContextMenu(hesapListesi);

        secilenSiparislerinListesi.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                selectedSiparisItemPosition = position;
                adapterSecilenSiparisler.setSelectedIndex(position);
            }
        });

        buttonArttir.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if(selectedSiparisItemPosition != -1)
                {
                    if(lstOrderedProducts.get(selectedSiparisItemPosition).porsiyonSinifi == 0) // tam porsiyon
                    {
                        porsiyonEkle(1d);
                    }
                    else // diğer porsiyonlar
                    {
                        int varMi = -1;
                        for(int i=0;i<g.birBucukPorsiyon.size();i++)
                        {
                            if(g.birBucukPorsiyon.get(i).yemekAdi.contentEquals(lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi))
                            {
                                porsiyonlarPozitif.add("1.5 Porsiyon x" + g.birBucukPorsiyon.get(i).miktar);
                                varMi = i;
                                break;
                            }
                        }

                        if(varMi == -1)
                            porsiyonlarPozitif.add("1.5 Porsiyon ");

                        varMi = -1;

                        for(int i=0;i<g.tamPorsiyon.size();i++)
                        {
                            if(g.tamPorsiyon.get(i).yemekAdi.contentEquals(lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi))
                            {
                                porsiyonlarPozitif.add("1 Porsiyon x" + g.tamPorsiyon.get(i).miktar);
                                varMi = i;
                                break;
                            }
                        }

                        if(varMi == -1)
                            porsiyonlarPozitif.add("1 Porsiyon ");

                        if(lstOrderedProducts.get(selectedSiparisItemPosition).porsiyonSinifi == 2d)
                        {
                            varMi = -1;

                            for(int i=0;i<g.ucCeyrekPorsiyon.size();i++)
                            {
                                if(g.ucCeyrekPorsiyon.get(i).yemekAdi.contentEquals(lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi))
                                {
                                    porsiyonlarPozitif.add("0.75 Porsiyon x" + g.ucCeyrekPorsiyon.get(i).miktar);
                                    varMi = i;
                                    break;
                                }
                            }

                            if(varMi == -1)
                                porsiyonlarPozitif.add("0.75 Porsiyon ");
                        }

                        varMi = -1;

                        for(int i=0;i<g.yarimPorsiyon.size();i++)
                        {
                            if(g.yarimPorsiyon.get(i).yemekAdi.contentEquals(lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi))
                            {
                                porsiyonlarPozitif.add("0.5 Porsiyon x" + g.yarimPorsiyon.get(i).miktar);
                                varMi = i;
                                break;
                            }
                        }

                        if(varMi == -1)
                            porsiyonlarPozitif.add("0.5 Porsiyon ");

                        if(lstOrderedProducts.get(selectedSiparisItemPosition).porsiyonSinifi == 2d)
                        {
                            varMi = -1;

                            for(int i=0;i<g.ceyrekPorsiyon.size();i++)
                            {
                                if(g.ceyrekPorsiyon.get(i).yemekAdi.contentEquals(lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi))
                                {
                                    porsiyonlarPozitif.add("0.25 Porsiyon x" + g.ceyrekPorsiyon.get(i).miktar);
                                    varMi = i;
                                    break;
                                }
                            }

                            if(varMi == -1)
                                porsiyonlarPozitif.add("0.25 Porsiyon ");
                        }

                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(HesapEkrani.this, android.R.layout.simple_list_item_1,porsiyonlarPozitif);

                        final AlertDialog alert = new AlertDialog.Builder(HesapEkrani.this)
                                .setTitle("Porsiyon Seçiniz")
                                .setCancelable(false)
                                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int item) {
                                        // porsiyon seçimi tamamlanınca yapılacaklar
                                        porsiyonlarPozitif.clear();
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int item) {
                                        Double porsiyon;

                                        try {
                                            porsiyon = Double.parseDouble(porsiyonlarPozitif.get(item).substring(0, 3));
                                        } catch (Exception ex) {
                                            porsiyon = 1d;
                                        }

                                        if (porsiyon == 0.7)
                                            porsiyon = 0.75;
                                        else if (porsiyon == 0.2)
                                            porsiyon = 0.25;

                                        porsiyonEkle(porsiyon);

                                        String [] urunBilgileri = porsiyonlarPozitif.get(item).split("x");

                                        int adet = 0;

                                        if(urunBilgileri.length > 1)
                                            adet = Integer.parseInt(urunBilgileri[1]);

                                        adet++;

                                        porsiyonlarPozitif.set(item, urunBilgileri[0] +"x" + adet);
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .create();

                        alert.show();
                    }
                }
            }
        });

        buttonAzalt.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if(selectedSiparisItemPosition != -1)
                {
                    if(lstOrderedProducts.get(selectedSiparisItemPosition).porsiyonSinifi == 0) // tam porsiyon
                    {
                        porsiyonCikar(1d);
                    }
                    else // diğer porsiyonlar
                    {
                        if(g.birBucukPorsiyon.size()>0) // eğer önceden eklenmiş 1.5 porsiyon varsa
                        {
                            for (Siparis aBirBucukPorsiyon : g.birBucukPorsiyon) {
                                if (aBirBucukPorsiyon.yemekAdi.contentEquals(lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi)) {
                                    porsiyonlarNegatif.add("-1.5 Porsiyon x" + aBirBucukPorsiyon.miktar);
                                    break;
                                }
                            }
                        }

                        if(g.tamPorsiyon.size()>0)  // eğer önceden eklenmiş 1 porsiyon varsa
                        {
                            for (Siparis aTamPorsiyon : g.tamPorsiyon) {
                                if (aTamPorsiyon.yemekAdi.contentEquals(lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi)) {
                                    porsiyonlarNegatif.add("-1 Porsiyon x" + aTamPorsiyon.miktar);
                                    break;
                                }
                            }
                        }

                        if(g.ucCeyrekPorsiyon.size()>0) // eğer önceden eklenmiş 0.75 porsiyon varsa
                        {
                            for (Siparis anUcCeyrekPorsiyon : g.ucCeyrekPorsiyon) {
                                if (anUcCeyrekPorsiyon.yemekAdi.contentEquals(lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi)) {
                                    porsiyonlarNegatif.add("-0.75 Porsiyon x" + anUcCeyrekPorsiyon.miktar);
                                    break;
                                }
                            }
                        }

                        if(g.yarimPorsiyon.size()>0) // eğer önceden eklenmiş 0.5 porsiyon varsa
                        {
                            for (Siparis aYarimPorsiyon : g.yarimPorsiyon) {
                                if (aYarimPorsiyon.yemekAdi.contentEquals(lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi)) {
                                    porsiyonlarNegatif.add("-0.5 Porsiyon x" + aYarimPorsiyon.miktar);
                                    break;
                                }
                            }
                        }

                        if(g.ceyrekPorsiyon.size()>0)  // eğer önceden eklenmiş 0.25 porsiyon varsa
                        {
                            for (Siparis aCeyrekPorsiyon : g.ceyrekPorsiyon) {
                                if (aCeyrekPorsiyon.yemekAdi.contentEquals(lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi)) {
                                    porsiyonlarNegatif.add("-0.25 Porsiyon x" + aCeyrekPorsiyon.miktar);
                                    break;
                                }
                            }
                        }

                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(HesapEkrani.this, android.R.layout.simple_list_item_1,porsiyonlarNegatif);

                        final AlertDialog alert = new AlertDialog.Builder(HesapEkrani.this)
                                .setTitle("Porsiyon Seçiniz")
                                .setCancelable(false)
                                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int item) {
                                        // porsiyon seçimi tamamlanınca yapılacaklar
                                        porsiyonlarNegatif.clear();
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int item) {
                                        Double porsiyon;
                                        try {
                                            porsiyon = Double.parseDouble(porsiyonlarNegatif.get(item).substring(0, 4));
                                        } catch (Exception ex) {
                                            porsiyon = -1d;
                                        }

                                        porsiyon *= -1;

                                        if (porsiyon == 0.7)
                                            porsiyon = 0.75;
                                        else if (porsiyon == 0.2)
                                            porsiyon = 0.25;
                                        porsiyonCikar(porsiyon);

                                        String[] urunBilgileri = porsiyonlarNegatif.get(item).split("x");

                                        int adet = 1;

                                        if (urunBilgileri.length > 1)
                                            adet = Integer.parseInt(urunBilgileri[1]);

                                        adet--;

                                        if (adet != 0)
                                        {
                                            porsiyonlarNegatif.set(item, urunBilgileri[0] + "x" + adet);
                                        }
                                        else
                                        {
                                            porsiyonlarNegatif.remove(item);
                                        }
                                        adapter.notifyDataSetChanged();

                                        if(porsiyonlarNegatif.size() < 1)
                                        {
                                            dialog.dismiss();
                                        }
                                    }
                                })
                                .create();

                        alert.show();
                    }
                }
            }
        });

        if(lstOrderedProducts.size() < 1)
        {
            buttonSepet.setEnabled(false);
            buttonNot.setEnabled(false);
        }
    }

    private void porsiyonCikar(Double azaltmaMiktari)
    {
        if(azaltmaMiktari == 1)
        {
            // eğer varsa azalt
            porsiyonAzaltmaliMi(g.tamPorsiyon, lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi);
        }
        else if(azaltmaMiktari == 0.5)
        {
            // eğer varsa azalt
            porsiyonAzaltmaliMi(g.yarimPorsiyon, lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi);
        }
        else if(azaltmaMiktari == 0.25)
        {
            // eğer varsa azalt
            porsiyonAzaltmaliMi(g.ceyrekPorsiyon, lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi);
        }
        else if(azaltmaMiktari == 1.5)
        {
            // eğer varsa azalt
            porsiyonAzaltmaliMi(g.birBucukPorsiyon, lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi);
        }
        else
        {
            // eğer varsa azalt
            porsiyonAzaltmaliMi(g.ucCeyrekPorsiyon, lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi);
        }

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        lstOrderedProducts.get(selectedSiparisItemPosition).miktar =  df.format(Double.parseDouble(lstOrderedProducts.get(selectedSiparisItemPosition).miktar) - azaltmaMiktari);

        if(Double.parseDouble(lstOrderedProducts.get(selectedSiparisItemPosition).miktar) == 0d)
        {
            lstOrderedProducts.remove(selectedSiparisItemPosition);
            if(selectedSiparisItemPosition == lstOrderedProducts.size())
            {
                selectedSiparisItemPosition = lstOrderedProducts.size() - 1;
                adapterSecilenSiparisler.setSelectedIndex(lstOrderedProducts.size() - 1);
            }
            else
            {
                adapterSecilenSiparisler.notifyDataSetChanged();
            }
            if(lstOrderedProducts.size() < 1)
            {
                buttonSepet.setEnabled(false);
                buttonNot.setEnabled(false);
            }
        }
        else
        {
            adapterSecilenSiparisler.notifyDataSetChanged();
        }
    }

    private void porsiyonAzaltmaliMi(ArrayList<Siparis> porsiyonArrayi, String yemekAdi)
    {
        for(int i=0;i<porsiyonArrayi.size();i++)
        {
            if(porsiyonArrayi.get(i).yemekAdi.contentEquals(yemekAdi))
            {
                porsiyonArrayi.get(i).miktar = String.valueOf(Double.parseDouble(porsiyonArrayi.get(i).miktar)-1);
                if(Double.parseDouble(porsiyonArrayi.get(i).miktar) == 0d)
                    porsiyonArrayi.remove(i);
                break;
            }
        }
    }

    private void porsiyonEkle(Double arttirmaMiktari)
    {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        lstOrderedProducts.get(selectedSiparisItemPosition).miktar = df.format(Double.parseDouble(lstOrderedProducts.get(selectedSiparisItemPosition).miktar) + arttirmaMiktari);
        adapterSecilenSiparisler.notifyDataSetChanged();

        if(arttirmaMiktari == 1d)
        {
            porsiyonArttir(g.tamPorsiyon,lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi);
        }
        else if(arttirmaMiktari == 0.5)
        {
            porsiyonArttir(g.yarimPorsiyon,lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi);
        }
        else if(arttirmaMiktari == 0.25)
        {
            porsiyonArttir(g.ceyrekPorsiyon,lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi);
        }
        else if(arttirmaMiktari == 1.5)
        {
            porsiyonArttir(g.birBucukPorsiyon,lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi);
        }
        else
        {
            porsiyonArttir(g.ucCeyrekPorsiyon,lstOrderedProducts.get(selectedSiparisItemPosition).yemekAdi);
        }
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
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            df.setMinimumFractionDigits(0);
            df.setGroupingUsed(false);

            porsiyonArrayi.get(siparisVarMi).miktar = String.valueOf(df.format(Double.parseDouble(porsiyonArrayi.get(siparisVarMi).miktar) + 1));
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(!MasaKilitliMi)
        {
            if (v.getId()==R.id.listViewHesap) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.menu_urun, menu);

                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
                menu.setHeaderTitle(urunListesiToplam.get(info.position).yemekAdi);

                if(urunListesiToplam.get(info.position).porsiyonFiyati.contentEquals("ikram"))
                    menu.getItem(1).setTitle("İkramı İptal Et");
                else
                    menu.getItem(1).setTitle("Ürünü İkram Et");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        String baslik;

        switch(item.getItemId()) {
            case R.id.iptal:
                baslik = "Kaç adet ürün iptal edilecek? Bulunan :" + urunListesiToplam.get(info.position).miktar + " adet";
                break;
            case R.id.ikram:
                baslik = "Kaç adet ürün ikram edilecek? Bulunan :" + urunListesiToplam.get(info.position).miktar + " adet";
                break;
            default:
                return super.onContextItemSelected(item);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(HesapEkrani.this);
        builder.setTitle(baslik);

        final EditText input = new EditText(HesapEkrani.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);

        input.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

                double girilenSayi;
                try
                {
                    String x= s.toString();
                    girilenSayi = Double.parseDouble(x);
                }
                catch (Exception ex)
                {  return; }

                if(girilenSayi>Double.parseDouble(urunListesiToplam.get(info.position).miktar))
                {
                    input.setText(urunListesiToplam.get(info.position).miktar);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        builder.setView(input);

        builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String [] x = input.getText().toString().split("\\.");
                if(x.length == 2)
                {
                    if(!x[1].contentEquals("25")&&!x[1].contentEquals("50")&&!x[1].contentEquals("75"))
                    {
                        AlertDialog.Builder aBuilder = new AlertDialog.Builder(HesapEkrani.this);
                        aBuilder.setTitle("Porsiyon Hatası");
                        aBuilder.setMessage("Porsiyon hatalı girildiği için işlem gerçekleştirilemedi\nGirilen Porsiyon :" + input.getText()).setCancelable(false)
                                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        AlertDialog alertDialog = aBuilder.create();
                        alertDialog.show();
                    }
                }
            }
        });
        builder.setNegativeButton("Vazgeç", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
        return true;
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
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
