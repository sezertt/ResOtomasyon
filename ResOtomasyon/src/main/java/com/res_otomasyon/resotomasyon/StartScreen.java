package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.res_otomasyon.resotomasyon.LoginScreen;
import com.res_otomasyon.resotomasyon.R;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import TCPClientSide.CommonAsyncTask;
import TCPClientSide.ConnectTCP;
import TCPClientSide.TCPClient;

public class StartScreen extends Activity implements CommonAsyncTask.OnAsyncRequestComplete {

    TCPClient mTcpClient;
    CommonAsyncTask commonAsyncTask;
    SharedPreferences preferences;
    Context context = this;
    String srvrMessage;
    public boolean mesajGeldi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setVisible(false);
        String sdcardReady = Environment.getExternalStorageState();

        while (!sdcardReady.contentEquals("mounted")) {
            sdcardReady = Environment.getExternalStorageState();
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        GlobalApplication g = (GlobalApplication) getApplicationContext();
        if(g.commonAsyncTask ==null) {
            preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            g.activity = this;
            LocalBroadcastManager.getInstance(context).registerReceiver(rec, new IntentFilter("myevent"));
            g.connectServer(myHandler, rec);
            commonAsyncTask = g.commonAsyncTask;
            setContentView(R.layout.activity_start_screen);
        }
    }

    public Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (mTcpClient != null) {
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
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    BroadcastReceiver rec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //all events will be received here
            //get message
            srvrMessage = intent.getStringExtra("message");
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
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(rec);

                } else if (komut.toString().contentEquals("giris") && !baglanti.contentEquals("basarili")) {

                } else {

                }
            }
        }
    };

    public enum Komutlar {
        siparis, iptal, hesapOdeniyor, masaGirilebilirMi, masaDegistir, urunTasindi, ikram, ikramIptal,
        BulunanYazicilar, giris, IndirimOnay, OdemeOnay, LoadSiparis, OdenenleriGonder, toplumesaj, departman,
        masaAcildi, masaKapandi, AdisyonNotu, IslemHatasi;
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start_screen, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setVisible(false);
        Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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

    @Override
    public void asyncResponse(String mesaj) {

    }
}
