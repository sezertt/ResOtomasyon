package com.res_otomasyon.resotomasyon;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;

import Entity.GlobalDepartman;
import Entity.GlobalMasalar;
import Entity.MasaninSiparisleri;
import Entity.Siparis;
import ekclasslar.BildirimBilgileriIslemler;
import ekclasslar.GarsonIslemler;
import ekclasslar.HesapIslemler;
import ekclasslar.MasaTemizleIslemler;
import ekclasslar.SiparisIslemler;
import ekclasslar.CollectionPagerAdapter;
import ekclasslar.FileIO;
import Entity.Departman;
import Entity.Employee;
import Entity.MasaDizayn;
import TCPClientSide.CommonAsyncTask;
import XMLReader.ReadXML;
import ekclasslar.SetViewGroupEnabled;
import ekclasslar.TryConnection;


public class MasaEkrani extends ActionBarActivity implements CommonAsyncTask.OnAsyncRequestComplete, android.support.v7.app.ActionBar.TabListener {

    final FragmentMasaEkrani[] fragment = {new FragmentMasaEkrani()};
    //
    android.support.v7.app.ActionBar actionBar;
    android.support.v7.app.ActionBar.Tab tab;
    ViewPager mViewPager;
    //
    ArrayList<Departman> lstDepartmanlar;
    ArrayList<MasaDizayn> lstMasaDizayn;
    String[] masaPlanIsmi;
    String tabName;
    String acilanMasa;
    String acilanMasaDepartman;
    String kapananMasa;
    String kapananMasaDepartman;
    SharedPreferences preferences;
    //
    public String[] acikMasalar;
    public String srvrMessage;
    public boolean mesajGeldi = false;
    boolean activityVisible = true;
    boolean masaKilitliMi = false;
    Employee employee;

    //
    CollectionPagerAdapter collectionPagerAdapter;
    Context context = this;
    GlobalApplication g;
    TryConnection t;
    Menu menu;
    Dictionary<String, String> collection;
    int siparisCounter = 0, yapilmasiGerekenIslem;

    AlertDialog masaTasimaIcinMasaSecimiAlertDialog, alertDialog2;
    TasimaIcinMasaSecimiExpandableListAdapter tasimaIcinMasaExpandableListAdapter;

    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    //Server ile bağlantı kurulup kurulmadığını kontrol etmek için gönderilen mesaj.
                    preferences = MasaEkrani.this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                    String girisKomutu = "komut=giris&nick=" + preferences.getString("TabletName", "Tablet");

