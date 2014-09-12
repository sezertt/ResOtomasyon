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
import ekclasslar.BildirimBilgileriIslemler;
import ekclasslar.GarsonIslemler;
import ekclasslar.HesapIste;
import ekclasslar.MasaTemizle;
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
    int siparisCounter = 0;

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
                            //Sipariş geldiğinde yapılacak işlemler.
                            SiparisIslemler siparisIslemler = new SiparisIslemler(collection, g);

                            masaKilitliMi = preferences.getBoolean("MasaKilitli", masaKilitliMi);

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
                            break;
                        case departman:
                            mesajGeldi = false;
                            acikMasalar = null;
                            try {
                                acikMasalar = collection.get("masa").split("\\*");
                            } catch (Exception e) {
                                acikMasalar = null;
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
                            int bild = 1;
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
                                    GarsonIslemler garsonIslemler = new GarsonIslemler(collection, g);
                                    if (garsonIslemler.Istendi()){
                                        if (!menu.findItem(R.id.action_notification).isVisible())
                                            menu.findItem(R.id.action_notification).setVisible(true);
                                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                            menu.findItem(R.id.action_notification).setVisible(true);
                                            v.vibrate(2000);
                                    }
                                }
                            });
                            break;
                        case TemizlikIstendi:
                            MasaTemizle masaTemizle = new MasaTemizle(collection, g);
                            masaTemizle.Islem();
                            break;
                        case HesapIstendi:
                            HesapIste hesapIste = new HesapIste(collection, g);
                            hesapIste.Islem();
                            break;
                        case bildirimGoruldu:
                            int masaSiparisCounter = 0;
                            boolean hepsiSilindi = false;
                            for (MasaninSiparisleri msp : g.lstMasaninSiparisleri) {
                                if (msp.DepartmanAdi.contentEquals(collection.get("departmanAdi")) && msp.MasaAdi.contentEquals(collection.get("masa"))) {
                                    int siparisSize = msp.siparisler.size();
                                    for (int i = 0; i < siparisSize; i++) {
                                        Siparis siparis = msp.siparisler.get(i);
                                        if (siparis.siparisYemekAdi.contentEquals(collection.get("yemekAdi")) && siparis.siparisAdedi == Integer.parseInt(collection.get("adedi")) && siparis.siparisPorsiyonu == Double.parseDouble(collection.get("porsiyonu"))) {
                                            msp.siparisler.remove(i);
                                            break;
                                        }
                                        else if(collection.get("yemekAdi").contentEquals("hepsi"))
                                        {
                                            g.lstMasaninSiparisleri.remove(masaSiparisCounter);
                                            hepsiSilindi = true;
                                            break;
                                        }
                                    }
                                }
                                if(hepsiSilindi)
                                    break;
                                masaSiparisCounter++;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    g.adapter.notifyDataSetChanged();
                                }
                            });
                            break;
                        case GarsonGoruldu:
                            int a = 1;
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
        g.commonAsyncTask.client.sendMessage(notificationMessage());
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

//    BroadcastReceiver rec;
//
//    {
//        rec = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                //all events will be received here
//                //get message
//                srvrMessage = intent.getStringExtra("message");
//                myHandler.sendEmptyMessage(1);
//            }
//        };
//    }

    @Override
    protected void onResume() {
        if (g == null) {
            g = (GlobalApplication) getApplicationContext();
            g.isServerReachable = false;
        }
        if (t == null)
            t = new TryConnection(g, myHandler);

        g.isServerReachable = g.commonAsyncTask != null && g.commonAsyncTask.client != null && g.commonAsyncTask.client.mRun;

        if (!g.isServerReachable && !t.timerRunning) {
            t.startTimer();
        }
        activityVisible = true;
        if (menu != null)
            if (g.lstMasaninSiparisleri == null || g.lstMasaninSiparisleri.size() == 0)
                menu.findItem(R.id.action_notification).setVisible(false);
        super.onResume();
    }

    public String notificationMessage() {
        int counter = 0;
        String komut, masalar = "";
        komut = "komut=bildirim&masalar=";
        if (g.secilenMasalar.size() > 0) {
            for (Departman dpt : lstDepartmanlar) {
                if (g.secilenMasalar != null && g.secilenMasalar.size() > 0) {
                    if (g.secilenMasalar.get(counter).Masalar.size() > 0) {
                        masalar += "*" + dpt.DepartmanAdi;
                        for (String dptMasalar : g.secilenMasalar.get(counter).Masalar) {
                            masalar += "-" + dptMasalar;
                        }
                    }
                }
                counter++;
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
        super.onBackPressed();
        if (g.broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(g.broadcastReceiver);
        }
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