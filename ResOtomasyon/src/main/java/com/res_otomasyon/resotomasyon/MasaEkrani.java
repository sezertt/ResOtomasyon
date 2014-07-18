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
import android.os.Handler;
import android.os.Message;
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
import android.view.Window;
import android.view.WindowManager;

import ekclasslar.CollectionPagerAdapter;
import ekclasslar.FileIO;
import Entity.Departman;
import Entity.Employee;
import Entity.MasaDizayn;
import TCPClientSide.CommonAsyncTask;
import TCPClientSide.ConnectTCP;
import TCPClientSide.TCPClient;
import XMLReader.ReadXML;


public class MasaEkrani extends FragmentActivity implements ActionBar.TabListener,
        CommonAsyncTask.OnAsyncRequestComplete {

    final FragmentMasaEkrani[] fragment = {new FragmentMasaEkrani()};
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
    SharedPreferences preferences;
    //
    public String[] acikMasalar;
    public String srvrMessage;
    public boolean mesajGeldi = false;
    public boolean firstRun = false;
    boolean masaKilitliMi = false;
    ArrayList<Employee> lstEmployees;
    //
    CollectionPagerAdapter collectionPagerAdapter;
    Context context = this;

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

    public Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    firstRun = true;
                    String departmanKomutu = "<komut=departman&departmanAdi=" + lstDepartmanlar.get(0).DepartmanAdi + ">";
                    if (mTcpClient != null) {
                        mTcpClient.sendMessage(departmanKomutu);
                    }
                    ConnectTCP.getInstance().setmTCPClient(mTcpClient);
                    break;
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
                    Komutlar komut = Komutlar.valueOf(gelenkomut);

                    switch (komut) {
                        case iptal:
                            mesajGeldi = false;
                            break;
                        case masaKapandi:
                            mesajGeldi = false;

                            kapananMasa = collection.get("masa");
                            kapananMasaDepartman = collection.get("departmanAdi");
                            fragment[0] = (FragmentMasaEkrani) collectionPagerAdapter.fragments[mViewPager
                                    .getCurrentItem()];
                            fragment[0].startKapananMasa(kapananMasa, acilanMasaDepartman);
                            break;
                        case siparis:
                            mesajGeldi = false;
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
                        case AdisyonNotu:
                            break;
                        case IslemHatasi:
                            break;
                    }
                    break;
                case 2:
                    //Server ile bağlantı kurulup kurulmadığını kontrol etmek için gönderilen mesaj.
                    String girisKomutu = "<komut=giris&nick=" + preferences.getString("TabletName", "Tablet") + ">";

                    mTcpClient = commonAsyncTask.client;

                    if (mTcpClient != null) {
                        if (mTcpClient.out != null)
                            mTcpClient.sendMessage(girisKomutu);
                        else {
                            hataVer();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            commonAsyncTask = new CommonAsyncTask(this, myHandler);
            LocalBroadcastManager.getInstance(context).registerReceiver(rec, new IntentFilter("myevent"));
            preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            mTcpClient.SERVERIP = preferences.getString("IPAddress", "0");
            mTcpClient.SERVERPORT = Integer.parseInt(preferences.getString("Port", "13759"));
            commonAsyncTask.execute((android.os.Handler[]) null);
        } catch (Exception e) {
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FileIO fileIO = new FileIO();
        List<File> files;
        files = fileIO.getListFiles(new File("/mnt/sdcard/shared/Lenovo"));
        ReadXML readXML = new ReadXML();
        lstDepartmanlar = readXML.readDepartmanlar(files);
        lstMasaDizayn = readXML.readMasaDizayn(files);
        this.masaPlanIsmi = readXML.masaPlanIsimleri;

        //Giriş ekranından gelen çalışan bilgilerini alır.
        Bundle extras = getIntent().getExtras();
        lstEmployees = (ArrayList<Employee>) extras.getSerializable("lstEmployees");
        Log.e("OnCreate", "mesajGeldi=true");
        Object lockObject = new Object();
        synchronized (lockObject) {
            setContentView(R.layout.activity_masa_ekrani);
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
        preferences = this.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE);
        if(preferences.getBoolean("MasaKilitli",masaKilitliMi))
        {
            this.setVisible(false);
            intent = new Intent(MasaEkrani.this, MenuEkrani.class);
            intent.putExtra("lstEmployees", lstEmployees);
            startActivity(intent);
        }
    }

    BroadcastReceiver rec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //all events will be received here
            //get message
            srvrMessage = intent.getStringExtra("message");
            if (firstRun == false) {
                if (srvrMessage != null) {
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
                    final String baglanti = collection.get("sonuc");

                    if (komut.toString().contentEquals("giris") && baglanti.contentEquals("basarili")) {
                        mesajGeldi = true;
                        myHandler.sendEmptyMessage(0);
                    } else if (komut.toString().contentEquals("giris")&&!baglanti.contentEquals("basarili")){
                        hataVerIsim();
                    }
                    else {
                        hataVer();
                    }
                }
            } else {
                myHandler.sendEmptyMessage(1);
            }
        }
    };

    private void hataVerIsim() {
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
        aBuilder.setTitle("Bağlantı Hatası");
        aBuilder.setMessage("Bilgisayar adı kullanımda.Lütfen ayarları kullanarak kullanıcı adınızı değiştirip tekrar deneniyiniz.").setCancelable(false)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MasaEkrani.this.finish();
                    }
                });
        AlertDialog alertDialog = aBuilder.create();
        alertDialog.show();
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