                    if (g.commonAsyncTask.client != null) {
                        if (g.commonAsyncTask.client.out != null) {
                            g.commonAsyncTask.client.sendMessage(girisKomutu);
                            MasaEkrani.this.getSupportActionBar().setTitle(getString(R.string.app_name) + "(Bağlı)");
                            SetViewGroupEnabled.setViewGroupEnabled((ViewPager) findViewById(R.id.masaEkrani), true);
                            t.stopTimer();
                            if (menu.findItem(R.id.action_notification).isVisible()) {
                                menu.findItem(R.id.action_notification).setEnabled(true);
                            }
                            g.commonAsyncTask.client.sendMessage(notificationMessage());
                            g.commonAsyncTask.client.sendMessage("komut=departman&departmanAdi=" + tabName);
                        } else {
                            hataVer();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = this.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE);
        try {
            g = (GlobalApplication) getApplicationContext();

            if (g.broadcastReceiver != null) {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(g.broadcastReceiver);
            }
            g.broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    srvrMessage = intent.getStringExtra("message");
                    String[] parametreler = srvrMessage.split("&");
                    String[] esitlik;
                    collection = new Hashtable<String, String>(parametreler.length);
                    for (String parametre : parametreler) {
                        esitlik = parametre.split("=");
                        if (esitlik.length == 2)
                            collection.put(esitlik[0], esitlik[1]);
                    }
                    String gelenkomut = collection.get("komut");
                    GlobalApplication.Komutlar komut = GlobalApplication.Komutlar.valueOf(gelenkomut);
                    switch (komut) {
                        case baglanti:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (activityVisible) {
                                        if (!t.timerRunning)
                                            t.startTimer();
                                        MasaEkrani.this.getSupportActionBar().setTitle(getString(R.string.app_name) + "(Bağlantı yok)");
                                        SetViewGroupEnabled.setViewGroupEnabled((ViewPager) findViewById(R.id.masaEkrani), false);
                                        if (menu.findItem(R.id.action_notification).isVisible()) {
                                            menu.findItem(R.id.action_notification).setEnabled(false);
                                        }
                                    }
                                }
                            });
                            break;
                        case iptal:
                            mesajGeldi = false;
                            for (int j = 0; j < g.lstMasaninSiparisleri.size(); j++) {
                                if (g.lstMasaninSiparisleri.get(j).MasaAdi.contentEquals(collection.get("masa")) && g.lstMasaninSiparisleri.get(j).DepartmanAdi.contentEquals(collection.get("departmanAdi"))) {
                                    for (int i = 0; i < g.lstMasaninSiparisleri.get(j).siparisler.size(); i++) {
                                        if (g.lstMasaninSiparisleri.get(j).siparisler.get(i).siparisYemekAdi.contentEquals(collection.get("yemekAdi"))) {
                                            int siparisAdedi = g.lstMasaninSiparisleri.get(j).siparisler.get(i).siparisAdedi;
                                            int iptalAdedi = Integer.parseInt(collection.get("miktar"));
                                            if (siparisAdedi > iptalAdedi)
                                                g.lstMasaninSiparisleri.get(j).siparisler.get(i).siparisAdedi = siparisAdedi - iptalAdedi;
                                            else {
                                                g.lstMasaninSiparisleri.get(j).siparisler.remove(i);
                                                if (g.lstMasaninSiparisleri.get(j).siparisler.size() == 0)
                                                    g.lstMasaninSiparisleri.remove(j);
                                            }
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (g.adapter != null)
                                                        g.adapter.notifyDataSetChanged();
                                                }
                                            });
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                        case masaKapandi:
                            mesajGeldi = false;
                            kapananMasa = collection.get("masa");
                            kapananMasaDepartman = collection.get("departmanAdi");
                            fragment[0] = (FragmentMasaEkrani) collectionPagerAdapter.fragments[mViewPager.getCurrentItem()];
                            fragment[0].startKapananMasa(kapananMasa, kapananMasaDepartman);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i = 0; i < g.lstMasaninSiparisleri.size(); i++) {
                                        if (g.lstMasaninSiparisleri.get(i).DepartmanAdi.contentEquals(kapananMasaDepartman) && g.lstMasaninSiparisleri.get(i).MasaAdi.contentEquals(kapananMasa)) {
                                            if (g.lstMasaninSiparisleri != null)
                                                g.lstMasaninSiparisleri.remove(i);
                                            if (g.adapter != null)
                                                g.adapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                }
                            });

                            for (int i = 0; i < g.globalDepartmanlar.size(); i++) {
                                if (g.globalDepartmanlar.get(i).globalDepartmanAdi.contentEquals(collection.get("departmanAdi"))) {
                                    for (int x = 0; x < g.globalDepartmanlar.get(i).globalMasalar.size(); x++) {
                                        if (g.globalDepartmanlar.get(i).globalMasalar.get(x).globalMasaAdi.contentEquals(collection.get("masa"))) {
                                            g.globalDepartmanlar.get(i).globalMasalar.get(x).globalMasaAcikMi = false;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            break;
                        case siparis:
                            if (collection.get("tur") != null)
                                if (collection.get("tur").contentEquals("K"))
                                    return;
                            masaKilitliMi = preferences.getBoolean("MasaKilitli", masaKilitliMi);
                            //Sipariş geldiğinde yapılacak işlemler.
                            SiparisIslemler siparisIslemler = new SiparisIslemler(collection, g);
                            //Gelen sipariş seçilen masalara ait ise titreşim yarat.
                            if (siparisIslemler.Islem() && !masaKilitliMi) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Get instance of Vibrator from current Context
                                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                        if (!menu.findItem(R.id.action_notification).isVisible()) {
                                            menu.findItem(R.id.action_notification).setVisible(true);
                                            // Vibrate for 300 milliseconds
                                            v.vibrate(2000);
                                        }
                                        siparisCounter++;
                                        if (siparisCounter == 10) {
                                            siparisCounter = 0;
                                            v.vibrate(2000);
                                        }
                                    }
                                });
                            }
                            break;
                        case masaAcildi:
                            mesajGeldi = false;
                            acilanMasa = collection.get("masa");
                            acilanMasaDepartman = collection.get("departmanAdi");
                            fragment[0] = (FragmentMasaEkrani) collectionPagerAdapter.fragments[mViewPager
                                    .getCurrentItem()];
                            fragment[0].startAcilanMasa(acilanMasa, acilanMasaDepartman);
                            if (acilanMasa != null && acilanMasaDepartman != null)
                                for (int i = 0; i < g.globalDepartmanlar.size(); i++) {
                                    if (g.globalDepartmanlar.get(i).globalDepartmanAdi.contentEquals(acilanMasaDepartman)) {
                                        for (int x = 0; x < g.globalDepartmanlar.get(i).globalMasalar.size(); x++) {
                                            if (g.globalDepartmanlar.get(i).globalMasalar.get(x).globalMasaAdi.contentEquals(acilanMasa)) {
                                                g.globalDepartmanlar.get(i).globalMasalar.get(x).globalMasaAcikMi = true;
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                            break;
                        case departman:
                            mesajGeldi = false;
                            acikMasalar = null;
                            try {
                                acikMasalar = collection.get("masa").split("\\*");
                            } catch (Exception e) {
                                acikMasalar = null;
                            }
                            if (acikMasalar != null) {
                                for (int i = 0; i < g.globalDepartmanlar.size(); i++) {
                                    if (g.globalDepartmanlar.get(i).globalDepartmanAdi.contentEquals(tabName)) {
                                        for (String anAcikMasalar : acikMasalar) {
                                            for (int k = 0; k < g.globalDepartmanlar.get(i).globalMasalar.size(); k++) {
                                                if (g.globalDepartmanlar.get(i).globalMasalar.get(k).globalMasaAdi.contentEquals(anAcikMasalar)) {
                                                    g.globalDepartmanlar.get(i).globalMasalar.get(k).globalMasaAcikMi = true;
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    fragment[0] = (FragmentMasaEkrani) collectionPagerAdapter.fragments[mViewPager.getCurrentItem()];
                                    fragment[0].startSendAcikMasalar(acikMasalar, tabName);
                                }
                            });
                            break;
                        case bildirimBilgileri:
                            //onResume tetiklendiğin notification uyarı butonunun görnüp görünmeyeceği sipariş listesinin dolu olup olmadığına
                            //bağlı olduğu için onResume() metodunun bildirdimBilgilerinden sonra çalışması gerekmektedir.
                            //Bu yüzden runOnUiThread e konmuştur. bildirimBilgileri için gerekli mesaj ise onAttachedToWindow metdodunda
                            //gönderildi.
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (activityVisible) {
                                        g.lstMasaninSiparisleri = new ArrayList<MasaninSiparisleri>();
                                        if (g.lstMasaninSiparisleri.size() == 0) {
                                            BildirimBilgileriIslemler bildirimBilgileriIslemler = new BildirimBilgileriIslemler(collection, g);
                                            if (bildirimBilgileriIslemler.bildirimBilgileri()) {
                                                if (!menu.findItem(R.id.action_notification).isVisible())
                                                    menu.findItem(R.id.action_notification).setVisible(true);
                                            }
                                        }
                                    }
                                }
                            });

                            break;
                        case GarsonIstendi:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    GarsonIslemler garsonIstendi = new GarsonIslemler(collection, g);
                                    if (garsonIstendi.Istendi()) {
                                        if (!menu.findItem(R.id.action_notification).isVisible())
                                            menu.findItem(R.id.action_notification).setVisible(true);
                                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                        v.vibrate(2000);
                                    }
                                }
                            });
                            break;
                        case TemizlikIstendi:
                            MasaTemizleIslemler masaTemizleIstendi = new MasaTemizleIslemler(collection, g);
                            if (masaTemizleIstendi.Islem()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!menu.findItem(R.id.action_notification).isVisible())
                                            menu.findItem(R.id.action_notification).setVisible(true);
                                    }
                                });
                            }
                            break;
                        case HesapIstendi:
                            HesapIslemler hesapIstendi = new HesapIslemler(collection, g);
                            hesapIstendi.Istendi();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!menu.findItem(R.id.action_notification).isVisible())
                                        menu.findItem(R.id.action_notification).setVisible(true);
                                }
                            });
                            break;
                        case bildirimGoruldu:

                            for (int j = 0; j < g.lstMasaninSiparisleri.size(); j++) {
                                if (g.lstMasaninSiparisleri.get(j).DepartmanAdi.contentEquals(collection.get("departmanAdi")) && g.lstMasaninSiparisleri.get(j).MasaAdi.contentEquals(collection.get("masa"))) {

                                    for (int i = 0; i < g.lstMasaninSiparisleri.get(j).siparisler.size(); i++) {
                                        Siparis siparis = g.lstMasaninSiparisleri.get(j).siparisler.get(i);
                                        if (siparis.siparisYemekAdi.contentEquals(collection.get("yemekAdi")) && siparis.siparisAdedi == Integer.parseInt(collection.get("adedi")) && siparis.siparisPorsiyonu == Double.parseDouble(collection.get("porsiyonu"))) {
                                            g.lstMasaninSiparisleri.get(j).siparisler.remove(i);
                                            if (g.lstMasaninSiparisleri.get(j).siparisler.size() == 0)
                                                g.lstMasaninSiparisleri.remove(j);
                                            break;
                                        } else if (collection.get("yemekAdi").contentEquals("hepsi")) {
                                            g.lstMasaninSiparisleri.remove(j);
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (g.adapter != null)
                                        g.adapter.notifyDataSetChanged();
                                }
                            });
                            break;
                        case urunuTasiTablet:
                            String[] aktarmalar = collection.get("aktarmaBilgileri").split("\\*");

                            for (String anAktarmalar : aktarmalar) {
                                String yemekAdi;
                                double fiyat;
                                int istenilenTasimaMiktari, tasinacakUrunIkramMi;
                                Double porsiyon;

                                String[] detaylari = anAktarmalar.split("-");
                                yemekAdi = detaylari[0];
                                fiyat = Double.parseDouble(detaylari[1].replace(',', '.'));
                                istenilenTasimaMiktari = Integer.parseInt(detaylari[2].replace(',', '.'));
                                int notificationTasinacakMiktar = 0;
                                tasinacakUrunIkramMi = Integer.parseInt(detaylari[3]);
                                porsiyon = Double.parseDouble(detaylari[4].replace(',', '.'));
                                if (g.lstMasaninSiparisleri.size() > 0) {
                                    for (int x = 0; x < g.lstMasaninSiparisleri.size(); x++) {
                                        MasaninSiparisleri msp = g.lstMasaninSiparisleri.get(x);
                                        if (msp.DepartmanAdi.contentEquals(collection.get("departmanAdi")) && msp.MasaAdi.contentEquals(collection.get("masa"))) {
                                            for (int z = 0; z < msp.siparisler.size(); z++) {
                                                if (msp.siparisler.get(z).siparisYemekAdi.contentEquals(yemekAdi) && msp.siparisler.get(z).siparisPorsiyonu == porsiyon) {
                                                    if (msp.siparisler.get(z).siparisAdedi <= istenilenTasimaMiktari) {
                                                        notificationTasinacakMiktar = msp.siparisler.get(z).siparisAdedi;
                                                        msp.siparisler.get(z).siparisAdedi = msp.siparisler.get(z).siparisAdedi - istenilenTasimaMiktari;
                                                        msp.siparisler.remove(z);
                                                        z--;
                                                        Dictionary<String, String> collectionSiparisIslem = new Hashtable<String, String>();
                                                        collectionSiparisIslem.put("departmanAdi", collection.get("yeniDepartmanAdi"));
                                                        collectionSiparisIslem.put("masa", collection.get("yeniMasa"));
                                                        collectionSiparisIslem.put("miktar", String.valueOf(notificationTasinacakMiktar));
                                                        collectionSiparisIslem.put("yemekAdi", yemekAdi);
                                                        collectionSiparisIslem.put("porsiyon", String.valueOf(porsiyon));
                                                        SiparisIslemler siparisIslemler1 = new SiparisIslemler(collectionSiparisIslem, g);
                                                        siparisIslemler1.Islem();
                                                        istenilenTasimaMiktari -= notificationTasinacakMiktar;

                                                    } else {
                                                        notificationTasinacakMiktar = istenilenTasimaMiktari;
                                                        msp.siparisler.get(z).siparisAdedi = msp.siparisler.get(z).siparisAdedi - istenilenTasimaMiktari;
                                                        Dictionary<String, String> collectionSiparisIslem = new Hashtable<String, String>();
                                                        collectionSiparisIslem.put("departmanAdi", collection.get("yeniDepartmanAdi"));
                                                        collectionSiparisIslem.put("masa", collection.get("yeniMasa"));
                                                        collectionSiparisIslem.put("miktar", String.valueOf(notificationTasinacakMiktar));
                                                        collectionSiparisIslem.put("yemekAdi", yemekAdi);
                                                        collectionSiparisIslem.put("porsiyon", String.valueOf(porsiyon));
                                                        SiparisIslemler siparisIslemler1 = new SiparisIslemler(collectionSiparisIslem, g);
                                                        siparisIslemler1.Islem();
                                                        break;
                                                    }
                                                }
                                            }
                                            if (g.lstMasaninSiparisleri.get(x).siparisler.size() == 0) {
                                                g.lstMasaninSiparisleri.remove(x);
                                                break;
                                            }

                                        }
                                    }
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (g.adapter != null)
                                        g.adapter.notifyDataSetChanged();
                                }
                            });
                            break;
                        case GarsonGoruldu:
                            GarsonIslemler garsonIslemler = new GarsonIslemler(collection, g);
                            garsonIslemler.Goruldu();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (g.adapter != null)
                                        g.adapter.notifyDataSetChanged();
                                }
                            });
                            break;
                        case TemizlikGoruldu:
                            MasaTemizleIslemler masaTemizle = new MasaTemizleIslemler(collection, g);
                            masaTemizle.Goruldu();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (g.adapter != null)
                                        g.adapter.notifyDataSetChanged();
                                }
                            });
                            break;
                        case HesapGoruldu:
                            HesapIslemler hesapIslemler = new HesapIslemler(collection, g);
                            hesapIslemler.Goruldu();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (g.adapter != null)
                                        g.adapter.notifyDataSetChanged();
                                }
                            });
                            break;
                        case departmanMasaTasimaIcin:

                            if (collection.get("masaDepartman") == null) {
                                String[] masaSecimiIcinAcikMasalar;
                                try {
                                    masaSecimiIcinAcikMasalar = collection.get("masa").split("\\*");
                                } catch (Exception e) {
                                    masaSecimiIcinAcikMasalar = null;
                                }

                                if (masaSecimiIcinAcikMasalar != null) {
                                    for (int i = 0; i < g.globalDepartmanlar.size(); i++) {
                                        if (g.globalDepartmanlar.get(i).globalDepartmanAdi.contentEquals(tabName)) {
                                            for (String anmasaSecimiIcinAcikMasalar : masaSecimiIcinAcikMasalar) {
                                                for (int k = 0; k < g.globalDepartmanlar.get(i).globalMasalar.size(); k++) {
                                                    if (g.globalDepartmanlar.get(i).globalMasalar.get(k).globalMasaAdi.contentEquals(anmasaSecimiIcinAcikMasalar)) {
                                                        g.globalDepartmanlar.get(i).globalMasalar.get(k).globalMasaAcikMi = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final String[] masaDepartman = collection.get("masaDepartman").split("-");
                                        final String masaAdi = masaDepartman[0], departmanAdi = masaDepartman[1];

                                        LayoutInflater inflater = ((LayoutInflater) MasaEkrani.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE));

                                        ExpandableListView expandableListViewTasimaIcinMasaSec = (ExpandableListView) inflater.inflate(R.layout.expandaplelistviewalert, null, false);

                                        tasimaIcinMasaExpandableListAdapter = new TasimaIcinMasaSecimiExpandableListAdapter(MasaEkrani.this, g);
                                        expandableListViewTasimaIcinMasaSec.setAdapter(tasimaIcinMasaExpandableListAdapter);
                                        expandableListViewTasimaIcinMasaSec.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                                            @Override
                                            public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {
                                                if (!masaTasimaIcinMasaSecimiAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled())
                                                    return true;

                                                // SEÇİLEN DEPARTMAN g.globalDepartmanlar.get(groupPosition).globalDepartmanAdi
                                                // SEÇİLEN MASA g.globalDepartmanlar.get(groupPosition).globalMasalar.get(childPosition).globalMasaAdi

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (g.globalDepartmanlar.get(groupPosition).globalDepartmanAdi.contentEquals(departmanAdi) && g.globalDepartmanlar.get(groupPosition).globalMasalar.get(childPosition).globalMasaAcikMi) // departman değişmedi ve masaların ikisi de açık
                                                        {
                                                            yapilmasiGerekenIslem = 0;
                                                        } else if (g.globalDepartmanlar.get(groupPosition).globalMasalar.get(childPosition).globalMasaAcikMi) // masalar açık departman değişti
                                                        {
                                                            yapilmasiGerekenIslem = 1;
                                                        } else if (g.globalDepartmanlar.get(groupPosition).globalDepartmanAdi.contentEquals(departmanAdi)) // departman değişmedi 1 masa açık
                                                        {
                                                            yapilmasiGerekenIslem = 2;
                                                        } else // departmanda değişti 1 masa açık
                                                        {
                                                            yapilmasiGerekenIslem = 3;
                                                        }

                                                        AlertDialog.Builder aBuilder = new AlertDialog.Builder(MasaEkrani.this);
                                                        aBuilder.setTitle("Masa Değiştirme")
                                                                .setMessage(departmanAdi + " departmanı " + masaAdi + " masası, " + g.globalDepartmanlar.get(groupPosition).globalDepartmanAdi + " departmanı " + g.globalDepartmanlar.get(groupPosition).globalMasalar.get(childPosition).globalMasaAdi + " masası ile yer değiştirilecektir. Onaylıyor musunuz?")
                                                                .setCancelable(false)
                                                                .setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int item) {
                                                                        if (departmanAdi.contentEquals(g.globalDepartmanlar.get(groupPosition).globalDepartmanAdi) && masaAdi.contentEquals(g.globalDepartmanlar.get(groupPosition).globalMasalar.get(childPosition).globalMasaAdi)) {
                                                                            masaTasimaIcinMasaSecimiAlertDialog.dismiss();
                                                                            return;
                                                                        }
                                                                        g.commonAsyncTask.client.sendMessage("komut=masaDegistirTablet&yeniMasa=" + g.globalDepartmanlar.get(groupPosition).globalMasalar.get(childPosition).globalMasaAdi + "&yeniDepartmanAdi=" + g.globalDepartmanlar.get(groupPosition).globalDepartmanAdi + "&eskiMasa=" + masaAdi + "&eskiDepartmanAdi=" + departmanAdi + "&yapilmasiGereken=" + yapilmasiGerekenIslem);
                                                                        masaTasimaIcinMasaSecimiAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                                                                    }
                                                                })
                                                                .setNegativeButton("Hayır", null)
                                                                .create();
                                                        alertDialog2 = aBuilder.create();
                                                        alertDialog2.show();
                                                    }
                                                });
                                                return true;
                                            }
                                        });

                                        masaTasimaIcinMasaSecimiAlertDialog = new AlertDialog.Builder(MasaEkrani.this)
                                                .setTitle("Seçilen masanın değiştirilmesi istediğini masayı seçiniz (" + departmanAdi + " - " + masaAdi + ")")
                                                .setView(expandableListViewTasimaIcinMasaSec)
                                                .setCancelable(false)
                                                .setNegativeButton("Vazgeç", null)
                                                .create();

                                        masaTasimaIcinMasaSecimiAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                            @Override
                                            public void onShow(DialogInterface dialog) {
                                                final Button negativeButton = masaTasimaIcinMasaSecimiAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                                                negativeButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {

                                                        masaTasimaIcinMasaSecimiAlertDialog.dismiss();
                                                    }
                                                });
                                            }
                                        });
                                        masaTasimaIcinMasaSecimiAlertDialog.show();
                                    }
                                });
                            }
                            break;
                        case masaDegistirTablet:
                            //2 ve 3 bir masa açık.
                            int yapilmasiGerekenIslem = Integer.parseInt(collection.get("yapilmasiGerekenIslem"));
                            if (yapilmasiGerekenIslem == 2 || yapilmasiGerekenIslem == 3) {
                                g.commonAsyncTask.client.sendMessage("komut=masaKapandi&masa=" + collection.get("masa") + "&departmanAdi=" + collection.get("departmanAdi"));
                                g.commonAsyncTask.client.sendMessage("komut=masaAcildi&masa=" + collection.get("yeniMasa") + "&departmanAdi=" + collection.get("yeniDepartmanAdi"));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (masaTasimaIcinMasaSecimiAlertDialog != null)
                                        masaTasimaIcinMasaSecimiAlertDialog.dismiss();
                                    if(g.adapter!=null)
                                        g.adapter.notifyDataSetChanged();
                                }
                            });
                            break;
                        case masaDegistir:
                            for (int i = 0; i<g.lstMasaninSiparisleri.size(); i++)
                            {
                                if(g.lstMasaninSiparisleri.get(i).DepartmanAdi.contentEquals(collection.get("departmanAdi")) && g.lstMasaninSiparisleri.get(i).MasaAdi.contentEquals(collection.get("masa")))
                                {
                                    g.lstMasaninSiparisleri.get(i).DepartmanAdi = collection.get("yeniDepartmanAdi");
                                    g.lstMasaninSiparisleri.get(i).MasaAdi = collection.get("yeniMasa");
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(g.adapter!=null)
                                        g.adapter.notifyDataSetChanged();
                                }
                            });
                            break;
                        default:
                            break;
                    }
                    Log.e("MasaEkrani mesaj:", srvrMessage);
