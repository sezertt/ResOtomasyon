package com.res_otomasyon.resotomasyon;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import android.support.v4.app.FragmentActivity;

import Entity.Departman;
import Entity.Employee;
import Entity.MasaDizayn;
import TCPClientSide.CommonAsyncTask;
import TCPClientSide.ConnectTCP;
import TCPClientSide.TCPClient;
import XMLReader.ReadXML;


public class MainScreen extends FragmentActivity implements ActionBar.TabListener,
        CommonAsyncTask.OnAsyncRequestComplete {

    final FragmentMasaDesign[] fragment = {new FragmentMasaDesign()};
    //
    ActionBar actionBar;
    ActionBar.Tab tab;
    ViewPager mViewPager;
    //
    TCPClient mTcpClient;
    CommonAsyncTask commonAsyncTask;
    //
    ArrayList<Departman> lstDepartmanlar;
    ArrayList<MasaDizayn> lstMasaDizayn;
    String[] masaPlanIsmi;
    String tabName;
    String acilanMasa;
    String acilanMasaDepartman;
    String kapananMasa;
    String kapananMasaDepartman;
    //
    public String[] acikMasalar;
    public String srvrMessage;
    public boolean mesajGeldi = false;
    public boolean viewPagerCreated = false;
    public boolean firstRun = false;
    ArrayList<Employee> lstEmployees;
    //
    CollectionPagerAdapter collectionPagerAdapter;
    Context context = this;
    int tabpos = 0;

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
        tabName = (String) tab.getText();
        String komut = "<komut=departman&departmanAdi=" + tabName + ">";
        if (mTcpClient != null && mesajGeldi == false) {
            mTcpClient.sendMessage(komut);
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    BroadcastReceiver rec = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //all events will be received here
            //get message
            srvrMessage = intent.getStringExtra("message");
            if (firstRun == false) {
                if (srvrMessage != null) {
                    mesajGeldi = true;
                }
            }
            int count = 0;
            while (!viewPagerCreated) {
                count++;
            }
            String[] parametreler = srvrMessage.split("&");
            String[] esitlik;
            final Dictionary<String, String> collection = new Hashtable<String, String>(parametreler.length);
            for (String parametre : parametreler) {
                esitlik = parametre.split("=");
                if (esitlik.length == 2)
                    collection.put(esitlik[0], esitlik[1]);
            }
            String gelenkomut = collection.get("komut");
            Komutlar komut = Komutlar.valueOf(gelenkomut);

            switch (komut) {

                case hesapOdeniyor:
                    break;
                case masaGirilebilirMi:
                    break;
                case masaDegistir:
                    break;
                case urunTasindi:
                    break;
                case ikram:
                    break;
                case ikramIptal:
                    break;
                case BulunanYazicilar:
                    break;
                case giris:
                    final String baglanti = collection.get("sonuc");
                    break;
                case iptal:
                    mesajGeldi = false;
                    break;

                case masaKapandi:
                    mesajGeldi = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            kapananMasa = collection.get("masa");
                            kapananMasaDepartman = collection.get("departmanAdi");
                            fragment[0] = (FragmentMasaDesign) collectionPagerAdapter.fragments[mViewPager
                                    .getCurrentItem()];
                            fragment[0].startKapananMasa(kapananMasa);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (kapananMasaDepartman.contentEquals(tabName))
                                fragment[0].masaKapat();
                        }
                    });
                    break;

                case siparis:
                    mesajGeldi = false;

                    break;

                case masaAcildi:
                    mesajGeldi = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            acilanMasa = collection.get("masa");
                            acilanMasaDepartman = collection.get("departmanAdi");
                            fragment[0] = (FragmentMasaDesign) collectionPagerAdapter.fragments[mViewPager
                                    .getCurrentItem()];
                            fragment[0].startAcilanMasa(acilanMasa);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (acilanMasaDepartman.contentEquals(tabName))
                                fragment[0].masaAc();
                        }
                    });
                    break;
                case IndirimOnay:
                    break;
                case OdemeOnay:
                    break;
                case LoadSiparis:
                    break;
                case OdenenleriGonder:
                    break;
                case toplumesaj:
                    break;

                case departman:
                    mesajGeldi = false;
                    acikMasalar = null;
                    if (firstRun) {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (collection.size() <= 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fragment[0] = (FragmentMasaDesign) collectionPagerAdapter.fragments[mViewPager
                                        .getCurrentItem()];
                                fragment[0].startSendAcikMasalar(acikMasalar, tabName);
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                fragment[0].acikMasalariGoster();
                            }
                        });
                        break;
                    }

                    try {
                        acikMasalar = collection.get("masa").split("\\*");
                    } catch (Exception e) {
                        break;
                    }
                    if (firstRun) {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fragment[0] = (FragmentMasaDesign) collectionPagerAdapter.fragments[mViewPager
                                    .getCurrentItem()];
                            fragment[0].startSendAcikMasalar(acikMasalar, tabName);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            fragment[0].acikMasalariGoster();
                        }
                    });
                    break;
                case AdisyonNotu:
                    break;
                case IslemHatasi:
                    break;
            }
        }
    };

    @Override
    public void asyncResponse(String mesaj) {

    }

    public Fragment getVisibleFragment() {
        List<Fragment> allFragments = getSupportFragmentManager().getFragments();

        Fragment visibleFragment = new Fragment();
        for (Fragment fragment : allFragments) {
            if (fragment.getUserVisibleHint()) {
                visibleFragment = fragment;
                break;
            }
        }
        return visibleFragment;
    }

    public enum Komutlar {
        siparis, iptal, hesapOdeniyor, masaGirilebilirMi, masaDegistir, urunTasindi, ikram, ikramIptal,
        BulunanYazicilar, giris, IndirimOnay, OdemeOnay, LoadSiparis, OdenenleriGonder, toplumesaj, departman,
        masaAcildi, masaKapandi, AdisyonNotu, IslemHatasi;
    }

    @Override
    protected void onDestroy() {
        if (mTcpClient != null) {
            try {
                mTcpClient.stopClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LocalBroadcastManager.getInstance(context).unregisterReceiver(rec);
        mTcpClient = null;
        commonAsyncTask = null;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = null;
        super.onCreate(savedInstanceState);
        try {
            commonAsyncTask = new CommonAsyncTask(this);
            LocalBroadcastManager.getInstance(context).registerReceiver(rec, new IntentFilter("myevent"));
            preferences = this.getSharedPreferences("MyPreferences",
                    Context.MODE_PRIVATE);
            mTcpClient.SERVERIP = preferences.getString("IPAddress", "0");
            mTcpClient.SERVERPORT = Integer.parseInt(preferences.getString("Port", "13759"));
            commonAsyncTask.execute((android.os.Handler[]) null);
        } catch (Exception e) {

        }
        FileIO fileIO = new FileIO();
        List<File> files;
        files = fileIO.getListFiles(new File("/mnt/sdcard/shared/Lenovo"));
        ReadXML readXML = new ReadXML();
        lstDepartmanlar = readXML.readDepartmanlar(files);
        lstMasaDizayn = readXML.readMasaDizayn(files);
        this.masaPlanIsmi = readXML.masaPlanIsimleri;
        //Server ile bağlantı kurulup kurulmadığını kontrol etmek için gönderilen mesaj.
        String girisKomutu = "<komut=giris&nick=" + preferences.getString("TabletName", "Tablet") + ">";
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mTcpClient = commonAsyncTask.client;
        if (mTcpClient != null) {
            mTcpClient.sendMessage(girisKomutu);
        }
        long counter = 0;
        String baslangicKomutu = "<komut=departman&departmanAdi=" + lstDepartmanlar.get(0).DepartmanAdi + ">";
        if (mTcpClient != null) {
            mTcpClient.sendMessage(baslangicKomutu);
        }
        //Serverdan açık masalar gelmezse bağlantı hatası ver.
        while (!mesajGeldi) {
            counter++;
            if (counter == 10000000) {

                AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
                aBuilder.setTitle("Bağlantı Hatası");
                aBuilder.setMessage("Sunucu ile bağlantı kurulamadı. Bağlantı ayarlarınızı kontrol edip " +
                        "programı tekrar çalıştırınız.").setCancelable(false)
                        .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainScreen.this.finish();
                            }
                        });
                AlertDialog alertDialog = aBuilder.create();
                alertDialog.show();
                break;
            }
        }
        //Giriş ekranından gelen çalışan bilgilerini alır.
        Bundle extras = getIntent().getExtras();
        lstEmployees = (ArrayList<Employee>) extras.getSerializable("lstEmployees");
        Log.e("OnCreate", "mesajGeldi=true");
        Object lockObject = new Object();
        synchronized (lockObject) {
            setContentView(R.layout.activity_main_screen);
            collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());
            collectionPagerAdapter.lstDepartmanlar = lstDepartmanlar;
            collectionPagerAdapter.lstMasaDizayn = lstMasaDizayn;
            collectionPagerAdapter.masaPlanIsmi = masaPlanIsmi;
            collectionPagerAdapter.lstEmployees = lstEmployees;
            mViewPager = (ViewPager) findViewById(R.id.pager);
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
            actionBar = getActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            for (Departman departman : lstDepartmanlar) {
                tab = actionBar.newTab().setText(departman.DepartmanAdi);
                tab.setTabListener(this);
                actionBar.addTab(tab);
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ConnectTCP.getInstance().setmTCPClient(mTcpClient);
        viewPagerCreated = true;
        firstRun = true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
