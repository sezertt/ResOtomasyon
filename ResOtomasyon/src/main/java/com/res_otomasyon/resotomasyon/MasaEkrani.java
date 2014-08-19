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
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import android.view.ViewGroup;
import android.view.WindowManager;

import Entity.MasaninSiparisleri;
import Entity.Siparis;
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
    ArrayList<Employee> lstEmployees;
    //
    CollectionPagerAdapter collectionPagerAdapter;
    Context context = this;
    GlobalApplication g;
    TryConnection t;

    public Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String[] parametreler = srvrMessage.split("&");
                    String[] esitlik;
                    final Dictionary<String, String> collection = new Hashtable<String, String>(parametreler.length);
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
                                    }
                                }
                            });
                            break;
                        case iptal:
                            mesajGeldi = false;
                            break;
                        case masaKapandi:
                            mesajGeldi = false;
                            kapananMasa = collection.get("masa");
                            kapananMasaDepartman = collection.get("departmanAdi");
                            fragment[0] = (FragmentMasaEkrani) collectionPagerAdapter.fragments[mViewPager
                                    .getCurrentItem()];
                            fragment[0].startKapananMasa(kapananMasa, kapananMasaDepartman);
                            break;
                        case siparis:
                            mesajGeldi = false;
                            if (activityVisible) {
                                if (g.secilenMasalar != null) {
                                    for (int i = 0; i < g.secilenMasalar.size(); i++) {
                                        MasaninSiparisleri masaninSiparisleri = new MasaninSiparisleri();
                                        masaninSiparisleri.DepartmanAdi = collection.get("departmanAdi");
                                        masaninSiparisleri.MasaAdi = collection.get("masa");
                                        Siparis siparis = new Siparis();
                                        if (g.secilenMasalar.get(i).DepartmanAdi.contentEquals(collection.get("departmanAdi"))) {
                                            for (String masa : g.secilenMasalar.get(i).Masalar) {
                                                if (masa.contentEquals(collection.get("masa"))) {
                                                    //process
                                                    siparis.miktar = collection.get("miktar");
                                                    siparis.porsiyonFiyati = collection.get("porsiyonFiyati");
                                                    siparis.porsiyonSinifi = Double.parseDouble(("1"));
                                                    siparis.yemekAdi = collection.get("yemekAdi");
                                                    masaninSiparisleri.siparisler.add(siparis);
                                                    g.masaninSiparisleri.get(i).siparisler.add(siparis);
                                                }
                                            }
                                            g.masaninSiparisleri.add(masaninSiparisleri);
                                        }
                                    }
                                } else {
                                    //process
                                }
                            }

                            break;
                        case masaAcildi:
                            mesajGeldi = false;
                            acilanMasa = collection.get("masa");
                            acilanMasaDepartman = collection.get("departmanAdi");
                            fragment[0] = (FragmentMasaEkrani) collectionPagerAdapter.fragments[mViewPager
                                    .getCurrentItem()];
                            fragment[0].startAcilanMasa(acilanMasa, acilanMasaDepartman);
                            break;
                        case departman:
                            mesajGeldi = false;
                            acikMasalar = null;
                            try {
                                acikMasalar = collection.get("masa").split("\\*");
                            } catch (Exception e) {
                                acikMasalar = null;
                            }
                            fragment[0] = (FragmentMasaEkrani) collectionPagerAdapter.fragments[mViewPager.getCurrentItem()];
                            fragment[0].startSendAcikMasalar(acikMasalar, tabName);
                            break;
                    }
                    break;
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
        try {
            g = (GlobalApplication) getApplicationContext();

            if (g.broadcastReceiver != null) {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(g.broadcastReceiver);

            }
            g.broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    srvrMessage = intent.getStringExtra("message");
                    myHandler.sendEmptyMessage(1);
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
        this.masaPlanIsmi = readXML.masaPlanIsimleri;
        preferences = this.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE);
        if (preferences.getBoolean("MasaKilitli", masaKilitliMi)) {
            this.setVisible(false);
        }
        //Giriş ekranından gelen çalışan bilgilerini alır.
        Bundle extras = getIntent().getExtras();
        lstEmployees = (ArrayList<Employee>) extras.getSerializable("lstEmployees");
        Object obj = new Object();
        //fragment[0] 'ın boş gelmemesi için gerekli.
        synchronized (obj) {
            setContentView(R.layout.activity_masa_ekrani);
            collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());
            collectionPagerAdapter.lstDepartmanlar = lstDepartmanlar;
            collectionPagerAdapter.lstMasaDizayn = lstMasaDizayn;
            collectionPagerAdapter.masaPlanIsmi = masaPlanIsmi;
            collectionPagerAdapter.lstEmployees = lstEmployees;
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
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
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

    BroadcastReceiver rec;

    {
        rec = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //all events will be received here
                //get message
                srvrMessage = intent.getStringExtra("message");
                myHandler.sendEmptyMessage(1);
            }
        };
    }

    @Override
    protected void onResume() {
        if (g == null)
            g = (GlobalApplication) getApplicationContext();
        if (t == null)
            t = new TryConnection(g, myHandler);
        if (!g.commonAsyncTask.client.mRun && !t.timerRunning) {
            t.startTimer();
        }
        activityVisible = true;
        super.onResume();
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

        /*
        if (id == R.id.action_settings)
            return true;
        return super.onOptionsItemSelected(item);
        */

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