//                    myHandler.sendEmptyMessage(1);
                }
            };
            LocalBroadcastManager.getInstance(context).registerReceiver(g.broadcastReceiver, new IntentFilter("myevent"));
        } catch (Exception ignored) {

        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FileIO fileIO = new FileIO();
        List<File> files;
        files = fileIO.getListFiles(new File(Environment.getExternalStorageDirectory().getPath() + "/shared/Lenovo"));
        ReadXML readXML = new ReadXML();
        lstDepartmanlar = readXML.readDepartmanlar(files);
        lstMasaDizayn = readXML.readMasaDizayn(files);
//        for (Departman aLstDepartmanlar : lstDepartmanlar) {
//            GlobalDepartman departman = new GlobalDepartman();
//            departman.globalDepartmanAdi = aLstDepartmanlar.DepartmanAdi;
//            departman.globalMasalar = new ArrayList<GlobalMasalar>();
//
//            for (MasaDizayn aLstMasaDizayn : lstMasaDizayn) {
//                if (aLstMasaDizayn.MasaEkraniAdi.contentEquals(aLstDepartmanlar.DepartmanEkrani)) {
//                    GlobalMasalar masa = new GlobalMasalar();
//                    masa.globalMasaAdi = aLstMasaDizayn.MasaAdi;
//                    masa.globalMasaAcikMi = false;
//                    departman.globalMasalar.add(masa);
//                } else {
//                    if (departman.globalMasalar.size() > 0)
//                        break;
//                }
//            }
//            g.globalDepartmanlar.add(departman);
//        }

        this.masaPlanIsmi = readXML.masaPlanIsimleri;
        if (preferences.getBoolean("MasaKilitli", masaKilitliMi)) {
            this.setVisible(false);
        }
        //Giriş ekranından gelen çalışan bilgilerini alır.
        Bundle extras = getIntent().getExtras();
        employee = (Employee) extras.getSerializable("Employee");
        Object obj = new Object();
        //fragment[0] 'ın boş gelmemesi için gerekli.
        synchronized (obj) {
            setContentView(R.layout.activity_masa_ekrani);
            collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());
            collectionPagerAdapter.lstDepartmanlar = lstDepartmanlar;
            collectionPagerAdapter.lstMasaDizayn = lstMasaDizayn;
            collectionPagerAdapter.masaPlanIsmi = masaPlanIsmi;
            collectionPagerAdapter.employee = employee;
            collectionPagerAdapter.kilitliDepartmanAdi = preferences.getString("departmanAdi", tabName);
            collectionPagerAdapter.kilitliMasaAdi = preferences.getString("masaAdi", null);
            mViewPager = (ViewPager) findViewById(R.id.masaEkrani);
            mViewPager.setOffscreenPageLimit(lstDepartmanlar.size() - 1);
            mViewPager.setAdapter(collectionPagerAdapter);
            mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    actionBar.setSelectedNavigationItem(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
        t = new TryConnection(g, myHandler);
        SetViewGroupEnabled.setViewGroupEnabled((ViewGroup) findViewById(R.id.masaEkrani), false);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        actionBar = MasaEkrani.this.getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        for (Departman departman : lstDepartmanlar) {
            tab = actionBar.newTab().setText(departman.DepartmanAdi);
            tab.setTabListener(this);
            actionBar.addTab(tab);
        }
        if (g.commonAsyncTask.client != null) {
            if (g.commonAsyncTask.client.out != null) {
                MasaEkrani.this.getSupportActionBar().setTitle(getString(R.string.app_name) + "(Bağlı)");
                SetViewGroupEnabled.setViewGroupEnabled((ViewGroup) findViewById(R.id.masaEkrani), true);
            } else {
                MasaEkrani.this.getSupportActionBar().setTitle(getString(R.string.app_name) + "(Bağlantı yok)");
                SetViewGroupEnabled.setViewGroupEnabled((ViewGroup) findViewById(R.id.masaEkrani), false);
            }
        }
        if (g.commonAsyncTask.client != null)
            g.commonAsyncTask.client.sendMessage(notificationMessage());
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (preferences == null)
            preferences = MasaEkrani.this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        if (preferences.getBoolean("MasaKilitli", masaKilitliMi)) {
            for (int i = 0; i < actionBar.getTabCount(); i++) {
                if (actionBar.getTabAt(i).getText().toString().contentEquals(preferences.getString
                        ("departmanAdi", "Departman2"))) {
                    actionBar.setSelectedNavigationItem(actionBar.getTabAt(i).getPosition());
                    this.fragment[0] = (FragmentMasaEkrani) collectionPagerAdapter.fragments[actionBar
                            .getTabAt(i).getPosition()];
                }
            }
        }
        super.onAttachFragment(fragment);
    }

    @Override
    protected void onPause() {
        activityVisible = false;
        if (t.timerRunning)
            t.stopTimer();
        super.onPause();
    }

    @Override
    protected void onStop() {
        activityVisible = false;
        if (t.timerRunning)
            t.stopTimer();
        super.onStop();
    }

    @Override
    protected void onResume() {
        if (g == null)
            g = (GlobalApplication) getApplicationContext();
        if (t == null)
            t = new TryConnection(g, myHandler);
        if (!g.baglantiVarMi && !t.timerRunning) {
            t.startTimer();
        }
        activityVisible = true;
        if (menu != null)
            if (g.lstMasaninSiparisleri == null || g.lstMasaninSiparisleri.size() == 0)
                menu.findItem(R.id.action_notification).setVisible(false);
        super.onResume();
    }

    public String notificationMessage() {
        String komut, masalar = "";
        komut = "komut=bildirim&masalar=";
        if (g.secilenMasalar.size() > 0) {
            for (int i = 0; i < lstDepartmanlar.size(); i++) {
                if (g.secilenMasalar != null && g.secilenMasalar.size() > 0) {
                    if (g.secilenMasalar.get(i).Masalar.size() > 0) {
                        masalar += "*" + lstDepartmanlar.get(i).DepartmanAdi;
                        for (String dptMasalar : g.secilenMasalar.get(i).Masalar) {
                            masalar += "-" + dptMasalar;
                        }
                    }
                }
            }
            komut += masalar.substring(1, masalar.length());
        } else {
            komut = "komut=bildirim&masalar=hepsi";
        }
        return komut;
    }

    private void hataVer() {
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
        aBuilder.setTitle("Bağlantı Hatası");
        aBuilder.setMessage("Sunucuya bağlanırken bir hata ile karşılaşıldı. Lütfen tekrar deneyiniz")
                .setCancelable(false)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MasaEkrani.this.finish();
                    }
                });
        AlertDialog alertDialog = aBuilder.create();
        alertDialog.show();
    }

    @Override
    public void asyncResponse(String mesaj) {
        //Hata veridir ve finish yap.
        this.finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.masa_ekrani, menu);
        this.menu = menu;
        if (g.lstMasaninSiparisleri.size() == 0)
            menu.findItem(R.id.action_notification).setVisible(false);
        else
            menu.findItem(R.id.action_notification).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_notification) {
            Intent intent = new Intent(MasaEkrani.this, NotificationScreen.class);
            intent.putExtra("lstDepartmanlar", lstDepartmanlar);
            startActivity(intent);
        }
        return id == R.id.action_settings || super.onOptionsItemSelected(item);

        /*
        if (id == R.id.action_settings)
            return true;
        return super.onOptionsItemSelected(item);
        */

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Çıkış İşlemi")
                .setMessage("Çıkış yapmak istediğinizden emin misiniz?")
                .setCancelable(false)
                .setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (g.broadcastReceiver != null) {
                            LocalBroadcastManager.getInstance(context).unregisterReceiver(g.broadcastReceiver);
                        }
                        MasaEkrani.this.finish();
                    }
                })
                .setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onTabSelected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
        tabName = (String) tab.getText();
        String komut = "komut=departman&departmanAdi=" + tabName;
        if (g.commonAsyncTask.client != null && !mesajGeldi) {
            g.commonAsyncTask.client.sendMessage(komut);
        }
    }

    @Override
    public void onTabUnselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }
}