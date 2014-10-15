package com.res_otomasyon.resotomasyon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import Entity.Departman;
import Entity.MasaninSiparisleri;
import ekclasslar.BildirimBilgileriIslemler;
import ekclasslar.NotificationExpandableAdapter;
import ekclasslar.SetViewGroupEnabled;
import ekclasslar.TryConnection;

public class NotificationScreen extends ActionBarActivity {
    GlobalApplication g;
    public String srvrMessage;
    Context context = this;
    boolean activityVisible = true;
    TryConnection t;
    SharedPreferences preferences;
    ArrayList<Departman> lstDepartmanlar;

    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    //Server ile bağlantı kurulup kurulmadığını kontrol etmek için gönderilen mesaj.
                    preferences = NotificationScreen.this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                    String girisKomutu = "komut=giris&nick=" + preferences.getString("TabletName", "Tablet");

                    if (g.commonAsyncTask.client != null) {
                        if (g.commonAsyncTask.client.out != null) {
                            g.commonAsyncTask.client.sendMessage(girisKomutu);
                            NotificationScreen.this.getSupportActionBar().setTitle(getString(R.string.app_name) + "(Bağlı)");
                            SetViewGroupEnabled.setViewGroupEnabled((android.view.ViewGroup) findViewById(R.id.notificationScreen), true);
                            t.stopTimer();
                            g.commonAsyncTask.client.sendMessage(notificationMessage());
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Notification bar (status bar) kilitleme
        lstDepartmanlar = (ArrayList<Departman>) getIntent().getSerializableExtra("lstDepartmanlar");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_notifications);
        g = (GlobalApplication) getApplicationContext();
        LocalBroadcastManager.getInstance(context).registerReceiver(rec, new IntentFilter("myevent"));
        final NotificationExpandableAdapter expandableAdapter = new NotificationExpandableAdapter(this, g);
        ExpandableListView expandableListviewNotification = (ExpandableListView) findViewById(R.id.expandable_notification_listview);
        g.adapter = expandableAdapter;
        expandableListviewNotification.setAdapter(g.adapter);
        t = new TryConnection(g, myHandler);
    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(rec);
        activityVisible = false;
        if (t.timerRunning)
            t.stopTimer();
        super.onPause();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(rec);
        activityVisible = false;
        if (t.timerRunning)
            t.stopTimer();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notifications, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (g == null)
            g = (GlobalApplication) getApplicationContext();
        if (t == null)
            t = new TryConnection(g, myHandler);
        if (!g.commonAsyncTask.client.mRun && !t.timerRunning) {
            t.startTimer();
        }
        activityVisible = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
        */
    }

    BroadcastReceiver rec;

    {
        rec = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //all events will be received here
                //get message
                srvrMessage = intent.getStringExtra("message");
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
                                    NotificationScreen.this.getSupportActionBar().setTitle(getString(R.string.app_name) + "(Bağlantı yok)");
                                    SetViewGroupEnabled.setViewGroupEnabled((android.view.ViewGroup) findViewById(R.id.notificationScreen), false);
                                }
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
                                            final NotificationExpandableAdapter expandableAdapter = new NotificationExpandableAdapter(NotificationScreen.this, g);
                                            ExpandableListView expandableListviewNotification = (ExpandableListView) findViewById(R.id.expandable_notification_listview);
                                            g.adapter = expandableAdapter;
                                            expandableListviewNotification.setAdapter(g.adapter);
                                        }

                                    }
                                }
                            }
                        });

                        break;

                    case bildirimGoruldu:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (collection.get("yemekAdi").contentEquals("hepsi"))
                                {
                                    ExpandableListView expandableListviewNotification = (ExpandableListView) findViewById(R.id.expandable_notification_listview);
                                    int count =  g.adapter.getGroupCount();
                                    for (int i = 0; i <count ; i++)
                                        expandableListviewNotification.collapseGroup(i);
                                }
                            }
                        });
                        break;

                    case siparis:
                        if (collection.get("tur") != null)
                            if (collection.get("tur").contentEquals("K"))
                                return;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                g.adapter.notifyDataSetChanged();
                            }
                        });
                        break;
                    case GarsonIstendi:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                g.adapter.notifyDataSetChanged();
                            }
                        });
                        break;
                    case TemizlikIstendi:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                g.adapter.notifyDataSetChanged();
                            }
                        });
                        break;
                    case HesapIstendi:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                g.adapter.notifyDataSetChanged();
                            }
                        });
                    case urunuTasiTablet:
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                g.adapter.notifyDataSetChanged();
//                            }
//                        });
                        break;
                    default:
                        break;
                }
//                myHandler.sendEmptyMessage(1);
            }
        };
    }


}
